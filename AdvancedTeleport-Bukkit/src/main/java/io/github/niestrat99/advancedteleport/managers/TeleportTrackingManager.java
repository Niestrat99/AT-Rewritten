package io.github.niestrat99.advancedteleport.managers;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.api.AdvancedTeleportAPI;
import io.github.niestrat99.advancedteleport.api.Warp;
import io.github.niestrat99.advancedteleport.api.events.ATTeleportEvent;
import io.github.niestrat99.advancedteleport.api.Spawn;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.MainConfig;
import io.github.niestrat99.advancedteleport.utilities.ConditionChecker;
import io.github.thatsmusic99.configurationmaster.api.ConfigSection;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

public class TeleportTrackingManager implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {

        // If it's an NPC, we don't care
        if (e.getPlayer().hasMetadata("NPC")) return;

        // Get the player being used
        final var player = e.getPlayer();

        // If the player doesn't need to teleport to any spawnpoints, stop there
        if (player.hasPermission("at.admin.bypass.teleport-on-join")) return;

        // If the player hasn't played before and needs teleporting, go there
        if (!player.hasPlayedBefore() && MainConfig.get().TELEPORT_TO_SPAWN_FIRST.get()) {

            final String name = MainConfig.get().FIRST_SPAWN_POINT.get();
            final Spawn spawn = AdvancedTeleportAPI.getSpawn(name);

            // If the spawn exists, go there, otherwise alert the admins
            if (spawn != null) {
                spawn(player, spawn);
                return;
            }
            CoreClass.getInstance()
                    .getLogger()
                    .warning("First-join teleport point " + name + " does not exist.");
        }

