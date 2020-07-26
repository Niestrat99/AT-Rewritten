package io.github.at.events;

import io.github.at.api.ATTeleportEvent;
import io.github.at.config.*;
import io.github.at.main.CoreClass;
import io.github.at.utilities.ConditionChecker;
import io.github.at.utilities.DistanceLimiter;
import org.bukkit.Location;
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

    private static final HashMap<UUID, Location> lastLocations = new HashMap<>();
    // This needs a separate hashmap because players may not immediately click "Respawn".
    private static final HashMap<UUID, Location> deathLocations = new HashMap<>();

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
            }.runTaskLater(CoreClass.getInstance(), 10);
        }
        if (!e.getPlayer().hasPlayedBefore()) {
            if (Config.spawnTPOnFirstJoin()) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (Spawn.getSpawnFile() != null) {
                            e.getPlayer().teleport(Spawn.getSpawnFile());
                        } else {
                            e.getPlayer().teleport(e.getPlayer().getWorld().getSpawnLocation());
                        }
                    }
                }.runTaskLater(CoreClass.getInstance(), 10);
            }
        } else if (Config.spawnTPEveryTime()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (Spawn.getSpawnFile() != null) {
                        e.getPlayer().teleport(Spawn.getSpawnFile());
                    } else {
                        e.getPlayer().teleport(e.getPlayer().getWorld().getSpawnLocation());
                    }
                }
            }.runTaskLater(CoreClass.getInstance(), 10);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent e) {
        String result = ConditionChecker.canTeleport(e.getFrom(), e.getTo(), null, e.getPlayer());
        if (!result.isEmpty()) {
            e.getPlayer().sendMessage(result);
            e.setCancelled(true);
            return;
        }
        if (Config.isFeatureEnabled("teleport") && Config.isCauseAllowed(e.getCause())) {
            lastLocations.put(e.getPlayer().getUniqueId(), e.getFrom());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTeleport(ATTeleportEvent e) {
        String result = ConditionChecker.canTeleport(e.getFromLocation(), e.getToLocation(), null, e.getPlayer());
        if (!result.isEmpty()) {
            e.getPlayer().sendMessage(result);
            e.setCancelled(true);
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
        UUID uuid = e.getPlayer().getUniqueId();
        if (Config.isFeatureEnabled("teleport")) {
            new BukkitRunnable() { // They also call PlayerTeleportEvent when you respawn
                @Override
                public void run() {

                    if (deathLocations.get(uuid) != null) {
                        lastLocations.put(uuid, deathLocations.get(uuid));
                        deathLocations.remove(uuid);
                    }
                }
            }.runTaskLater(CoreClass.getInstance(), 10);
        }
        String spawnCommand = Config.getSpawnCommand(deathLocations.get(uuid).getWorld());
        switch (spawnCommand) {
            case "spawn":
                if (Spawn.getSpawnFile() != null) {
                    e.setRespawnLocation(Spawn.getSpawnFile());
                } else {
                    e.setRespawnLocation(e.getPlayer().getWorld().getSpawnLocation());
                }
                break;
            case "bed":
                if (!e.isBedSpawn() && e.getPlayer().getBedSpawnLocation() != null) {
                    e.setRespawnLocation(e.getPlayer().getBedSpawnLocation());
                }
                break;
            default:
                if (spawnCommand.startsWith("warp:")) {
                    try {
                        String warp = spawnCommand.split(":")[1];
                        if (Warps.getWarps().containsKey(warp)) {
                            e.setRespawnLocation(Warps.getWarps().get(warp));
                        } else {
                            CoreClass.getInstance().getLogger().warning("Unknown warp " + warp + " for death in " + deathLocations.get(uuid).getWorld());
                        }
                    } catch (IndexOutOfBoundsException ex) {
                        CoreClass.getInstance().getLogger().warning("Malformed warp name for death in " + deathLocations.get(uuid).getWorld());
                    }
                }
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
