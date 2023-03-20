package io.github.niestrat99.advancedteleport.utilities;

import io.github.niestrat99.advancedteleport.config.MainConfig;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class RandomTPAlgorithms {

    // Currently, this is all WIP.
    // In the future, users will be able to choose between different RTP algorithms.
    // By default, this uses the binary search algorithm, of which has yielded the most positive results.
    private static final HashMap<String, Algorithm> algorithms = new HashMap<>();

    public static void init() {
        algorithms.put("binary", (player, world) -> {

                // Generate random coordinates
                Location location = RandomCoords.generateCoords(world);

                // Whilst the location is too far away...
                while (ConditionChecker.canTeleport(player.getLocation(), location, "tpr", player) != null) {
                    location = RandomCoords.generateCoords(world);
                }

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
                        return getAlgorithms().get("binary").fire(player, world);
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
                        for (String material : MainConfig.get().AVOID_BLOCKS.get()) {
                            if (currentMat.name().equalsIgnoreCase(material)) {
                                mustBreak = true;
                                break;
                            }
                        }
                        if (mustBreak) {
                            return getAlgorithms().get("binary").fire(player, world);
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
                return location;
        });
    }

    public static HashMap<String, Algorithm> getAlgorithms() {
        return algorithms;
    }

    public interface Algorithm {
        Location fire(
                Player player,
                World world
        );
    }
}
