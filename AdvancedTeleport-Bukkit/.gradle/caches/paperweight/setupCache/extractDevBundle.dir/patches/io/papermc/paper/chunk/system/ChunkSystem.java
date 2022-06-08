package io.papermc.paper.chunk.system;

import ca.spottedleaf.concurrentutil.executor.standard.PrioritisedExecutor;
import com.destroystokyo.paper.util.SneakyThrow;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import io.papermc.paper.util.CoordinateUtils;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import org.bukkit.Bukkit;
import org.slf4j.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public final class ChunkSystem {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static void scheduleChunkTask(final ServerLevel level, final int chunkX, final int chunkZ, final Runnable run) {
        scheduleChunkTask(level, chunkX, chunkZ, run, PrioritisedExecutor.Priority.NORMAL);
    }

    public static void scheduleChunkTask(final ServerLevel level, final int chunkX, final int chunkZ, final Runnable run, final PrioritisedExecutor.Priority priority) {
        level.chunkTaskScheduler.scheduleChunkTask(chunkX, chunkZ, run, priority); // Paper - rewrite chunk system
    }

    public static void scheduleChunkLoad(final ServerLevel level, final int chunkX, final int chunkZ, final boolean gen,
                                         final ChunkStatus toStatus, final boolean addTicket, final PrioritisedExecutor.Priority priority,
                                         final Consumer<ChunkAccess> onComplete) {
        level.chunkTaskScheduler.scheduleChunkLoad(chunkX, chunkZ, gen, toStatus, addTicket, priority, onComplete); // Paper - rewrite chunk system
    }

    // Paper - rewrite chunk system
    public static void scheduleChunkLoad(final ServerLevel level, final int chunkX, final int chunkZ, final ChunkStatus toStatus,
                                         final boolean addTicket, final PrioritisedExecutor.Priority priority, final Consumer<ChunkAccess> onComplete) {
        level.chunkTaskScheduler.scheduleChunkLoad(chunkX, chunkZ, toStatus, addTicket, priority, onComplete); // Paper - rewrite chunk system
    }

    public static void scheduleTickingState(final ServerLevel level, final int chunkX, final int chunkZ,
                                            final ChunkHolder.FullChunkStatus toStatus, final boolean addTicket,
                                            final PrioritisedExecutor.Priority priority, final Consumer<LevelChunk> onComplete) {
        level.chunkTaskScheduler.scheduleTickingState(chunkX, chunkZ, toStatus, addTicket, priority, onComplete); // Paper - rewrite chunk system
    }

    public static List<ChunkHolder> getVisibleChunkHolders(final ServerLevel level) {
        return level.chunkTaskScheduler.chunkHolderManager.getOldChunkHolders(); // Paper - rewrite chunk system
    }

    public static List<ChunkHolder> getUpdatingChunkHolders(final ServerLevel level) {
        return level.chunkTaskScheduler.chunkHolderManager.getOldChunkHolders(); // Paper - rewrite chunk system
    }

    public static int getVisibleChunkHolderCount(final ServerLevel level) {
        return level.chunkTaskScheduler.chunkHolderManager.size(); // Paper - rewrite chunk system
    }

    public static int getUpdatingChunkHolderCount(final ServerLevel level) {
        return level.chunkTaskScheduler.chunkHolderManager.size(); // Paper - rewrite chunk system
    }

    public static boolean hasAnyChunkHolders(final ServerLevel level) {
        return getUpdatingChunkHolderCount(level) != 0;
    }

    public static void onEntityPreAdd(final ServerLevel level, final Entity entity) {
        // Paper start - duplicate uuid resolving
        if (net.minecraft.server.level.ChunkMap.checkDupeUUID(level, entity)) {
            return;
        }
        if (net.minecraft.world.level.Level.DEBUG_ENTITIES && ((Entity) entity).level.paperConfig().entities.spawning.duplicateUuid.mode != io.papermc.paper.configuration.WorldConfiguration.Entities.Spawning.DuplicateUUID.DuplicateUUIDMode.NOTHING) {
            if (((Entity) entity).addedToWorldStack != null) {
                ((Entity) entity).addedToWorldStack.printStackTrace();
            }
            net.minecraft.server.level.ServerLevel.getAddToWorldStackTrace((Entity) entity).printStackTrace();
        }
        // Paper end - duplicate uuid resolving
    }

    public static void onChunkHolderCreate(final ServerLevel level, final ChunkHolder holder) {
        final ChunkMap chunkMap = level.chunkSource.chunkMap;
        for (int index = 0, len = chunkMap.regionManagers.size(); index < len; ++index) {
            chunkMap.regionManagers.get(index).addChunk(holder.pos.x, holder.pos.z);
        }
    }

    public static void onChunkHolderDelete(final ServerLevel level, final ChunkHolder holder) {
        final ChunkMap chunkMap = level.chunkSource.chunkMap;
        for (int index = 0, len = chunkMap.regionManagers.size(); index < len; ++index) {
            chunkMap.regionManagers.get(index).removeChunk(holder.pos.x, holder.pos.z);
        }
    }

    public static void onChunkBorder(final LevelChunk chunk, final ChunkHolder holder) {
        chunk.playerChunk = holder;
    }

    public static void onChunkNotBorder(final LevelChunk chunk, final ChunkHolder holder) {

    }

    public static void onChunkTicking(final LevelChunk chunk, final ChunkHolder holder) {
        chunk.level.getChunkSource().tickingChunks.add(chunk);
    }

    public static void onChunkNotTicking(final LevelChunk chunk, final ChunkHolder holder) {
        chunk.level.getChunkSource().tickingChunks.remove(chunk);
    }

    public static void onChunkEntityTicking(final LevelChunk chunk, final ChunkHolder holder) {
        chunk.level.getChunkSource().entityTickingChunks.add(chunk);
    }

    public static void onChunkNotEntityTicking(final LevelChunk chunk, final ChunkHolder holder) {
        chunk.level.getChunkSource().entityTickingChunks.remove(chunk);
    }

    public static ChunkHolder getUnloadingChunkHolder(final ServerLevel level, final int chunkX, final int chunkZ) {
        return level.chunkSource.chunkMap.getUnloadingChunkHolder(chunkX, chunkZ);
    }

    public static int getSendViewDistance(final ServerPlayer player) {
        return io.papermc.paper.chunk.PlayerChunkLoader.getSendViewDistance(player);
    }

    public static int getLoadViewDistance(final ServerPlayer player) {
        return io.papermc.paper.chunk.PlayerChunkLoader.getLoadViewDistance(player);
    }

    public static int getTickViewDistance(final ServerPlayer player) {
        return io.papermc.paper.chunk.PlayerChunkLoader.getTickViewDistance(player);
    }

    private ChunkSystem() {
        throw new RuntimeException();
    }
}
