package io.github.niestrat99.advancedteleport.hooks;

import io.github.niestrat99.advancedteleport.config.MainConfig;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A plugin that sets up a border around a given world.
 */
public abstract class BorderPlugin<P extends Plugin, R> extends PluginHook<P, R> {

    @Contract(pure = true)
    protected BorderPlugin(
        @Nullable final String pluginName,
        @Nullable final Class<R> provider
    ) {
        super(pluginName, provider);
    }

    @Contract(pure = true)
    protected BorderPlugin(@Nullable final String pluginName) {
        super(pluginName, null);
    }

    /**
     * Checks if the plugin is enabled and there is a world border for the given world.
     *
     * @param world the world to check.
     * @return true if the plugin is enabled and there is a viable world border, false if not.
     */
    public boolean canUse(@NotNull final World world) {
        return MainConfig.get().USE_PLUGIN_BORDERS.get() && this.pluginUsable();
    }

    /**
     * Returns the minimum X coordinate of the world border.
     *
     * @param world the world to check.
     * @return the minimum X coordinate of the world border.
     */
    public abstract double getMinX(@NotNull final World world);

    /**
     * Returns the minimum Z coordinate of the world border.
     *
     * @param world the world to check.
     * @return the minimum Z coordinate of the world border.
     */
    public abstract double getMinZ(@NotNull final World world);

    /**
     * Returns the maximum X coordinate of the world border.
     *
     * @param world the world to check.
     * @return the maximum X coordinate of the world border.
     */
    public abstract double getMaxX(@NotNull final World world);

    /**
     * Returns the maximum Z coordinate of the world border.
     *
     * @param world the world to check.
     * @return the maximum Z coordinate of the world border.
     */
    public abstract double getMaxZ(@NotNull final World world);

    /**
     * Returns the centre of the world border.
     *
     * @param world the world to check.
     * @return the location of the centre at Y 128.
     */
    public abstract @NotNull Location getCentre(@NotNull final World world);
}
