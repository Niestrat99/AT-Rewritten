package io.github.niestrat99.advancedteleport.hooks.borders;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.hooks.BorderPlugin;
import org.bukkit.Location;
import org.bukkit.World;

public class VanillaBorderHook extends BorderPlugin {

    @Override
    public boolean canUse(World world) {

        // imagine being on 1.7 - default/resetting border size is 59999968 according to https://minecraft.fandom.com/wiki/World_border#Commands
        return NewConfig.get().USE_VANILLA_BORDER.get()
                && CoreClass.getInstance().getVersion() > 7
                && world.getWorldBorder().getSize() != 59999968
                && world.getWorldBorder().getSize() != (float) 5.9999968E7;
    }

    @Override
    public double getMinX(World world) {
        return getCentre(world).getX() - world.getWorldBorder().getSize() / 2;
    }

    @Override
    public double getMinZ(World world) {
        return getCentre(world).getZ() - world.getWorldBorder().getSize() / 2;
    }

    @Override
    public double getMaxX(World world) {
        return getCentre(world).getX() + world.getWorldBorder().getSize() / 2;
    }

    @Override
    public double getMaxZ(World world) {
        return getCentre(world).getZ() + world.getWorldBorder().getSize() / 2;
    }

    @Override
    public Location getCentre(World world) {
        return world.getWorldBorder().getCenter();
    }
}
