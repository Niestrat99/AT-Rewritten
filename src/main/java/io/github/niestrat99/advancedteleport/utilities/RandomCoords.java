package io.github.niestrat99.advancedteleport.utilities;

import com.wimbli.WorldBorder.BorderData;
import io.github.niestrat99.advancedteleport.config.Config;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Random;

import static io.github.niestrat99.advancedteleport.CoreClass.worldBorder;

public class RandomCoords {

    public static double getRandomCoords(double min, double max){
        Random r = new Random();
        return r.nextInt((int)Math.round(max - min)+1)+min ;
    }

    public static Location generateCoords(World world) {
        double x;
        double z;
        if (NewConfig.getInstance().USE_WORLD_BORDER.get() && worldBorder != null) {
            BorderData border = com.wimbli.WorldBorder.Config.Border(world.getName());
            // If a border has been set
            if (border != null) {
                x = getRandomCoords(border.getX() - border.getRadiusX(), border.getX() + border.getRadiusX());
                z = getRandomCoords(border.getZ() - border.getRadiusZ(), border.getZ() + border.getRadiusZ());
            } else {
                x = getRandomCoords(NewConfig.getInstance().MINIMUM_X.get(), NewConfig.getInstance().MAXIMUM_X.get());
                z = getRandomCoords(NewConfig.getInstance().MINIMUM_Z.get(), NewConfig.getInstance().MAXIMUM_Z.get());
            }
        } else {
            x = getRandomCoords(NewConfig.getInstance().MINIMUM_X.get(), NewConfig.getInstance().MAXIMUM_X.get());
            z = getRandomCoords(NewConfig.getInstance().MINIMUM_Z.get(), NewConfig.getInstance().MAXIMUM_Z.get());
        }


        int y = world.getEnvironment() == World.Environment.NETHER ? 0 : 255;
        return new Location(world, x, y, z);
    }

}
