package io.github.niestrat99.advancedteleport.limitations.worlds.list;

import io.github.niestrat99.advancedteleport.limitations.worlds.WorldRule;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class StopIntoRule extends WorldRule {

    public StopIntoRule(String worldRule) {
        super(worldRule);
    }

    @Override
    public boolean canTeleport(Player player, Location toLoc) {
        String fromWorld = player.getLocation().getWorld().getName();
        if (worlds.isEmpty()) {
            return toLoc.getWorld().getName().equals(fromWorld);
        }
        // If inclusive (1) and contains world (0), allow
        // If not inclusive (0) and contains world (0), deny
        // If not inclusive (0) and doesn't contain world (1), allow
        // If inclusive (1) and doesn't contain world (1), deny
        return inclusive ^ worlds.contains(fromWorld);
    }
}
