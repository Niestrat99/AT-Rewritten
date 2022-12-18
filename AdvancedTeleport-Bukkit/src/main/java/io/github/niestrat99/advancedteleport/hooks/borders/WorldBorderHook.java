package io.github.niestrat99.advancedteleport.hooks.borders;

import com.wimbli.WorldBorder.BorderData;
import com.wimbli.WorldBorder.WorldBorder;
import io.github.niestrat99.advancedteleport.hooks.BorderPlugin;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class WorldBorderHook extends BorderPlugin<WorldBorder, Void> {
    private WorldBorder worldBorder;

    @Contract(pure = true)
    public WorldBorderHook() {
        super("WorldBorder");
    }

    @Override
    @Contract(pure = true)
    public boolean canUse(@NotNull final World world) {
        if (!super.canUse(world)) return false;

        return this.plugin().map(plugin -> {
            worldBorder = plugin;
            return worldBorder.getWorldBorder(world.getName()) != null;
        }).orElse(false);
    }

    @Override
    @Contract(pure = true)
    public double getMinX(@NotNull final World world) {
        BorderData data = worldBorder.getWorldBorder(world.getName());
        return getCentre(world).getX() - data.getRadiusX();
    }

    @Override
    @Contract(pure = true)
    public double getMinZ(@NotNull final World world) {
        BorderData data = worldBorder.getWorldBorder(world.getName());
        return getCentre(world).getZ() - data.getRadiusZ();
    }

    @Override
    @Contract(pure = true)
    public double getMaxX(@NotNull final World world) {
        BorderData data = worldBorder.getWorldBorder(world.getName());
        return getCentre(world).getX() + data.getRadiusX();
    }

    @Override
    @Contract(pure = true)
    public double getMaxZ(@NotNull final World world) {
        BorderData data = worldBorder.getWorldBorder(world.getName());
        return getCentre(world).getZ() + data.getRadiusZ();
    }

    @Override
    @Contract(pure = true)
    public @NotNull Location getCentre(@NotNull final World world) {
        BorderData data = worldBorder.getWorldBorder(world.getName());
        return new Location(world, data.getX(), 128, data.getZ());
    }
}
