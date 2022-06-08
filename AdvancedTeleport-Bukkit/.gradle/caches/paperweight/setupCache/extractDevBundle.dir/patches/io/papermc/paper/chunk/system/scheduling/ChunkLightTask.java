package io.papermc.paper.chunk.system.scheduling;

import ca.spottedleaf.concurrentutil.executor.standard.PrioritisedExecutor;
import ca.spottedleaf.starlight.common.light.StarLightEngine;
import ca.spottedleaf.starlight.common.light.StarLightInterface;
import io.papermc.paper.chunk.system.light.LightQueue;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.ProtoChunk;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.function.BooleanSupplier;

public final class ChunkLightTask extends ChunkProgressionTask {

    private static final Logger LOGGER = LogManager.getLogger();

    protected final ChunkAccess fromChunk;

    private final LightTaskPriorityHolder priorityHolder;

    public ChunkLightTask(final ChunkTaskScheduler scheduler, final ServerLevel world, final int chunkX, final int chunkZ,
                          final ChunkAccess chunk, final PrioritisedExecutor.Priority priority) {
        super(scheduler, world, chunkX, chunkZ);
        if (!PrioritisedExecutor.Priority.isValidPriority(priority)) {
            throw new IllegalArgumentException("Invalid priority " + priority);
        }
        this.priorityHolder = new LightTaskPriorityHolder(priority, this);
        this.fromChunk = chunk;
    }

    @Override
    public boolean isScheduled() {
        return this.priorityHolder.isScheduled();
    }

    @Override
    public ChunkStatus getTargetStatus() {
        return ChunkStatus.LIGHT;
    }

    @Override
    public void schedule() {
        this.priorityHolder.schedule();
    }

    @Override
    public void cancel() {
        this.priorityHolder.cancel();
    }

    @Override
    public PrioritisedExecutor.Priority getPriority() {
        return this.priorityHolder.getPriority();
    }

    @Override
    public void lowerPriority(final PrioritisedExecutor.Priority priority) {
        this.priorityHolder.raisePriority(priority);
    }

    @Override
    public void setPriority(final PrioritisedExecutor.Priority priority) {
        this.priorityHolder.setPriority(priority);
    }

    @Override
    public void raisePriority(final PrioritisedExecutor.Priority priority) {
        this.priorityHolder.raisePriority(priority);
    }

    private static final class LightTaskPriorityHolder extends PriorityHolder {

        protected final ChunkLightTask task;

        protected LightTaskPriorityHolder(final PrioritisedExecutor.Priority priority, final ChunkLightTask task) {
            super(priority);
            this.task = task;
        }

        @Override
        protected void cancelScheduled() {
            final ChunkLightTask task = this.task;
            task.complete(null, null);
        }

        @Override
        protected PrioritisedExecutor.Priority getScheduledPriority() {
            final ChunkLightTask task = this.task;
            return task.world.getChunkSource().getLightEngine().theLightEngine.lightQueue.getPriority(task.chunkX, task.chunkZ);
        }

        @Override
        protected void scheduleTask(final PrioritisedExecutor.Priority priority) {
            final ChunkLightTask task = this.task;
            final StarLightInterface starLightInterface = task.world.getChunkSource().getLightEngine().theLightEngine;
            final LightQueue lightQueue = starLightInterface.lightQueue;
            lightQueue.queueChunkLightTask(new ChunkPos(task.chunkX, task.chunkZ), new LightTask(starLightInterface, task), priority);
            lightQueue.setPriority(task.chunkX, task.chunkZ, priority);
        }

        @Override
        protected void lowerPriorityScheduled(final PrioritisedExecutor.Priority priority) {
            final ChunkLightTask task = this.task;
            final StarLightInterface starLightInterface = task.world.getChunkSource().getLightEngine().theLightEngine;
            final LightQueue lightQueue = starLightInterface.lightQueue;
            lightQueue.lowerPriority(task.chunkX, task.chunkZ, priority);
        }

        @Override
        protected void setPriorityScheduled(final PrioritisedExecutor.Priority priority) {
            final ChunkLightTask task = this.task;
            final StarLightInterface starLightInterface = task.world.getChunkSource().getLightEngine().theLightEngine;
            final LightQueue lightQueue = starLightInterface.lightQueue;
            lightQueue.setPriority(task.chunkX, task.chunkZ, priority);
        }

        @Override
        protected void raisePriorityScheduled(final PrioritisedExecutor.Priority priority) {
            final ChunkLightTask task = this.task;
            final StarLightInterface starLightInterface = task.world.getChunkSource().getLightEngine().theLightEngine;
            final LightQueue lightQueue = starLightInterface.lightQueue;
            lightQueue.raisePriority(task.chunkX, task.chunkZ, priority);
        }
    }

    private static final class LightTask implements BooleanSupplier {

        protected final StarLightInterface lightEngine;
        protected final ChunkLightTask task;

        public LightTask(final StarLightInterface lightEngine, final ChunkLightTask task) {
            this.lightEngine = lightEngine;
            this.task = task;
        }

        @Override
        public boolean getAsBoolean() {
            final ChunkLightTask task = this.task;
            // executed on light thread
            if (!task.priorityHolder.markExecuting()) {
                // cancelled
                return false;
            }

            try {
                final Boolean[] emptySections = StarLightEngine.getEmptySectionsForChunk(task.fromChunk);

                if (task.fromChunk.isLightCorrect() && task.fromChunk.getStatus().isOrAfter(ChunkStatus.LIGHT)) {
                    this.lightEngine.forceLoadInChunk(task.fromChunk, emptySections);
                    this.lightEngine.checkChunkEdges(task.chunkX, task.chunkZ);
                } else {
                    task.fromChunk.setLightCorrect(false);
                    this.lightEngine.lightChunk(task.fromChunk, emptySections);
                    task.fromChunk.setLightCorrect(true);
                }
                // we need to advance status
                if (task.fromChunk instanceof ProtoChunk chunk && chunk.getStatus() == ChunkStatus.LIGHT.getParent()) {
                    chunk.setStatus(ChunkStatus.LIGHT);
                }
            } catch (final Throwable thr) {
                if (!(thr instanceof ThreadDeath)) {
                    LOGGER.fatal("Failed to light chunk " + task.fromChunk.getPos().toString() + " in world '" + this.lightEngine.getWorld().getWorld().getName() + "'", thr);
                }

                task.complete(null, thr);

                if (thr instanceof ThreadDeath) {
                    throw (ThreadDeath)thr;
                }

                return true;
            }

            task.complete(task.fromChunk, null);
            return true;
        }
    }
}
