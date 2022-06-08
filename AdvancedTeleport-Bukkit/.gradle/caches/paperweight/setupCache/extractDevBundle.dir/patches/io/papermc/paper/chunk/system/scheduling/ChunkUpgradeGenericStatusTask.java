package io.papermc.paper.chunk.system.scheduling;

import ca.spottedleaf.concurrentutil.executor.standard.PrioritisedExecutor;
import ca.spottedleaf.concurrentutil.util.ConcurrentUtil;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.ProtoChunk;
import org.slf4j.Logger;
import java.lang.invoke.VarHandle;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class ChunkUpgradeGenericStatusTask extends ChunkProgressionTask implements Runnable {

    private static final Logger LOGGER = LogUtils.getClassLogger();

    protected final ChunkAccess fromChunk;
    protected final ChunkStatus fromStatus;
    protected final ChunkStatus toStatus;
    protected final List<ChunkAccess> neighbours;

    protected final PrioritisedExecutor.PrioritisedTask generateTask;

    public ChunkUpgradeGenericStatusTask(final ChunkTaskScheduler scheduler, final ServerLevel world, final int chunkX,
                                         final int chunkZ, final ChunkAccess chunk, final List<ChunkAccess> neighbours,
                                         final ChunkStatus toStatus, final PrioritisedExecutor.Priority priority) {
        super(scheduler, world, chunkX, chunkZ);
        if (!PrioritisedExecutor.Priority.isValidPriority(priority)) {
            throw new IllegalArgumentException("Invalid priority " + priority);
        }
        this.fromChunk = chunk;
        this.fromStatus = chunk.getStatus();
        this.toStatus = toStatus;
        this.neighbours = neighbours;
        this.generateTask = (this.toStatus.isParallelCapable ? this.scheduler.parallelGenExecutor : this.scheduler.genExecutor)
            .createTask(this, priority);
    }

    @Override
    public ChunkStatus getTargetStatus() {
        return this.toStatus;
    }

    private boolean isEmptyTask() {
        // must use fromStatus here to avoid any race condition with run() overwriting the status
        final boolean generation = !this.fromStatus.isOrAfter(this.toStatus);
        return (generation && this.toStatus.isEmptyGenStatus()) || (!generation && this.toStatus.isEmptyLoadStatus());
    }

    @Override
    public void run() {
        final ChunkAccess chunk = this.fromChunk;

        final ServerChunkCache serverChunkCache = this.world.chunkSource;
        final ChunkMap chunkMap = serverChunkCache.chunkMap;

        final CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completeFuture;

        final boolean generation;
        boolean completing = false;

        // note: should optimise the case where the chunk does not need to execute the status, because
        // schedule() calls this synchronously if it will run through that path

        try {
            generation = !chunk.getStatus().isOrAfter(this.toStatus);
            if (generation) {
                if (this.toStatus.isEmptyGenStatus()) {
                    if (chunk instanceof ProtoChunk) {
                        ((ProtoChunk)chunk).setStatus(this.toStatus);
                    }
                    completing = true;
                    this.complete(chunk, null);
                    return;
                }
                completeFuture = this.toStatus.generate(Runnable::run, this.world, chunkMap.generator, chunkMap.structureTemplateManager,
                    serverChunkCache.getLightEngine(), null, this.neighbours, false)
                    .whenComplete((final Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure> either, final Throwable throwable) -> {
                        final ChunkAccess newChunk = (either == null) ? null : either.left().orElse(null);
                        if (newChunk instanceof ProtoChunk) {
                            ((ProtoChunk)newChunk).setStatus(ChunkUpgradeGenericStatusTask.this.toStatus);
                        }
                    }
                );
            } else {
                if (this.toStatus.isEmptyLoadStatus()) {
                    completing = true;
                    this.complete(chunk, null);
                    return;
                }
                completeFuture = this.toStatus.load(this.world, chunkMap.structureTemplateManager, serverChunkCache.getLightEngine(), null, chunk);
            }
        } catch (final Throwable throwable) {
            if (!completing) {
                this.complete(null, throwable);

                if (throwable instanceof ThreadDeath) {
                    throw (ThreadDeath)throwable;
                }
                return;
            }

            this.scheduler.unrecoverableChunkSystemFailure(this.chunkX, this.chunkZ, Map.of(
                "Target status", ChunkTaskScheduler.stringIfNull(this.toStatus),
                "From status", ChunkTaskScheduler.stringIfNull(this.fromStatus),
                "Generation task", this
            ), throwable);

            if (!(throwable instanceof ThreadDeath)) {
                LOGGER.error("Failed to complete status for chunk: status:" + this.toStatus + ", chunk: (" + this.chunkX + "," + this.chunkZ + "), world: " + this.world.getWorld().getName(), throwable);
            } else {
                // ensure the chunk system can respond, then die
                throw (ThreadDeath)throwable;
            }
            return;
        }

        if (!completeFuture.isDone() && !this.toStatus.warnedAboutNoImmediateComplete.getAndSet(true)) {
            LOGGER.warn("Future status not complete after scheduling: " + this.toStatus.toString() + ", generate: " + generation);
        }

        final Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure> either;
        final ChunkAccess newChunk;

        try {
            either = completeFuture.join();
            newChunk = (either == null) ? null : either.left().orElse(null);
        } catch (final Throwable throwable) {
            this.complete(null, throwable);
            // ensure the chunk system can respond, then die
            if (throwable instanceof ThreadDeath) {
                throw (ThreadDeath)throwable;
            }
            return;
        }

        if (newChunk == null) {
            this.complete(null, new IllegalStateException("Chunk for status: " + ChunkUpgradeGenericStatusTask.this.toStatus.toString() + ", generation: " + generation + " should not be null! Either: " + either).fillInStackTrace());
            return;
        }

        this.complete(newChunk, null);
    }

    protected volatile boolean scheduled;
    protected static final VarHandle SCHEDULED_HANDLE = ConcurrentUtil.getVarHandle(ChunkUpgradeGenericStatusTask.class, "scheduled", boolean.class);

    @Override
    public boolean isScheduled() {
        return this.scheduled;
    }

    @Override
    public void schedule() {
        if ((boolean)SCHEDULED_HANDLE.getAndSet((ChunkUpgradeGenericStatusTask)this, true)) {
            throw new IllegalStateException("Cannot double call schedule()");
        }
        if (this.isEmptyTask()) {
            if (this.generateTask.cancel()) {
                this.run();
            }
        } else {
            this.generateTask.queue();
        }
    }

    @Override
    public void cancel() {
        if (this.generateTask.cancel()) {
            this.complete(null, null);
        }
    }

    @Override
    public PrioritisedExecutor.Priority getPriority() {
        return this.generateTask.getPriority();
    }

    @Override
    public void lowerPriority(final PrioritisedExecutor.Priority priority) {
        if (!PrioritisedExecutor.Priority.isValidPriority(priority)) {
            throw new IllegalArgumentException("Invalid priority " + priority);
        }
        this.generateTask.lowerPriority(priority);
    }

    @Override
    public void setPriority(final PrioritisedExecutor.Priority priority) {
        if (!PrioritisedExecutor.Priority.isValidPriority(priority)) {
            throw new IllegalArgumentException("Invalid priority " + priority);
        }
        this.generateTask.setPriority(priority);
    }

    @Override
    public void raisePriority(final PrioritisedExecutor.Priority priority) {
        if (!PrioritisedExecutor.Priority.isValidPriority(priority)) {
            throw new IllegalArgumentException("Invalid priority " + priority);
        }
        this.generateTask.raisePriority(priority);
    }
}
