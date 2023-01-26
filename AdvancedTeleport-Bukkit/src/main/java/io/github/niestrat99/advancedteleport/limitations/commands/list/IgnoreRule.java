package io.github.niestrat99.advancedteleport.limitations.commands.list;

import io.github.niestrat99.advancedteleport.limitations.commands.CommandRule;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a rule that leads to a teleportation being cancelled depending on the world context.
 */
public final class IgnoreRule extends CommandRule {

    public IgnoreRule(@NotNull final String commandRule) {
        super(commandRule);
    }

    @Override
    public boolean canTeleport(
        @NotNull final Player player,
        @NotNull final Location toLoc
    ) {

        // If the worlds aren't loaded, stop there
        if (player.getLocation().getWorld() == null) return false;
        if (toLoc.getWorld() == null) return false;

        // Get the world the player is currently in as well as the destination world.
        String fromWorld = player.getLocation().getWorld().getName();
        String toWorld = toLoc.getWorld().getName();

        // If they're in the same world, don't worry about it
        if (toWorld.equals(fromWorld)) return true;

        // If inclusive (1) and contains world (0), deny
        // If not inclusive (0) and contains world (0), allow
        // If not inclusive (0) and doesn't contain world (1), deny
        // If inclusive (1) and doesn't contain world (1), allow
        if (inclusive ^ !worlds.contains(fromWorld)) return false;
        return inclusive ^ worlds.contains(">" + toWorld);
    }
}
