package io.github.niestrat99.advancedteleport.managers;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.api.Warp;
import io.github.niestrat99.advancedteleport.api.events.ATTeleportEvent;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.config.Spawn;
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
        if (e.getPlayer().hasMetadata("NPC")) return;
        Player player = e.getPlayer();
        if (!player.hasPlayedBefore()) {
            if (NewConfig.get().TELEPORT_TO_SPAWN_FIRST.get()) {
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
        } else if (NewConfig.get().TELEPORT_TO_SPAWN_EVERY.get()) {
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
        if (e.getPlayer().hasMetadata("NPC")) return;
        String result = ConditionChecker.canTeleport(e.getFrom(), e.getTo(), null, e.getPlayer());
        if (!result.isEmpty()) {
            e.getPlayer().sendMessage(result);
            e.setCancelled(true);
            return;
        }
        if (NewConfig.get().USE_BASIC_TELEPORT_FEATURES.get()
                && NewConfig.get().BACK_TELEPORT_CAUSES.get().contains(e.getCause().name())) {
            ATPlayer.getPlayer(e.getPlayer()).setPreviousLocation(e.getFrom());
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
        if (e.getEntity().hasMetadata("NPC")) return;
        if (NewConfig.get().USE_BASIC_TELEPORT_FEATURES.get() && e.getEntity().hasPermission("at.member.back.death")) {
            ATPlayer.getPlayer(e.getEntity()).setPreviousLocation(e.getEntity().getLocation());
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        if (e.getPlayer().hasMetadata("NPC")) return;
        UUID uuid = e.getPlayer().getUniqueId();
        ATPlayer atPlayer = ATPlayer.getPlayer(e.getPlayer());
        if (NewConfig.get().USE_SPAWN.get()) {
            if (atPlayer.getPreviousLocation() == null) return;
            if (atPlayer.getPreviousLocation().getWorld() == null) return;
            ConfigurationSection deathManagement = NewConfig.get().DEATH_MANAGEMENT.get();
            String spawnCommand = deathManagement.getString(atPlayer.getPreviousLocation().getWorld().getName());
            if (spawnCommand == null) {
                spawnCommand = deathManagement.getString("default");
                if (spawnCommand == null) return;
            }
            switch (spawnCommand) {
                case "spawn":
                    if (Spawn.getSpawnFile() != null) {
                        e.setRespawnLocation(Spawn.getSpawnFile());
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
                            if (Warp.getWarps().containsKey(warp)) {
                                e.setRespawnLocation(Warp.getWarps().get(warp).getLocation());
                            } else {
                                CoreClass.getInstance().getLogger().warning("Unknown warp " + warp + " for death in " + deathLocations.get(uuid).getWorld());
                            }
                        } catch (IndexOutOfBoundsException ex) {
                            CoreClass.getInstance().getLogger().warning("Malformed warp name for death in " + deathLocations.get(uuid).getWorld());
                        }
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