package io.github.niestrat99.advancedteleport.managers;

import io.github.niestrat99.advancedteleport.config.MainConfig;
import io.github.niestrat99.advancedteleport.hooks.ParticlesPlugin;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class ParticleManager {
    private ParticleManager() {}

    public static void applyParticles(@NotNull final Player player, @NotNull final String command) {
        if (!MainConfig.get().USE_PARTICLES.get()) return;
        PluginHookManager.get()
                .getPluginHooks(ParticlesPlugin.class, true)
                .forEach(plugin -> plugin.applyParticles(player, command));
    }

    public static void removeParticles(
            @NotNull final Player player, @NotNull final String command) {
        if (!MainConfig.get().USE_PARTICLES.get()) return;
        PluginHookManager.get()
                .getPluginHooks(ParticlesPlugin.class, true)
                .map(
                        hook -> {
                            hook.removeParticles(player, command);
                            return MainConfig.get().TELEPORT_PARTICLES.valueOf(command).get();
                        })
                .filter("spark"::equals)
                .forEach(inbuilt -> doSpark(player.getLocation()));
    }

    public static void onTeleport(@NotNull final Player player, @NotNull final String command) {
        if (!MainConfig.get().USE_PARTICLES.get()) return;
        removeParticles(player, command);
        PluginHookManager.get()
                .getPluginHooks(ParticlesPlugin.class, true)
                .map(hook -> MainConfig.get().TELEPORT_PARTICLES.valueOf(command).get())
                .filter("spark"::equals)
                .forEach(hook -> doSpark(player.getLocation()));
    }

    public static @Nullable String getData(@NotNull final Player player) {
        if (!MainConfig.get().USE_PARTICLES.get()) return null;
        return PluginHookManager.get()
                .getPluginHooks(ParticlesPlugin.class, true)
                .map(hook -> hook.getParticle(player))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    public static void doSpark(@NotNull final Location location) {
        location.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, location, 50, 0, 0, 0, 0.5);
    }
}
