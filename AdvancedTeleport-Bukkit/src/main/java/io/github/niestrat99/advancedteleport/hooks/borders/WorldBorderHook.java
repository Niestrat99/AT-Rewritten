package io.github.niestrat99.advancedteleport.hooks.borders;

import com.wimbli.WorldBorder.BorderData;
import com.wimbli.WorldBorder.WorldBorder;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.hooks.BorderPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class WorldBorderHook extends BorderPlugin {

    private WorldBorder worldBorder;

    @Override
    public boolean canUse(World world) {

        // Ensures plugin world borders are enabled
        if (!NewConfig.get().USE_PLUGIN_BORDERS.get()) return false;

        // Ensures the plugin is enabled and there's a world border in the world
        this.worldBorder = (WorldBorder) Bukkit.getPluginManager().getPlugin("WorldBorder");
        return worldBorder != null && worldBorder.getWorldBorder(world.getName()) != null;
    }

    @Override
    public double getMinX(World world) {

        // Get the border in the world, its centre and take away the X radius.
        BorderData data = worldBorder.getWorldBorder(world.getName());
        return getCentre(world).getX() - data.getRadiusX();
    }

    @Override
    public double getMinZ(World world) {

        // Get the border in the world, its centre and take away the Z radius.
        BorderData data = worldBorder.getWorldBorder(world.getName());
        return getCentre(world).getZ() - data.getRadiusZ();
    }

    @Override
    public double getMaxX(World world) {

        // Get the border in the world, its centre and add the X radius.
        BorderData data = worldBorder.getWorldBorder(world.getName());
        return getCentre(world).getX() + data.getRadiusX();
    }

    @Override
    public double getMaxZ(World world) {

        // Get the border in the world, its centre and add the Z radius.
        BorderData data = worldBorder.getWorldBorder(world.getName());
        return getCentre(world).getZ() + data.getRadiusZ();
    }

    @Override
    public Location getCentre(World world) {

        // Get the border in the world and return its centre.
        BorderData data = worldBorder.getWorldBorder(world.getName());
        return new Location(world, data.getX(), 128, data.getZ());
    }
}
