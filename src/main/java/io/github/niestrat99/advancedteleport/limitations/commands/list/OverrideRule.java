package io.github.niestrat99.advancedteleport.limitations.commands.list;

import io.github.niestrat99.advancedteleport.limitations.commands.CommandRule;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class OverrideRule extends CommandRule {

    public OverrideRule(String commandRule) {
        super(commandRule);
    }

    @Override
    public boolean canTeleport(Player player, Location toLoc) {
        String fromWorld = player.getLocation().getWorld().getName();
        // If inclusive (1) and contains world (0), allow
        // If not inclusive (0) and contains world (0), deny
        // If not inclusive (0) and doesn't contain world (1), allow
        // If inclusive (1) and doesn't contain world (1), deny
        String toWorld = toLoc.getWorld().getName();
        if (inclusive ^ !worlds.contains(fromWorld)) {
            return true;
        }
        return (inclusive ^ !worlds.contains(">" + toWorld));
    }
}
