package io.github.niestrat99.advancedteleport.utilities;

import io.github.niestrat99.advancedteleport.config.Config;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class RandomTPAlgorithms {

    // Currently, this is all WIP.
    // In the future, users will be able to choose between different RTP algorithms.
    // By default, this uses the binary search algorithm, of which has yielded the most positive results.
    private static HashMap<String, Algorithm> algorithms = new HashMap<>();

    public static void init() {
        algorithms.put("linear", (player, callback) -> {
            World world = player.getWorld();
            Location location = RandomCoords.generateCoords(world);
            boolean validLocation = false;
            while (!validLocation) {
                if (location.getWorld().getEnvironment() == World.Environment.NETHER) { // We'll search up instead of down in the Nether!
                    while (location.getBlock().getType() != Material.AIR) {
                        location.add(0, 1, 0);
                    }
                } else {
                    while (location.getBlock().getType() == Material.AIR) {
                        location.subtract(0, 1, 0);
                    }
                }


                boolean b = true;
                for (String Material: Config.avoidBlocks()) {
                    if (location.getWorld().getEnvironment() == World.Environment.NETHER) {
                        if (location.clone().subtract(0, 1, 0).getBlock().getType().name().equalsIgnoreCase(Material)) {
                            location = RandomCoords.generateCoords(world);
                            b = false;
                            break;
                        }
                    } else {
                        if (location.getBlock().getType().name().equalsIgnoreCase(Material)){
                            location = RandomCoords.generateCoords(world);
                            b = false;
                            break;
                        }
                    }

                }
                if (!DistanceLimiter.canTeleport(player.getLocation(), location, "tpr") && !player.hasPermission("at.admin.bypass.distance-limit")) {
                    b = false;
                }
                if (b) {
                    location.add(0 , 2 , 0);
                    validLocation = true;
                }
            }
            if (callback != null) {
                callback.onSuccess(location);
            }
        });

        algorithms.put("binary", (player, callback) -> {
            new Thread(() -> {
                World world = player.getWorld();
                // Generate random coordinates
                Location location = RandomCoords.generateCoords(world);
                // Set the Y coordinate to 128
                location.setY(128);
                // This is how much we'll jump by at first
                int jumpAmount = 128;
                // However, if we're in the Nether...
                if (world.getEnvironment() == World.Environment.NETHER) {
                    // We'll start at level 64 instead and start at a jump of 64.
                    location.setY(64);
                    jumpAmount = 64;
                }
                // Whether a valid location has been found or not
                boolean validLocation = false;
                // Whether to go up or down.
                boolean up = false;
                // Temporary location.
                Location tempLoc = location.clone();
                // Whilst there's no valid location...
                while (!validLocation) {
                    // Divide the amount to jump by 2.
                    jumpAmount = jumpAmount / 2;
                    // If we've hit a dead end with the jumps...
                    if (jumpAmount == 0) {
                        // Start over.
                        getAlgorithms().get("binary").fire(player, callback);
                        return;
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

                    boolean mustBreak = false;
                    if (currentMat != Material.AIR) {
                        for (String material : Config.avoidBlocks()) {
                            if (currentMat.name().equalsIgnoreCase(material)) {
                                mustBreak = true;
                                break;
                            }
                        }
                        if (mustBreak) {
                            getAlgorithms().get("binary").fire(player, callback);
                            return;
                        }

                        if (subTempLocation.add(0, 1, 0).getBlock().getType() == Material.AIR
                                && subTempLocation.clone().add(0, 1, 0).getBlock().getType() == Material.AIR) {
                            location = subTempLocation.add(0.5, 0, 0.5);
                            validLocation = true;
                        } else {
                            up = true;
                        }
                    } else {
                        up = false;
                    }
                }
                if (callback != null) {
                    callback.onSuccess(location);
                }
            }, "AdvancedTeleport RTP Worker").start();
        });

        algorithms.put("jump", (player, callback) -> {
            World world = player.getWorld();
            Location location = RandomCoords.generateCoords(world);
            boolean validLocation = false;
            while (!validLocation) {
                if (location.getWorld().getEnvironment() == World.Environment.NETHER) { // We'll search up instead of down in the Nether!
                    while (location.getBlock().getType() != Material.AIR) {
                        location.add(0, 10, 0);
                    }
                } else {
                    while (location.getBlock().getType() == Material.AIR) {
                        location.subtract(0, 10, 0);
                    }
                }


                boolean b = true;
                for (String Material: Config.avoidBlocks()) {
                    if (location.getWorld().getEnvironment() == World.Environment.NETHER) {
                        if (location.clone().subtract(0, 1, 0).getBlock().getType().name().equalsIgnoreCase(Material)) {
                            location = RandomCoords.generateCoords(world);
                            b = false;
                            break;
                        }
                    } else {
                        if (location.getBlock().getType().name().equalsIgnoreCase(Material)){
                            location = RandomCoords.generateCoords(world);
                            b = false;
                            break;
                        }
                    }

                }
                if (!DistanceLimiter.canTeleport(player.getLocation(), location, "tpr") && !player.hasPermission("at.admin.bypass.distance-limit")) {
                    b = false;
                }
                if (b) {
                    location.add(0 , 2 , 0);
                    validLocation = true;
                }
            }
            if (callback != null) {
                callback.onSuccess(location);
            }
        });
    }

    public static HashMap<String, Algorithm> getAlgorithms() {
        return algorithms;
    }

    public static interface Algorithm {
        void fire(Player player, Callback<Location> callback);
    }

    public static interface Callback<D> {
        void onSuccess(D data);
    }
}
