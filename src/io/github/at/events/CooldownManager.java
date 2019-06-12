package io.github.at.events;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;

public class CooldownManager {

    private static HashMap<Player, BukkitRunnable> cooldown = new HashMap<>();

    public static HashMap<Player, BukkitRunnable> getCooldown() {
        return cooldown;
    }
}
