package io.papermc.paper.util;

import com.destroystokyo.paper.profile.CraftPlayerProfile;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet;
import java.lang.ref.Cleaner;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.Ticket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import org.apache.commons.lang.exception.ExceptionUtils;
import com.mojang.authlib.GameProfile;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.util.Waitable;
import org.spigotmc.AsyncCatcher;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class MCUtil {
    public static final ThreadPoolExecutor asyncExecutor = new ThreadPoolExecutor(
        0, 2, 60L, TimeUnit.SECONDS,
        new LinkedBlockingQueue<>(),
        new ThreadFactoryBuilder()
            .setNameFormat("Paper Async Task Handler Thread - %1$d")
            .setUncaughtExceptionHandler(new net.minecraft.DefaultUncaughtExceptionHandlerWithName(MinecraftServer.LOGGER))
            .build()
    );
    public static final ThreadPoolExecutor cleanerExecutor = new ThreadPoolExecutor(
        1, 1, 0L, TimeUnit.SECONDS,
        new LinkedBlockingQueue<>(),
        new ThreadFactoryBuilder()
            .setNameFormat("Paper Object Cleaner")
            .setUncaughtExceptionHandler(new net.minecraft.DefaultUncaughtExceptionHandlerWithName(MinecraftServer.LOGGER))
            .build()
    );

    public static final long INVALID_CHUNK_KEY = getCoordinateKey(Integer.MAX_VALUE, Integer.MAX_VALUE);


    public static Runnable once(Runnable run) {
        AtomicBoolean ran = new AtomicBoolean(false);
        return () -> {
            if (ran.compareAndSet(false, true)) {
                run.run();
            }
        };
    }

    public static <T> Runnable once(List<T> list, Consumer<T> cb) {
        return once(() -> {
            list.forEach(cb);
        });
    }

    private static Runnable makeCleanerCallback(Runnable run) {
        return once(() -> cleanerExecutor.execute(run));
    }

    /**
     * DANGER WILL ROBINSON: Be sure you do not use a lambda that lives in the object being monitored, or leaky leaky!
     * @param obj
     * @param run
     * @return
     */
    public static Runnable registerCleaner(Object obj, Runnable run) {
        // Wrap callback in its own method above or the lambda will leak object
        Runnable cleaner = makeCleanerCallback(run);
        CleanerHolder.CLEANER.register(obj, cleaner);
        return cleaner;
    }

    private static final class CleanerHolder {
        private static final Cleaner CLEANER = Cleaner.create();
    }

    /**
     * DANGER WILL ROBINSON: Be sure you do not use a lambda that lives in the object being monitored, or leaky leaky!
     * @param obj
     * @param list
     * @param cleaner
     * @param <T>
     * @return
     */
    public static <T> Runnable registerListCleaner(Object obj, List<T> list, Consumer<T> cleaner) {
        return registerCleaner(obj, () -> {
            list.forEach(cleaner);
            list.clear();
        });
    }

    /**
     * DANGER WILL ROBINSON: Be sure you do not use a lambda that lives in the object being monitored, or leaky leaky!
     * @param obj
     * @param resource
     * @param cleaner
     * @param <T>
     * @return
     */
    public static <T> Runnable registerCleaner(Object obj, T resource, java.util.function.Consumer<T> cleaner) {
        return registerCleaner(obj, () -> cleaner.accept(resource));
    }

    public static List<ChunkPos> getSpiralOutChunks(BlockPos blockposition, int radius) {
        List<ChunkPos> list = com.google.common.collect.Lists.newArrayList();

        list.add(new ChunkPos(blockposition.getX() >> 4, blockposition.getZ() >> 4));
        for (int r = 1; r <= radius; r++) {
            int x = -r;
            int z = r;

            // Iterates the edge of half of the box; then negates for other half.
            while (x <= r && z > -r) {
                list.add(new ChunkPos((blockposition.getX() + (x << 4)) >> 4, (blockposition.getZ() + (z << 4)) >> 4));
                list.add(new ChunkPos((blockposition.getX() - (x << 4)) >> 4, (blockposition.getZ() - (z << 4)) >> 4));

                if (x < r) {
                    x++;
                } else {
                    z--;
                }
            }
        }
        return list;
    }

    public static int fastFloor(double x) {
        int truncated = (int)x;
        return x < (double)truncated ? truncated - 1 : truncated;
    }

    public static int fastFloor(float x) {
        int truncated = (int)x;
        return x < (double)truncated ? truncated - 1 : truncated;
    }

    public static float normalizeYaw(float f) {
        float f1 = f % 360.0F;

        if (f1 >= 180.0F) {
            f1 -= 360.0F;
        }

        if (f1 < -180.0F) {
            f1 += 360.0F;
        }

        return f1;
    }

    /**
     * Quickly generate a stack trace for current location
     *
     * @return Stacktrace
     */
    public static String stack() {
        return ExceptionUtils.getFullStackTrace(new Throwable());
    }

    /**
     * Quickly generate a stack trace for current location with message
     *
     * @param str
     * @return Stacktrace
     */
    public static String stack(String str) {
        return ExceptionUtils.getFullStackTrace(new Throwable(str));
    }

    public static long getCoordinateKey(final BlockPos blockPos) {
        return ((long)(blockPos.getZ() >> 4) << 32) | ((blockPos.getX() >> 4) & 0xFFFFFFFFL);
    }

    public static long getCoordinateKey(final Entity entity) {
        return ((long)(MCUtil.fastFloor(entity.getZ()) >> 4) << 32) | ((MCUtil.fastFloor(entity.getX()) >> 4) & 0xFFFFFFFFL);
    }

    public static long getCoordinateKey(final ChunkPos pair) {
        return ((long)pair.z << 32) | (pair.x & 0xFFFFFFFFL);
    }

    public static long getCoordinateKey(final int x, final int z) {
        return ((long)z << 32) | (x & 0xFFFFFFFFL);
    }

    public static int getCoordinateX(final long key) {
        return (int)key;
    }

    public static int getCoordinateZ(final long key) {
        return (int)(key >>> 32);
    }

    public static int getChunkCoordinate(final double coordinate) {
        return MCUtil.fastFloor(coordinate) >> 4;
    }

    public static int getBlockCoordinate(final double coordinate) {
        return MCUtil.fastFloor(coordinate);
    }

    public static long getBlockKey(final int x, final int y, final int z) {
        return ((long)x & 0x7FFFFFF) | (((long)z & 0x7FFFFFF) << 27) | ((long)y << 54);
    }

    public static long getBlockKey(final BlockPos pos) {
        return ((long)pos.getX() & 0x7FFFFFF) | (((long)pos.getZ() & 0x7FFFFFF) << 27) | ((long)pos.getY() << 54);
    }

    public static long getBlockKey(final Entity entity) {
        return getBlockKey(getBlockCoordinate(entity.getX()), getBlockCoordinate(entity.getY()), getBlockCoordinate(entity.getZ()));
    }

    // assumes the sets have the same comparator, and if this comparator is null then assume T is Comparable
    public static <T> void mergeSortedSets(final java.util.function.Consumer<T> consumer, final java.util.Comparator<? super T> comparator, final java.util.SortedSet<T>...sets) {
        final ObjectRBTreeSet<T> all = new ObjectRBTreeSet<>(comparator);
        // note: this is done in log(n!) ~ nlogn time. It could be improved if it were to mimic what mergesort does.
        for (java.util.SortedSet<T> set : sets) {
            if (set != null) {
                all.addAll(set);
            }
        }
        all.forEach(consumer);
    }

    private MCUtil() {}

    public static final java.util.concurrent.Executor MAIN_EXECUTOR = (run) -> {
        if (!isMainThread()) {
            MinecraftServer.getServer().execute(run);
        } else {
            run.run();
        }
    };

    public static <T> CompletableFuture<T> ensureMain(CompletableFuture<T> future) {
        return future.thenApplyAsync(r -> r, MAIN_EXECUTOR);
    }

    public static <T> void thenOnMain(CompletableFuture<T> future, Consumer<T> consumer) {
        future.thenAcceptAsync(consumer, MAIN_EXECUTOR);
    }
    public static <T> void thenOnMain(CompletableFuture<T> future, BiConsumer<T, Throwable> consumer) {
        future.whenCompleteAsync(consumer, MAIN_EXECUTOR);
    }

    public static boolean isMainThread() {
        return MinecraftServer.getServer().isSameThread();
    }

    public static org.bukkit.scheduler.BukkitTask scheduleTask(int ticks, Runnable runnable) {
        return scheduleTask(ticks, runnable, null);
    }

    public static org.bukkit.scheduler.BukkitTask scheduleTask(int ticks, Runnable runnable, String taskName) {
        return MinecraftServer.getServer().server.getScheduler().scheduleInternalTask(runnable, ticks, taskName);
    }

    public static void processQueue() {
        Runnable runnable;
        Queue<Runnable> processQueue = getProcessQueue();
        while ((runnable = processQueue.poll()) != null) {
            try {
                runnable.run();
            } catch (Exception e) {
                MinecraftServer.LOGGER.error("Error executing task", e);
            }
        }
    }
    public static <T> T processQueueWhileWaiting(CompletableFuture <T> future) {
        try {
            if (isMainThread()) {
                while (!future.isDone()) {
                    try {
                        return future.get(1, TimeUnit.MILLISECONDS);
                    } catch (TimeoutException ignored) {
                        processQueue();
                    }
                }
            }
            return future.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void ensureMain(Runnable run) {
        ensureMain(null, run);
    }
    /**
     * Ensures the target code is running on the main thread
     * @param reason
     * @param run
     * @return
     */
    public static void ensureMain(String reason, Runnable run) {
        if (!isMainThread()) {
            if (reason != null) {
                MinecraftServer.LOGGER.warn("Asynchronous " + reason + "!", new IllegalStateException());
            }
            getProcessQueue().add(run);
            return;
        }
        run.run();
    }

    private static Queue<Runnable> getProcessQueue() {
        return MinecraftServer.getServer().processQueue;
    }

    public static <T> T ensureMain(Supplier<T> run) {
        return ensureMain(null, run);
    }
    /**
     * Ensures the target code is running on the main thread
     * @param reason
     * @param run
     * @param <T>
     * @return
     */
    public static <T> T ensureMain(String reason, Supplier<T> run) {
        if (!isMainThread()) {
            if (reason != null) {
                MinecraftServer.LOGGER.warn("Asynchronous " + reason + "! Blocking thread until it returns ", new IllegalStateException());
            }
            Waitable<T> wait = new Waitable<T>() {
                @Override
                protected T evaluate() {
                    return run.get();
                }
            };
            getProcessQueue().add(wait);
            try {
                return wait.get();
            } catch (InterruptedException | ExecutionException e) {
                MinecraftServer.LOGGER.warn("Encountered exception", e);
            }
            return null;
        }
        return run.get();
    }

    public static PlayerProfile toBukkit(GameProfile profile) {
        return CraftPlayerProfile.asBukkitMirror(profile);
    }

    /**
     * Calculates distance between 2 entities
     * @param e1
     * @param e2
     * @return
     */
    public static double distance(Entity e1, Entity e2) {
        return Math.sqrt(distanceSq(e1, e2));
    }


    /**
     * Calculates distance between 2 block positions
     * @param e1
     * @param e2
     * @return
     */
    public static double distance(BlockPos e1, BlockPos e2) {
        return Math.sqrt(distanceSq(e1, e2));
    }

    /**
     * Gets the distance between 2 positions
     * @param x1
     * @param y1
     * @param z1
     * @param x2
     * @param y2
     * @param z2
     * @return
     */
    public static double distance(double x1, double y1, double z1, double x2, double y2, double z2) {
        return Math.sqrt(distanceSq(x1, y1, z1, x2, y2, z2));
    }

    /**
     * Get's the distance squared between 2 entities
     * @param e1
     * @param e2
     * @return
     */
    public static double distanceSq(Entity e1, Entity e2) {
        return distanceSq(e1.getX(),e1.getY(),e1.getZ(), e2.getX(),e2.getY(),e2.getZ());
    }

    /**
     * Gets the distance sqaured between 2 block positions
     * @param pos1
     * @param pos2
     * @return
     */
    public static double distanceSq(BlockPos pos1, BlockPos pos2) {
        return distanceSq(pos1.getX(), pos1.getY(), pos1.getZ(), pos2.getX(), pos2.getY(), pos2.getZ());
    }

    /**
     * Gets the distance squared between 2 positions
     * @param x1
     * @param y1
     * @param z1
     * @param x2
     * @param y2
     * @param z2
     * @return
     */
    public static double distanceSq(double x1, double y1, double z1, double x2, double y2, double z2) {
        return (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2) + (z1 - z2) * (z1 - z2);
    }

    /**
     * Converts a NMS World/BlockPosition to Bukkit Location
     * @param world
     * @param x
     * @param y
     * @param z
     * @return
     */
    public static Location toLocation(Level world, double x, double y, double z) {
        return new Location(world.getWorld(), x, y, z);
    }

    /**
     * Converts a NMS World/BlockPosition to Bukkit Location
     * @param world
     * @param pos
     * @return
     */
    public static Location toLocation(Level world, BlockPos pos) {
        return new Location(world.getWorld(), pos.getX(), pos.getY(), pos.getZ());
    }

    /**
     * Converts an NMS entity's current location to a Bukkit Location
     * @param entity
     * @return
     */
    public static Location toLocation(Entity entity) {
        return new Location(entity.getCommandSenderWorld().getWorld(), entity.getX(), entity.getY(), entity.getZ());
    }

    public static org.bukkit.block.Block toBukkitBlock(Level world, BlockPos pos) {
        return world.getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ());
    }

    public static BlockPos toBlockPosition(Location loc) {
        return new BlockPos(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    public static boolean isEdgeOfChunk(BlockPos pos) {
        final int modX = pos.getX() & 15;
        final int modZ = pos.getZ() & 15;
        return (modX == 0 || modX == 15 || modZ == 0 || modZ == 15);
    }

    /**
     * Posts a task to be executed asynchronously
     * @param run
     */
    public static void scheduleAsyncTask(Runnable run) {
        asyncExecutor.execute(run);
    }

    @Nonnull
    public static ServerLevel getNMSWorld(@Nonnull org.bukkit.World world) {
        return ((CraftWorld) world).getHandle();
    }

    public static ServerLevel getNMSWorld(@Nonnull org.bukkit.entity.Entity entity) {
        return getNMSWorld(entity.getWorld());
    }

    public static ClipContext.Fluid getNMSFluidCollisionOption(com.destroystokyo.paper.block.TargetBlockInfo.FluidMode fluidMode) {
        switch (fluidMode) {
            case NEVER:
                return ClipContext.Fluid.NONE;
            case SOURCE_ONLY:
                return ClipContext.Fluid.SOURCE_ONLY;
            case ALWAYS:
                return ClipContext.Fluid.ANY;
        }
        return null;
    }

    public static BlockFace toBukkitBlockFace(Direction enumDirection) {
        switch (enumDirection) {
            case DOWN:
                return BlockFace.DOWN;
            case UP:
                return BlockFace.UP;
            case NORTH:
                return BlockFace.NORTH;
            case SOUTH:
                return BlockFace.SOUTH;
            case WEST:
                return BlockFace.WEST;
            case EAST:
                return BlockFace.EAST;
            default:
                return null;
        }
    }

    @Nullable
    public static Component getBaseComponentFromNbt(String key, CompoundTag compound) {
        if (!compound.contains(key)) {
            return null;
        }
        String string = compound.getString(key);
        try {
            return Component.Serializer.fromJson(string);
        } catch (com.google.gson.JsonParseException e) {
            org.bukkit.Bukkit.getLogger().warning("Unable to parse " + key + " from " + compound +": " + e.getMessage());
        }

        return null;
    }

    public static ChunkStatus getChunkStatus(ChunkHolder chunk) {
        return chunk.getChunkHolderStatus();
    }

    public static void dumpChunks(File file, boolean watchdog) throws IOException {
        file.getParentFile().mkdirs();
        file.createNewFile();
        ReferenceArrayList<org.bukkit.World> worlds = new ReferenceArrayList<>(org.bukkit.Bukkit.getWorlds());
        ReferenceArrayList<org.bukkit.World> loadedWorlds = new ReferenceArrayList<>(worlds);
        JsonObject data = new JsonObject();

        data.addProperty("server-version", org.bukkit.Bukkit.getVersion());
        data.addProperty("data-version", 1);

        {
            JsonArray players = new JsonArray();
            data.add("all-players", players);
            List<ServerPlayer> playerList = MinecraftServer.getServer().getPlayerList().players;
            for (ServerPlayer player : playerList) {
                JsonObject playerData = new JsonObject();
                players.add(playerData);

                Level playerWorld = player.getLevel();
                org.bukkit.World craftWorld = playerWorld.getWorld();
                Entity.RemovalReason removalReason = player.getRemovalReason();

                playerData.addProperty("name", player.getScoreboardName());
                playerData.addProperty("x", player.getX());
                playerData.addProperty("y", player.getY());
                playerData.addProperty("z", player.getZ());
                playerData.addProperty("world", playerWorld == null ? "null world" : craftWorld.getName());
                playerData.addProperty("removalReason", removalReason == null ? "null" : removalReason.name());

                if (!worlds.contains(craftWorld)) {
                    worlds.add(craftWorld);
                }
            }
        }

        JsonArray chunkWaitInformation = new JsonArray();
        data.add("chunk-wait-infos", chunkWaitInformation);

        for (io.papermc.paper.chunk.system.scheduling.ChunkTaskScheduler.ChunkInfo chunkInfo : io.papermc.paper.chunk.system.scheduling.ChunkTaskScheduler.getChunkInfos()) {
            chunkWaitInformation.add(chunkInfo.toString());
        }

        JsonArray worldsData = new JsonArray();

        for (org.bukkit.World bukkitWorld : worlds) {
            JsonObject worldData = new JsonObject();

            ServerLevel world = ((org.bukkit.craftbukkit.v1_19_R1.CraftWorld)bukkitWorld).getHandle();
            List<ServerPlayer> players = world.players;

            worldData.addProperty("is-loaded", loadedWorlds.contains(bukkitWorld));
            worldData.addProperty("name", world.getWorld().getName());
            worldData.addProperty("view-distance", world.getChunkSource().chunkMap.playerChunkManager.getTargetNoTickViewDistance()); // Paper - replace chunk loader system
            worldData.addProperty("tick-view-distance", world.getChunkSource().chunkMap.playerChunkManager.getTargetTickViewDistance()); // Paper - replace chunk loader system
            worldData.addProperty("keep-spawn-loaded", world.keepSpawnInMemory);
            worldData.addProperty("keep-spawn-loaded-range", world.paperConfig().spawn.keepSpawnLoadedRange * 16);

            JsonArray playersData = new JsonArray();

            for (ServerPlayer player : players) {
                JsonObject playerData = new JsonObject();

                playerData.addProperty("name", player.getScoreboardName());
                playerData.addProperty("x", player.getX());
                playerData.addProperty("y", player.getY());
                playerData.addProperty("z", player.getZ());

                playersData.add(playerData);
            }

            worldData.add("players", playersData);
            worldData.add("chunk-data", watchdog ? world.chunkTaskScheduler.chunkHolderManager.getDebugJsonForWatchdog() : world.chunkTaskScheduler.chunkHolderManager.getDebugJson());
            worldsData.add(worldData);
        }

        data.add("worlds", worldsData);

        StringWriter stringWriter = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(stringWriter);
        jsonWriter.setIndent(" ");
        jsonWriter.setLenient(false);
        Streams.write(data, jsonWriter);

        String fileData = stringWriter.toString();

        try (PrintStream out = new PrintStream(new FileOutputStream(file), false, StandardCharsets.UTF_8)) {
            out.print(fileData);
        }
    }

    public static int getTicketLevelFor(net.minecraft.world.level.chunk.ChunkStatus status) {
        return net.minecraft.server.level.ChunkMap.MAX_VIEW_DISTANCE + net.minecraft.world.level.chunk.ChunkStatus.getDistance(status);
    }
}
