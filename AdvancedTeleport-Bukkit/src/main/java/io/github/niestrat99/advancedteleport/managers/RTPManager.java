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
import java.util.concurrent.ExecutionException;

import static io.github.niestrat99.advancedteleport.CoreClass.worldBorder;

public class RTPManager {

    private static HashMap<UUID, Queue<Location>> locQueue;
    private static HashMap<UUID, Double[]> borderData;

    public static void init() {
        locQueue = new HashMap<>();
        borderData = new HashMap<>();
        for (World loadedWorld : Bukkit.getWorlds()) {
            addLocation(loadedWorld, false);
            addLocation(loadedWorld, false);
            addLocation(loadedWorld, false);
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
        Queue<Location> queue = locQueue.get(world.getUID());
        if (queue == null || queue.isEmpty()) {
            return addLocation(world, true).thenApply(loc -> loc);
        } else {
            Location loc = queue.poll();
            addLocation(world, false);
            return CompletableFuture.completedFuture(loc);
        }
    }

    public static Location getLocationUrgently(World world) {
        Queue<Location> queue = locQueue.get(world.getUID());
        if (queue.isEmpty()) {
            return null;
        } else {
            return queue.remove();
        }
    }

    public static CompletableFuture<Location> addLocation(World world, boolean urgent) {
        int[] coords = getRandomCoords(world);
        return PaperLib.getChunkAtAsync(world, coords[0] >> 4, coords[1] >> 4, true, urgent).thenApplyAsync(chunk -> {
            Block block = world.getHighestBlockAt(coords[0], coords[1], HeightMap.WORLD_SURFACE);
            if (isValidLocation(block)) {
                Queue<Location> queue = locQueue.get(world.getUID());
                if (queue == null) queue = new ArrayDeque<>();
                queue.add(block.getLocation().add(0.5, 1, 0.5));
                System.out.println("Added " + block.getLocation() + " to " + world.getName() + ", new size: " + queue.size());
                locQueue.put(world.getUID(), queue);
                return block.getLocation();
            } else {
                return addLocation(world, urgent).join();
            }
        }, CoreClass.async);
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
