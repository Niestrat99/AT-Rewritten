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
        if (!NewConfig.get().USE_PLUGIN_BORDERS.get()) return false;
        this.worldBorder = (WorldBorder) Bukkit.getPluginManager().getPlugin("WorldBorder");
        return worldBorder != null && worldBorder.getWorldBorder(world.getName()) != null;
    }

    @Override
    public double getMinX(World world) {
        BorderData data = worldBorder.getWorldBorder(world.getName());
        return getCentre(world).getX() - data.getRadiusX();
    }

    @Override
    public double getMinZ(World world) {
        BorderData data = worldBorder.getWorldBorder(world.getName());
        return getCentre(world).getZ() - data.getRadiusZ();
    }

    @Override
    public double getMaxX(World world) {
        BorderData data = worldBorder.getWorldBorder(world.getName());
        return getCentre(world).getX() + data.getRadiusX();
    }

    @Override
    public double getMaxZ(World world) {
        BorderData data = worldBorder.getWorldBorder(world.getName());
        return getCentre(world).getZ() + data.getRadiusZ();
    }

    @Override
    public Location getCentre(World world) {
        BorderData data = worldBorder.getWorldBorder(world.getName());
        return new Location(world, data.getX(), 128, data.getZ());
    }
}
