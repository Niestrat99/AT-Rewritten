package io.papermc.paper.chunk.system.light;

import ca.spottedleaf.concurrentutil.executor.standard.PrioritisedExecutor;
import ca.spottedleaf.starlight.common.light.BlockStarLightEngine;
import ca.spottedleaf.starlight.common.light.SkyStarLightEngine;
import ca.spottedleaf.starlight.common.light.StarLightInterface;
import io.papermc.paper.chunk.system.scheduling.ChunkTaskScheduler;
import io.papermc.paper.util.CoordinateUtils;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.ShortCollection;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;

public final class LightQueue {

    protected final Long2ObjectOpenHashMap<ChunkTasks> chunkTasks = new Long2ObjectOpenHashMap<>();
    protected final StarLightInterface manager;
    protected final ServerLevel world;

    public LightQueue(final StarLightInterface manager) {
        this.manager = manager;
        this.world = ((ServerLevel)manager.getWorld());
    }

    public void lowerPriority(final int chunkX, final int chunkZ, final PrioritisedExecutor.Priority priority) {
        final ChunkTasks task;
        synchronized (this) {
            task = this.chunkTasks.get(CoordinateUtils.getChunkKey(chunkX, chunkZ));
        }
        if (task != null) {
            task.lowerPriority(priority);
        }
    }

    public void setPriority(final int chunkX, final int chunkZ, final PrioritisedExecutor.Priority priority) {
        final ChunkTasks task;
        synchronized (this) {
            task = this.chunkTasks.get(CoordinateUtils.getChunkKey(chunkX, chunkZ));
        }
        if (task != null) {
            task.setPriority(priority);
        }
    }

    public void raisePriority(final int chunkX, final int chunkZ, final PrioritisedExecutor.Priority priority) {
        final ChunkTasks task;
        synchronized (this) {
            task = this.chunkTasks.get(CoordinateUtils.getChunkKey(chunkX, chunkZ));
        }
        if (task != null) {
            task.raisePriority(priority);
        }
    }

    public PrioritisedExecutor.Priority getPriority(final int chunkX, final int chunkZ) {
        final ChunkTasks task;
        synchronized (this) {
            task = this.chunkTasks.get(CoordinateUtils.getChunkKey(chunkX, chunkZ));
        }
        if (task != null) {
            return task.getPriority();
        }

        return PrioritisedExecutor.Priority.COMPLETING;
    }

    public boolean isEmpty() {
        synchronized (this) {
            return this.chunkTasks.isEmpty();
        }
    }

    public CompletableFuture<Void> queueBlockChange(final BlockPos pos) {
        final ChunkTasks tasks;
        synchronized (this) {
            tasks = this.chunkTasks.computeIfAbsent(CoordinateUtils.getChunkKey(pos), (final long keyInMap) -> {
                return new ChunkTasks(keyInMap, LightQueue.this.manager, LightQueue.this);
            });
            tasks.changedPositions.add(pos.immutable());
        }

        tasks.schedule();

        return tasks.onComplete;
    }

    public CompletableFuture<Void> queueSectionChange(final SectionPos pos, final boolean newEmptyValue) {
        final ChunkTasks tasks;
        synchronized (this) {
            tasks = this.chunkTasks.computeIfAbsent(CoordinateUtils.getChunkKey(pos), (final long keyInMap) -> {
                return new ChunkTasks(keyInMap, LightQueue.this.manager, LightQueue.this);
            });

            if (tasks.changedSectionSet == null) {
                tasks.changedSectionSet = new Boolean[this.manager.maxSection - this.manager.minSection + 1];
            }
            tasks.changedSectionSet[pos.getY() - this.manager.minSection] = Boolean.valueOf(newEmptyValue);
        }

        tasks.schedule();

        return tasks.onComplete;
    }

    public CompletableFuture<Void> queueChunkLightTask(final ChunkPos pos, final BooleanSupplier lightTask, final PrioritisedExecutor.Priority priority) {
        final ChunkTasks tasks;
        synchronized (this) {
            tasks = this.chunkTasks.computeIfAbsent(CoordinateUtils.getChunkKey(pos), (final long keyInMap) -> {
                return new ChunkTasks(keyInMap, LightQueue.this.manager, LightQueue.this, priority);
            });
            if (tasks.lightTasks == null) {
                tasks.lightTasks = new ArrayList<>();
            }
            tasks.lightTasks.add(lightTask);
        }

        tasks.schedule();

        return tasks.onComplete;
    }

    public CompletableFuture<Void> queueChunkSkylightEdgeCheck(final SectionPos pos, final ShortCollection sections) {
        final ChunkTasks tasks;
        synchronized (this) {
            tasks = this.chunkTasks.computeIfAbsent(CoordinateUtils.getChunkKey(pos), (final long keyInMap) -> {
                return new ChunkTasks(keyInMap, LightQueue.this.manager, LightQueue.this);
            });

            ShortOpenHashSet queuedEdges = tasks.queuedEdgeChecksSky;
            if (queuedEdges == null) {
                queuedEdges = tasks.queuedEdgeChecksSky = new ShortOpenHashSet();
            }
            queuedEdges.addAll(sections);
        }

        tasks.schedule();

        return tasks.onComplete;
    }

