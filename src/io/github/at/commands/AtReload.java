package io.github.at.commands;

import io.github.at.config.Config;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.io.IOException;

public class AtReload implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!sender.hasPermission("tbh.tp.admin.reload")) {
            sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ERROR:" + ChatColor.RED + " You do not have permission to use this command!");
            return false;
        } else {
            sender.sendMessage(ChatColor.GOLD + "Reloading Config of " + ChatColor.AQUA + "AdvancedTeleport" + ChatColor.GOLD + "...");
            try {
                Config.reloadConfig();
                // TODO reload custom-messages.yml
            } catch (IOException e) {
                e.printStackTrace();
            }
            sender.sendMessage(ChatColor.GREEN + "Done!");
        }
        return false;
    }
}
