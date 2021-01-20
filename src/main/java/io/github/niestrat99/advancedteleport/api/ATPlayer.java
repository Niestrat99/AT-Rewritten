package io.github.niestrat99.advancedteleport.api;

import io.github.niestrat99.advancedteleport.sql.BlocklistManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ATPlayer {

    private Player bukkitPlayer;
    private HashMap<String, Home> homes;

    public ATPlayer(Player player) {
        this(player.getUniqueId());
    }

    public ATPlayer(UUID uuid) {
        this.uuid = uuid;

        BlocklistManager.get().getBlockedPlayers(uuid.toString(), (list) -> this.blockedUsers = list);
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public void teleport(Location location) {

    }

    public boolean isBlocked(Player otherPlayer) {
        return blockedUsers.containsKey(otherPlayer.getUniqueId());
    }

    public BlockInfo getBlockInfo(Player otherPlayer) {
        return blockedUsers.get(otherPlayer.getUniqueId());
    }

}