    public CompletableFuture<Void> queueChunkBlocklightEdgeCheck(final SectionPos pos, final ShortCollection sections) {
        final ChunkTasks tasks;

        synchronized (this) {
            tasks = this.chunkTasks.computeIfAbsent(CoordinateUtils.getChunkKey(pos), (final long keyInMap) -> {
                return new ChunkTasks(keyInMap, LightQueue.this.manager, LightQueue.this);
            });

            ShortOpenHashSet queuedEdges = tasks.queuedEdgeChecksBlock;
            if (queuedEdges == null) {
                queuedEdges = tasks.queuedEdgeChecksBlock = new ShortOpenHashSet();
            }
            queuedEdges.addAll(sections);
        }

        tasks.schedule();

        return tasks.onComplete;
    }

    public void removeChunk(final ChunkPos pos) {
        final ChunkTasks tasks;
        synchronized (this) {
            tasks = this.chunkTasks.remove(CoordinateUtils.getChunkKey(pos));
        }
        if (tasks != null && tasks.cancel()) {
            tasks.onComplete.complete(null);
        }
    }

    protected static final class ChunkTasks implements Runnable {

        final Set<BlockPos> changedPositions = new HashSet<>();
        Boolean[] changedSectionSet;
        ShortOpenHashSet queuedEdgeChecksSky;
        ShortOpenHashSet queuedEdgeChecksBlock;
        List<BooleanSupplier> lightTasks;

        final CompletableFuture<Void> onComplete = new CompletableFuture<>();

        public final long chunkCoordinate;
        private final StarLightInterface lightEngine;
        private final LightQueue queue;
        private final PrioritisedExecutor.PrioritisedTask task;

        public ChunkTasks(final long chunkCoordinate, final StarLightInterface lightEngine, final LightQueue queue) {
            this(chunkCoordinate, lightEngine, queue, PrioritisedExecutor.Priority.NORMAL);
        }

        public ChunkTasks(final long chunkCoordinate, final StarLightInterface lightEngine, final LightQueue queue,
                          final PrioritisedExecutor.Priority priority) {
            this.chunkCoordinate = chunkCoordinate;
            this.lightEngine = lightEngine;
            this.queue = queue;
            this.task = queue.world.chunkTaskScheduler.lightExecutor.createTask(this, priority);
        }

        public void schedule() {
            this.task.queue();
        }

        public boolean cancel() {
            return this.task.cancel();
        }

        public PrioritisedExecutor.Priority getPriority() {
            return this.task.getPriority();
        }

        public void lowerPriority(final PrioritisedExecutor.Priority priority) {
            this.task.lowerPriority(priority);
        }

        public void setPriority(final PrioritisedExecutor.Priority priority) {
            this.task.setPriority(priority);
        }

        public void raisePriority(final PrioritisedExecutor.Priority priority) {
            this.task.raisePriority(priority);
        }

        @Override
        public void run() {
            final SkyStarLightEngine skyEngine = this.lightEngine.getSkyLightEngine();
            final BlockStarLightEngine blockEngine = this.lightEngine.getBlockLightEngine();
            try {
                synchronized (this.queue) {
                    this.queue.chunkTasks.remove(this.chunkCoordinate);
                }

                boolean litChunk = false;
                if (this.lightTasks != null) {
                    for (final BooleanSupplier run : this.lightTasks) {
                        if (run.getAsBoolean()) {
                            litChunk = true;
                            break;
                        }
                    }
                }

                final long coordinate = this.chunkCoordinate;
                final int chunkX = CoordinateUtils.getChunkX(coordinate);
                final int chunkZ = CoordinateUtils.getChunkZ(coordinate);

                final Set<BlockPos> positions = this.changedPositions;
                final Boolean[] sectionChanges = this.changedSectionSet;

                if (!litChunk) {
                    if (skyEngine != null && (!positions.isEmpty() || sectionChanges != null)) {
                        skyEngine.blocksChangedInChunk(this.lightEngine.getLightAccess(), chunkX, chunkZ, positions, sectionChanges);
                    }
                    if (blockEngine != null && (!positions.isEmpty() || sectionChanges != null)) {
                        blockEngine.blocksChangedInChunk(this.lightEngine.getLightAccess(), chunkX, chunkZ, positions, sectionChanges);
                    }

                    if (skyEngine != null && this.queuedEdgeChecksSky != null) {
                        skyEngine.checkChunkEdges(this.lightEngine.getLightAccess(), chunkX, chunkZ, this.queuedEdgeChecksSky);
                    }
                    if (blockEngine != null && this.queuedEdgeChecksBlock != null) {
                        blockEngine.checkChunkEdges(this.lightEngine.getLightAccess(), chunkX, chunkZ, this.queuedEdgeChecksBlock);
                    }
                }

                this.onComplete.complete(null);
            } finally {
                this.lightEngine.releaseSkyLightEngine(skyEngine);
                this.lightEngine.releaseBlockLightEngine(blockEngine);
            }
        }
    }
}
