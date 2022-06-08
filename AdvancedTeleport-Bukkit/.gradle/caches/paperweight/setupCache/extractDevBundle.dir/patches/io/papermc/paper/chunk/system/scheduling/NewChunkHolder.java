package io.papermc.paper.chunk.system.scheduling;

import ca.spottedleaf.concurrentutil.completable.Completable;
import ca.spottedleaf.concurrentutil.executor.Cancellable;
import ca.spottedleaf.concurrentutil.executor.standard.DelayedPrioritisedTask;
import ca.spottedleaf.concurrentutil.executor.standard.PrioritisedExecutor;
import ca.spottedleaf.concurrentutil.util.ConcurrentUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.logging.LogUtils;
import io.papermc.paper.chunk.system.io.RegionFileIOThread;
import io.papermc.paper.chunk.system.poi.PoiChunk;
import io.papermc.paper.util.CoordinateUtils;
import io.papermc.paper.util.TickThread;
import io.papermc.paper.util.WorldUtil;
import io.papermc.paper.world.ChunkEntitySlices;
import it.unimi.dsi.fastutil.objects.Reference2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import net.minecraft.world.level.chunk.storage.EntityStorage;
import org.slf4j.Logger;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public final class NewChunkHolder {

    private static final Logger LOGGER = LogUtils.getClassLogger();

    public static final Thread.UncaughtExceptionHandler CHUNKSYSTEM_UNCAUGHT_EXCEPTION_HANDLER = new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(final Thread thread, final Throwable throwable) {
            if (!(throwable instanceof ThreadDeath)) {
                LOGGER.error("Uncaught exception in thread " + thread.getName(), throwable);
            }
        }
    };

    public final ServerLevel world;
    public final int chunkX;
    public final int chunkZ;

    public final ChunkTaskScheduler scheduler;

    // load/unload state

    // chunk data state

    private ChunkEntitySlices entityChunk;
    // entity chunk that is loaded, but not yet deserialized
    private CompoundTag pendingEntityChunk;

    ChunkEntitySlices loadInEntityChunk(final boolean transientChunk) {
        TickThread.ensureTickThread(this.world, this.chunkX, this.chunkZ, "Cannot sync load entity data off-main");
        final CompoundTag entityChunk;
        final ChunkEntitySlices ret;
        this.scheduler.schedulingLock.lock();
        try {
            if (this.entityChunk != null && (transientChunk || !this.entityChunk.isTransient())) {
                return this.entityChunk;
            }
            final CompoundTag pendingEntityChunk = this.pendingEntityChunk;
            if (!transientChunk && pendingEntityChunk == null) {
                throw new IllegalStateException("Must load entity data from disk before loading in the entity chunk!");
            }

            if (this.entityChunk == null) {
                ret = this.entityChunk = new ChunkEntitySlices(
                    this.world, this.chunkX, this.chunkZ, this.getChunkStatus(),
                    WorldUtil.getMinSection(this.world), WorldUtil.getMaxSection(this.world)
                );

                ret.setTransient(transientChunk);

                this.world.getEntityLookup().entitySectionLoad(this.chunkX, this.chunkZ, ret);
            } else {
                // transientChunk = false here
                ret = this.entityChunk;
                this.entityChunk.setTransient(false);
            }

            if (!transientChunk) {
                this.pendingEntityChunk = null;
                entityChunk = pendingEntityChunk == EMPTY_ENTITY_CHUNK ? null : pendingEntityChunk;
            } else {
                entityChunk = null;
            }
        } finally {
            this.scheduler.schedulingLock.unlock();
        }

        if (!transientChunk) {
            if (entityChunk != null) {
                final List<Entity> entities = EntityStorage.readEntities(this.world, entityChunk);

                this.world.getEntityLookup().addEntityChunkEntities(entities);
            }
        }

        return ret;
    }

    // needed to distinguish whether the entity chunk has been read from disk but is empty or whether it has _not_
    // been read from disk
    private static final CompoundTag EMPTY_ENTITY_CHUNK = new CompoundTag();

    private ChunkLoadTask.EntityDataLoadTask entityDataLoadTask;
    // note: if entityDataLoadTask is cancelled, but on its completion entityDataLoadTaskWaiters.size() != 0,
    // then the task is rescheduled
    private List<GenericDataLoadTaskCallback> entityDataLoadTaskWaiters;

    public ChunkLoadTask.EntityDataLoadTask getEntityDataLoadTask() {
        return this.entityDataLoadTask;
    }

    // must hold schedule lock for the two below functions

    // returns only if the data has been loaded from disk, DOES NOT relate to whether it has been deserialized
    // or added into the world (or even into entityChunk)
    public boolean isEntityChunkNBTLoaded() {
        return (this.entityChunk != null && !this.entityChunk.isTransient()) || this.pendingEntityChunk != null;
    }

    private void completeEntityLoad(final GenericDataLoadTask.TaskResult<CompoundTag, Throwable> result) {
        final List<GenericDataLoadTaskCallback> completeWaiters;
        ChunkLoadTask.EntityDataLoadTask entityDataLoadTask = null;
        boolean scheduleEntityTask = false;
        this.scheduler.schedulingLock.lock();
        try {
            final List<GenericDataLoadTaskCallback> waiters = this.entityDataLoadTaskWaiters;
            this.entityDataLoadTask = null;
            if (result != null) {
                this.entityDataLoadTaskWaiters = null;
                this.pendingEntityChunk = result.left() == null ? EMPTY_ENTITY_CHUNK : result.left();
                if (result.right() != null) {
                    LOGGER.error("Unhandled entity data load exception, data data will be lost: ", result.right());
                }

                completeWaiters = waiters;
            } else {
                // cancelled
                completeWaiters = null;

                // need to re-schedule?
                if (waiters.isEmpty()) {
                    this.entityDataLoadTaskWaiters = null;
                    // no tasks to schedule _for_
                } else {
                    entityDataLoadTask = this.entityDataLoadTask = new ChunkLoadTask.EntityDataLoadTask(
                        this.scheduler, this.world, this.chunkX, this.chunkZ, this.getEffectivePriority()
                    );
                    entityDataLoadTask.addCallback(this::completeEntityLoad);
                    // need one schedule() per waiter
                    for (final GenericDataLoadTaskCallback callback : waiters) {
                        scheduleEntityTask |= entityDataLoadTask.schedule(true);
                    }
                }
            }
        } finally {
            this.scheduler.schedulingLock.unlock();
        }

        if (scheduleEntityTask) {
            entityDataLoadTask.scheduleNow();
        }

        // avoid holding the scheduling lock while completing
        if (completeWaiters != null) {
            for (final GenericDataLoadTaskCallback callback : completeWaiters) {
                callback.accept(result);
            }
        }

        this.scheduler.schedulingLock.lock();
        try {
            this.checkUnload();
        } finally {
            this.scheduler.schedulingLock.unlock();
        }
    }

    // note: it is guaranteed that the consumer cannot be called for the entirety that the schedule lock is held
    // however, when the consumer is invoked, it will hold the schedule lock
    public GenericDataLoadTaskCallback getOrLoadEntityData(final Consumer<GenericDataLoadTask.TaskResult<CompoundTag, Throwable>> consumer) {
        if (this.isEntityChunkNBTLoaded()) {
            throw new IllegalStateException("Cannot load entity data, it is already loaded");
        }
        // why not just acquire the lock? because the caller NEEDS to call isEntityChunkNBTLoaded before this!
        if (!this.scheduler.schedulingLock.isHeldByCurrentThread()) {
            throw new IllegalStateException("Must hold scheduling lock");
        }

        final GenericDataLoadTaskCallback ret = new EntityDataLoadTaskCallback((Consumer)consumer, this);

        if (this.entityDataLoadTask == null) {
            this.entityDataLoadTask = new ChunkLoadTask.EntityDataLoadTask(
                this.scheduler, this.world, this.chunkX, this.chunkZ, this.getEffectivePriority()
            );
            this.entityDataLoadTask.addCallback(this::completeEntityLoad);
            this.entityDataLoadTaskWaiters = new ArrayList<>();
        }
        this.entityDataLoadTaskWaiters.add(ret);
        if (this.entityDataLoadTask.schedule(true)) {
            ret.schedule = this.entityDataLoadTask;
        }
        this.checkUnload();

        return ret;
    }

    private static final class EntityDataLoadTaskCallback extends GenericDataLoadTaskCallback {

        public EntityDataLoadTaskCallback(final Consumer<GenericDataLoadTask.TaskResult<?, Throwable>> consumer, final NewChunkHolder chunkHolder) {
            super(consumer, chunkHolder);
        }

        @Override
        void internalCancel() {
            this.chunkHolder.entityDataLoadTaskWaiters.remove(this);
            this.chunkHolder.entityDataLoadTask.cancel();
        }
    }

    private PoiChunk poiChunk;

    private ChunkLoadTask.PoiDataLoadTask poiDataLoadTask;
    // note: if entityDataLoadTask is cancelled, but on its completion entityDataLoadTaskWaiters.size() != 0,
    // then the task is rescheduled
    private List<GenericDataLoadTaskCallback> poiDataLoadTaskWaiters;

    public ChunkLoadTask.PoiDataLoadTask getPoiDataLoadTask() {
        return this.poiDataLoadTask;
    }

    // must hold schedule lock for the two below functions

    public boolean isPoiChunkLoaded() {
        return this.poiChunk != null;
    }

    private void completePoiLoad(final GenericDataLoadTask.TaskResult<PoiChunk, Throwable> result) {
        final List<GenericDataLoadTaskCallback> completeWaiters;
        ChunkLoadTask.PoiDataLoadTask poiDataLoadTask = null;
        boolean schedulePoiTask = false;
        this.scheduler.schedulingLock.lock();
        try {
            final List<GenericDataLoadTaskCallback> waiters = this.poiDataLoadTaskWaiters;
            this.poiDataLoadTask = null;
            if (result != null) {
                this.poiDataLoadTaskWaiters = null;
                this.poiChunk = result.left();
                if (result.right() != null) {
                    LOGGER.error("Unhandled poi load exception, poi data will be lost: ", result.right());
                }

                completeWaiters = waiters;
            } else {
                // cancelled
                completeWaiters = null;

                // need to re-schedule?
                if (waiters.isEmpty()) {
                    this.poiDataLoadTaskWaiters = null;
                    // no tasks to schedule _for_
                } else {
                    poiDataLoadTask = this.poiDataLoadTask = new ChunkLoadTask.PoiDataLoadTask(
                        this.scheduler, this.world, this.chunkX, this.chunkZ, this.getEffectivePriority()
                    );
                    poiDataLoadTask.addCallback(this::completePoiLoad);
                    // need one schedule() per waiter
                    for (final GenericDataLoadTaskCallback callback : waiters) {
                        schedulePoiTask |= poiDataLoadTask.schedule(true);
                    }
                }
            }
        } finally {
            this.scheduler.schedulingLock.unlock();
        }

        if (schedulePoiTask) {
            poiDataLoadTask.scheduleNow();
        }

        // avoid holding the scheduling lock while completing
        if (completeWaiters != null) {
            for (final GenericDataLoadTaskCallback callback : completeWaiters) {
                callback.accept(result);
            }
        }
        this.scheduler.schedulingLock.lock();
        try {
            this.checkUnload();
        } finally {
            this.scheduler.schedulingLock.unlock();
        }
    }

    // note: it is guaranteed that the consumer cannot be called for the entirety that the schedule lock is held
    // however, when the consumer is invoked, it will hold the schedule lock
    public GenericDataLoadTaskCallback getOrLoadPoiData(final Consumer<GenericDataLoadTask.TaskResult<PoiChunk, Throwable>> consumer) {
        if (this.isPoiChunkLoaded()) {
            throw new IllegalStateException("Cannot load poi data, it is already loaded");
        }
        // why not just acquire the lock? because the caller NEEDS to call isPoiChunkLoaded before this!
        if (!this.scheduler.schedulingLock.isHeldByCurrentThread()) {
            throw new IllegalStateException("Must hold scheduling lock");
        }

        final GenericDataLoadTaskCallback ret = new PoiDataLoadTaskCallback((Consumer)consumer, this);

        if (this.poiDataLoadTask == null) {
            this.poiDataLoadTask = new ChunkLoadTask.PoiDataLoadTask(
                this.scheduler, this.world, this.chunkX, this.chunkZ, this.getEffectivePriority()
            );
            this.poiDataLoadTask.addCallback(this::completePoiLoad);
            this.poiDataLoadTaskWaiters = new ArrayList<>();
        }
        this.poiDataLoadTaskWaiters.add(ret);
        if (this.poiDataLoadTask.schedule(true)) {
            ret.schedule = this.poiDataLoadTask;
        }
        this.checkUnload();

        return ret;
    }

    private static final class PoiDataLoadTaskCallback extends GenericDataLoadTaskCallback {

        public PoiDataLoadTaskCallback(final Consumer<GenericDataLoadTask.TaskResult<?, Throwable>> consumer, final NewChunkHolder chunkHolder) {
            super(consumer, chunkHolder);
        }

        @Override
        void internalCancel() {
            this.chunkHolder.poiDataLoadTaskWaiters.remove(this);
            this.chunkHolder.poiDataLoadTask.cancel();
        }
    }

    public static abstract class GenericDataLoadTaskCallback implements Cancellable, Consumer<GenericDataLoadTask.TaskResult<?, Throwable>> {

        protected final Consumer<GenericDataLoadTask.TaskResult<?, Throwable>> consumer;
        protected final NewChunkHolder chunkHolder;
        protected boolean completed;
        protected GenericDataLoadTask<?, ?> schedule;
        protected final AtomicBoolean scheduled = new AtomicBoolean();

        public GenericDataLoadTaskCallback(final Consumer<GenericDataLoadTask.TaskResult<?, Throwable>> consumer,
                                           final NewChunkHolder chunkHolder) {
            this.consumer = consumer;
            this.chunkHolder = chunkHolder;
        }

        public void schedule() {
            if (this.scheduled.getAndSet(true)) {
                throw new IllegalStateException("Double calling schedule()");
            }
            if (this.schedule != null) {
                this.schedule.scheduleNow();
                this.schedule = null;
            }
        }

        boolean isCompleted() {
            return this.completed;
        }

        // must hold scheduling lock
        private boolean setCompleted() {
            if (this.completed) {
                return false;
            }
            return this.completed = true;
        }

        @Override
        public void accept(final GenericDataLoadTask.TaskResult<?, Throwable> result) {
            if (result != null) {
                if (this.setCompleted()) {
                    this.consumer.accept(result);
                } else {
                    throw new IllegalStateException("Cannot be cancelled at this point");
                }
            } else {
                throw new NullPointerException("Result cannot be null (cancelled)");
            }
        }

        // holds scheduling lock
        abstract void internalCancel();

        @Override
        public boolean cancel() {
            this.chunkHolder.scheduler.schedulingLock.lock();
            try {
                if (!this.completed) {
                    this.completed = true;
                    this.internalCancel();
                    return true;
                }
                return false;
            } finally {
                this.chunkHolder.scheduler.schedulingLock.unlock();
            }
        }
    }

    private ChunkAccess currentChunk;

    // generation status state

    /**
     * Current status the chunk has been brought up to by the chunk system. null indicates no work at all
     */
    private ChunkStatus currentGenStatus;

    // This allows unsynchronised access to the chunk and last gen status
    private volatile ChunkCompletion lastChunkCompletion;

    public ChunkCompletion getLastChunkCompletion() {
        return this.lastChunkCompletion;
    }

    public static final record ChunkCompletion(ChunkAccess chunk, ChunkStatus genStatus) {};

    /**
     * The target final chunk status the chunk system will bring the chunk to.
     */
    private ChunkStatus requestedGenStatus;

    private ChunkProgressionTask generationTask;
    private ChunkStatus generationTaskStatus;

    /**
     * contains the neighbours that this chunk generation is blocking on
     */
    protected final ReferenceLinkedOpenHashSet<NewChunkHolder> neighboursBlockingGenTask = new ReferenceLinkedOpenHashSet<>(4);

    /**
     * map of ChunkHolder -> Required Status for this chunk
     */
    protected final Reference2ObjectLinkedOpenHashMap<NewChunkHolder, ChunkStatus> neighboursWaitingForUs = new Reference2ObjectLinkedOpenHashMap<>();

    public void addGenerationBlockingNeighbour(final NewChunkHolder neighbour) {
        this.neighboursBlockingGenTask.add(neighbour);
    }

    public void addWaitingNeighbour(final NewChunkHolder neighbour, final ChunkStatus requiredStatus) {
        final boolean wasEmpty = this.neighboursWaitingForUs.isEmpty();
        this.neighboursWaitingForUs.put(neighbour, requiredStatus);
        if (wasEmpty) {
            this.checkUnload();
        }
    }

    // priority state

    // the target priority for this chunk to generate at
    // TODO this will screw over scheduling at lower priorities to neighbours, fix
    private PrioritisedExecutor.Priority priority = PrioritisedExecutor.Priority.NORMAL;
    private boolean priorityLocked;

    // the priority neighbouring chunks have requested this chunk generate at
    private PrioritisedExecutor.Priority neighbourRequestedPriority = PrioritisedExecutor.Priority.IDLE;

    public PrioritisedExecutor.Priority getEffectivePriority() {
        return PrioritisedExecutor.Priority.max(this.priority, this.neighbourRequestedPriority);
    }

    protected void recalculateNeighbourRequestedPriority() {
        if (this.neighboursWaitingForUs.isEmpty()) {
            this.neighbourRequestedPriority = PrioritisedExecutor.Priority.IDLE;
            return;
        }

        PrioritisedExecutor.Priority max = PrioritisedExecutor.Priority.IDLE;

        for (final NewChunkHolder holder : this.neighboursWaitingForUs.keySet()) {
            final PrioritisedExecutor.Priority neighbourPriority = holder.getEffectivePriority();
            if (neighbourPriority.isHigherPriority(max)) {
                max = neighbourPriority;
            }
        }

        final PrioritisedExecutor.Priority current = this.getEffectivePriority();
        this.neighbourRequestedPriority = max;
        final PrioritisedExecutor.Priority next = this.getEffectivePriority();

        if (current == next) {
            return;
        }

        // our effective priority has changed, so change our task
        if (this.generationTask != null) {
            this.generationTask.setPriority(next);
        }

        // now propagate this to our neighbours
        this.recalculateNeighbourPriorities();
    }

    public void recalculateNeighbourPriorities() {
        for (final NewChunkHolder holder : this.neighboursBlockingGenTask) {
            holder.recalculateNeighbourRequestedPriority();
        }
    }

    // must hold scheduling lock
    public void raisePriority(final PrioritisedExecutor.Priority priority) {
        if (this.priority != null && this.priority.isHigherOrEqualPriority(priority)) {
            return;
        }
        this.setPriority(priority);
    }

    private void lockPriority() {
        this.priority = PrioritisedExecutor.Priority.NORMAL;
        this.priorityLocked = true;
    }

    // must hold scheduling lock
    public void setPriority(final PrioritisedExecutor.Priority priority) {
        if (this.priorityLocked) {
            return;
        }
        final PrioritisedExecutor.Priority old = this.getEffectivePriority();
        this.priority = priority;
        final PrioritisedExecutor.Priority newPriority = this.getEffectivePriority();

        if (old != newPriority) {
            if (this.generationTask != null) {
                this.generationTask.setPriority(newPriority);
            }
        }

        this.recalculateNeighbourPriorities();
    }

    // must hold scheduling lock
    public void lowerPriority(final PrioritisedExecutor.Priority priority) {
        if (this.priority != null && this.priority.isLowerOrEqualPriority(priority)) {
            return;
        }
        this.setPriority(priority);
    }

    // error handling state
    private ChunkStatus failedGenStatus;
    private Throwable genTaskException;
    private Thread genTaskFailedThread;

    private boolean failedLightUpdate;

    public void failedLightUpdate() {
        this.failedLightUpdate = true;
    }

    public boolean hasFailedGeneration() {
        return this.genTaskException != null;
    }

    // ticket level state
    private int oldTicketLevel = ChunkMap.MAX_CHUNK_DISTANCE + 1;
    private int currentTicketLevel = ChunkMap.MAX_CHUNK_DISTANCE + 1;

    public int getTicketLevel() {
        return this.currentTicketLevel;
    }

    public final ChunkHolder vanillaChunkHolder;

    public NewChunkHolder(final ServerLevel world, final int chunkX, final int chunkZ, final ChunkTaskScheduler scheduler) {
        this.world = world;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.scheduler = scheduler;
        this.vanillaChunkHolder = new ChunkHolder(new ChunkPos(chunkX, chunkZ), world, world.getLightEngine(), world.chunkSource.chunkMap, this);
    }

    protected ImposterProtoChunk wrappedChunkForNeighbour;

    // holds scheduling lock
    public ChunkAccess getChunkForNeighbourAccess() {
        // Vanilla overrides the status futures with an imposter chunk to prevent writes to full chunks
        // But we don't store per-status futures, so we need this hack
        if (this.wrappedChunkForNeighbour != null) {
            return this.wrappedChunkForNeighbour;
        }
        final ChunkAccess ret = this.currentChunk;
        return ret instanceof LevelChunk fullChunk ? this.wrappedChunkForNeighbour = new ImposterProtoChunk(fullChunk, false) : ret;
    }

    public ChunkAccess getCurrentChunk() {
        return this.currentChunk;
    }

    int getCurrentTicketLevel() {
        return this.currentTicketLevel;
    }

    void updateTicketLevel(final int toLevel) {
        this.currentTicketLevel = toLevel;
    }

    private int totalNeighboursUsingThisChunk = 0;

    // holds schedule lock
    public void addNeighbourUsingChunk() {
        final int now = ++this.totalNeighboursUsingThisChunk;

        if (now == 1) {
            this.checkUnload();
        }
    }

    // holds schedule lock
    public void removeNeighbourUsingChunk() {
        final int now = --this.totalNeighboursUsingThisChunk;

        if (now == 0) {
            this.checkUnload();
        }

        if (now < 0) {
            throw new IllegalStateException("Neighbours using this chunk cannot be negative");
        }
    }

    // must hold scheduling lock
    // returns string reason for why chunk should remain loaded, null otherwise
    public final String isSafeToUnload() {
        // is ticket level below threshold?
        if (this.oldTicketLevel <= ChunkHolderManager.MAX_TICKET_LEVEL) {
            return "ticket_level";
        }

        // are we being used by another chunk for generation?
        if (this.totalNeighboursUsingThisChunk != 0) {
            return "neighbours_generating";
        }

        // are we going to be used by another chunk for generation?
        if (!this.neighboursWaitingForUs.isEmpty()) {
            return "neighbours_waiting";
        }

        // chunk must be marked inaccessible (i.e unloaded to plugins)
        if (this.getChunkStatus() != ChunkHolder.FullChunkStatus.INACCESSIBLE) {
            return "fullchunkstatus";
        }

        // are we currently generating anything, or have requested generation?
        if (this.generationTask != null) {
            return "generating";
        }
        if (this.requestedGenStatus != null) {
            return "requested_generation";
        }

        // entity data requested?
        if (this.entityDataLoadTask != null) {
            return "entity_data_requested";
        }

        // poi data requested?
        if (this.poiDataLoadTask != null) {
            return "poi_data_requested";
        }

        // are we pending serialization?
        if (this.entityDataUnload != null) {
            return "entity_serialization";
        }
        if (this.poiDataUnload != null) {
            return "poi_serialization";
        }
        if (this.chunkDataUnload != null) {
            return "chunk_serialization";
        }

        // Note: light tasks do not need a check, as they add a ticket.

        // nothing is using this chunk, so it should be unloaded
        return null;
    }

    /** Unloaded from chunk map */
    boolean killed;

    // must hold scheduling lock
    private void checkUnload() {
        if (this.killed) {
            return;
        }
        if (this.isSafeToUnload() == null) {
            // ensure in unload queue
            this.scheduler.chunkHolderManager.unloadQueue.add(this);
        } else {
            // ensure not in unload queue
            this.scheduler.chunkHolderManager.unloadQueue.remove(this);
        }
    }

    static final record UnloadState(NewChunkHolder holder, ChunkAccess chunk, ChunkEntitySlices entityChunk, PoiChunk poiChunk) {};

    // note: these are completed with null to indicate that no write occurred
    // they are also completed with null to indicate a null write occurred
    private UnloadTask chunkDataUnload;
    private UnloadTask entityDataUnload;
    private UnloadTask poiDataUnload;

    public static final record UnloadTask(Completable<CompoundTag> completable, DelayedPrioritisedTask task) {}

    public UnloadTask getUnloadTask(final RegionFileIOThread.RegionFileType type) {
        switch (type) {
            case CHUNK_DATA:
                return this.chunkDataUnload;
            case ENTITY_DATA:
                return this.entityDataUnload;
            case POI_DATA:
                return this.poiDataUnload;
            default:
                throw new IllegalStateException("Unknown regionfile type " + type);
        }
    }

    private UnloadState unloadState;

    // holds schedule lock
    UnloadState unloadStage1() {
        // because we hold the scheduling lock, we cannot actually unload anything
        // so we need to null this chunk's state
        ChunkAccess chunk = this.currentChunk;
        ChunkEntitySlices entityChunk = this.entityChunk;
        PoiChunk poiChunk = this.poiChunk;
        // chunk state
        this.currentChunk = null;
        this.currentGenStatus = null;
        this.wrappedChunkForNeighbour = null;
        this.lastChunkCompletion = null;
        // entity chunk state
        this.entityChunk = null;
        this.pendingEntityChunk = null;

        // poi chunk state
        this.poiChunk = null;

        // priority state
        this.priorityLocked = false;

        if (chunk != null) {
            this.chunkDataUnload = new UnloadTask(new Completable<>(), new DelayedPrioritisedTask(PrioritisedExecutor.Priority.NORMAL));
        }
        if (poiChunk != null) {
            this.poiDataUnload = new UnloadTask(new Completable<>(), null);
        }
        if (entityChunk != null) {
            this.entityDataUnload = new UnloadTask(new Completable<>(), null);
        }

        return this.unloadState = (chunk != null || entityChunk != null || poiChunk != null) ? new UnloadState(this, chunk, entityChunk, poiChunk) : null;
    }

    // data is null if failed or does not need to be saved
    void completeAsyncChunkDataSave(final CompoundTag data) {
        if (data != null) {
            RegionFileIOThread.scheduleSave(this.world, this.chunkX, this.chunkZ, data, RegionFileIOThread.RegionFileType.CHUNK_DATA);
        }
        this.chunkDataUnload.completable().complete(data);
        this.scheduler.schedulingLock.lock();
        try {
            // can only write to these fields while holding the schedule lock
            this.chunkDataUnload = null;
            this.checkUnload();
        } finally {
            this.scheduler.schedulingLock.unlock();
        }
    }

    void unloadStage2(final UnloadState state) {
        this.unloadState = null;
        final ChunkAccess chunk = state.chunk();
        final ChunkEntitySlices entityChunk = state.entityChunk();
        final PoiChunk poiChunk = state.poiChunk();

        final boolean shouldLevelChunkNotSave = (chunk instanceof LevelChunk levelChunk && levelChunk.mustNotSave);

        // unload chunk data
        if (chunk != null) {
            if (chunk instanceof LevelChunk levelChunk) {
                levelChunk.setLoaded(false);
            }

            if (!shouldLevelChunkNotSave) {
                this.saveChunk(chunk, true);
            } else {
                this.completeAsyncChunkDataSave(null);
            }

            if (chunk instanceof LevelChunk levelChunk) {
                this.world.unload(levelChunk);
            }
        }

        // unload entity data
        if (entityChunk != null) {
            this.saveEntities(entityChunk, true);
            // yes this is a hack to pass the compound tag through...
            final CompoundTag lastEntityUnload = this.lastEntityUnload;
            this.lastEntityUnload = null;

            if (entityChunk.unload()) {
                this.scheduler.schedulingLock.lock();
                try {
                    entityChunk.setTransient(true);
                    this.entityChunk = entityChunk;
                } finally {
                    this.scheduler.schedulingLock.unlock();
                }
            } else {
                this.world.getEntityLookup().entitySectionUnload(this.chunkX, this.chunkZ);
            }
            // we need to delay the callback until after determining transience, otherwise a potential loader could
            // set entityChunk before we do
            this.entityDataUnload.completable().complete(lastEntityUnload);
        }

        // unload poi data
        if (poiChunk != null) {
            if (poiChunk.isDirty() && !shouldLevelChunkNotSave) {
                this.savePOI(poiChunk, true);
            } else {
                this.poiDataUnload.completable().complete(null);
            }

            if (poiChunk.isLoaded()) {
                this.world.getPoiManager().onUnload(CoordinateUtils.getChunkKey(this.chunkX, this.chunkZ));
            }
        }
    }

    boolean unloadStage3() {
        // can only write to these while holding the schedule lock, and we instantly complete them in stage2
        this.poiDataUnload = null;
        this.entityDataUnload = null;

        // we need to check if anything has been loaded in the meantime (or if we have transient entities)
        if (this.entityChunk != null || this.poiChunk != null || this.currentChunk != null) {
            return false;
        }

        return this.isSafeToUnload() == null;
    }

    private void cancelGenTask() {
        if (this.generationTask != null) {
            this.generationTask.cancel();
        } else {
            // otherwise, we are blocking on neighbours, so remove them
            if (!this.neighboursBlockingGenTask.isEmpty()) {
                for (final NewChunkHolder neighbour : this.neighboursBlockingGenTask) {
                    if (neighbour.neighboursWaitingForUs.remove(this) == null) {
                        throw new IllegalStateException("Corrupt state");
                    }
                    if (neighbour.neighboursWaitingForUs.isEmpty()) {
                        neighbour.checkUnload();
                    }
                }
                this.neighboursBlockingGenTask.clear();
                this.checkUnload();
            }
        }
    }

    // holds: ticket level update lock
    // holds: schedule lock
    public void processTicketLevelUpdate(final List<ChunkProgressionTask> scheduledTasks, final List<NewChunkHolder> changedLoadStatus) {
        final int oldLevel = this.oldTicketLevel;
        final int newLevel = this.currentTicketLevel;

        if (oldLevel == newLevel) {
            return;
        }

        this.oldTicketLevel = newLevel;

        final ChunkHolder.FullChunkStatus oldState = ChunkHolder.getFullChunkStatus(oldLevel);
        final ChunkHolder.FullChunkStatus newState = ChunkHolder.getFullChunkStatus(newLevel);
        final boolean oldUnloaded = oldLevel > ChunkHolderManager.MAX_TICKET_LEVEL;
        final boolean newUnloaded = newLevel > ChunkHolderManager.MAX_TICKET_LEVEL;

        final ChunkStatus maxGenerationStatusOld = ChunkHolder.getStatus(oldLevel);
        final ChunkStatus maxGenerationStatusNew = ChunkHolder.getStatus(newLevel);

        // check for cancellations from downgrading ticket level
        if (this.requestedGenStatus != null && !newState.isOrAfter(ChunkHolder.FullChunkStatus.BORDER) && newLevel > oldLevel) {
            // note: cancel() may invoke onChunkGenComplete synchronously here
            if (newUnloaded) {
                // need to cancel all tasks
                // note: requested status must be set to null here before cancellation, to indicate to the
                // completion logic that we do not want rescheduling to occur
                this.requestedGenStatus = null;
                this.cancelGenTask();
            } else {
                final ChunkStatus toCancel = maxGenerationStatusNew.getNextStatus();
                final ChunkStatus currentRequestedStatus = this.requestedGenStatus;

                if (currentRequestedStatus.isOrAfter(toCancel)) {
                    // we do have to cancel something here
                    // clamp requested status to the maximum
                    if (this.currentGenStatus != null && this.currentGenStatus.isOrAfter(maxGenerationStatusNew)) {
                        // already generated to status, so we must cancel
                        this.requestedGenStatus = null;
                        this.cancelGenTask();
                    } else {
                        // not generated to status, so we may have to cancel
                        // note: gen task is always 1 status above current gen status if not null
                        this.requestedGenStatus = maxGenerationStatusNew;
                        if (this.generationTaskStatus != null && this.generationTaskStatus.isOrAfter(toCancel)) {
                            // TOOD is this even possible? i don't think so
                            throw new IllegalStateException("?????");
                        }
                    }
                }
            }
        }

        if (newState != oldState) {
            if (newState.isOrAfter(oldState)) {
                // status upgrade
                if (!oldState.isOrAfter(ChunkHolder.FullChunkStatus.BORDER) && newState.isOrAfter(ChunkHolder.FullChunkStatus.BORDER)) {
                    // may need to schedule full load
                    if (this.currentGenStatus != ChunkStatus.FULL) {
                        if (this.requestedGenStatus != null) {
                            this.requestedGenStatus = ChunkStatus.FULL;
                        } else {
                            this.scheduler.schedule(
                                this.chunkX, this.chunkZ, ChunkStatus.FULL, this, scheduledTasks
                            );
                        }
                    } else {
                        // now we are fully loaded
                        this.queueBorderFullStatus(true, changedLoadStatus);
                    }
                }
            } else {
                // status downgrade
                if (!newState.isOrAfter(ChunkHolder.FullChunkStatus.ENTITY_TICKING) && oldState.isOrAfter(ChunkHolder.FullChunkStatus.ENTITY_TICKING)) {
                    this.completeFullStatusConsumers(ChunkHolder.FullChunkStatus.ENTITY_TICKING, null);
                }

                if (!newState.isOrAfter(ChunkHolder.FullChunkStatus.TICKING) && oldState.isOrAfter(ChunkHolder.FullChunkStatus.TICKING)) {
                    this.completeFullStatusConsumers(ChunkHolder.FullChunkStatus.TICKING, null);
                }

                if (!newState.isOrAfter(ChunkHolder.FullChunkStatus.BORDER) && oldState.isOrAfter(ChunkHolder.FullChunkStatus.BORDER)) {
                    this.completeFullStatusConsumers(ChunkHolder.FullChunkStatus.BORDER, null);
                }
            }
        }

        if (oldState != newState) {
            if (this.onTicketUpdate(oldState, newState)) {
                changedLoadStatus.add(this);
            }
        }

        if (oldUnloaded != newUnloaded) {
            this.checkUnload();
        }
    }

    /*
        For full chunks, vanilla just loads chunks around it up to FEATURES, 1 radius

        For ticking chunks, it updates the persistent entity manager (soon to be completely nuked by EntitySliceManager, which
        will also need to be updated but with far less implications)
        It also shoves the scheduled block ticks into the tick scheduler

        For entity ticking chunks, updates the entity manager (see above)
     */

    static final int NEIGHBOUR_RADIUS = 2;
    private long fullNeighbourChunksLoadedBitset;

    private static int getFullNeighbourIndex(final int relativeX, final int relativeZ) {
        // index = (relativeX + NEIGHBOUR_CACHE_RADIUS) + (relativeZ + NEIGHBOUR_CACHE_RADIUS) * (NEIGHBOUR_CACHE_RADIUS * 2 + 1)
        // optimised variant of the above by moving some of the ops to compile time
        return relativeX + (relativeZ * (NEIGHBOUR_RADIUS * 2 + 1)) + (NEIGHBOUR_RADIUS + NEIGHBOUR_RADIUS * ((NEIGHBOUR_RADIUS * 2 + 1)));
    }
    public final boolean isNeighbourFullLoaded(final int relativeX, final int relativeZ) {
        return (this.fullNeighbourChunksLoadedBitset & (1L << getFullNeighbourIndex(relativeX, relativeZ))) != 0;
    }

    // returns true if this chunk changed full status
    public final boolean setNeighbourFullLoaded(final int relativeX, final int relativeZ) {
        final long before = this.fullNeighbourChunksLoadedBitset;
        final int index = getFullNeighbourIndex(relativeX, relativeZ);
        this.fullNeighbourChunksLoadedBitset |= (1L << index);
        return this.onNeighbourChange(before, this.fullNeighbourChunksLoadedBitset);
    }

    // returns true if this chunk changed full status
    public final boolean setNeighbourFullUnloaded(final int relativeX, final int relativeZ) {
        final long before = this.fullNeighbourChunksLoadedBitset;
        final int index = getFullNeighbourIndex(relativeX, relativeZ);
        this.fullNeighbourChunksLoadedBitset &= ~(1L << index);
        return this.onNeighbourChange(before, this.fullNeighbourChunksLoadedBitset);
    }

    public static boolean areNeighboursFullLoaded(final long bitset, final int radius) {
        // index = relativeX + (relativeZ * (NEIGHBOUR_CACHE_RADIUS * 2 + 1)) + (NEIGHBOUR_CACHE_RADIUS + NEIGHBOUR_CACHE_RADIUS * ((NEIGHBOUR_CACHE_RADIUS * 2 + 1)))
        switch (radius) {
            case 0: {
                return (bitset & (1L << getFullNeighbourIndex(0, 0))) != 0L;
            }
            case 1: {
                long mask = 0L;
                for (int dx = -1; dx <= 1; ++dx) {
                    for (int dz = -1; dz <= 1; ++dz) {
                        mask |= (1L << getFullNeighbourIndex(dx, dz));
                    }
                }
                return (bitset & mask) == mask;
            }
            case 2: {
                long mask = 0L;
                for (int dx = -2; dx <= 2; ++dx) {
                    for (int dz = -2; dz <= 2; ++dz) {
                        mask |= (1L << getFullNeighbourIndex(dx, dz));
                    }
                }
                return (bitset & mask) == mask;
            }

            default: {
                throw new IllegalArgumentException("Radius not recognized: " + radius);
            }
        }
    }

    // upper 16 bits are pending status, lower 16 bits are current status
    private volatile long chunkStatus;
    private static final long PENDING_STATUS_MASK = Long.MIN_VALUE >> 31;
    private static final ChunkHolder.FullChunkStatus[] CHUNK_STATUS_BY_ID = ChunkHolder.FullChunkStatus.values();
    private static final VarHandle CHUNK_STATUS_HANDLE = ConcurrentUtil.getVarHandle(NewChunkHolder.class, "chunkStatus", long.class);

    public static ChunkHolder.FullChunkStatus getCurrentChunkStatus(final long encoded) {
        return CHUNK_STATUS_BY_ID[(int)encoded];
    }

    public static ChunkHolder.FullChunkStatus getPendingChunkStatus(final long encoded) {
        return CHUNK_STATUS_BY_ID[(int)(encoded >>> 32)];
    }

    public ChunkHolder.FullChunkStatus getChunkStatus() {
        return getCurrentChunkStatus(((long)CHUNK_STATUS_HANDLE.getVolatile((NewChunkHolder)this)));
    }

    public boolean isEntityTickingReady() {
        return this.getChunkStatus().isOrAfter(ChunkHolder.FullChunkStatus.ENTITY_TICKING);
    }

    public boolean isTickingReady() {
        return this.getChunkStatus().isOrAfter(ChunkHolder.FullChunkStatus.TICKING);
    }

    public boolean isFullChunkReady() {
        return this.getChunkStatus().isOrAfter(ChunkHolder.FullChunkStatus.BORDER);
    }

    private static ChunkHolder.FullChunkStatus getStatusForBitset(final long bitset) {
        if (areNeighboursFullLoaded(bitset, 2)) {
            return ChunkHolder.FullChunkStatus.ENTITY_TICKING;
        } else if (areNeighboursFullLoaded(bitset, 1)) {
            return ChunkHolder.FullChunkStatus.TICKING;
        } else if (areNeighboursFullLoaded(bitset, 0)) {
            return ChunkHolder.FullChunkStatus.BORDER;
        } else {
            return ChunkHolder.FullChunkStatus.INACCESSIBLE;
        }
    }

    // note: only while updating ticket level, so holds ticket update lock + scheduling lock
    protected final boolean onTicketUpdate(final ChunkHolder.FullChunkStatus oldState, final ChunkHolder.FullChunkStatus newState) {
        if (oldState == newState) {
            return false;
        }

        // preserve border request after full status complete, as it does not set anything in the bitset
        ChunkHolder.FullChunkStatus byNeighbours = getStatusForBitset(this.fullNeighbourChunksLoadedBitset);
        if (byNeighbours == ChunkHolder.FullChunkStatus.INACCESSIBLE && newState.isOrAfter(ChunkHolder.FullChunkStatus.BORDER) && this.currentGenStatus == ChunkStatus.FULL) {
            byNeighbours = ChunkHolder.FullChunkStatus.BORDER;
        }

        final ChunkHolder.FullChunkStatus toSet;

        if (newState.isOrAfter(byNeighbours)) {
            // must clamp to neighbours level, even though we have the ticket level
            toSet = byNeighbours;
        } else {
            // must clamp to ticket level, even though we have the neighbours
            toSet = newState;
        }

        long curr = (long)CHUNK_STATUS_HANDLE.getVolatile((NewChunkHolder)this);

        if (curr == ((long)toSet.ordinal() | ((long)toSet.ordinal() << 32))) {
            // nothing to do
            return false;
        }

        int failures = 0;
        for (;;) {
            final long update = (curr & ~PENDING_STATUS_MASK) | ((long)toSet.ordinal() << 32);
            if (curr == (curr = (long)CHUNK_STATUS_HANDLE.compareAndExchange((NewChunkHolder)this, curr, update))) {
                return true;
            }

            ++failures;
            for (int i = 0; i < failures; ++i) {
                ConcurrentUtil.backoff();
            }
        }
    }

    protected final boolean onNeighbourChange(final long bitsetBefore, final long bitsetAfter) {
        ChunkHolder.FullChunkStatus oldState = getStatusForBitset(bitsetBefore);
        ChunkHolder.FullChunkStatus newState = getStatusForBitset(bitsetAfter);
        final ChunkHolder.FullChunkStatus currStateTicketLevel = ChunkHolder.getFullChunkStatus(this.oldTicketLevel);
        if (oldState.isOrAfter(currStateTicketLevel)) {
            oldState = currStateTicketLevel;
        }
        if (newState.isOrAfter(currStateTicketLevel)) {
            newState = currStateTicketLevel;
        }
        // preserve border request after full status complete, as it does not set anything in the bitset
        if (newState == ChunkHolder.FullChunkStatus.INACCESSIBLE && currStateTicketLevel.isOrAfter(ChunkHolder.FullChunkStatus.BORDER) && this.currentGenStatus == ChunkStatus.FULL) {
            newState = ChunkHolder.FullChunkStatus.BORDER;
        }

        if (oldState == newState) {
            return false;
        }

        int failures = 0;
        for (long curr = (long)CHUNK_STATUS_HANDLE.getVolatile((NewChunkHolder)this);;) {
            final long update = (curr & ~PENDING_STATUS_MASK) | ((long)newState.ordinal() << 32);
            if (curr == (curr = (long)CHUNK_STATUS_HANDLE.compareAndExchange((NewChunkHolder)this, curr, update))) {
                return true;
            }

            ++failures;
            for (int i = 0; i < failures; ++i) {
                ConcurrentUtil.backoff();
            }
        }
    }

    private boolean queueBorderFullStatus(final boolean loaded, final List<NewChunkHolder> changedFullStatus) {
        final ChunkHolder.FullChunkStatus toStatus = loaded ? ChunkHolder.FullChunkStatus.BORDER : ChunkHolder.FullChunkStatus.INACCESSIBLE;

        int failures = 0;
        for (long curr = (long)CHUNK_STATUS_HANDLE.getVolatile((NewChunkHolder)this);;) {
            final ChunkHolder.FullChunkStatus currPending = getPendingChunkStatus(curr);
            if (loaded && currPending != ChunkHolder.FullChunkStatus.INACCESSIBLE) {
                throw new IllegalStateException("Expected " + ChunkHolder.FullChunkStatus.INACCESSIBLE + " for pending, but got " + currPending);
            }

            final long update = (curr & ~PENDING_STATUS_MASK) | ((long)toStatus.ordinal() << 32);
            if (curr == (curr = (long)CHUNK_STATUS_HANDLE.compareAndExchange((NewChunkHolder)this, curr, update))) {
                if ((int)(update) != (int)(update >>> 32)) {
                    changedFullStatus.add(this);
                    return true;
                }
                return false;
            }

            ++failures;
            for (int i = 0; i < failures; ++i) {
                ConcurrentUtil.backoff();
            }
        }
    }

    // only call on main thread, must hold ticket level and scheduling lock
    private void onFullChunkLoadChange(final boolean loaded, final List<NewChunkHolder> changedFullStatus) {
        for (int dz = -NEIGHBOUR_RADIUS; dz <= NEIGHBOUR_RADIUS; ++dz) {
            for (int dx = -NEIGHBOUR_RADIUS; dx <= NEIGHBOUR_RADIUS; ++dx) {
                final NewChunkHolder holder = (dx | dz) == 0 ? this : this.scheduler.chunkHolderManager.getChunkHolder(dx + this.chunkX, dz + this.chunkZ);
                if (loaded) {
                    if (holder.setNeighbourFullLoaded(-dx, -dz)) {
                        changedFullStatus.add(holder);
                    }
                } else {
                    if (holder != null && holder.setNeighbourFullUnloaded(-dx, -dz)) {
                        changedFullStatus.add(holder);
                    }
                }
            }
        }
    }

    private ChunkHolder.FullChunkStatus updateCurrentState(final ChunkHolder.FullChunkStatus to) {
        int failures = 0;
        for (long curr = (long)CHUNK_STATUS_HANDLE.getVolatile((NewChunkHolder)this);;) {
            final long update = (curr & PENDING_STATUS_MASK) | (long)to.ordinal();
            if (curr == (curr = (long)CHUNK_STATUS_HANDLE.compareAndExchange((NewChunkHolder)this, curr, update))) {
                return getPendingChunkStatus(curr);
            }

            ++failures;
            for (int i = 0; i < failures; ++i) {
                ConcurrentUtil.backoff();
            }
        }
    }

    private void changeEntityChunkStatus(final ChunkHolder.FullChunkStatus toStatus) {
        this.world.getEntityLookup().chunkStatusChange(this.chunkX, this.chunkZ, toStatus);
    }

    private boolean processingFullStatus = false;

    // only to be called on the main thread, no locks need to be held
    public boolean handleFullStatusChange(final List<NewChunkHolder> changedFullStatus) {
        TickThread.ensureTickThread(this.world, this.chunkX, this.chunkZ, "Cannot update full status thread off-main");

        boolean ret = false;

        if (this.processingFullStatus) {
            // we cannot process updates recursively
            return ret;
        }

        // note: use opaque reads for chunk status read since we need it to be atomic

        // test if anything changed
        final long statusCheck = (long)CHUNK_STATUS_HANDLE.getOpaque((NewChunkHolder)this);
        if ((int)statusCheck == (int)(statusCheck >>> 32)) {
            // nothing changed
            return ret;
        }

        final ChunkTaskScheduler scheduler = this.scheduler;
        final ChunkHolderManager holderManager = scheduler.chunkHolderManager;
        final int ticketKeep;
        final Long ticketId;
        holderManager.ticketLock.lock();
        try {
            ticketKeep = this.currentTicketLevel;
            ticketId = Long.valueOf(holderManager.getNextStatusUpgradeId());
            holderManager.addTicketAtLevel(TicketType.STATUS_UPGRADE, this.chunkX, this.chunkZ, ticketKeep, ticketId);
        } finally {
            holderManager.ticketLock.unlock();
        }

        this.processingFullStatus = true;
        try {
            for (;;) {
                final long currStateEncoded = (long)CHUNK_STATUS_HANDLE.getOpaque((NewChunkHolder)this);
                final ChunkHolder.FullChunkStatus currState = getCurrentChunkStatus(currStateEncoded);
                ChunkHolder.FullChunkStatus nextState = getPendingChunkStatus(currStateEncoded);
                if (currState == nextState) {
                    if (nextState == ChunkHolder.FullChunkStatus.INACCESSIBLE) {
                        this.scheduler.schedulingLock.lock();
                        try {
                            this.checkUnload();
                        } finally {
                            this.scheduler.schedulingLock.unlock();
                        }
                    }
                    break;
                }

                // chunks cannot downgrade state while status is pending a change
                final LevelChunk chunk = (LevelChunk)this.currentChunk;

                // Note: we assume that only load/unload contain plugin logic
                // plugin logic is anything stupid enough to possibly change the chunk status while it is already
                // being changed (i.e during load it is possible it will try to set to full ticking)
                // in order to allow this change, we also need this plugin logic to be contained strictly after all
                // of the chunk system load callbacks are invoked
                if (nextState.isOrAfter(currState)) {
                    // state upgrade
                    if (!currState.isOrAfter(ChunkHolder.FullChunkStatus.BORDER) && nextState.isOrAfter(ChunkHolder.FullChunkStatus.BORDER)) {
                        nextState = this.updateCurrentState(ChunkHolder.FullChunkStatus.BORDER);
                        holderManager.ensureInAutosave(this);
                        chunk.pushChunkIntoLoadedMap();
                        this.changeEntityChunkStatus(ChunkHolder.FullChunkStatus.BORDER);
                        chunk.onChunkLoad(this);
                        this.onFullChunkLoadChange(true, changedFullStatus);
                        this.completeFullStatusConsumers(ChunkHolder.FullChunkStatus.BORDER, chunk);
                    }

                    if (!currState.isOrAfter(ChunkHolder.FullChunkStatus.TICKING) && nextState.isOrAfter(ChunkHolder.FullChunkStatus.TICKING)) {
                        nextState = this.updateCurrentState(ChunkHolder.FullChunkStatus.TICKING);
                        this.changeEntityChunkStatus(ChunkHolder.FullChunkStatus.TICKING);
                        chunk.onChunkTicking(this);
                        this.completeFullStatusConsumers(ChunkHolder.FullChunkStatus.TICKING, chunk);
                    }

                    if (!currState.isOrAfter(ChunkHolder.FullChunkStatus.ENTITY_TICKING) && nextState.isOrAfter(ChunkHolder.FullChunkStatus.ENTITY_TICKING)) {
                        nextState = this.updateCurrentState(ChunkHolder.FullChunkStatus.ENTITY_TICKING);
                        this.changeEntityChunkStatus(ChunkHolder.FullChunkStatus.ENTITY_TICKING);
                        chunk.onChunkEntityTicking(this);
                        this.completeFullStatusConsumers(ChunkHolder.FullChunkStatus.ENTITY_TICKING, chunk);
                    }
                } else {
                    if (currState.isOrAfter(ChunkHolder.FullChunkStatus.ENTITY_TICKING) && !nextState.isOrAfter(ChunkHolder.FullChunkStatus.ENTITY_TICKING)) {
                        this.changeEntityChunkStatus(ChunkHolder.FullChunkStatus.TICKING);
                        chunk.onChunkNotEntityTicking(this);
                        nextState = this.updateCurrentState(ChunkHolder.FullChunkStatus.TICKING);
                    }

                    if (currState.isOrAfter(ChunkHolder.FullChunkStatus.TICKING) && !nextState.isOrAfter(ChunkHolder.FullChunkStatus.TICKING)) {
                        this.changeEntityChunkStatus(ChunkHolder.FullChunkStatus.BORDER);
                        chunk.onChunkNotTicking(this);
                        nextState = this.updateCurrentState(ChunkHolder.FullChunkStatus.BORDER);
                    }

                    if (currState.isOrAfter(ChunkHolder.FullChunkStatus.BORDER) && !nextState.isOrAfter(ChunkHolder.FullChunkStatus.BORDER)) {
                        this.onFullChunkLoadChange(false, changedFullStatus);
                        this.changeEntityChunkStatus(ChunkHolder.FullChunkStatus.INACCESSIBLE);
                        chunk.onChunkUnload(this);
                        nextState = this.updateCurrentState(ChunkHolder.FullChunkStatus.INACCESSIBLE);
                    }
                }

                ret = true;
            }
        } finally {
            this.processingFullStatus = false;
            holderManager.removeTicketAtLevel(TicketType.STATUS_UPGRADE, this.chunkX, this.chunkZ, ticketKeep, ticketId);
        }

        return ret;
    }

    // note: must hold scheduling lock
    // rets true if the current requested gen status is not null (effectively, whether further scheduling is not needed)
    boolean upgradeGenTarget(final ChunkStatus toStatus) {
        if (toStatus == null) {
            throw new NullPointerException("toStatus cannot be null");
        }
        if (this.requestedGenStatus == null && this.generationTask == null) {
            return false;
        }
        if (this.requestedGenStatus == null || !this.requestedGenStatus.isOrAfter(toStatus)) {
            this.requestedGenStatus = toStatus;
        }
        return true;
    }

    public void setGenerationTarget(final ChunkStatus toStatus) {
        this.requestedGenStatus = toStatus;
    }

    public boolean hasGenerationTask() {
        return this.generationTask != null;
    }

    public ChunkStatus getCurrentGenStatus() {
        return this.currentGenStatus;
    }

    public ChunkStatus getRequestedGenStatus() {
        return this.requestedGenStatus;
    }

    private final Reference2ObjectOpenHashMap<ChunkStatus, List<Consumer<ChunkAccess>>> statusWaiters = new Reference2ObjectOpenHashMap<>();

    void addStatusConsumer(final ChunkStatus status, final Consumer<ChunkAccess> consumer) {
        this.statusWaiters.computeIfAbsent(status, (final ChunkStatus keyInMap) -> {
            return new ArrayList<>(4);
        }).add(consumer);
    }

    private void completeStatusConsumers(ChunkStatus status, final ChunkAccess chunk) {
        // need to tell future statuses to complete if cancelled
        do {
            this.completeStatusConsumers0(status, chunk);
        } while (chunk == null && status != (status = status.getNextStatus()));
    }

    private void completeStatusConsumers0(final ChunkStatus status, final ChunkAccess chunk) {
        final List<Consumer<ChunkAccess>> consumers;
        consumers = this.statusWaiters.remove(status);

        if (consumers == null) {
            return;
        }

        // must be scheduled to main, we do not trust the callback to not do anything stupid
        this.scheduler.scheduleChunkTask(this.chunkX, this.chunkZ, () -> {
            for (final Consumer<ChunkAccess> consumer : consumers) {
                try {
                    consumer.accept(chunk);
                } catch (final ThreadDeath thr) {
                    throw thr;
                } catch (final Throwable thr) {
                    LOGGER.error("Failed to process chunk status callback", thr);
                }
            }
        }, PrioritisedExecutor.Priority.HIGHEST);
    }

    private final Reference2ObjectOpenHashMap<ChunkHolder.FullChunkStatus, List<Consumer<LevelChunk>>> fullStatusWaiters = new Reference2ObjectOpenHashMap<>();

    void addFullStatusConsumer(final ChunkHolder.FullChunkStatus status, final Consumer<LevelChunk> consumer) {
        this.fullStatusWaiters.computeIfAbsent(status, (final ChunkHolder.FullChunkStatus keyInMap) -> {
            return new ArrayList<>(4);
        }).add(consumer);
    }

    private void completeFullStatusConsumers(ChunkHolder.FullChunkStatus status, final LevelChunk chunk) {
        // need to tell future statuses to complete if cancelled
        final ChunkHolder.FullChunkStatus max = CHUNK_STATUS_BY_ID[CHUNK_STATUS_BY_ID.length - 1];

        for (;;) {
            this.completeFullStatusConsumers0(status, chunk);
            if (chunk != null || status == max) {
                break;
            }
            status = CHUNK_STATUS_BY_ID[status.ordinal() + 1];
        }
    }

    private void completeFullStatusConsumers0(final ChunkHolder.FullChunkStatus status, final LevelChunk chunk) {
        final List<Consumer<LevelChunk>> consumers;
        consumers = this.fullStatusWaiters.remove(status);

        if (consumers == null) {
            return;
        }

        // must be scheduled to main, we do not trust the callback to not do anything stupid
        this.scheduler.scheduleChunkTask(this.chunkX, this.chunkZ, () -> {
            for (final Consumer<LevelChunk> consumer : consumers) {
                try {
                    consumer.accept(chunk);
                } catch (final ThreadDeath thr) {
                    throw thr;
                } catch (final Throwable thr) {
                    LOGGER.error("Failed to process chunk status callback", thr);
                }
            }
        }, PrioritisedExecutor.Priority.HIGHEST);
    }

    // note: must hold scheduling lock
    private void onChunkGenComplete(final ChunkAccess newChunk, final ChunkStatus newStatus,
                                    final List<ChunkProgressionTask> scheduleList, final List<NewChunkHolder> changedLoadStatus) {
        if (!this.neighboursBlockingGenTask.isEmpty()) {
            throw new IllegalStateException("Cannot have neighbours blocking this gen task");
        }
        if (newChunk != null || (this.requestedGenStatus == null || !this.requestedGenStatus.isOrAfter(newStatus))) {
            this.completeStatusConsumers(newStatus, newChunk);
        }
        // done now, clear state (must be done before scheduling new tasks)
        this.generationTask = null;
        this.generationTaskStatus = null;
        if (newChunk == null) {
            // task was cancelled
            // should be careful as this could be called while holding the schedule lock and/or inside the
            // ticket level update
            // while a task may be cancelled, it is possible for it to be later re-scheduled
            // however, because generationTask is only set to null on _completion_, the scheduler leaves
            // the rescheduling logic to us here
            final ChunkStatus requestedGenStatus = this.requestedGenStatus;
            this.requestedGenStatus = null;
            if (requestedGenStatus != null) {
                // it looks like it has been requested, so we must reschedule
                if (!this.neighboursWaitingForUs.isEmpty()) {
                    for (final Iterator<Reference2ObjectMap.Entry<NewChunkHolder, ChunkStatus>> iterator = this.neighboursWaitingForUs.reference2ObjectEntrySet().fastIterator(); iterator.hasNext();) {
                        final Reference2ObjectMap.Entry<NewChunkHolder, ChunkStatus> entry = iterator.next();

                        final NewChunkHolder chunkHolder = entry.getKey();
                        final ChunkStatus toStatus = entry.getValue();

                        if (!requestedGenStatus.isOrAfter(toStatus)) {
                            // if we were cancelled, we are responsible for removing the waiter
                            if (!chunkHolder.neighboursBlockingGenTask.remove(this)) {
                                throw new IllegalStateException("Corrupt state");
                            }
                            if (chunkHolder.neighboursBlockingGenTask.isEmpty()) {
                                chunkHolder.checkUnload();
                            }
                            iterator.remove();
                            continue;
                        }
                    }
                }

                // note: only after generationTask -> null, generationTaskStatus -> null, and requestedGenStatus -> null
                this.scheduler.schedule(
                    this.chunkX, this.chunkZ, requestedGenStatus, this, scheduleList
                );

                // return, can't do anything further
                return;
            }

            if (!this.neighboursWaitingForUs.isEmpty()) {
                for (final NewChunkHolder chunkHolder : this.neighboursWaitingForUs.keySet()) {
                    if (!chunkHolder.neighboursBlockingGenTask.remove(this)) {
                        throw new IllegalStateException("Corrupt state");
                    }
                    if (chunkHolder.neighboursBlockingGenTask.isEmpty()) {
                        chunkHolder.checkUnload();
                    }
                }
                this.neighboursWaitingForUs.clear();
            }
            // reset priority, we have nothing left to generate to
            this.setPriority(PrioritisedExecutor.Priority.NORMAL);
            this.checkUnload();
            return;
        }

        this.currentChunk = newChunk;
        this.currentGenStatus = newStatus;
        this.lastChunkCompletion = new ChunkCompletion(newChunk, newStatus);

        final ChunkStatus requestedGenStatus = this.requestedGenStatus;

        List<NewChunkHolder> needsScheduling = null;
        boolean recalculatePriority = false;
        for (final Iterator<Reference2ObjectMap.Entry<NewChunkHolder, ChunkStatus>> iterator
             = this.neighboursWaitingForUs.reference2ObjectEntrySet().fastIterator(); iterator.hasNext();) {
            final Reference2ObjectMap.Entry<NewChunkHolder, ChunkStatus> entry = iterator.next();
            final NewChunkHolder neighbour = entry.getKey();
            final ChunkStatus requiredStatus = entry.getValue();

            if (!newStatus.isOrAfter(requiredStatus)) {
                if (requestedGenStatus == null || !requestedGenStatus.isOrAfter(requiredStatus)) {
                    // if we're cancelled, still need to clear this map
                    if (!neighbour.neighboursBlockingGenTask.remove(this)) {
                        throw new IllegalStateException("Neighbour is not waiting for us?");
                    }
                    if (neighbour.neighboursBlockingGenTask.isEmpty()) {
                        neighbour.checkUnload();
                    }

                    iterator.remove();
                }
                continue;
            }

            // doesn't matter what isCancelled is here, we need to schedule if we can

            recalculatePriority = true;
            if (!neighbour.neighboursBlockingGenTask.remove(this)) {
                throw new IllegalStateException("Neighbour is not waiting for us?");
            }

            if (neighbour.neighboursBlockingGenTask.isEmpty()) {
                if (neighbour.requestedGenStatus != null) {
                    if (needsScheduling == null) {
                        needsScheduling = new ArrayList<>();
                    }
                    needsScheduling.add(neighbour);
                } else {
                    neighbour.checkUnload();
                }
            }

            // remove last; access to entry will throw if removed
            iterator.remove();
        }

        if (newStatus == ChunkStatus.FULL) {
            this.lockPriority();
            // must use oldTicketLevel, we hold the schedule lock but not the ticket level lock
            // however, schedule lock needs to be held for ticket level callback, so we're fine here
            if (ChunkHolder.getFullChunkStatus(this.oldTicketLevel).isOrAfter(ChunkHolder.FullChunkStatus.BORDER)) {
                this.queueBorderFullStatus(true, changedLoadStatus);
            }
        }

        if (recalculatePriority) {
            this.recalculateNeighbourRequestedPriority();
        }

        if (requestedGenStatus != null && !newStatus.isOrAfter(requestedGenStatus)) {
            this.scheduleNeighbours(needsScheduling, scheduleList);

            // we need to schedule more tasks now
            this.scheduler.schedule(
                this.chunkX, this.chunkZ, requestedGenStatus, this, scheduleList
            );
        } else {
            // we're done now
            if (requestedGenStatus != null) {
                this.requestedGenStatus = null;
            }
            // reached final stage, so stop scheduling now
            this.setPriority(PrioritisedExecutor.Priority.NORMAL);
            this.checkUnload();

            this.scheduleNeighbours(needsScheduling, scheduleList);
        }
    }

    private void scheduleNeighbours(final List<NewChunkHolder> needsScheduling, final List<ChunkProgressionTask> scheduleList) {
        if (needsScheduling != null) {
            for (int i = 0, len = needsScheduling.size(); i < len; ++i) {
                final NewChunkHolder neighbour = needsScheduling.get(i);

                this.scheduler.schedule(
                    neighbour.chunkX, neighbour.chunkZ, neighbour.requestedGenStatus, neighbour, scheduleList
                );
            }
        }
    }

    public void setGenerationTask(final ChunkProgressionTask generationTask, final ChunkStatus taskStatus,
                                  final List<NewChunkHolder> neighbours) {
        if (this.generationTask != null || (this.currentGenStatus != null && this.currentGenStatus.isOrAfter(taskStatus))) {
            throw new IllegalStateException("Currently generating or provided task is trying to generate to a level we are already at!");
        }
        if (this.requestedGenStatus == null || !this.requestedGenStatus.isOrAfter(taskStatus)) {
            throw new IllegalStateException("Cannot schedule generation task when not requested");
        }
        this.generationTask = generationTask;
        this.generationTaskStatus = taskStatus;

        for (int i = 0, len = neighbours.size(); i < len; ++i) {
            neighbours.get(i).addNeighbourUsingChunk();
        }

        this.checkUnload();

        generationTask.onComplete((final ChunkAccess access, final Throwable thr) -> {
            if (generationTask != this.generationTask) {
                throw new IllegalStateException(
                    "Cannot complete generation task '" + generationTask + "' because we are waiting on '" + this.generationTask + "' instead!"
                );
            }
            if (thr != null) {
                if (this.genTaskException != null) {
                    // first one is probably the TRUE problem
                    return;
                }
                // don't set generation task to null, so that scheduling will not attempt to create another task and it
                // will automatically block any further scheduling usage of this chunk as it will wait forever for a failed
                // task to complete
                this.genTaskException = thr;
                this.failedGenStatus = taskStatus;
                this.genTaskFailedThread = Thread.currentThread();

                this.scheduler.unrecoverableChunkSystemFailure(this.chunkX, this.chunkZ, Map.of(
                    "Generation task", ChunkTaskScheduler.stringIfNull(generationTask),
                    "Task to status", ChunkTaskScheduler.stringIfNull(taskStatus)
                ), thr);
                return;
            }

            final boolean scheduleTasks;
            List<ChunkProgressionTask> tasks = ChunkHolderManager.getCurrentTicketUpdateScheduling();
            if (tasks == null) {
                scheduleTasks = true;
                tasks = new ArrayList<>();
            } else {
                scheduleTasks = false;
                // we are currently updating ticket levels, so we already hold the schedule lock
                // this means we have to leave the ticket level update to handle the scheduling
            }
            final List<NewChunkHolder> changedLoadStatus = new ArrayList<>();
            this.scheduler.schedulingLock.lock();
            try {
                for (int i = 0, len = neighbours.size(); i < len; ++i) {
                    neighbours.get(i).removeNeighbourUsingChunk();
                }
                this.onChunkGenComplete(access, taskStatus, tasks, changedLoadStatus);
            } finally {
                this.scheduler.schedulingLock.unlock();
            }
            this.scheduler.chunkHolderManager.addChangedStatuses(changedLoadStatus);

            if (scheduleTasks) {
                // can't hold the lock while scheduling, so we have to build the tasks and then schedule after
                for (int i = 0, len = tasks.size(); i < len; ++i) {
                    tasks.get(i).schedule();
                }
            }
        });
    }

    public PoiChunk getPoiChunk() {
        return this.poiChunk;
    }

    public ChunkEntitySlices getEntityChunk() {
        return this.entityChunk;
    }

    public long lastAutoSave;

    public static final record SaveStat(boolean savedChunk, boolean savedEntityChunk, boolean savedPoiChunk) {}

    public SaveStat save(final boolean shutdown, final boolean unloading) {
        TickThread.ensureTickThread(this.world, this.chunkX, this.chunkZ, "Cannot save data off-main");

        ChunkAccess chunk = this.getCurrentChunk();
        PoiChunk poi = this.getPoiChunk();
        ChunkEntitySlices entities = this.getEntityChunk();
        boolean executedUnloadTask = false;

        if (shutdown) {
            // make sure that the async unloads complete
            if (this.unloadState != null) {
                // must have errored during unload
                chunk = this.unloadState.chunk();
                poi = this.unloadState.poiChunk();
                entities = this.unloadState.entityChunk();
            }
            final UnloadTask chunkUnloadTask = this.chunkDataUnload;
            final DelayedPrioritisedTask chunkDataUnloadTask = chunkUnloadTask == null ? null : chunkUnloadTask.task();
            if (chunkDataUnloadTask != null) {
                final PrioritisedExecutor.PrioritisedTask unloadTask = chunkDataUnloadTask.getTask();
                if (unloadTask != null) {
                    executedUnloadTask = unloadTask.execute();
                }
            }
        }

        boolean canSaveChunk = !(chunk instanceof LevelChunk levelChunk && levelChunk.mustNotSave) &&
                                (chunk != null && ((shutdown || chunk instanceof LevelChunk) && chunk.isUnsaved()));
        boolean canSavePOI = !(chunk instanceof LevelChunk levelChunk && levelChunk.mustNotSave) && (poi != null && poi.isDirty());
        boolean canSaveEntities = entities != null;

        try (co.aikar.timings.Timing ignored = this.world.timings.chunkSave.startTiming()) { // Paper
            if (canSaveChunk) {
                canSaveChunk = this.saveChunk(chunk, unloading);
            }
            if (canSavePOI) {
                canSavePOI = this.savePOI(poi, unloading);
            }
            if (canSaveEntities) {
                // on shutdown, we need to force transient entity chunks to save
                canSaveEntities = this.saveEntities(entities, unloading || shutdown);
                if (unloading || shutdown) {
                    this.lastEntityUnload = null;
                }
            }
        }

        return executedUnloadTask | canSaveChunk | canSaveEntities | canSavePOI ? new SaveStat(executedUnloadTask || canSaveChunk, canSaveEntities, canSavePOI): null;
    }

    static final class AsyncChunkSerializeTask implements Runnable {

        private final ServerLevel world;
        private final ChunkAccess chunk;
        private final ChunkSerializer.AsyncSaveData asyncSaveData;
        private final NewChunkHolder toComplete;

        public AsyncChunkSerializeTask(final ServerLevel world, final ChunkAccess chunk, final ChunkSerializer.AsyncSaveData asyncSaveData,
                                       final NewChunkHolder toComplete) {
            this.world = world;
            this.chunk = chunk;
            this.asyncSaveData = asyncSaveData;
            this.toComplete = toComplete;
        }

        @Override
        public void run() {
            final CompoundTag toSerialize;
            try {
                toSerialize = ChunkSerializer.saveChunk(this.world, this.chunk, this.asyncSaveData);
            } catch (final ThreadDeath death) {
                throw death;
            } catch (final Throwable throwable) {
                LOGGER.error("Failed to asynchronously save chunk " + this.chunk.getPos() + " for world '" + this.world.getWorld().getName() + "', falling back to synchronous save", throwable);
                this.world.chunkTaskScheduler.scheduleChunkTask(this.chunk.locX, this.chunk.locZ, () -> {
                    final CompoundTag synchronousSave;
                    try {
                        synchronousSave = ChunkSerializer.saveChunk(AsyncChunkSerializeTask.this.world, AsyncChunkSerializeTask.this.chunk, AsyncChunkSerializeTask.this.asyncSaveData);
                    } catch (final ThreadDeath death) {
                        throw death;
                    } catch (final Throwable throwable2) {
                        LOGGER.error("Failed to synchronously save chunk " + AsyncChunkSerializeTask.this.chunk.getPos() + " for world '" + AsyncChunkSerializeTask.this.world.getWorld().getName() + "', chunk data will be lost", throwable2);
                        AsyncChunkSerializeTask.this.toComplete.completeAsyncChunkDataSave(null);
                        return;
                    }

                    AsyncChunkSerializeTask.this.toComplete.completeAsyncChunkDataSave(synchronousSave);
                    LOGGER.info("Successfully serialized chunk " + AsyncChunkSerializeTask.this.chunk.getPos() + " for world '" + AsyncChunkSerializeTask.this.world.getWorld().getName() + "' synchronously");

                }, PrioritisedExecutor.Priority.HIGHEST);
                return;
            }
            this.toComplete.completeAsyncChunkDataSave(toSerialize);
        }

        @Override
        public String toString() {
            return "AsyncChunkSerializeTask{" +
                "chunk={pos=" + this.chunk.getPos() + ",world=\"" + this.world.getWorld().getName() + "\"}" +
                "}";
        }
    }

    private boolean saveChunk(final ChunkAccess chunk, final boolean unloading) {
        if (!chunk.isUnsaved()) {
            if (unloading) {
                this.completeAsyncChunkDataSave(null);
            }
            return false;
        }
        boolean completing = false;
        try {
            if (unloading) {
                try {
                    final ChunkSerializer.AsyncSaveData asyncSaveData = ChunkSerializer.getAsyncSaveData(this.world, chunk);

                    final PrioritisedExecutor.PrioritisedTask task = this.scheduler.loadExecutor.createTask(new AsyncChunkSerializeTask(this.world, chunk, asyncSaveData, this));

                    this.chunkDataUnload.task().setTask(task);

                    task.queue();

                    chunk.setUnsaved(false);

                    return true;
                } catch (final ThreadDeath death) {
                    throw death;
                } catch (final Throwable thr) {
                    LOGGER.error("Failed to prepare async chunk data (" + this.chunkX + "," + this.chunkZ + ") in world '" + this.world.getWorld().getName() + "', falling back to synchronous save", thr);
                    // fall through to synchronous save
                }
            }

            final CompoundTag save = ChunkSerializer.saveChunk(this.world, chunk, null);

            if (unloading) {
                completing = true;
                this.completeAsyncChunkDataSave(save);
                LOGGER.info("Successfully serialized chunk data (" + this.chunkX + "," + this.chunkZ + ") in world '" + this.world.getWorld().getName() + "' synchronously");
            } else {
                RegionFileIOThread.scheduleSave(this.world, this.chunkX, this.chunkZ, save, RegionFileIOThread.RegionFileType.CHUNK_DATA);
            }
            chunk.setUnsaved(false);
        } catch (final ThreadDeath death) {
            throw death;
        } catch (final Throwable thr) {
            LOGGER.error("Failed to save chunk data (" + this.chunkX + "," + this.chunkZ + ") in world '" + this.world.getWorld().getName() + "'");
            if (unloading && !completing) {
                this.completeAsyncChunkDataSave(null);
            }
        }

        return true;
    }

    private boolean lastEntitySaveNull;
    private CompoundTag lastEntityUnload;
    private boolean saveEntities(final ChunkEntitySlices entities, final boolean unloading) {
        try {
            CompoundTag mergeFrom = null;
            if (entities.isTransient()) {
                if (!unloading) {
                    // if we're a transient chunk, we cannot save until unloading because otherwise a double save will
                    // result in double adding the entities
                    return false;
                }
                try {
                    mergeFrom = RegionFileIOThread.loadData(this.world, this.chunkX, this.chunkZ, RegionFileIOThread.RegionFileType.ENTITY_DATA, PrioritisedExecutor.Priority.BLOCKING);
                } catch (final Exception ex) {
                    LOGGER.error("Cannot merge transient entities for chunk (" + this.chunkX + "," + this.chunkZ + ") in world '" + this.world.getWorld().getName() + "', data on disk will be replaced", ex);
                }
            }

            final CompoundTag save = entities.save();
            if (mergeFrom != null) {
                if (save == null) {
                    // don't override the data on disk with nothing
                    return false;
                } else {
                    EntityStorage.copyEntities(mergeFrom, save);
                }
            }
            if (save == null && this.lastEntitySaveNull) {
                return false;
            }

            RegionFileIOThread.scheduleSave(this.world, this.chunkX, this.chunkZ, save, RegionFileIOThread.RegionFileType.ENTITY_DATA);
            this.lastEntitySaveNull = save == null;
            if (unloading) {
                this.lastEntityUnload = save;
            }
        } catch (final ThreadDeath death) {
            throw death;
        } catch (final Throwable thr) {
            LOGGER.error("Failed to save entity data (" + this.chunkX + "," + this.chunkZ + ") in world '" + this.world.getWorld().getName() + "'");
        }

        return true;
    }

    private boolean lastPoiSaveNull;
    private boolean savePOI(final PoiChunk poi, final boolean unloading) {
        try {
            final CompoundTag save = poi.save();
            poi.setDirty(false);
            if (save == null && this.lastPoiSaveNull) {
                if (unloading) {
                    this.poiDataUnload.completable().complete(null);
                }
                return false;
            }

            RegionFileIOThread.scheduleSave(this.world, this.chunkX, this.chunkZ, save, RegionFileIOThread.RegionFileType.POI_DATA);
            this.lastPoiSaveNull = save == null;
            if (unloading) {
                this.poiDataUnload.completable().complete(save);
            }
        } catch (final ThreadDeath death) {
            throw death;
        } catch (final Throwable thr) {
            LOGGER.error("Failed to save poi data (" + this.chunkX + "," + this.chunkZ + ") in world '" + this.world.getWorld().getName() + "'");
        }

        return true;
    }

    @Override
    public String toString() {
        final ChunkCompletion lastCompletion = this.lastChunkCompletion;
        final ChunkEntitySlices entityChunk = this.entityChunk;
        final long chunkStatus = this.chunkStatus;
        final int fullChunkStatus = (int)chunkStatus;
        final int pendingChunkStatus = (int)(chunkStatus >>> 32);
        final ChunkHolder.FullChunkStatus currentFullStatus = fullChunkStatus < 0 || fullChunkStatus >= CHUNK_STATUS_BY_ID.length ? null : CHUNK_STATUS_BY_ID[fullChunkStatus];
        final ChunkHolder.FullChunkStatus pendingFullStatus = pendingChunkStatus < 0 || pendingChunkStatus >= CHUNK_STATUS_BY_ID.length ? null : CHUNK_STATUS_BY_ID[pendingChunkStatus];
        return "NewChunkHolder{" +
            "world=" + this.world.getWorld().getName() +
            ", chunkX=" + this.chunkX +
            ", chunkZ=" + this.chunkZ +
            ", entityChunkFromDisk=" + (entityChunk != null && !entityChunk.isTransient()) +
            ", lastChunkCompletion={chunk_class=" + (lastCompletion == null || lastCompletion.chunk() == null ? "null" : lastCompletion.chunk().getClass().getName()) + ",status=" + (lastCompletion == null ? "null" : lastCompletion.genStatus()) + "}" +
            ", currentGenStatus=" + this.currentGenStatus +
            ", requestedGenStatus=" + this.requestedGenStatus +
            ", generationTask=" + this.generationTask +
            ", generationTaskStatus=" + this.generationTaskStatus +
            ", priority=" + this.priority +
            ", priorityLocked=" + this.priorityLocked +
            ", neighbourRequestedPriority=" + this.neighbourRequestedPriority +
            ", effective_priority=" + this.getEffectivePriority() +
            ", oldTicketLevel=" + this.oldTicketLevel +
            ", currentTicketLevel=" + this.currentTicketLevel +
            ", totalNeighboursUsingThisChunk=" + this.totalNeighboursUsingThisChunk +
            ", fullNeighbourChunksLoadedBitset=" + this.fullNeighbourChunksLoadedBitset +
            ", chunkStatusRaw=" + chunkStatus +
            ", currentChunkStatus=" + currentFullStatus +
            ", pendingChunkStatus=" + pendingFullStatus +
            ", is_unload_safe=" + this.isSafeToUnload() +
            ", killed=" + this.killed +
            '}';
    }

    private static JsonElement serializeCompletable(final Completable<?> completable) {
        if (completable == null) {
            return new JsonPrimitive("null");
        }

        final JsonObject ret = new JsonObject();
        final boolean isCompleted = completable.isCompleted();
        ret.addProperty("completed", Boolean.valueOf(isCompleted));

        if (isCompleted) {
            ret.addProperty("completed_exceptionally", Boolean.valueOf(completable.getThrowable() != null));
        }

        return ret;
    }

    // holds ticket and scheduling lock
    public JsonObject getDebugJson() {
        final JsonObject ret = new JsonObject();

        final ChunkCompletion lastCompletion = this.lastChunkCompletion;
        final ChunkEntitySlices slices = this.entityChunk;
        final PoiChunk poiChunk = this.poiChunk;

        ret.addProperty("chunkX", Integer.valueOf(this.chunkX));
        ret.addProperty("chunkZ", Integer.valueOf(this.chunkZ));
        ret.addProperty("entity_chunk", slices == null ? "null" : "transient=" + slices.isTransient());
        ret.addProperty("poi_chunk", "null=" + (poiChunk == null));
        ret.addProperty("completed_chunk_class", lastCompletion == null ? "null" : lastCompletion.chunk().getClass().getName());
        ret.addProperty("completed_gen_status", lastCompletion == null ? "null" : lastCompletion.genStatus().toString());
        ret.addProperty("priority", Objects.toString(this.priority));
        ret.addProperty("neighbour_requested_priority", Objects.toString(this.neighbourRequestedPriority));
        ret.addProperty("generation_task", Objects.toString(this.generationTask));
        ret.addProperty("is_safe_unload", Objects.toString(this.isSafeToUnload()));
        ret.addProperty("old_ticket_level", Integer.valueOf(this.oldTicketLevel));
        ret.addProperty("current_ticket_level", Integer.valueOf(this.currentTicketLevel));
        ret.addProperty("neighbours_using_chunk", Integer.valueOf(this.totalNeighboursUsingThisChunk));

        final JsonObject neighbourWaitState = new JsonObject();
        ret.add("neighbour_state", neighbourWaitState);

        final JsonArray blockingGenNeighbours = new JsonArray();
        neighbourWaitState.add("blocking_gen_task", blockingGenNeighbours);
        for (final NewChunkHolder blockingGenNeighbour : this.neighboursBlockingGenTask) {
            final JsonObject neighbour = new JsonObject();
            blockingGenNeighbours.add(neighbour);

            neighbour.addProperty("chunkX", Integer.valueOf(blockingGenNeighbour.chunkX));
            neighbour.addProperty("chunkZ", Integer.valueOf(blockingGenNeighbour.chunkZ));
        }

        final JsonArray neighboursWaitingForUs = new JsonArray();
        neighbourWaitState.add("neighbours_waiting_on_us", neighboursWaitingForUs);
        for (final Reference2ObjectMap.Entry<NewChunkHolder, ChunkStatus> entry : this.neighboursWaitingForUs.reference2ObjectEntrySet()) {
            final NewChunkHolder holder = entry.getKey();
            final ChunkStatus status = entry.getValue();

            final JsonObject neighbour = new JsonObject();
            neighboursWaitingForUs.add(neighbour);


            neighbour.addProperty("chunkX", Integer.valueOf(holder.chunkX));
            neighbour.addProperty("chunkZ", Integer.valueOf(holder.chunkZ));
            neighbour.addProperty("waiting_for", Objects.toString(status));
        }

        ret.addProperty("fullchunkstatus", Objects.toString(this.getChunkStatus()));
        ret.addProperty("fullchunkstatus_raw", Long.valueOf(this.chunkStatus));
        ret.addProperty("generation_task", Objects.toString(this.generationTask));
        ret.addProperty("requested_generation", Objects.toString(this.requestedGenStatus));
        ret.addProperty("has_entity_load_task", Boolean.valueOf(this.entityDataLoadTask != null));
        ret.addProperty("has_poi_load_task", Boolean.valueOf(this.poiDataLoadTask != null));

        final UnloadTask entityDataUnload = this.entityDataUnload;
        final UnloadTask poiDataUnload = this.poiDataUnload;
        final UnloadTask chunkDataUnload = this.chunkDataUnload;

        ret.add("entity_unload_completable", serializeCompletable(entityDataUnload == null ? null : entityDataUnload.completable()));
        ret.add("poi_unload_completable", serializeCompletable(poiDataUnload == null ? null : poiDataUnload.completable()));
        ret.add("chunk_unload_completable", serializeCompletable(chunkDataUnload == null ? null : chunkDataUnload.completable()));

        final DelayedPrioritisedTask unloadTask = chunkDataUnload == null ? null : chunkDataUnload.task();
        if (unloadTask == null) {
            ret.addProperty("unload_task_priority", "null");
            ret.addProperty("unload_task_priority_raw", "null");
        } else {
            ret.addProperty("unload_task_priority", Objects.toString(unloadTask.getPriority()));
            ret.addProperty("unload_task_priority_raw", Integer.valueOf(unloadTask.getPriorityInternal()));
        }

        ret.addProperty("killed", Boolean.valueOf(this.killed));

        return ret;
    }
}
