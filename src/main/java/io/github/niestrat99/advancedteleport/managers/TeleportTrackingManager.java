package io.github.niestrat99.advancedteleport.managers;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.ATTeleportEvent;
import io.github.niestrat99.advancedteleport.config.*;
import io.github.niestrat99.advancedteleport.utilities.ConditionChecker;
import io.papermc.lib.PaperLib;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
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
        if (NewConfig.getInstance().USE_BASIC_TELEPORT_FEATURES.get()) {
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
            if (NewConfig.getInstance().TELEPORT_TO_SPAWN_FIRST.get()) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (Spawn.getSpawnFile() != null) {
                            PaperLib.teleportAsync(player, Spawn.getSpawnFile(), PlayerTeleportEvent.TeleportCause.COMMAND);
                        } else {
                            PaperLib.teleportAsync(player, player.getWorld().getSpawnLocation(), PlayerTeleportEvent.TeleportCause.COMMAND);
                        }
                    }
                }.runTaskLater(CoreClass.getInstance(), 10);
            }
        } else if (NewConfig.getInstance().TELEPORT_TO_SPAWN_EVERY.get()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (Spawn.getSpawnFile() != null) {
                        PaperLib.teleportAsync(player, Spawn.getSpawnFile(), PlayerTeleportEvent.TeleportCause.COMMAND);
                    } else {
                        PaperLib.teleportAsync(player, player.getWorld().getSpawnLocation(), PlayerTeleportEvent.TeleportCause.COMMAND);
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
        if (NewConfig.getInstance().USE_BASIC_TELEPORT_FEATURES.get()
                && NewConfig.getInstance().BACK_TELEPORT_CAUSES.get().contains(e.getCause().name())) {
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
        if (NewConfig.getInstance().USE_BASIC_TELEPORT_FEATURES.get() && e.getEntity().hasPermission("at.member.back.death")) {
            deathLocations.put(e.getEntity().getUniqueId(), e.getEntity().getLocation());
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        if (NewConfig.getInstance().USE_BASIC_TELEPORT_FEATURES.get()) {
            LastLocations.saveLocations();
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        if (NewConfig.getInstance().USE_SPAWN.get()) {
            if (deathLocations.get(uuid) == null) return;
            ConfigurationSection deathManagement = NewConfig.getInstance().DEATH_MANAGEMENT.get();
            String spawnCommand = deathManagement.getString(deathLocations.get(uuid).getWorld().getName());
            if (spawnCommand == null) {
                spawnCommand = deathManagement.getString("default");
                if (spawnCommand == null) return;
            }
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
                case "anchor":
                    // Vanilla just handles that
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
        if (NewConfig.getInstance().USE_BASIC_TELEPORT_FEATURES.get()) {
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
