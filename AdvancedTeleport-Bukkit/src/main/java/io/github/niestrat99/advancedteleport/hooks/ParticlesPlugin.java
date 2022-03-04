package io.github.niestrat99.advancedteleport.hooks;

import org.bukkit.entity.Player;

public abstract class ParticlesPlugin {

    public abstract void canUse();

    public abstract void applyParticles(Player player, String command);

}
