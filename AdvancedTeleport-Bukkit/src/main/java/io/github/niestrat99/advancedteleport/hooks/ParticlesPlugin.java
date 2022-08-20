package io.github.niestrat99.advancedteleport.hooks;

import org.bukkit.entity.Player;

public abstract class ParticlesPlugin {

    public abstract boolean canUse();

    public abstract void applyParticles(Player player, String command);

    public abstract void removeParticles(Player player, String command);

    public abstract String getParticle(Player player);

}
