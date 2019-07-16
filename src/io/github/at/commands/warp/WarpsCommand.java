package io.github.at.commands.warp;

import io.github.at.config.Config;
import io.github.at.config.CustomMessages;
import io.github.at.config.Warps;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class WarpsCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (Config.isFeatureEnabled("warps")) {
            if (commandSender.hasPermission("tbh.tp.member.warps")){
                StringBuilder wList = new StringBuilder();
                wList.append(ChatColor.AQUA + "" + ChatColor.BOLD + "Warps: " + ChatColor.YELLOW);
                for (String warp: Warps.getWarps().keySet()) {
                    wList.append(warp + ", ");
                }
                commandSender.sendMessage(wList.toString());
            }
        } else {
            commandSender.sendMessage(CustomMessages.getString("Error.featureDisabled"));
            return false;
        }
        return false;
    }
}
