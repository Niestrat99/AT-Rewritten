package io.github.niestrat99.advancedteleport.hooks.borders;

import io.github.niestrat99.advancedteleport.hooks.BorderPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.popcraft.chunkyborder.BorderData;
import org.popcraft.chunkyborder.ChunkyBorder;

public class ChunkyBorderHook extends BorderPlugin {

    private ChunkyBorder chunkyBorder;

    @Override
    public boolean canUse(World world) {
        if (!Bukkit.getPluginManager().isPluginEnabled("ChunkyBorder")) return false;
        RegisteredServiceProvider<ChunkyBorder> provider = Bukkit.getServer().getServicesManager().getRegistration(ChunkyBorder.class);
        if (provider == null) return false;
        chunkyBorder = provider.getProvider();
        // Check if it's enabled - player may not have Chunky installed
        return chunkyBorder.getBorders().containsKey(world.getName());
    }

    @Override
    public double getMinX(World world) {
        BorderData data = chunkyBorder.getBorders().get(world.getName());
        return data.getCenterX() - data.getRadiusX();
    }

    @Override
    public double getMinZ(World world) {
        BorderData data = chunkyBorder.getBorders().get(world.getName());
        return data.getCenterZ() - data.getRadiusZ();
    }

    @Override
    public double getMaxX(World world) {
        BorderData data = chunkyBorder.getBorders().get(world.getName());
        return data.getCenterX() + data.getRadiusX();
    }

    @Override
    public double getMaxZ(World world) {
        BorderData data = chunkyBorder.getBorders().get(world.getName());
        return data.getCenterZ() + data.getRadiusZ();
    }

    @Override
    public Location getCentre(World world) {
        BorderData data = chunkyBorder.getBorders().get(world.getName());
        return new Location(world, data.getCenterX(), 128, data.getCenterZ());
    }
}
