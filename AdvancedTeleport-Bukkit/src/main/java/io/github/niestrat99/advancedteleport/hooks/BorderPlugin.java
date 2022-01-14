package io.github.niestrat99.advancedteleport.hooks;

import org.bukkit.Location;
import org.bukkit.World;

public abstract class BorderPlugin {

    public abstract boolean canUse(World world);

    public abstract double getMinX(World world);

    public abstract double getMinZ(World world);

    public abstract double getMaxX(World world);

    public abstract double getMaxZ(World world);

    public abstract Location getCentre(World world);
}
