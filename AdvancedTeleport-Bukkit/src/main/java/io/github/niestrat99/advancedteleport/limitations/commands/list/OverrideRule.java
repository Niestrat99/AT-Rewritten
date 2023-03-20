package io.github.niestrat99.advancedteleport.limitations.commands.list;

import io.github.niestrat99.advancedteleport.limitations.commands.CommandRule;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a rule that leads to a teleportation being forced depending on the world context.
 */
public final class OverrideRule extends CommandRule {

    public OverrideRule(@NotNull final String commandRule) {
        super(commandRule);
    }

    @Override
    public boolean canTeleport(
        @NotNull final Player player,
        @NotNull final Location toLoc
    ) {

        // Get the world the player is currently in as well as the destination world.
        String fromWorld = player.getLocation().getWorld().getName();
        String toWorld = toLoc.getWorld().getName();

        // If the world is the same, don't try to override it
        if (toWorld.equals(fromWorld)) return false;

        // If inclusive (1) and contains world (0), allow
        // If not inclusive (0) and contains world (0), deny
        // If not inclusive (0) and doesn't contain world (1), allow
        // If inclusive (1) and doesn't contain world (1), deny
        if (inclusive ^ !worlds.contains(fromWorld)) return true;
        return inclusive ^ !worlds.contains(">" + toWorld);
    }
}
