package io.github.at.events;

import io.github.at.config.Config;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.HashMap;

public class TeleportTrackingManager implements Listener {

    private static HashMap<Player, Location> lastLocations = new HashMap<>();

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        if (Config.isFeatureEnabled("teleport")) {
            lastLocations.put(e.getPlayer(), e.getFrom());
        }
    }

    public static Location getLastLocation(Player player) {
        return lastLocations.get(player);
    }

    public static HashMap<Player, Location> getLastLocations() {
        return lastLocations;
    }
}
