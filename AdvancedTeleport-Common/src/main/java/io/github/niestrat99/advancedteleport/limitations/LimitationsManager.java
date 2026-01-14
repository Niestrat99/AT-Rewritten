package io.github.niestrat99.advancedteleport.limitations;

import io.github.niestrat99.advancedteleport.CoreAdvancedTeleport;
import io.github.niestrat99.advancedteleport.limitations.commands.CommandRuleManager;
import io.github.niestrat99.advancedteleport.limitations.worlds.WorldRulesManager;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class LimitationsManager {
    private LimitationsManager() {}

    private static WorldRulesManager worldMan;
    private static CommandRuleManager commandMan;

    public static void init() {
        worldMan = new WorldRulesManager();
        commandMan = new CommandRuleManager();
    }

    public static boolean canTeleport(
            @NotNull final Player player,
            @NotNull final Location toLoc,
            @NotNull final String command) {
        CoreAdvancedTeleport.debug(
                "Checking "
                        + player.getName()
                        + " to "
                        + CoreAdvancedTeleport.getShortLocation(toLoc)
                        + " with command "
                        + command);
        int commandResponse = commandMan.canTeleport(player, toLoc, command);
        if (commandResponse == 0) {
            return worldMan.canTeleport(player, toLoc);
        } else {
            return commandResponse == 1;
        }
    }
}
