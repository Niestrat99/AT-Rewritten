package io.github.niestrat99.advancedteleport.hooks.borders;

import io.github.niestrat99.advancedteleport.hooks.BorderPlugin;

import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.popcraft.chunkyborder.ChunkyBorder;

public final class ChunkyBorderHook extends BorderPlugin<ChunkyBorder, ChunkyBorder> {

    private ChunkyBorder chunkyBorder;

    @Contract(pure = true)
    public ChunkyBorderHook() {
        super("ChunkyBorder", ChunkyBorder.class);
    }

    @Override
    @Contract(pure = true)
    public boolean canUse(@NotNull final World world) {
        if (super.canUse(world)) return false;

        return this.provider()
                .map(
                        provider -> {
                            chunkyBorder = provider;
                            return provider.getBorders().containsKey(world.getName());
                        })
                .orElse(false);
    }

    @Override
    @Contract(pure = true)
    public double getMinX(@NotNull final World world) {
        final var data = chunkyBorder.getBorders().get(world.getName());
        return data.getCenterX() - data.getRadiusX();
    }

    @Override
    @Contract(pure = true)
    public double getMinZ(@NotNull final World world) {
        final var data = chunkyBorder.getBorders().get(world.getName());
        return data.getCenterZ() - data.getRadiusZ();
    }

    @Override
    @Contract(pure = true)
    public double getMaxX(@NotNull final World world) {
        final var data = chunkyBorder.getBorders().get(world.getName());
        return data.getCenterX() + data.getRadiusX();
    }

    @Override
    @Contract(pure = true)
    public double getMaxZ(@NotNull final World world) {
        final var data = chunkyBorder.getBorders().get(world.getName());
        return data.getCenterZ() + data.getRadiusZ();
    }

    @Override
    @Contract(pure = true)
    public @NotNull Location getCentre(@NotNull final World world) {
        final var data = chunkyBorder.getBorders().get(world.getName());
        return new Location(world, data.getCenterX(), 128, data.getCenterZ());
    }
}
