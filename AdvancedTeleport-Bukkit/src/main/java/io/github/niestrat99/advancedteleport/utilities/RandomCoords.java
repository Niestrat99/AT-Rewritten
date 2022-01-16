package io.github.niestrat99.advancedteleport.utilities;

import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.managers.PluginHookManager;
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
            coords = new double[]{
                    getRandomCoords(NewConfig.get().MINIMUM_X.get(), NewConfig.get().MAXIMUM_X.get()),
                    getRandomCoords(NewConfig.get().MINIMUM_Z.get(), NewConfig.get().MAXIMUM_Z.get())
            };
        }
        int y = world.getEnvironment() == World.Environment.NETHER ? 0 : 255;
        return new Location(world, coords[0], y, coords[1]);
    }

}
