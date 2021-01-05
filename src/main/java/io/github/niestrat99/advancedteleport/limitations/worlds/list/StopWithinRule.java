package io.github.niestrat99.advancedteleport.limitations.worlds.list;

import io.github.niestrat99.advancedteleport.limitations.worlds.WorldRule;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class StopWithinRule extends WorldRule {

    public StopWithinRule(String worldRule) {
        super(worldRule);
    }

    @Override
    public boolean canTeleport(Player player, Location toLoc) {
        return !player.getWorld().getName().equals(toLoc.getWorld().getName());
    }
}
