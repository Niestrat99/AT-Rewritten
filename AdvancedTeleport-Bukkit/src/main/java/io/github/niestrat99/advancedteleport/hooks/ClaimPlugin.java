package io.github.niestrat99.advancedteleport.hooks;

import io.github.niestrat99.advancedteleport.config.MainConfig;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ClaimPlugin<P extends Plugin, R> extends PluginHook<P, R> {

    @Contract(pure = true)
    protected ClaimPlugin(@Nullable final String pluginName, @Nullable final Class<R> provider) {
        super(pluginName, provider);
    }

    @Contract(pure = true)
    protected ClaimPlugin(@Nullable final String pluginName) {
        super(pluginName, null);
    }

    @Contract(pure = true)
    public boolean canUse(@NotNull final World world) {
        return MainConfig.get().PROTECT_CLAIM_LOCATIONS.get() && this.pluginUsable();
    }

    public abstract boolean isClaimed(@NotNull final Location location);

    public abstract boolean canAccess(final @NotNull Player player, final @NotNull Location location);
}
