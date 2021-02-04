package io.github.niestrat99.advancedteleport.listeners;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.sql.PlayerSQLManager;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerListeners implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        ATPlayer.getPlayer(event.getPlayer());
        PlayerSQLManager.get().getLocation(event.getPlayer().getName(), location -> {
            if (location.distance(event.getPlayer().getLocation()) > 1) {
                System.out.println("A");
                Bukkit.getScheduler().runTask(CoreClass.getInstance(), () -> {
                    // Being sneaky with the other random teleports so /back doesn't pick it up
                    PaperLib.teleportAsync(event.getPlayer(), location, PlayerTeleportEvent.TeleportCause.PLUGIN);
                });

            }
        });
        PlayerSQLManager.get().updatePlayerData(event.getPlayer());
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        PlayerSQLManager.get().movePlayer(event.getPlayer().getName(), event.getPlayer().getLocation(), null);
        ATPlayer.removePlayer(event.getPlayer());
    }


}
