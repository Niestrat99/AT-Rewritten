package io.github.niestrat99.advancedteleport.managers;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.config.MainConfig;
import io.github.niestrat99.advancedteleport.hooks.ParticlesPlugin;

import org.bukkit.*;
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

        // Check if using inbuilt particles
        final var particle = MainConfig.get().TELEPORT_PARTICLES.valueOf(command).get();
        if (particle.equals("spark")) {
            doSpark(player.getLocation());
            return;
        }

        PluginHookManager.get()
                .getPluginHooks(ParticlesPlugin.class, true)
                .forEach(hook -> hook.removeParticles(player, command));
    }

    public static void onTeleport(@NotNull final Player player, @NotNull final String command) {
        if (!MainConfig.get().USE_PARTICLES.get()) return;
        removeParticles(player, command);

        // Check if using inbuilt particles
        final var particle = MainConfig.get().TELEPORT_PARTICLES.valueOf(command).get();
        if (particle.equals("spark")) {
            doSpark(player.getLocation());
            return;
        }

        applyParticles(player, command);
        Bukkit.getServer().getScheduler().runTask(CoreClass.getInstance(), () -> removeParticles(player, command));
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
        location.getWorld().spawnParticle(getSpark(), location, 50, 0, 0, 0, 0.5);
    }

    private static Particle getSpark() {
        try {

            final var particle = Registry.PARTICLE_TYPE.get(NamespacedKey.fromString("minecraft:firework"));
            if (particle == null) return Particle.valueOf("FIREWORKS_SPARK");
            return particle;
        } catch (NoSuchFieldError whyWouldYouDoThisSpigot) {
            return Particle.valueOf("FIREWORKS_SPARK");
        }
    }
}
