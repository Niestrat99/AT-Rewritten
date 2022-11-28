package io.github.niestrat99.advancedteleport.managers;

import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.hooks.ParticlesPlugin;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

public class ParticleManager {

    public static void applyParticles(Player player, String command) {
        if (!NewConfig.get().USE_PARTICLES.get()) return;
        for (ParticlesPlugin plugin : PluginHookManager.get().getParticlesPlugins().values()) {
            if (!plugin.canUse()) continue;
            plugin.applyParticles(player, command);
        }
    }

    public static void removeParticles(Player player, String command) {
        if (!NewConfig.get().USE_PARTICLES.get()) return;
        for (ParticlesPlugin plugin : PluginHookManager.get().getParticlesPlugins().values()) {
            if (!plugin.canUse()) continue;
            plugin.removeParticles(player, command);
            String inbuilt = NewConfig.get().TELEPORT_PARTICLES.valueOf(command).get();
            if (inbuilt != null && inbuilt.equals("spark")) doSpark(player.getLocation());
        }
    }

    public static void onTeleport(Player player, String command) {
        if (!NewConfig.get().USE_PARTICLES.get()) return;
        removeParticles(player, command);
        for (ParticlesPlugin plugin : PluginHookManager.get().getParticlesPlugins().values()) {
            if (!plugin.canUse()) continue;
            String inbuilt = NewConfig.get().TELEPORT_PARTICLES.valueOf(command).get();
            if (inbuilt.equals("spark")) doSpark(player.getLocation());
        }
    }

    public static String getData(Player player) {
        if (!NewConfig.get().USE_PARTICLES.get()) return null;
        for (ParticlesPlugin plugin : PluginHookManager.get().getParticlesPlugins().values()) {
            if (!plugin.canUse()) continue;
            String data = plugin.getParticle(player);
            if (data == null) continue;
            return data;
        }
        return null;
    }

    public static void doSpark(Location location) {
        location.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, location, 50, 0, 0, 0, 0.5);
    }
}
