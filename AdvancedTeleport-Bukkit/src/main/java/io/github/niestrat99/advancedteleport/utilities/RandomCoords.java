package io.github.niestrat99.advancedteleport.utilities;

import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.managers.PluginHookManager;
import io.github.thatsmusic99.configurationmaster.api.ConfigSection;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class RandomCoords {

    private static final Map<String, double[]> coordCache = new HashMap<>();

    private static final Random random = new Random();

    public static double getRandomCoords(double min, double max){
        return random.nextInt((int)Math.round(max - min)+1)+min ;
    }

    public static Location getRandCoords(World world, double[] coords, int y) {
        Location loc = new Location(world, getRandomCoords(coords[0], coords[1]), y, getRandomCoords(coords[2], coords[3]));
        if (PluginHookManager.get().isClaimed(loc)) { // Should look into a limiter, so we don't get stuck in a loop somehow
            return getRandCoords(world, coords, y);
        }
        return loc;
    }

    public static Location generateCoords(World world) {
        double[] coords = PluginHookManager.get().getRandomCoords(world);
        if (coords == null) {
            if (!coordCache.containsKey(world.getName())) {

                ConfigSection x = NewConfig.get().X.get();
                ConfigSection z = NewConfig.get().Z.get();

                String xStr = x.contains(world.getName()) ? x.getString(world.getName()) : x.getString("default");
                String zStr = x.contains(world.getName()) ? z.getString(world.getName()) : z.getString("default");

                if (xStr != null || zStr != null) {
                    Integer[] coordsInt = new Integer[4];

                    String[] xSplit = xStr != null ? xStr.split(";") : zStr.split(";"); // Use the Z coord if X isn't present for some reason
                    setIntegers(coordsInt, xSplit, 0, 1);

                    String[] zSplit = zStr != null ? zStr.split(";") : xStr.split(";"); // Use the X coord if Z isn't present for some reason
                    setIntegers(coordsInt, zSplit, 2, 3);

                    double[] coordsDouble = new double[]{coordsInt[0], coordsInt[1], coordsInt[2], coordsInt[3]}; // Is there a better way of doing this?
                    coordCache.put(world.getName(), coordsDouble);
                }
            }

            double[] coordsDouble = coordCache.get(world.getName());

            coords = coordsDouble != null
                    ? coordsDouble
                    : new double[]{NewConfig.get().MINIMUM_X.get(), NewConfig.get().MAXIMUM_X.get(), NewConfig.get().MINIMUM_Z.get(), NewConfig.get().MAXIMUM_Z.get()};
        }
        int y = world.getEnvironment() == World.Environment.NETHER ? 0 : 255;
        return getRandCoords(world, coords, y);
    }

    private static void setIntegers(Integer[] array, String[] strArray, int c1, int c2) {
        if (strArray.length > 1) {
            array[c1] = Integer.parseInt(strArray[0]);
            array[c2] = Integer.parseInt(strArray[1]);
        } else {
            array[c1] = Integer.parseInt(strArray[0]);
            array[c2] = Integer.parseInt(String.format("-%s", strArray[0]));
        }
    }

    public static void reload() {
        coordCache.clear();
    }

}
