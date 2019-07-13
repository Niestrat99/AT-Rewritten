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
import org.bukkit.permissions.PermissionAttachmentInfo;

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
                                // We'll just assume that the admin command overrides the homes limit.
                                if (args.length>1) {
                                    Player target = Bukkit.getOfflinePlayer(args[0]).getPlayer();
                                    setHome(target, args[1]);
                                } else {
                                    sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ERROR: " + ChatColor.RED + "You have to include the home name!");
                                    return false;
                                }
                            }
                        }
                        // If the number of homes a player has is smaller than or equal to the homes limit
                        if (Homes.getHomes(player).size() <= getHomesLimit(player) || player.hasPermission("at.admin.sethome.bypass")) {
                            setHome(player, args[0]);
                        } else {
                            sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ERROR: " + ChatColor.RED + "You can't set any more homes!");
                        }

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

    // Used to get the permission for how many homes a player can have.
    // If there is no permission, then it's assumed that the number of homes they can have is limitless (-1).
    // E.g.: at.member.homes.5
    // at.member.homes.40
    // at.member.homes.100000
    private int getHomesLimit(Player player) {
        for (PermissionAttachmentInfo permission : player.getEffectivePermissions()) {
            if (permission.getPermission().startsWith("at.member.homes.")) {
                String perm = permission.getPermission();
                return Integer.parseInt(perm.substring(perm.lastIndexOf(".") + 1));
            }
        }
        return -1;
    }
}
