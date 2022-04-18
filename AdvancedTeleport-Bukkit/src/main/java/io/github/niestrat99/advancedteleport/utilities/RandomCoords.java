package io.github.niestrat99.advancedteleport.utilities;

import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.managers.PluginHookManager;
import io.github.thatsmusic99.configurationmaster.api.ConfigSection;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Random;

public class RandomCoords {

    private static final Random random = new Random();

    public static double getRandomCoords(double min, double max){
        return random.nextInt((int)Math.round(max - min)+1)+min ;
    }

    public static Location generateCoords(World world) {
        double[] coords = PluginHookManager.get().getRandomCoords(world);
        if (coords == null) {
            ConfigSection x = NewConfig.get().X.get();
            ConfigSection z = NewConfig.get().Z.get();

            String xStr = x.contains(world.getName()) ? x.getString(world.getName()) : x.getString("default");
            String zStr = x.contains(world.getName()) ? z.getString(world.getName()) : z.getString("default");

            Integer[] xInt = null;
            Integer[] zInt = null;
            if (xStr != null) {
                String[] split = xStr.split(";");
                xInt = split.length > 1
                        ? new Integer[]{Integer.parseInt(split[0]), Integer.parseInt(split[1])}
                        : new Integer[]{Integer.parseInt(split[0]), Integer.parseInt(String.format("-%s", split[0]))};
            }
            if (zStr != null) {
                String[] split = zStr.split(";");
                zInt = split.length > 1
                        ? new Integer[]{Integer.parseInt(split[0]), Integer.parseInt(split[1])}
                        : new Integer[]{Integer.parseInt(split[0]), Integer.parseInt(String.format("-%s", split[0]))};
            }

            coords = new double[]{
                    getRandomCoords(xInt != null ? xInt[0] : NewConfig.get().MINIMUM_X.get(), xInt != null ? xInt[1] : NewConfig.get().MAXIMUM_X.get()),
                    getRandomCoords(zInt != null ? zInt[0] : NewConfig.get().MINIMUM_Z.get(), zInt != null ? zInt[1] : NewConfig.get().MAXIMUM_Z.get())
            };
        }
        int y = world.getEnvironment() == World.Environment.NETHER ? 0 : 255;
        return new Location(world, coords[0], y, coords[1]);
    }

}
