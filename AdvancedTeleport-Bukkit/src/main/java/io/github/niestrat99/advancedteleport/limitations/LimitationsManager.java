package io.github.niestrat99.advancedteleport.limitations;

import io.github.niestrat99.advancedteleport.limitations.commands.CommandRuleManager;
import io.github.niestrat99.advancedteleport.limitations.worlds.WorldRulesManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class LimitationsManager {

    private static WorldRulesManager worldMan;
    private static CommandRuleManager commandMan;

    public static void init() {
        worldMan = new WorldRulesManager();
        commandMan = new CommandRuleManager();
    }

    public static boolean canTeleport(Player player, Location toLoc, String command) {
        int commandResponse = commandMan.canTeleport(player, toLoc, command);
        if (commandResponse == 0) {
            return worldMan.canTeleport(player, toLoc);
        } else {
            return commandResponse == 1;
        }
    }
}
