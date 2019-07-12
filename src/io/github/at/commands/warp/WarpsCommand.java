package io.github.at.commands.warp;

import io.github.at.config.Config;
import io.github.at.config.Warps;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class WarpsCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (Config.featWarps()) {
            if (commandSender.hasPermission("tbh.tp.member.warps")){
                StringBuilder wList = new StringBuilder();
                wList.append(ChatColor.AQUA + "" + ChatColor.BOLD + "WarpsCommand: " + ChatColor.YELLOW);
                for (String warp: Warps.getWarps().keySet()) {
                    wList.append(warp + ", ");
                }
                commandSender.sendMessage(wList.toString());
            }
        } else {
            commandSender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ERROR: " + ChatColor.RED + "The feature " + ChatColor.GOLD + "WarpsCommand " + ChatColor.RED + "is disabled!");
            return false;
        }
        return false;
    }
}
