package io.github.niestrat99.advancedteleport.hooks;

import org.bukkit.Location;
import org.bukkit.World;

/**
 * A plugin that sets up a border around a given world.
 */
public abstract class BorderPlugin {

    /**
     * Checks if the plugin is enabled and there is a world border for the given world.
     *
     * @param world the world to check.
     * @return true if the plugin is enabled and there is a viable world border, false if not.
     */
    public abstract boolean canUse(World world);

    /**
     * Returns the minimum X coordinate of the world border.
     *
     * @param world the world to check.
     * @return the minimum X coordinate of the world border.
     */
    public abstract double getMinX(World world);

    /**
     * Returns the minimum Z coordinate of the world border.
     *
     * @param world the world to check.
     * @return the minimum Z coordinate of the world border.
     */
    public abstract double getMinZ(World world);

    /**
     * Returns the maximum X coordinate of the world border.
     *
     * @param world the world to check.
     * @return the maximum X coordinate of the world border.
     */
    public abstract double getMaxX(World world);

    /**
     * Returns the maximum Z coordinate of the world border.
     *
     * @param world the world to check.
     * @return the maximum Z coordinate of the world border.
     */
    public abstract double getMaxZ(World world);

    /**
     * Returns the centre of the world border.
     *
     * @param world the world to check.
     * @return the location of the centre at Y 128.
     */
    public abstract Location getCentre(World world);
}
