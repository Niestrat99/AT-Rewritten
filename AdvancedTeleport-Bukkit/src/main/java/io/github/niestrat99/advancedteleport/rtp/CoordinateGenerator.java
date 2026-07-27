package io.github.niestrat99.advancedteleport.rtp;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.config.MainConfig;
import io.github.niestrat99.advancedteleport.managers.PluginHookManager;
import io.github.thatsmusic99.configurationmaster.api.ConfigSection;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class CoordinateGenerator {

    private static final Random random = new Random();

    public static double getRandomCoords(double min, double max) {
        return random.nextInt((int) Math.round(max - min) + 1) + min;
    }

    public static @Nullable Location getRandCoords(
            World world, RandomTPBorders borders, int y, int attempt) {
        if (attempt++ > 15) {
            return null;
        }

        Location loc =
                new Location(
                        world,
                        getRandomCoords(borders.minX(), borders.maxX()),
                        y,
                        getRandomCoords(borders.minZ(), borders.maxZ()));
        if (PluginHookManager.get()
                .isClaimed(
                        loc)) { // Should look into a limiter, so we don't get stuck in a loop
                                // somehow
            return getRandCoords(world, borders, y, attempt);
        }
        return loc;
    }

    public static @Nullable Location generateCoords(World world) {

        int y = world.getEnvironment() == World.Environment.NETHER ? 0 : 255;
        return getRandCoords(world, getBorders(world), y, 0);
    }

    public static @NotNull RandomTPBorders getBorders(final @NotNull World world) {
        RandomTPBorders vanillaBorder = getVanillaWorldBorders(world);
        RandomTPBorders pluginBorders = PluginHookManager.get().getBorders(world);

        RandomTPBorders borders;
        if (MainConfig.get().SYNC_VANILLA_BORDER.get()) {
            borders = vanillaBorder;
        } else if (MainConfig.get().SYNC_PLUGIN_BORDERS.get()) {
            borders = pluginBorders;
        } else {

            ConfigSection x = MainConfig.get().X.get();
            ConfigSection z = MainConfig.get().Z.get();

            String xStr =
                    x.contains(world.getName())
                            ? x.getString(world.getName())
                            : x.getString("default");
            String zStr =
                    x.contains(world.getName())
                            ? z.getString(world.getName())
                            : z.getString("default");

            double[] coordsDouble = new double[4];

            if (xStr != null || zStr != null) {
                String[] xSplit =
                        xStr != null
                                ? xStr.split(";")
                                : zStr.split(
                                ";"); // Use the Z coord if X isn't present for
                // some reason
                setArray(coordsDouble, xSplit, 1, 0);

                String[] zSplit =
                        zStr != null
                                ? zStr.split(";")
                                : xStr.split(
                                ";"); // Use the X coord if Z isn't present for
                // some reason
                setArray(coordsDouble, zSplit, 3, 2);

                borders = new RandomTPBorders(
                        Math.min(coordsDouble[0], coordsDouble[1]),
                        Math.max(coordsDouble[0], coordsDouble[1]),
                        Math.min(coordsDouble[2], coordsDouble[3]),
                        Math.max(coordsDouble[2], coordsDouble[3]));
            } else {
                CoreClass.getInstance().getLogger().warning("X and Z /rtp boundaries for " + world.getName() + " are " +
                        "not set up properly. A range of -5000 to 5000 will be used.");
                borders = new RandomTPBorders(-5000, 5000, -5000, 5000);
            }
        }

        // Minimise the border so it's smaller
        borders = borders.minimal(vanillaBorder);
        borders = borders.minimal(pluginBorders);

        return borders;
    }

    private static RandomTPBorders getVanillaWorldBorders(final @NotNull World world) {
        WorldBorder border = world.getWorldBorder();
        return new RandomTPBorders(
                Math.ceil(border.getCenter().getX() - border.getSize() / 2),
                Math.floor(border.getCenter().getX() + border.getSize() / 2),
                Math.ceil(border.getCenter().getZ() - border.getSize() / 2),
                Math.floor(border.getCenter().getZ() + border.getSize() / 2));
    }

    private static void setArray(double[] array, String[] strArray, int c1, int c2) {
        if (strArray.length > 1) {
            array[c1] = Integer.parseInt(strArray[0]);
            array[c2] = Integer.parseInt(strArray[1]);
        } else {
            array[c1] = Integer.parseInt(strArray[0]);
            array[c2] = Integer.parseInt(String.format("-%s", strArray[0]));
        }
    }
}
