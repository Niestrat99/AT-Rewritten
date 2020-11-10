package io.github.niestrat99.advancedteleport.events;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.ATTeleportEvent;
import io.github.niestrat99.advancedteleport.config.Config;
import io.github.niestrat99.advancedteleport.config.LastLocations;
import io.github.niestrat99.advancedteleport.config.Spawn;
import io.github.niestrat99.advancedteleport.config.Warps;
import io.github.niestrat99.advancedteleport.utilities.ConditionChecker;
import io.papermc.lib.PaperLib;
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

    private static final HashMap<UUID, Location> lastLocations = new HashMap<>();
    // This needs a separate hashmap because players may not immediately click "Respawn".
    private static final HashMap<UUID, Location> deathLocations = new HashMap<>();

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        if (Config.isFeatureEnabled("teleport")) {
            new BukkitRunnable() { // Because when you join, PlayerTeleportEvent is also called
                @Override
                public void run() {
                    if (LastLocations.getDeathLocation(player) != null) {
                        deathLocations.put(player.getUniqueId(), LastLocations.getDeathLocation(player));
                        // We'll remove the last death location when the player has joined since it's one time use. Also saves space.
                        LastLocations.deleteDeathLocation(player);
                    } else {
                        lastLocations.put(player.getUniqueId(), LastLocations.getLocation(player));
                    }

                }
            }.runTaskLater(CoreClass.getInstance(), 10);
        }
        if (!player.hasPlayedBefore()) {
            if (Config.spawnTPOnFirstJoin()) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (Spawn.getSpawnFile() != null) {
                            PaperLib.teleportAsync(player, Spawn.getSpawnFile());
                        } else {
                            PaperLib.teleportAsync(player, player.getWorld().getSpawnLocation());
                        }
                    }
                }.runTaskLater(CoreClass.getInstance(), 10);
            }
        } else if (Config.spawnTPEveryTime()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (Spawn.getSpawnFile() != null) {
                        PaperLib.teleportAsync(player, Spawn.getSpawnFile());
                    } else {
                        PaperLib.teleportAsync(player, player.getWorld().getSpawnLocation());
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
        if (e.getType().isRestricted()) {
            String result = ConditionChecker.canTeleport(e.getFromLocation(), e.getToLocation(), e.getType().getName(), e.getPlayer());
            if (!result.isEmpty()) {
                e.getPlayer().sendMessage(result);
                e.setCancelled(true);
            }
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
        if (Config.isFeatureEnabled("spawn")) {
            if (deathLocations.get(uuid) == null) return;
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