        // If the player has played before but needs to be sent to spawn every login, go there
        if (MainConfig.get().TELEPORT_TO_SPAWN_EVERY.get()) {
            final Spawn spawn = AdvancedTeleportAPI.getDestinationSpawn(player.getWorld(), player);
            spawn(player, spawn);
        }
    }

    private void spawn(Player player, Spawn spawn) {
        Bukkit.getScheduler()
                .runTaskLater(
                        CoreClass.getInstance(),
                        () ->
                                ATPlayer.teleportWithOptions(
                                                player,
                                                spawn.getLocation(),
                                                PlayerTeleportEvent.TeleportCause.PLUGIN)
                                        .whenComplete(
                                                (result, err) -> {
                                                    if (!result)
                                                        CoreClass.getInstance()
                                                                .getLogger()
                                                                .warning(
                                                                        "Failed to teleport "
                                                                                + player.getName()
                                                                                + " on joining.");
                                                }),
                        10L);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent e) {

        // If it's an NPC, skip over them
        if (e.getPlayer().hasMetadata("NPC")) return;

        // Get the results from teleportation
        String result = ConditionChecker.canTeleport(e.getFrom(), e.getTo(), null, e.getPlayer());
        if (result != null) {
            CustomMessages.sendMessage(
                    e.getPlayer(),
                    result,
                    Placeholder.unparsed("world", e.getTo().getWorld().getName()));
            e.setCancelled(true);
            return;
        }

        // If the player can /back to this location, then set their previous location to it
        if (MainConfig.get().USE_BASIC_TELEPORT_FEATURES.get()
                && MainConfig.get().BACK_TELEPORT_CAUSES.get().contains(e.getCause().name())) {
            ATPlayer.getPlayer(e.getPlayer()).setPreviousLocation(e.getFrom());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTeleport(ATTeleportEvent e) {

        // If it's not bound by the condition checker, ignore it
        if (!e.getType().isRestricted()) return;

        // If the player can't teleport, stop there
        String result =
                ConditionChecker.canTeleport(
                        e.getFromLocation(),
                        e.getToLocation(),
                        e.getType().getName(),
                        e.getPlayer());
        if (result != null) {
            CustomMessages.sendMessage(
                    e.getPlayer(),
                    result,
                    Placeholder.unparsed("world", e.getToLocation().getWorld().getName()));
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {

        // If it's an NPC, once again, couldn't care less
        if (e.getEntity().hasMetadata("NPC")) return;

        // If the player can have their death location set, then set it
        if (MainConfig.get().USE_BASIC_TELEPORT_FEATURES.get()
                && e.getEntity().hasPermission("at.member.back.death")) {
            ATPlayer.getPlayer(e.getEntity()).setPreviousLocation(e.getEntity().getLocation());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRespawn(PlayerRespawnEvent e) {

        // How many times do we need to go over this?
        if (e.getPlayer().hasMetadata("NPC")) return;

        // If the spawn feature is disabled, stop there
        if (!MainConfig.get().USE_SPAWN.get()) return;

        // Get the configuration section for death management
        ConfigSection deathManagement = MainConfig.get().DEATH_MANAGEMENT.get();

        // Get the previous location of the world, or the default option
        String spawnCommand = deathManagement.getString(e.getPlayer().getWorld().getName());

        // If one of those don't work, try the default option again
        if (spawnCommand == null || spawnCommand.equals("default")) {
            spawnCommand = deathManagement.getString("default");
            if (spawnCommand == null) return;
        }

        // Go through each commands until you reach jackpot
        for (String command : spawnCommand.split(";")) {
            if (handleSpawn(e, command)) break;
        }
    }

    private static boolean handleSpawn(
            @NotNull PlayerRespawnEvent e, @NotNull String spawnCommand) {

        // Get the base stuff
        final var atPlayer = ATPlayer.getPlayer(e.getPlayer());
        final var deathManagement = MainConfig.get().DEATH_MANAGEMENT.get();
        final var operatingWorld =
                (atPlayer.getPreviousLocation() == null
                        ? (AdvancedTeleportAPI.getMainSpawn() == null
                                ? Bukkit.getWorlds().get(0)
                                : AdvancedTeleportAPI.getMainSpawn().getLocation().getWorld())
                        : atPlayer.getPreviousLocation()
                                .getWorld()); // this should really be tidier

        // If the default option is being used, check there - if it's invalid or such, stop there
        if (spawnCommand.equals("default")) {
            spawnCommand = deathManagement.getString("default");
            if (spawnCommand == null) return false;
        }

        // If rapid response is enabled and tpr is being used, use that
        if (spawnCommand.startsWith("tpr") && MainConfig.get().RAPID_RESPONSE.get()) {

            var world = operatingWorld;

            // If a world has been specified, use that
            if (spawnCommand.indexOf(':') != -1) {
                String worldStr = spawnCommand.substring(spawnCommand.indexOf(':'));
                if (!worldStr.isEmpty()) {

                    // If the world doesn't exist, use the original operating one
                    world = Bukkit.getWorld(worldStr);
                    if (world == null) world = operatingWorld;
                }
            }

            // Get an RTP location from the world urgently
            Location loc = RTPManager.getLocationUrgently(world);

            // If one was found, use that
            if (loc != null) {
                e.setRespawnLocation(loc);
                return true;
            }
        }

        // If spawn was specified, use that
        if (spawnCommand.equals("spawn")) {
            Spawn spawn = AdvancedTeleportAPI.getDestinationSpawn(operatingWorld, e.getPlayer());
            e.setRespawnLocation(spawn.getLocation());
            return true;
        }

        // If home was specified, use that
        if (spawnCommand.equals("home")) {

            // If there's a main home, use that
            if (atPlayer.hasMainHome()) {
                e.setRespawnLocation(atPlayer.getMainHome().getLocation());
                return true;
            }

            // Get their first home
            if (!atPlayer.getHomes().isEmpty()) {
                e.setRespawnLocation(atPlayer.getHomes().values().iterator().next().getLocation());
                return true;
            }
        }

        // If a bed was specified, just use that
        if (spawnCommand.equals("bed")) return e.getPlayer().getBedSpawnLocation() != null;

        // If we're using warps, then get the warp to be used
        if (spawnCommand.startsWith("warp:")) {
            try {

                // Get the warp name from the spawn command
                final String warpName = spawnCommand.split(":")[1];
                final Warp warp = AdvancedTeleportAPI.getWarp(warpName);

                // If it exists, set it, otherwise, send a warning
                if (warp != null) {
                    e.setRespawnLocation(warp.getLocation());
                    return true;
                } else {
                    CoreClass.getInstance()
                            .getLogger()
                            .warning(
                                    "Unknown warp " + warpName + " for death in " + operatingWorld);
                }
            } catch (IndexOutOfBoundsException ex) {
                CoreClass.getInstance()
                        .getLogger()
                        .warning("Malformed warp name for death in " + operatingWorld);
            }
        }

        // If it's an anchor... weh
        return spawnCommand.equals("anchor");
    }
}
