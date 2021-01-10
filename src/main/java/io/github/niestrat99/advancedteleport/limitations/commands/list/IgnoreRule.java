package io.github.niestrat99.advancedteleport.limitations.commands.list;

import io.github.niestrat99.advancedteleport.limitations.commands.CommandRule;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class IgnoreRule extends CommandRule {

    public IgnoreRule(String commandRule) {
        super(commandRule);
    }

    @Override
    public boolean canTeleport(Player player, Location toLoc) {
        String fromWorld = player.getLocation().getWorld().getName();
        // If inclusive (1) and contains world (0), deny
        // If not inclusive (0) and contains world (0), allow
        // If not inclusive (0) and doesn't contain world (1), deny
        // If inclusive (1) and doesn't contain world (1), allow
        String toWorld = toLoc.getWorld().getName();

        if (toWorld.equals(fromWorld)) {
            return true;
        }
        if (inclusive ^ !worlds.contains(fromWorld)) {
            return false;

        }
        return inclusive ^ !worlds.contains(">" + toWorld);
    }
}
