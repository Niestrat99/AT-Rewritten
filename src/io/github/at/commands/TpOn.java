package io.github.at.commands;

import io.github.at.config.Config;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TpOn implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player)sender;
            if (Config.featTP()) {
                if (sender.hasPermission("tbh.tp.member.on")) {
                    if (TpOff.getTpOff().contains(player)) {
                        TpOff.getTpOff().remove(player);
                        sender.sendMessage(ChatColor.GREEN + "Successfully enabled teleport requests!");
                        sender.sendMessage(ChatColor.GREEN + "You can now receive teleport requests.");
                    }
                }
            } else {
                sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ERROR: " + ChatColor.RED + "The feature " + ChatColor.GOLD + "Teleport " + ChatColor.RED + "is disabled!");
                return false;
            }
        }
        return false;
    }
}
