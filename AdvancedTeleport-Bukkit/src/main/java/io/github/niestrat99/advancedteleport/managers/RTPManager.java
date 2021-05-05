package io.github.niestrat99.advancedteleport.managers;

import com.wimbli.WorldBorder.BorderData;
import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static io.github.niestrat99.advancedteleport.CoreClass.worldBorder;

public class RTPManager {

    private static HashMap<UUID, Queue<Location>> locQueue;
    private static HashMap<UUID, Double[]> borderData;

    public static void init() {
        locQueue = new HashMap<>();
        borderData = new HashMap<>();
        for (World loadedWorld : Bukkit.getWorlds()) {
            for (int i = 0; i < 3; i++) {
                addLocation(loadedWorld, false).thenAccept(location -> {
                    Queue<Location> queue = locQueue.get(loadedWorld.getUID());
                    if (queue == null) queue = new ArrayDeque<>();
                    queue.add(location);
                    locQueue.put(loadedWorld.getUID(), queue);
                });
            }
            if (NewConfig.get().USE_WORLD_BORDER.get() && worldBorder != null) {
                BorderData border = com.wimbli.WorldBorder.Config.Border(loadedWorld.getName());
                if (border != null) {
                    borderData.put(loadedWorld.getUID(), new Double[]{
                            border.getX() - border.getRadiusX(),
                            border.getZ() - border.getRadiusZ(),
                            border.getX() + border.getRadiusX(),
                            border.getZ() + border.getRadiusZ()});
                }
            }
        }
    }

    public static CompletableFuture<Location> getNextAvailableLocation(World world) {
        final Queue<Location> queue = locQueue.get(world.getUID());
        addLocation(world, false).thenAccept(location -> {
            queue.add(location);
            locQueue.put(world.getUID(), queue);
        });
        if (queue == null || queue.isEmpty()) {
            return addLocation(world, true);
        } else {
            return CompletableFuture.completedFuture(queue.poll());
        }
    }

    public static Location getLocationUrgently(World world) {
        Queue<Location> queue = locQueue.get(world.getUID());
        addLocation(world, false).thenAccept(location -> {
            queue.add(location);
            locQueue.put(world.getUID(), queue);
        });
        if (queue == null || queue.isEmpty()) {
            return null;
        } else {
            return queue.remove();
        }
    }

    public static CompletableFuture<Location> addLocation(World world, boolean urgent) {
        if (locQueue.get(world.getUID()) != null && locQueue.get(world.getUID()).size() > 3) {
            return CompletableFuture.completedFuture(locQueue.get(world.getUID()).poll());
        }
        int[] coords = getRandomCoords(world);
        return PaperLib.getChunkAtAsync(world, coords[0] >> 4, coords[1] >> 4, true, urgent).thenApplyAsync(chunk -> {
            Block block = world.getHighestBlockAt(coords[0], coords[1], HeightMap.WORLD_SURFACE);
            if (isValidLocation(block)) {
                return block.getLocation().add(0.5, 1, 0.5);
            } else {
                return addLocation(world, urgent).join();
            }
        }, CoreClass.async).thenApplyAsync(loc -> loc, CoreClass.sync);
    }

    private static boolean isValidLocation(Block block) {
        if (block.getType().name().equals("AIR") || block.getType().name().equals("VOID_AIR")) return false;
        if (NewConfig.get().AVOID_BIOMES.get().contains(block.getBiome().name())) return false;
        if (NewConfig.get().AVOID_BLOCKS.get().contains(block.getType().name())) return false;
        return true;
    }

    private static int[] getRandomCoords(World world) {
        Double[] bounds = borderData.getOrDefault(world.getUID(), new Double[]{
                Double.valueOf(NewConfig.get().MINIMUM_X.get()),
                Double.valueOf(NewConfig.get().MINIMUM_Z.get()),
                Double.valueOf(NewConfig.get().MAXIMUM_X.get()),
                Double.valueOf(NewConfig.get().MAXIMUM_Z.get())});
        return new int[]{
                (int) (new Random().nextInt((int)Math.round(bounds[2] - bounds[0]) + 1) + bounds[0]),
                (int) (new Random().nextInt((int)Math.round(bounds[3] - bounds[1]) + 1) + bounds[1])
        };
    }

}
