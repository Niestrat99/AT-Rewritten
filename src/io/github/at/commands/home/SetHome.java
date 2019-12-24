package io.github.at.commands.home;

import io.github.at.config.Config;
import io.github.at.config.CustomMessages;
import io.github.at.config.Homes;
import org.bukkit.Bukkit;
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
                if (sender.hasPermission("at.member.sethome")) {
                    if (args.length>0) {
                        if (Bukkit.getPlayer(args[0]) != null) {
                            if (sender.hasPermission("at.admin.sethome")) {
                                // We'll just assume that the admin command overrides the homes limit.
                                if (args.length>1) {
                                    Player target = Bukkit.getOfflinePlayer(args[0]).getPlayer();
                                    setHome(target, args[1]);
                                }
                            }
                        }
                        // I don't really want to run this method twice if a player has a lot of permissions, so store it as an int
                        int limit = getHomesLimit(player);

                        // If the number of homes a player has is smaller than or equal to the homes limit, or they have a bypass permission
                        if (Homes.getHomes(player).size() < limit
                                || player.hasPermission("at.admin.sethome.bypass")
                                || limit == -1) {
                            setHome(player, args[0]);
                        } else {
                            sender.sendMessage(CustomMessages.getString("Error.reachedHomeLimit"));
                        }

                    } else {
                        sender.sendMessage(CustomMessages.getString("Error.noHomeInput"));
                        return false;
                    }
                }
            } else {
                sender.sendMessage(CustomMessages.getString("Error.notAPlayer"));
            }
        } else {
            sender.sendMessage(CustomMessages.getString("Error.featureDisabled"));
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
                player.sendMessage(CustomMessages.getString("Error.homeAlreadySet").replaceAll("\\{home}", name));
            } else {
                try {
                    Homes.setHome(player, name, home);
                    player.sendMessage(CustomMessages.getString("Info.setHome").replaceAll("\\{home}", name));
                } catch (IOException e) {
                    e.getStackTrace();
                }
            }
        } catch (NullPointerException ex) {
            try {
                Homes.setHome(player, name,home);
                player.sendMessage(CustomMessages.getString("Info.setHome").replaceAll("\\{home}", name));
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
