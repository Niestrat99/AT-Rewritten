package io.papermc.paper.chunk.system.scheduling;

import ca.spottedleaf.concurrentutil.executor.standard.PrioritisedExecutor;
import ca.spottedleaf.concurrentutil.executor.standard.PrioritisedThreadPool;
import ca.spottedleaf.concurrentutil.executor.standard.PrioritisedThreadedTaskQueue;
import ca.spottedleaf.concurrentutil.util.ConcurrentUtil;
import com.mojang.logging.LogUtils;
import io.papermc.paper.configuration.GlobalConfiguration;
import io.papermc.paper.util.CoordinateUtils;
import io.papermc.paper.util.TickThread;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import io.papermc.paper.util.MCUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import org.bukkit.Bukkit;
import org.slf4j.Logger;
import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public final class ChunkTaskScheduler {

    private static final Logger LOGGER = LogUtils.getClassLogger();

    static int newChunkSystemIOThreads;
    static int newChunkSystemWorkerThreads;
    static int newChunkSystemGenParallelism;
    static int newChunkSystemLoadParallelism;

    public static ca.spottedleaf.concurrentutil.executor.standard.PrioritisedThreadPool workerThreads;

    private static boolean initialised = false;

    public static void init(final GlobalConfiguration.ChunkSystem config) {
        if (initialised) {
            return;
        }
        initialised = true;
        newChunkSystemIOThreads = config.ioThreads;
        newChunkSystemWorkerThreads = config.workerThreads;
        if (newChunkSystemIOThreads < 0) {
            newChunkSystemIOThreads = 1;
        } else {
            newChunkSystemIOThreads = Math.max(1, newChunkSystemIOThreads);
        }
        int defaultWorkerThreads = Runtime.getRuntime().availableProcessors() / 2;
        if (defaultWorkerThreads <= 4) {
            defaultWorkerThreads = defaultWorkerThreads <= 3 ? 1 : 2;
        } else {
            defaultWorkerThreads = defaultWorkerThreads / 2;
        }
        defaultWorkerThreads = Integer.getInteger("Paper.WorkerThreadCount", Integer.valueOf(defaultWorkerThreads));

        if (newChunkSystemWorkerThreads < 0) {
            newChunkSystemWorkerThreads = defaultWorkerThreads;
        } else {
            newChunkSystemWorkerThreads = Math.max(1, newChunkSystemWorkerThreads);
        }

        String newChunkSystemGenParallelism = config.genParallelism;
        if (newChunkSystemGenParallelism.equalsIgnoreCase("default")) {
            newChunkSystemGenParallelism = "true";
        }
        boolean useParallelGen;
        if (newChunkSystemGenParallelism.equalsIgnoreCase("on") || newChunkSystemGenParallelism.equalsIgnoreCase("enabled")
            || newChunkSystemGenParallelism.equalsIgnoreCase("true")) {
            useParallelGen = true;
        } else if (newChunkSystemGenParallelism.equalsIgnoreCase("off") || newChunkSystemGenParallelism.equalsIgnoreCase("disabled")
            || newChunkSystemGenParallelism.equalsIgnoreCase("false")) {
            useParallelGen = false;
        } else {
            throw new IllegalStateException("Invalid option for gen-parallelism: must be one of [on, off, enabled, disabled, true, false, default]");
        }

        ChunkTaskScheduler.newChunkSystemGenParallelism = useParallelGen ? newChunkSystemWorkerThreads : 1;
        ChunkTaskScheduler.newChunkSystemLoadParallelism = newChunkSystemWorkerThreads;

        io.papermc.paper.chunk.system.io.RegionFileIOThread.init(newChunkSystemIOThreads);
        workerThreads = new ca.spottedleaf.concurrentutil.executor.standard.PrioritisedThreadPool(
            "Paper Chunk System Worker Pool", newChunkSystemWorkerThreads,
            (final Thread thread, final Integer id) -> {
                thread.setPriority(Thread.NORM_PRIORITY - 2);
                thread.setName("Tuinity Chunk System Worker #" + id.intValue());
                thread.setUncaughtExceptionHandler(io.papermc.paper.chunk.system.scheduling.NewChunkHolder.CHUNKSYSTEM_UNCAUGHT_EXCEPTION_HANDLER);
            }, (long)(20.0e6)); // 20ms

        LOGGER.info("Chunk system is using " + newChunkSystemIOThreads + " I/O threads, " + newChunkSystemWorkerThreads + " worker threads, and gen parallelism of " + ChunkTaskScheduler.newChunkSystemGenParallelism + " threads");
    }

    public final ServerLevel world;
    public final PrioritisedThreadPool workers;
    public final PrioritisedThreadPool.PrioritisedPoolExecutor lightExecutor;
    public final PrioritisedThreadPool.PrioritisedPoolExecutor genExecutor;
    public final PrioritisedThreadPool.PrioritisedPoolExecutor parallelGenExecutor;
    public final PrioritisedThreadPool.PrioritisedPoolExecutor loadExecutor;

    private final PrioritisedThreadedTaskQueue mainThreadExecutor = new PrioritisedThreadedTaskQueue();

    final ReentrantLock schedulingLock = new ReentrantLock();
    public final ChunkHolderManager chunkHolderManager;

    static {
        ChunkStatus.EMPTY.writeRadius = 0;
        ChunkStatus.STRUCTURE_STARTS.writeRadius = 0;
        ChunkStatus.STRUCTURE_REFERENCES.writeRadius = 0;
        ChunkStatus.BIOMES.writeRadius = 0;
        ChunkStatus.NOISE.writeRadius = 0;
        ChunkStatus.SURFACE.writeRadius = 0;
        ChunkStatus.CARVERS.writeRadius = 0;
        ChunkStatus.LIQUID_CARVERS.writeRadius = 0;
        ChunkStatus.FEATURES.writeRadius = 1;
        ChunkStatus.LIGHT.writeRadius = 1;
        ChunkStatus.SPAWN.writeRadius = 0;
        ChunkStatus.HEIGHTMAPS.writeRadius = 0;
        ChunkStatus.FULL.writeRadius = 0;

        /*
          It's important that the neighbour read radius is taken into account. If _any_ later status is using some chunk as
          a neighbour, it must be also safe if that neighbour is being generated. i.e for any status later than FEATURES,
          for a status to be parallel safe it must not read the block data from its neighbours.
         */
        final List<ChunkStatus> parallelCapableStatus = Arrays.asList(
                // No-op executor.
                ChunkStatus.EMPTY,

                // This is parallel capable, as CB has fixed the concurrency issue with stronghold generations.
                // Does not touch neighbour chunks.
                // TODO On another note, what the fuck is StructureFeatureManager.StructureCheck and why is it used? it's leaking
                ChunkStatus.STRUCTURE_STARTS,

                // Surprisingly this is parallel capable. It is simply reading the already-created structure starts
                // into the structure references for the chunk. So while it reads from it neighbours, its neighbours
                // will not change, even if executed in parallel.
                ChunkStatus.STRUCTURE_REFERENCES,

                // Safe. Mojang runs it in parallel as well.
                ChunkStatus.BIOMES,

                // Safe. Mojang runs it in parallel as well.
                ChunkStatus.NOISE,

                // Parallel safe. Only touches the target chunk. Biome retrieval is now noise based, which is
                // completely thread-safe.
                ChunkStatus.SURFACE,

                // No global state is modified in the carvers. It only touches the specified chunk. So it is parallel safe.
                ChunkStatus.CARVERS,

                // No-op executor. Was replaced in 1.18 with carvers, I think.
                ChunkStatus.LIQUID_CARVERS,

                // FEATURES is not parallel safe. It writes to neighbours.

                // LIGHT is not parallel safe. It also doesn't run on the generation executor, so no point.

                // Only writes to the specified chunk. State is not read by later statuses. Parallel safe.
                // Note: it may look unsafe because it writes to a worldgenregion, but the region size is always 0 -
                // see the task margin.
                // However, if the neighbouring FEATURES chunk is unloaded, but then fails to load in again (for whatever
                // reason), then it would write to this chunk - and since this status reads blocks from itself, it's not
                // safe to execute this in parallel.
                // SPAWN

                // No-op executor.
                ChunkStatus.HEIGHTMAPS

                // FULL is executed on main.
        );

        for (final ChunkStatus status : parallelCapableStatus) {
            status.isParallelCapable = true;
        }
    }

    public ChunkTaskScheduler(final ServerLevel world, final PrioritisedThreadPool workers) {
        this.world = world;
        this.workers = workers;

        final String worldName = world.getWorld().getName();
        this.genExecutor = workers.createExecutor("Chunk single-threaded generation executor for world '" + worldName + "'", 1);
        // same as genExecutor, as there are race conditions between updating blocks in FEATURE status while lighting chunks
        this.lightExecutor = this.genExecutor;
        this.parallelGenExecutor = newChunkSystemGenParallelism <= 1 ? this.genExecutor
                : workers.createExecutor("Chunk parallel generation executor for world '" + worldName + "'", newChunkSystemGenParallelism);
        this.loadExecutor = workers.createExecutor("Chunk load executor for world '" + worldName + "'", newChunkSystemLoadParallelism);
        this.chunkHolderManager = new ChunkHolderManager(world, this);
    }

    private final AtomicBoolean failedChunkSystem = new AtomicBoolean();

    public static Object stringIfNull(final Object obj) {
        return obj == null ? "null" : obj;
    }

    public void unrecoverableChunkSystemFailure(final int chunkX, final int chunkZ, final Map<String, Object> objectsOfInterest, final Throwable thr) {
        final NewChunkHolder holder = this.chunkHolderManager.getChunkHolder(chunkX, chunkZ);
        LOGGER.error("Chunk system error at chunk (" + chunkX + "," + chunkZ + "), holder: " + holder + ", exception:", new Throwable(thr));

        if (this.failedChunkSystem.getAndSet(true)) {
            return;
        }

        final ReportedException reportedException = thr instanceof ReportedException ? (ReportedException)thr : new ReportedException(new CrashReport("Chunk system error", thr));

        CrashReportCategory crashReportCategory = reportedException.getReport().addCategory("Chunk system details");
        crashReportCategory.setDetail("Chunk coordinate", new ChunkPos(chunkX, chunkZ).toString());
        crashReportCategory.setDetail("ChunkHolder", Objects.toString(holder));
        crashReportCategory.setDetail("unrecoverableChunkSystemFailure caller thread", Thread.currentThread().getName());

        crashReportCategory = reportedException.getReport().addCategory("Chunk System Objects of Interest");
        for (final Map.Entry<String, Object> entry : objectsOfInterest.entrySet()) {
            if (entry.getValue() instanceof Throwable thrObject) {
                crashReportCategory.setDetailError(Objects.toString(entry.getKey()), thrObject);
            } else {
                crashReportCategory.setDetail(Objects.toString(entry.getKey()), Objects.toString(entry.getValue()));
            }
        }

        final Runnable crash = () -> {
            throw new RuntimeException("Chunk system crash propagated from unrecoverableChunkSystemFailure", reportedException);
        };

        // this may not be good enough, specifically thanks to stupid ass plugins swallowing exceptions
        this.scheduleChunkTask(chunkX, chunkZ, crash, PrioritisedExecutor.Priority.BLOCKING);
        // so, make the main thread pick it up
        MinecraftServer.chunkSystemCrash = new RuntimeException("Chunk system crash propagated from unrecoverableChunkSystemFailure", reportedException);
    }

    public boolean executeMainThreadTask() {
        TickThread.ensureTickThread("Cannot execute main thread task off-main");
        return this.mainThreadExecutor.executeTask();
    }

    public void raisePriority(final int x, final int z, final PrioritisedExecutor.Priority priority) {
        this.chunkHolderManager.raisePriority(x, z, priority);
    }

    public void setPriority(final int x, final int z, final PrioritisedExecutor.Priority priority) {
        this.chunkHolderManager.setPriority(x, z, priority);
    }

    public void lowerPriority(final int x, final int z, final PrioritisedExecutor.Priority priority) {
        this.chunkHolderManager.lowerPriority(x, z, priority);
    }

    private final AtomicLong chunkLoadCounter = new AtomicLong();

    public void scheduleTickingState(final int chunkX, final int chunkZ, final ChunkHolder.FullChunkStatus toStatus,
                                     final boolean addTicket, final PrioritisedExecutor.Priority priority,
                                     final Consumer<LevelChunk> onComplete) {
        if (!TickThread.isTickThread()) {
            this.scheduleChunkTask(chunkX, chunkZ, () -> {
                ChunkTaskScheduler.this.scheduleTickingState(chunkX, chunkZ, toStatus, addTicket, priority, onComplete);
            }, priority);
            return;
        }
        if (this.chunkHolderManager.ticketLock.isHeldByCurrentThread()) {
            throw new IllegalStateException("Cannot schedule chunk load during ticket level update");
        }
        if (this.schedulingLock.isHeldByCurrentThread()) {
            throw new IllegalStateException("Cannot schedule chunk loading recursively");
        }

        if (toStatus == ChunkHolder.FullChunkStatus.INACCESSIBLE) {
            throw new IllegalArgumentException("Cannot wait for INACCESSIBLE status");
        }

        final int minLevel = 33 - (toStatus.ordinal() - 1);
        final Long chunkReference = addTicket ? Long.valueOf(this.chunkLoadCounter.getAndIncrement()) : null;
        final long chunkKey = CoordinateUtils.getChunkKey(chunkX, chunkZ);

        if (addTicket) {
            this.chunkHolderManager.addTicketAtLevel(TicketType.CHUNK_LOAD, chunkKey, minLevel, chunkReference);
            this.chunkHolderManager.processTicketUpdates();
        }

        final Consumer<LevelChunk> loadCallback = (final LevelChunk chunk) -> {
            try {
                if (onComplete != null) {
                    onComplete.accept(chunk);
                }
            } finally {
                if (addTicket) {
                    ChunkTaskScheduler.this.chunkHolderManager.addAndRemoveTickets(chunkKey,
                        TicketType.UNKNOWN, minLevel, new ChunkPos(chunkKey),
                        TicketType.CHUNK_LOAD, minLevel, chunkReference
                    );
                }
            }
        };

        final boolean scheduled;
        final LevelChunk chunk;
        this.chunkHolderManager.ticketLock.lock();
        try {
            this.schedulingLock.lock();
            try {
                final NewChunkHolder chunkHolder = this.chunkHolderManager.getChunkHolder(chunkKey);
                if (chunkHolder == null || chunkHolder.getTicketLevel() > minLevel) {
                    scheduled = false;
                    chunk = null;
                } else {
                    final ChunkHolder.FullChunkStatus currStatus = chunkHolder.getChunkStatus();
                    if (currStatus.isOrAfter(toStatus)) {
                        scheduled = false;
                        chunk = (LevelChunk)chunkHolder.getCurrentChunk();
                    } else {
                        scheduled = true;
                        chunk = null;

                        final int radius = toStatus.ordinal() - 1; // 0 -> BORDER, 1 -> TICKING, 2 -> ENTITY_TICKING
                        for (int dz = -radius; dz <= radius; ++dz) {
                            for (int dx = -radius; dx <= radius; ++dx) {
                                final NewChunkHolder neighbour =
                                    (dx | dz) == 0 ? chunkHolder : this.chunkHolderManager.getChunkHolder(dx + chunkX, dz + chunkZ);
                                if (neighbour != null) {
                                    neighbour.raisePriority(priority);
                                }
                            }
                        }

                        // ticket level should schedule for us
                        chunkHolder.addFullStatusConsumer(toStatus, loadCallback);
                    }
                }
            } finally {
                this.schedulingLock.unlock();
            }
        } finally {
            this.chunkHolderManager.ticketLock.unlock();
        }

        if (!scheduled) {
            // couldn't schedule
            try {
                loadCallback.accept(chunk);
            } catch (final ThreadDeath thr) {
                throw thr;
            } catch (final Throwable thr) {
                LOGGER.error("Failed to process chunk full status callback", thr);
            }
        }
    }

    public void scheduleChunkLoad(final int chunkX, final int chunkZ, final boolean gen, final ChunkStatus toStatus, final boolean addTicket,
                                  final PrioritisedExecutor.Priority priority, final Consumer<ChunkAccess> onComplete) {
        if (gen) {
            this.scheduleChunkLoad(chunkX, chunkZ, toStatus, addTicket, priority, onComplete);
            return;
        }
        this.scheduleChunkLoad(chunkX, chunkZ, ChunkStatus.EMPTY, addTicket, priority, (final ChunkAccess chunk) -> {
            if (chunk == null) {
                onComplete.accept(null);
            } else {
                if (chunk.getStatus().isOrAfter(toStatus)) {
                    this.scheduleChunkLoad(chunkX, chunkZ, toStatus, addTicket, priority, onComplete);
                } else {
                    onComplete.accept(null);
                }
            }
        });
    }

    public void scheduleChunkLoad(final int chunkX, final int chunkZ, final ChunkStatus toStatus, final boolean addTicket,
                                  final PrioritisedExecutor.Priority priority, final Consumer<ChunkAccess> onComplete) {
        if (!TickThread.isTickThread()) {
            this.scheduleChunkTask(chunkX, chunkZ, () -> {
                ChunkTaskScheduler.this.scheduleChunkLoad(chunkX, chunkZ, toStatus, addTicket, priority, onComplete);
            }, priority);
            return;
        }
        if (this.chunkHolderManager.ticketLock.isHeldByCurrentThread()) {
            throw new IllegalStateException("Cannot schedule chunk load during ticket level update");
        }
        if (this.schedulingLock.isHeldByCurrentThread()) {
            throw new IllegalStateException("Cannot schedule chunk loading recursively");
        }

        if (toStatus == ChunkStatus.FULL) {
            this.scheduleTickingState(chunkX, chunkZ, ChunkHolder.FullChunkStatus.BORDER, addTicket, priority, (Consumer)onComplete);
            return;
        }

        final int minLevel = 33 + ChunkStatus.getDistance(toStatus);
        final Long chunkReference = addTicket ? Long.valueOf(this.chunkLoadCounter.getAndIncrement()) : null;
        final long chunkKey = CoordinateUtils.getChunkKey(chunkX, chunkZ);

        if (addTicket) {
            this.chunkHolderManager.addTicketAtLevel(TicketType.CHUNK_LOAD, chunkKey, minLevel, chunkReference);
            this.chunkHolderManager.processTicketUpdates();
        }

        final Consumer<ChunkAccess> loadCallback = (final ChunkAccess chunk) -> {
            try {
                if (onComplete != null) {
                    onComplete.accept(chunk);
                }
            } finally {
                if (addTicket) {
                    ChunkTaskScheduler.this.chunkHolderManager.addAndRemoveTickets(chunkKey,
                        TicketType.UNKNOWN, minLevel, new ChunkPos(chunkKey),
                        TicketType.CHUNK_LOAD, minLevel, chunkReference
                    );
                }
            }
        };

        final List<ChunkProgressionTask> tasks = new ArrayList<>();

        final boolean scheduled;
        final ChunkAccess chunk;
        this.chunkHolderManager.ticketLock.lock();
        try {
            this.schedulingLock.lock();
            try {
                final NewChunkHolder chunkHolder = this.chunkHolderManager.getChunkHolder(chunkKey);
                if (chunkHolder == null || chunkHolder.getTicketLevel() > minLevel) {
                    scheduled = false;
                    chunk = null;
                } else {
                    final ChunkStatus genStatus = chunkHolder.getCurrentGenStatus();
                    if (genStatus != null && genStatus.isOrAfter(toStatus)) {
                        scheduled = false;
                        chunk = chunkHolder.getCurrentChunk();
                    } else {
                        scheduled = true;
                        chunk = null;
                        chunkHolder.raisePriority(priority);

                        if (!chunkHolder.upgradeGenTarget(toStatus)) {
                            this.schedule(chunkX, chunkZ, toStatus, chunkHolder, tasks);
                        }
                        chunkHolder.addStatusConsumer(toStatus, loadCallback);
                    }
                }
            } finally {
                this.schedulingLock.unlock();
            }
        } finally {
            this.chunkHolderManager.ticketLock.unlock();
        }

        for (int i = 0, len = tasks.size(); i < len; ++i) {
            tasks.get(i).schedule();
        }

        if (!scheduled) {
            // couldn't schedule
            try {
                loadCallback.accept(chunk);
            } catch (final ThreadDeath thr) {
                throw thr;
            } catch (final Throwable thr) {
                LOGGER.error("Failed to process chunk status callback", thr);
            }
        }
    }

    private ChunkProgressionTask createTask(final int chunkX, final int chunkZ, final ChunkAccess chunk,
                                            final NewChunkHolder chunkHolder, final List<ChunkAccess> neighbours,
                                            final ChunkStatus toStatus, final PrioritisedExecutor.Priority initialPriority) {
        if (toStatus == ChunkStatus.EMPTY) {
            return new ChunkLoadTask(this, this.world, chunkX, chunkZ, chunkHolder, initialPriority);
        }
        if (toStatus == ChunkStatus.LIGHT) {
            return new ChunkLightTask(this, this.world, chunkX, chunkZ, chunk, initialPriority);
        }
        if (toStatus == ChunkStatus.FULL) {
            return new ChunkFullTask(this, this.world, chunkX, chunkZ, chunkHolder, chunk, initialPriority);
        }

        return new ChunkUpgradeGenericStatusTask(this, this.world, chunkX, chunkZ, chunk, neighbours, toStatus, initialPriority);
    }

    ChunkProgressionTask schedule(final int chunkX, final int chunkZ, final ChunkStatus targetStatus, final NewChunkHolder chunkHolder,
                                  final List<ChunkProgressionTask> allTasks) {
        return this.schedule(chunkX, chunkZ, targetStatus, chunkHolder, allTasks, chunkHolder.getEffectivePriority());
    }

    // rets new task scheduled for the _specified_ chunk
    // note: this must hold the scheduling lock
    // minPriority is only used to pass the priority through to neighbours, as priority calculation has not yet been done
    // schedule will ignore the generation target, so it should be checked by the caller to ensure the target is not regressed!
    private ChunkProgressionTask schedule(final int chunkX, final int chunkZ, final ChunkStatus targetStatus,
                                          final NewChunkHolder chunkHolder, final List<ChunkProgressionTask> allTasks,
                                          final PrioritisedExecutor.Priority minPriority) {
        if (!this.schedulingLock.isHeldByCurrentThread()) {
            throw new IllegalStateException("Not holding scheduling lock");
        }

        if (chunkHolder.hasGenerationTask()) {
            chunkHolder.upgradeGenTarget(targetStatus);
            return null;
        }

        final PrioritisedExecutor.Priority requestedPriority = PrioritisedExecutor.Priority.max(minPriority, chunkHolder.getEffectivePriority());
        final ChunkStatus currentGenStatus = chunkHolder.getCurrentGenStatus();
        final ChunkAccess chunk = chunkHolder.getCurrentChunk();

        if (currentGenStatus == null) {
            // not yet loaded
            final ChunkProgressionTask task = this.createTask(
                chunkX, chunkZ, chunk, chunkHolder, Collections.emptyList(), ChunkStatus.EMPTY, requestedPriority
            );

            allTasks.add(task);

            final List<NewChunkHolder> chunkHolderNeighbours = new ArrayList<>(1);
            chunkHolderNeighbours.add(chunkHolder);

            chunkHolder.setGenerationTarget(targetStatus);
            chunkHolder.setGenerationTask(task, ChunkStatus.EMPTY, chunkHolderNeighbours);

            return task;
        }

        if (currentGenStatus.isOrAfter(targetStatus)) {
            // nothing to do
            return null;
        }

        // we know for sure now that we want to schedule _something_, so set the target
        chunkHolder.setGenerationTarget(targetStatus);

        final ChunkStatus chunkRealStatus = chunk.getStatus();
        final ChunkStatus toStatus = currentGenStatus.getNextStatus();

        // if this chunk has already generated up to or past the specified status, then we don't
        // need the neighbours AT ALL.
        final int neighbourReadRadius = chunkRealStatus.isOrAfter(toStatus) ? toStatus.loadRange : toStatus.getRange();

        boolean unGeneratedNeighbours = false;

        // copied from MCUtil.getSpiralOutChunks
        for (int r = 1; r <= neighbourReadRadius; r++) {
            int x = -r;
            int z = r;

            // Iterates the edge of half of the box; then negates for other half.
            while (x <= r && z > -r) {
                final int radius = Math.max(Math.abs(x), Math.abs(z));
                final ChunkStatus requiredNeighbourStatus = ChunkMap.getDependencyStatus(toStatus, radius);

                unGeneratedNeighbours |= this.checkNeighbour(
                    chunkX + x, chunkZ + z, requiredNeighbourStatus, chunkHolder, allTasks, requestedPriority
                );
                unGeneratedNeighbours |= this.checkNeighbour(
                    chunkX - x, chunkZ - z, requiredNeighbourStatus, chunkHolder, allTasks, requestedPriority
                );

                if (x < r) {
                    x++;
                } else {
                    z--;
                }
            }
        }

        if (unGeneratedNeighbours) {
            // can't schedule, but neighbour completion will schedule for us when they're ALL done

            // propagate our priority to neighbours
            chunkHolder.recalculateNeighbourPriorities();
            return null;
        }

        // need to gather neighbours

        final List<ChunkAccess> neighbours;
        final List<NewChunkHolder> chunkHolderNeighbours;
        if (neighbourReadRadius <= 0) {
            neighbours = new ArrayList<>(1);
            chunkHolderNeighbours = new ArrayList<>(1);
            neighbours.add(chunk);
            chunkHolderNeighbours.add(chunkHolder);
        } else {
            // the iteration order is _very_ important, as all generation statuses expect a certain order such that:
            // chunkAtRelative = neighbours.get(relX + relZ * (2 * radius + 1))
            neighbours = new ArrayList<>((2 * neighbourReadRadius + 1) * (2 * neighbourReadRadius + 1));
            chunkHolderNeighbours = new ArrayList<>((2 * neighbourReadRadius + 1) * (2 * neighbourReadRadius + 1));
            for (int dz = -neighbourReadRadius; dz <= neighbourReadRadius; ++dz) {
                for (int dx = -neighbourReadRadius; dx <= neighbourReadRadius; ++dx) {
                    final NewChunkHolder holder = (dx | dz) == 0 ? chunkHolder : this.chunkHolderManager.getChunkHolder(dx + chunkX, dz + chunkZ);
                    neighbours.add(holder.getChunkForNeighbourAccess());
                    chunkHolderNeighbours.add(holder);
                }
            }
        }

        final ChunkProgressionTask task = this.createTask(chunkX, chunkZ, chunk, chunkHolder, neighbours, toStatus, chunkHolder.getEffectivePriority());
        allTasks.add(task);

        chunkHolder.setGenerationTask(task, toStatus, chunkHolderNeighbours);

        return task;
    }

    // rets true if the neighbour is not at the required status, false otherwise
    private boolean checkNeighbour(final int chunkX, final int chunkZ, final ChunkStatus requiredStatus, final NewChunkHolder center,
                                   final List<ChunkProgressionTask> tasks, final PrioritisedExecutor.Priority minPriority) {
        final NewChunkHolder chunkHolder = this.chunkHolderManager.getChunkHolder(chunkX, chunkZ);

        if (chunkHolder == null) {
            throw new IllegalStateException("Missing chunkholder when required");
        }

        final ChunkStatus holderStatus = chunkHolder.getCurrentGenStatus();
        if (holderStatus != null && holderStatus.isOrAfter(requiredStatus)) {
            return false;
        }

        if (chunkHolder.hasFailedGeneration()) {
            return true;
        }

        center.addGenerationBlockingNeighbour(chunkHolder);
        chunkHolder.addWaitingNeighbour(center, requiredStatus);

        if (chunkHolder.upgradeGenTarget(requiredStatus)) {
            return true;
        }

        // not at status required, so we need to schedule its generation
        this.schedule(
            chunkX, chunkZ, requiredStatus, chunkHolder, tasks, minPriority
        );

        return true;
    }

    /**
     * @deprecated Chunk tasks must be tied to coordinates in the future
     */
    @Deprecated
    public PrioritisedExecutor.PrioritisedTask scheduleChunkTask(final Runnable run) {
        return this.scheduleChunkTask(run, PrioritisedExecutor.Priority.NORMAL);
    }

    /**
     * @deprecated Chunk tasks must be tied to coordinates in the future
     */
    @Deprecated
    public PrioritisedExecutor.PrioritisedTask scheduleChunkTask(final Runnable run, final PrioritisedExecutor.Priority priority) {
        return this.mainThreadExecutor.queueRunnable(run, priority);
    }

    public PrioritisedExecutor.PrioritisedTask createChunkTask(final int chunkX, final int chunkZ, final Runnable run) {
        return this.createChunkTask(chunkX, chunkZ, run, PrioritisedExecutor.Priority.NORMAL);
    }

    public PrioritisedExecutor.PrioritisedTask createChunkTask(final int chunkX, final int chunkZ, final Runnable run,
                                                               final PrioritisedExecutor.Priority priority) {
        return this.mainThreadExecutor.createTask(run, priority);
    }

    public PrioritisedExecutor.PrioritisedTask scheduleChunkTask(final int chunkX, final int chunkZ, final Runnable run) {
        return this.mainThreadExecutor.queueRunnable(run);
    }

    public PrioritisedExecutor.PrioritisedTask scheduleChunkTask(final int chunkX, final int chunkZ, final Runnable run,
                                                                 final PrioritisedExecutor.Priority priority) {
        return this.mainThreadExecutor.queueRunnable(run, priority);
    }

    public void executeTasksUntil(final BooleanSupplier exit) {
        if (Bukkit.isPrimaryThread()) {
            this.mainThreadExecutor.executeConditionally(exit);
        } else {
            long counter = 1L;
            while (!exit.getAsBoolean()) {
                counter = ConcurrentUtil.linearLongBackoff(counter, 100_000L, 5_000_000L); // 100us, 5ms
            }
        }
    }

    public boolean halt(final boolean sync, final long maxWaitNS) {
        this.lightExecutor.halt();
        this.genExecutor.halt();
        this.parallelGenExecutor.halt();
        this.loadExecutor.halt();
        final long time = System.nanoTime();
        if (sync) {
            for (long failures = 9L;; failures = ConcurrentUtil.linearLongBackoff(failures, 500_000L, 50_000_000L)) {
                if (
                    !this.lightExecutor.isActive() &&
                        !this.genExecutor.isActive() &&
                        !this.parallelGenExecutor.isActive() &&
                        !this.loadExecutor.isActive()
                ) {
                    return true;
                }
                if ((System.nanoTime() - time) >= maxWaitNS) {
                    return false;
                }
            }
        }

        return true;
    }

    public static final ArrayDeque<ChunkInfo> WAITING_CHUNKS = new ArrayDeque<>(); // stack

    public static final class ChunkInfo {

        public final int chunkX;
        public final int chunkZ;
        public final ServerLevel world;

        public ChunkInfo(final int chunkX, final int chunkZ, final ServerLevel world) {
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
            this.world = world;
        }

        @Override
        public String toString() {
            return "[( " + this.chunkX + "," + this.chunkZ + ") in '" + this.world.getWorld().getName() + "']";
        }
    }

    public static void pushChunkWait(final ServerLevel world, final int chunkX, final int chunkZ) {
        synchronized (WAITING_CHUNKS) {
            WAITING_CHUNKS.push(new ChunkInfo(chunkX, chunkZ, world));
        }
    }

    public static void popChunkWait() {
        synchronized (WAITING_CHUNKS) {
            WAITING_CHUNKS.pop();
        }
    }

    public static ChunkInfo[] getChunkInfos() {
        synchronized (WAITING_CHUNKS) {
            return WAITING_CHUNKS.toArray(new ChunkInfo[0]);
        }
    }

    public static void dumpAllChunkLoadInfo(final boolean longPrint) {
        final ChunkInfo[] chunkInfos = getChunkInfos();
        if (chunkInfos.length > 0) {
            LOGGER.error("Chunk wait task info below: ");
            for (final ChunkInfo chunkInfo : chunkInfos) {
                final NewChunkHolder holder = chunkInfo.world.chunkTaskScheduler.chunkHolderManager.getChunkHolder(chunkInfo.chunkX, chunkInfo.chunkZ);
                LOGGER.error("Chunk wait: " + chunkInfo);
                LOGGER.error("Chunk holder: " + holder);
            }

            if (longPrint) {
                final File file = new File(new File(new File("."), "debug"), "chunks-watchdog.txt");
                LOGGER.error("Writing chunk information dump to " + file);
                try {
                    MCUtil.dumpChunks(file, true);
                    LOGGER.error("Successfully written chunk information!");
                } catch (final Throwable thr) {
                    MinecraftServer.LOGGER.warn("Failed to dump chunk information to file " + file.toString(), thr);
                }
            }
        }
    }
}
