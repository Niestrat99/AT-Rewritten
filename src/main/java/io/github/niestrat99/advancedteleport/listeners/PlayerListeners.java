package io.github.niestrat99.advancedteleport.listeners;

import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.sql.PlayerSQLManager;
import io.github.niestrat99.advancedteleport.utilities.nbt.NBTReader;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListeners implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (event.getPlayer().hasMetadata("NPC")) return;
        ATPlayer.getPlayer(event.getPlayer());
        PlayerSQLManager.get().updatePlayerData(event.getPlayer());
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        if (event.getPlayer().hasMetadata("NPC")) return;
        ATPlayer.removePlayer(event.getPlayer());
        NBTReader.addLeaveToCache(event.getPlayer());
    }


}
