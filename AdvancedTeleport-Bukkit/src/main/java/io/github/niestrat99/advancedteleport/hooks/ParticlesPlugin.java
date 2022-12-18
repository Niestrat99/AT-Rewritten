package io.github.niestrat99.advancedteleport.hooks;

import io.github.niestrat99.advancedteleport.config.NewConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public abstract class ParticlesPlugin<P extends Plugin, R> extends PluginHook<P, R> {

    @Contract(pure = true)
    protected ParticlesPlugin(
        @Nullable final String pluginName,
        @Nullable final Class<R> providerClazz
    ) {
        super(pluginName, providerClazz);
    }

    @Contract(pure = true)
    protected ParticlesPlugin(@Nullable String pluginName) {
        super(pluginName, null);
    }

    @Contract(pure = true)
    public boolean canUse() {
        return NewConfig.get().USE_PARTICLES.get() && this.pluginUsable();
    }

    public abstract void applyParticles(Player player, String command);

    public abstract void removeParticles(Player player, String command);

    public abstract String getParticle(Player player);

}
