package io.github.at.events;

import io.github.at.config.Config;
import io.github.at.config.CustomMessages;
import io.github.at.config.LastLocations;
import io.github.at.main.Main;
import io.github.at.utilities.DistanceLimiter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;

public class TeleportTrackingManager implements Listener {

    private static HashMap<Player, Location> lastLocations = new HashMap<>();

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (Config.isFeatureEnabled("teleport")) {
            new BukkitRunnable() { // Because when you join, PlayerTeleportEvent is also called
                @Override
                public void run() {
                    lastLocations.put(e.getPlayer(), LastLocations.getLocation(e.getPlayer()));
                }
            }.runTaskLater(Main.getInstance(), 20);
        }

    }
    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        if (Config.hasStrictDistanceMonitor()) {
            if (!DistanceLimiter.canTeleport(e.getTo(), e.getFrom(), null) && !e.getPlayer().hasPermission("at.admin.bypass.distance-limit")) {
                e.getPlayer().sendMessage(CustomMessages.getString("Error.tooFarAway"));
                e.setCancelled(true);
                return;
            }
        }
    }

    public static Location getLastLocation(Player player) {
        return lastLocations.get(player);
    }

    public static HashMap<Player, Location> getLastLocations() {
        return lastLocations;
    }
}
