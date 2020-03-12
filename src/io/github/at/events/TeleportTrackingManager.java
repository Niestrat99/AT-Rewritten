package io.github.at.events;

import io.github.at.config.Config;
import io.github.at.config.CustomMessages;
import io.github.at.config.LastLocations;
import io.github.at.main.Main;
import io.github.at.utilities.DistanceLimiter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

public class TeleportTrackingManager implements Listener {

    private static HashMap<UUID, Location> lastLocations = new HashMap<>();
    // This needs a separate hashmap because players may not immediately click "Respawn".
    private static HashMap<UUID, Location> deathLocations = new HashMap<>();

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (Config.isFeatureEnabled("teleport")) {
            new BukkitRunnable() { // Because when you join, PlayerTeleportEvent is also called
                @Override
                public void run() {
                    if (LastLocations.getDeathLocation(e.getPlayer()) != null) {
                        deathLocations.put(e.getPlayer().getUniqueId(), LastLocations.getDeathLocation(e.getPlayer()));
                        // We'll remove the last death location when the player has joined since it's one time use. Also saves space.
                        LastLocations.deleteDeathLocation(e.getPlayer());
                    } else {
                        lastLocations.put(e.getPlayer().getUniqueId(), LastLocations.getLocation(e.getPlayer()));
                    }

                }
            }.runTaskLater(Main.getInstance(), 10);
        }

    }
    @EventHandler(priority = EventPriority.MONITOR)
    public void onTeleport(PlayerTeleportEvent e) {
        if (Config.hasStrictDistanceMonitor()) {
            if (!DistanceLimiter.canTeleport(e.getTo(), e.getFrom(), null) && !e.getPlayer().hasPermission("at.admin.bypass.distance-limit")) {
                e.getPlayer().sendMessage(CustomMessages.getString("Error.tooFarAway"));
                e.setCancelled(true);
                return;
            }
        }
        if (Config.isFeatureEnabled("teleport") && !e.isCancelled() && Config.isCauseAllowed(e.getCause())) {
            lastLocations.put(e.getPlayer().getUniqueId(), e.getFrom());
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        if (Config.isFeatureEnabled("teleport") && e.getEntity().hasPermission("at.member.back.death")) {
            deathLocations.put(e.getEntity().getUniqueId(), e.getEntity().getLocation());
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        if (Config.isFeatureEnabled("teleport")) {
            LastLocations.saveLocations();
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        if (Config.isFeatureEnabled("teleport")) {
            new BukkitRunnable() { // They also call PlayerTeleportEvent when you respawn
                @Override
                public void run() {
                    UUID uuid = e.getPlayer().getUniqueId();
                    if (deathLocations.get(uuid) != null) {
                        lastLocations.put(uuid, deathLocations.get(uuid));
                        deathLocations.remove(uuid);
                    }
                }
            }.runTaskLater(Main.getInstance(), 10);
        }
    }

    public static Location getLastLocation(UUID uuid) {
        return lastLocations.get(uuid);
    }

    public static HashMap<UUID, Location> getLastLocations() {
        return lastLocations;
    }

    public static HashMap<UUID, Location> getDeathLocations() {
        return deathLocations;
    }

    public static Location getDeathLocation(UUID uuid) {
        return deathLocations.get(uuid);
    }
}
