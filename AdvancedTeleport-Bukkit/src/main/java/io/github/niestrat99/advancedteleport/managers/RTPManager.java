package io.github.niestrat99.advancedteleport.managers;

import com.google.common.collect.Sets;
import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.utilities.RandomCoords;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class RTPManager {

    private static HashMap<UUID, Queue<Location>> locQueue;
    private static final HashSet<String> airs = Sets.newHashSet("AIR", "CAVE_AIR", "VOID_AIR");

    public static void init() {
        locQueue = new HashMap<>();
        if (!PaperLib.isPaper()) return;
        try {
            getPreviousLocations();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (World loadedWorld : Bukkit.getWorlds()) {
            loadWorldData(loadedWorld);
        }
    }

    public static CompletableFuture<Location> getNextAvailableLocation(World world) {
        final Queue<Location> queue = locQueue.get(world.getUID());
        addLocation(world, false, 0).thenAccept(location -> {
            if (location == null) return;
            queue.add(location);
            locQueue.put(world.getUID(), queue);
        });
        if (queue == null || queue.isEmpty()) {
            return addLocation(world, true, 0);
        } else {
            return CompletableFuture.completedFuture(queue.poll());
        }
    }

    public static Location getLocationUrgently(World world) {
        Queue<Location> queue = locQueue.get(world.getUID());
        addLocation(world, false, 0).thenAccept(location -> {
            if (location == null) return;
            queue.add(location);
            locQueue.put(world.getUID(), queue);
        });
        if (queue == null || queue.isEmpty()) {
            return null;
        } else {
            return queue.remove();
        }
    }

    public static CompletableFuture<Location> addLocation(World world, boolean urgent, int tries) {
        if (!PaperLib.isPaper()) return CompletableFuture.completedFuture(null);
        tries++;
        if (locQueue.get(world.getUID()) != null && locQueue.get(world.getUID()).size() > NewConfig.get().PREPARED_LOCATIONS_LIMIT.get()) {
            return CompletableFuture.completedFuture(locQueue.get(world.getUID()).poll());
        }
        Location location = RandomCoords.generateCoords(world);
        int[] coords = new int[]{location.getBlockX(), location.getBlockZ()};
        int finalTries = tries;
        return PaperLib.getChunkAtAsync(world, coords[0] >> 4, coords[1] >> 4, true, urgent).thenApplyAsync(chunk -> {
            Block block = world.getHighestBlockAt(coords[0], coords[1]);
            if (isValidLocation(block)) {
                return block.getLocation().add(0.5, 1, 0.5);
            } else if (finalTries < 5 || urgent) {
                return addLocation(world, urgent, finalTries).join();
            } else {
                return null;
            }
        }, CoreClass.async).thenApplyAsync(loc -> loc, CoreClass.sync);
    }

    private static boolean isValidLocation(Block block) {
        if (airs.contains(block.getType().name())) return false;
        if (NewConfig.get().AVOID_BIOMES.get().contains(block.getBiome().name())) return false;
        return !NewConfig.get().AVOID_BLOCKS.get().contains(block.getType().name());
    }

    public static void loadWorldData(World world) {
        if (locQueue == null) return;
        if (NewConfig.get().WHITELIST_WORLD.get() && !NewConfig.get().ALLOWED_WORLDS.get().contains(world.getName())) return;
        if (world.getGenerator() != null && NewConfig.get().IGNORE_WORLD_GENS.get().contains(world.getGenerator().getClass().getName())) return;
        int size = locQueue.getOrDefault(world.getUID(), new ArrayDeque<>()).size();

        for (int i = size; i < NewConfig.get().PREPARED_LOCATIONS_LIMIT.get(); i++) {
            addLocation(world, false, 0).thenAccept(location -> {
                Queue<Location> queue = locQueue.getOrDefault(world.getUID(), new ArrayDeque<>());
                queue.add(location);
                locQueue.put(world.getUID(), queue);
            });
        }
    }

    public static void unloadWorldData(World world) {
        locQueue.remove(world.getUID());
    }

    public static void getPreviousLocations() throws IOException {
        File rtpLocsFile = new File(CoreClass.getInstance().getDataFolder(), "rtp-locations.csv");
        if (!rtpLocsFile.exists()) return;
        BufferedReader reader = new BufferedReader(new FileReader(rtpLocsFile));
        String currentLine;
        while ((currentLine = reader.readLine()) != null) {
            try {
                String[] data = currentLine.split(",");
                UUID worldUUID = UUID.fromString(data[0]);
                World world = Bukkit.getWorld(worldUUID);
                double[] loc = new double[]{Double.parseDouble(data[1]), Double.parseDouble(data[2]), Double.parseDouble(data[3])};
                Queue<Location> queue = locQueue.getOrDefault(worldUUID, new ArrayDeque<>());
                queue.add(new Location(world, loc[0], loc[1], loc[2]));
                locQueue.put(worldUUID, queue);
            } catch (Exception ignored) {
            }
        }
        reader.close();
        rtpLocsFile.delete();
    }

    public static void saveLocations() throws IOException {
        File rtpLocsFile = new File(CoreClass.getInstance().getDataFolder(), "rtp-locations.csv");
        if (!rtpLocsFile.exists()) rtpLocsFile.createNewFile();
        BufferedWriter writer = new BufferedWriter(new FileWriter(rtpLocsFile));
        for (UUID worldUUID : locQueue.keySet()) {
            Queue<Location> locations = locQueue.get(worldUUID);
            while (locations.peek() != null) {
                Location loc = locations.poll();
                String locLine = loc.getWorld().getUID() +
                        "," + loc.getX() + "," + loc.getY() + "," + loc.getZ();
                writer.write(locLine);
                writer.write("\n");
            }
        }
        writer.close();
    }
}
