package io.github.at.commands.home;

import io.github.at.config.Config;
import io.github.at.config.Homes;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;

public class SetHome implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (Config.isFeatureEnabled("homes")) {
            if (sender instanceof Player) {
                Player player = (Player)sender;
                if (sender.hasPermission("tbh.tp.member.sethome")) {
                    if (args.length>0) {
                        if (Bukkit.getPlayer(args[0]) != null) {
                            if (sender.hasPermission("tbh.tp.admin.sethome")) {
                                if (args.length>1) {
                                    Player target = Bukkit.getOfflinePlayer(args[0]).getPlayer();
                                    setHome(target, args[1]);
                                } else {
                                    sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ERROR: " + ChatColor.RED + "You have to include the home name!");
                                    return false;
                                }
                            }
                        }
                        setHome(player, args[0]);
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

    // Separated this into a separate method so that the code is easier to read.
    // Player player - the player which is having the home set.
    // String name - the name of the home.
    private void setHome(Player player, String name) {
        Location home = player.getLocation();
        try {
            if (Homes.getHomes(player).containsKey(name)) {
                player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ERROR: " + ChatColor.RED + "You already have a home named " + ChatColor.GOLD + name + ChatColor.RED + "!");
            } else {
                try {
                    Homes.setHome(player, name, home);
                    player.sendMessage(ChatColor.GREEN + "Successfully set the home " + ChatColor.GOLD + name + ChatColor.GREEN + "!");
                } catch (IOException e) {
                    e.getStackTrace();
                }
            }
        } catch (NullPointerException ex) {
            try {
                Homes.setHome(player, name,home);
                player.sendMessage(ChatColor.GREEN + "Successfully set the home " + ChatColor.GOLD + name + ChatColor.GREEN + "!");
            } catch (IOException e) {
                e.getStackTrace();
            }
        }
    }
}
