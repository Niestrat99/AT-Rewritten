package io.github.niestrat99.advancedteleport.api;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class ATPlayer {

    private Player bukkitPlayer;
    private HashMap<String, Home> homes;

    public ATPlayer(Player player) {
        bukkitPlayer = player;
    }

    public void teleport(Location location) {

    }

    public boolean isBlocked(Player otherPlayer) {

    }

}
