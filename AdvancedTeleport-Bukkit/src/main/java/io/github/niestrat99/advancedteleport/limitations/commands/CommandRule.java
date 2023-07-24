package io.github.niestrat99.advancedteleport.limitations.commands;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Represents a teleportation rule controlled by the command. */
public abstract class CommandRule {

    protected final @NotNull List<String> worlds;
    protected boolean inclusive;

    protected CommandRule(@NotNull String commandRule) {

        // Create the worlds list.
        worlds = new ArrayList<>();

        // If we're including worlds, mark it as inclusive; if not, just remove the first symbol.
        if (commandRule.contains(":")) {
            inclusive = true;
            commandRule = commandRule.replaceFirst(":", "");
        } else if (commandRule.contains("!")) {
            commandRule = commandRule.replaceFirst("!", "");
        }

        // Add all the worlds to
        worlds.addAll(Arrays.asList(commandRule.split(",")));
    }

    /**
     * Determines whether the player can teleport to a location depending on the rule at hand.
     *
     * @param player the player being teleported.
     * @param toLoc the destination location.
     * @return true if the player can teleport to the destination location, false if not.
     */
    public abstract boolean canTeleport(
            @NotNull final Player player, @NotNull final Location toLoc);
}
