package io.github.niestrat99.advancedteleport.hooks;

import io.github.niestrat99.advancedteleport.config.NewConfig;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a particles plugin.
 */
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

    /**
     * Whether the plugin is enabled and can be used.
     *
     * @return true if the plugin is usable, false if not.
     */
    @Contract(pure = true)
    public boolean canUse() {
        return NewConfig.get().USE_PARTICLES.get() && this.pluginUsable();
    }

    /**
     * Attempts to apply the particles for a specific command.
     *
     * @param player  the player having particles applied.
     * @param command the command being run.
     */
    public abstract void applyParticles(
        Player player,
        String command
    );

    /**
     * Attempts to remove the particles from a player that were applied when the command has finished.
     *
     * @param player  the player having the particles removed.
     * @param command the command that was originally run.
     */
    public abstract void removeParticles(
        Player player,
        String command
    );

    /**
     * Gets a readable string of the particles applied to the player. Returns null if there are no particles.
     * <p>
     * The string is formatted as PARTICLE;PARTICLE;PARTICLE - for each particle, internal data is separated by ,.
     * <p>
     * The data should be parsed by the Particle Plugin class itself and not outside of it.
     *
     * @param player the player being checked.
     * @return a readable string of the particles currently applied, null if there is nothing.
     */
    @Nullable
    public abstract String getParticle(Player player);

}
