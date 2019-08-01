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
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;

public class TeleportTrackingManager implements Listener {

    private static HashMap<Player, Location> lastLocations = new HashMap<>();
    // This needs a separate hashmap because players may not immediately click "Respawn".
    private static HashMap<Player, Location> deathLocations = new HashMap<>();

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (Config.isFeatureEnabled("teleport")) {
            new BukkitRunnable() { // Because when you join, PlayerTeleportEvent is also called
                @Override
                public void run() {
                    if (LastLocations.getDeathLocation(e.getPlayer()) != null) {
                        deathLocations.put(e.getPlayer(), LastLocations.getDeathLocation(e.getPlayer()));
                        // We'll remove the last death location when the player has joined since it's one time use. Also saves space.
                        LastLocations.deleteDeathLocation(e.getPlayer());
                    } else {
                        lastLocations.put(e.getPlayer(), LastLocations.getLocation(e.getPlayer()));
                    }

                }
            }.runTaskLater(Main.getInstance(), 10);
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
        if (Config.isFeatureEnabled("teleport")) {
            lastLocations.put(e.getPlayer(), e.getFrom());
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        if (Config.isFeatureEnabled("teleport")) {
            deathLocations.put(e.getEntity(), e.getEntity().getLocation());
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        if (Config.isFeatureEnabled("teleport")) {
            new BukkitRunnable() { // They also call PlayerTeleportEvent when you respawn
                @Override
                public void run() {
                    if (deathLocations.get(e.getPlayer()) != null) {
                        lastLocations.put(e.getPlayer(), deathLocations.get(e.getPlayer()));
                        deathLocations.remove(e.getPlayer());
                    }
                }
            }.runTaskLater(Main.getInstance(), 10);
        }
    }

    public static Location getLastLocation(Player player) {
        return lastLocations.get(player);
    }

    public static HashMap<Player, Location> getLastLocations() {
        return lastLocations;
    }

    public static HashMap<Player, Location> getDeathLocations() {
        return deathLocations;
    }

    public static Location getDeathLocation(Player player) {
        return deathLocations.get(player);
    }
}
