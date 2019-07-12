package io.github.at.commands.home;

import io.github.at.config.Config;
import io.github.at.config.Homes;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;

public class DelHome implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (Config.isFeatureEnabled("homes")) {
            if (sender instanceof Player) {
                Player player = (Player)sender;
                if (sender.hasPermission("tbh.tp.member.delhome")) {
                    if (args.length>0) {
                        if (Bukkit.getPlayer(args[0]) != null) {
                            if (sender.hasPermission("tbh.tp.admin.delhome")) {
                                if (args.length>1) {
                                    Player target = Bukkit.getOfflinePlayer(args[0]).getPlayer();
                                    delHome(target, args[1]);
                                } else {
                                    sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ERROR: " + ChatColor.RED + "You have to include the home name!");
                                    return false;
                                }
                            }
                        }
                        delHome(player, args[0]);
                    } else {
                        sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ERROR: " + ChatColor.RED + "You have to include the home name!");
                        return false;
                    }
                }
            }
        } else {
            sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ERROR: " + ChatColor.RED + "The feature " + ChatColor.GOLD + "Homes " + ChatColor.RED + "is disabled!");
            return false;
        }
        return false;
    }

    private void delHome(Player player, String name) {
        try {
            if (Homes.getHomes(player).containsKey(name)) {
                try {
                    Homes.delHome(player, name);
                    player.sendMessage(ChatColor.GREEN + "Successfully deleted the home " + ChatColor.GOLD + name + ChatColor.GREEN + "!");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ERROR: " + ChatColor.RED + "This home does not exist!");
            }
        } catch (NullPointerException ex) {
            try {
                Homes.delHome(player, name);
                player.sendMessage(ChatColor.GREEN + "Successfully deleted the home " + ChatColor.GOLD + name + ChatColor.GREEN + "!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
