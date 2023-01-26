package io.github.niestrat99.advancedteleport.hooks.borders;

import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.hooks.BorderPlugin;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class VanillaBorderHook extends BorderPlugin<Plugin, Void> {

    public VanillaBorderHook() {
        super(null);
    }

    @Override
    @Contract(pure = true)
    public boolean canUse(@NotNull final World world) {
        return NewConfig.get().USE_VANILLA_BORDER.get()
                && world.getWorldBorder().getSize() != 59999968
                && world.getWorldBorder().getSize() != (float) 5.9999968E7;
    }

    @Override
    @Contract(pure = true)
    public double getMinX(@NotNull final World world) {
        return getCentre(world).getX() - world.getWorldBorder().getSize() / 2;
    }

    @Override
    @Contract(pure = true)
    public double getMinZ(@NotNull final World world) {
        return getCentre(world).getZ() - world.getWorldBorder().getSize() / 2;
    }

    @Override
    @Contract(pure = true)
    public double getMaxX(@NotNull final World world) {
        return getCentre(world).getX() + world.getWorldBorder().getSize() / 2;
    }

    @Override
    @Contract(pure = true)
    public double getMaxZ(@NotNull final World world) {
        return getCentre(world).getZ() + world.getWorldBorder().getSize() / 2;
    }

    @Override
    @Contract(pure = true)
    public @NotNull Location getCentre(@NotNull final World world) {
        return world.getWorldBorder().getCenter();
    }
}
