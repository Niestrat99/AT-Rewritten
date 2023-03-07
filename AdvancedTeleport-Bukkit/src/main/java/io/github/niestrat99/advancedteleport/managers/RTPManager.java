package io.github.niestrat99.advancedteleport.managers;

import com.google.common.collect.Sets;
import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.config.MainConfig;
import io.github.niestrat99.advancedteleport.utilities.RandomCoords;
import io.papermc.lib.PaperLib;
import org.bukkit.*;
import org.bukkit.block.Block;

import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.Nullable;

public class RTPManager {

    private static HashMap<UUID, Queue<Location>> locQueue;
    private static final HashSet<String> airs = Sets.newHashSet("AIR", "CAVE_AIR", "VOID_AIR");

    public static void init() {
        locQueue = new HashMap<>();
        if (!PaperLib.isPaper()) return;
        if (!NewConfig.get().RAPID_RESPONSE.get()) return;

        try {
            getPreviousLocations();
        } catch (IOException e) {
            CoreClass.getInstance().getLogger().severe("Failed to load previous RTP locations, generating new ones: " + e.getMessage());
        }
        for (World loadedWorld : Bukkit.getWorlds()) {
            loadWorldData(loadedWorld);
        }
    }

    public static boolean isInitialised() {
        return locQueue != null;
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

    public static CompletableFuture<@Nullable Location> addLocation(final World world, final boolean urgent, int tries) {

        // If it's not a Paper server, stop there.
        if (!PaperLib.isPaper()) return CompletableFuture.completedFuture(null);

        // Increment the number of attempts so we don't exhaust the server.
        tries++;

        // If there are too many locations for a world, just return the first one and remove it from the queue.
        if (locQueue.get(world.getUID()) != null && locQueue.get(world.getUID()).size() > MainConfig.get().PREPARED_LOCATIONS_LIMIT.get()) {
            Location loc = locQueue.get(world.getUID()).poll();
            if (!PluginHookManager.get().isClaimed(loc)) {
                return CompletableFuture.completedFuture(loc);
            }
        }


        // Generate the coordinates.
        Location location = RandomCoords.generateCoords(world);

        if (location == null) {
            return CompletableFuture.completedFuture(null);
        }

        int[] coords = new int[]{location.getBlockX(), location.getBlockZ()};
        int finalTries = tries;

        // Attempt to fetch the chunk to be loaded.
        return PaperLib.getChunkAtAsync(world, coords[0] >> 4, coords[1] >> 4, true, urgent).thenApplyAsync(chunk -> {

            // If we're in the Nether, do a binary jump, otherwise get the highest block.
            Block block = world.getEnvironment().equals(World.Environment.NETHER) ? doBinaryJump(world, coords) : world.getHighestBlockAt(coords[0], coords[1]);

            // If it's a valid location, return it. If not, try again unless the plugin has exhausted its attempts.
            if (isValidLocation(block)) {
                return block.getLocation().add(0.5, 1, 0.5);
            } else if (finalTries < 5 || urgent) {
                return addLocation(world, urgent, finalTries).join();
            } else {
                return null;
            }
        }, CoreClass.async).thenApplyAsync(loc -> loc, CoreClass.sync);
    }

    private static Block doBinaryJump(World world, int[] coords) {
        Location location = new Location(world, coords[0], 128, coords[1]);

        // This is how much we'll jump by at first
        int jumpAmount = 128;

        // However, if we're in the Nether...
        if (world.getEnvironment() == World.Environment.NETHER) {

            // We'll start at level 64 instead and start at a jump of 64.
            location.setY(64);
            jumpAmount = 64;
        }

        // Whether to go up or down.
        boolean up = false;

        // Temporary location.
        Location tempLoc = location.clone();

        // Whilst there's no valid location...
        while (true) {

            // Divide the amount to jump by 2.
            jumpAmount = jumpAmount / 2;

            // If we've hit a dead end with the jumps...
            if (jumpAmount == 0) {

                // Return an invalid location.
                location.setY(-3);
                return location.getBlock();
            }

            // Clone the current location.
            Location subTempLocation = tempLoc.clone();

            // The current material we're looking at.
            Material currentMat;

            // If we're going up...
            if (up) {

                // Get the material
                currentMat = subTempLocation.add(0, jumpAmount, 0).getBlock().getType();
            } else {
                currentMat = subTempLocation.subtract(0, jumpAmount, 0).getBlock().getType();
            }
            tempLoc = subTempLocation.clone();

            if (currentMat != Material.AIR) {
                if (subTempLocation.add(0, 1, 0).getBlock().getType() == Material.AIR
                        && subTempLocation.clone().add(0, 1, 0).getBlock().getType() == Material.AIR) {
                    return subTempLocation.add(0.5, -1, 0.5).getBlock();
                } else {
                    up = true;
                }
            } else {
                up = false;
            }
        }
    }

    private static boolean isValidLocation(Block block) {
        if (airs.contains(block.getType().name())) return false;
        if (MainConfig.get().AVOID_BIOMES.get().contains(block.getBiome().name())) return false;
        return !MainConfig.get().AVOID_BLOCKS.get().contains(block.getType().name());
    }

    public static void loadWorldData(World world) {
        if (locQueue == null) return;
        if (MainConfig.get().WHITELIST_WORLD.get() && !MainConfig.get().ALLOWED_WORLDS.get().contains(world.getName())) return;
        if (world.getGenerator() != null && MainConfig.get().IGNORE_WORLD_GENS.get().contains(world.getGenerator().getClass().getName())) return;
        int size = locQueue.getOrDefault(world.getUID(), new ArrayDeque<>()).size();

        for (int i = size; i < MainConfig.get().PREPARED_LOCATIONS_LIMIT.get(); i++) {
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

    public static void clearEverything() {
        locQueue.clear();
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
                String locLine = worldUUID.toString() +
                        "," + loc.getX() + "," + loc.getY() + "," + loc.getZ();
                writer.write(locLine);
                writer.write("\n");
            }
        }
        writer.close();
    }
}
