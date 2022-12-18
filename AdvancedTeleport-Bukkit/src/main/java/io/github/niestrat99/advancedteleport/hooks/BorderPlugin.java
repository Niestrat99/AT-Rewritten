package io.github.niestrat99.advancedteleport.hooks;

import io.github.niestrat99.advancedteleport.config.NewConfig;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BorderPlugin<P extends Plugin, R> extends PluginHook<P, R> {

    @Contract(pure = true)
    protected BorderPlugin(
        @Nullable final String pluginName,
        @Nullable final Class<?> provider
    ) {
        super(pluginName);
    }

    @Contract(pure = true)
    protected BorderPlugin(@Nullable final String pluginName) {
        super(pluginName, null);
    }

    public boolean canUse(@NotNull final World world) {
        return NewConfig.get().USE_PLUGIN_BORDERS.get() && this.pluginUsable();
    }

    public abstract double getMinX(@NotNull final World world);

    public abstract double getMinZ(@NotNull final World world);

    public abstract double getMaxX(@NotNull final World world);

    public abstract double getMaxZ(@NotNull final World world);

    public abstract @NotNull Location getCentre(@NotNull final World world);
}
