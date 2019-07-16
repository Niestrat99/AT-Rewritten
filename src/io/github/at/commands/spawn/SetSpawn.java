package io.github.at.commands.spawn;

import io.github.at.config.Config;
import io.github.at.config.CustomMessages;
import io.github.at.config.Spawn;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;

public class SetSpawn implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (Config.isFeatureEnabled("spawn")) {
            if (commandSender.hasPermission("at.admin.setspawn")){
                if (commandSender instanceof Player) {
                    Player player = (Player) commandSender;
                    Location spawn = player.getLocation();
                    try {
                        Spawn.setSpawn(spawn);
                        commandSender.sendMessage(ChatColor.GREEN + "Successfully set the spawn location!");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                commandSender.sendMessage(CustomMessages.getString("Error.noPermission"));
                return false;
            }
        } else {
            commandSender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ERROR: " + ChatColor.RED + "The feature " + ChatColor.GOLD + "Spawn " + ChatColor.RED + "is disabled!");
            return false;
        }
        return false;
    }
}
