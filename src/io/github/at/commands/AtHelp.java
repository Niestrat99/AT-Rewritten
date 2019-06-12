package io.github.at.commands;

import io.github.at.config.Config;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AtHelp implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (player.hasPermission("at.member.help")) {
                if (args.length == 0) {
                    sender.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "AdvancedTeleport Help");
                    sender.sendMessage(ChatColor.GOLD + "Please type " + ChatColor.AQUA + "/athelp <category>" + ChatColor.GOLD + " to get a command list of one of these categories.");
                    sender.sendMessage(ChatColor.AQUA + "--[" + ChatColor.GOLD + "Categories" + ChatColor.AQUA + "]--");
                    if (Config.featTP()) {
                        sender.sendMessage(ChatColor.GOLD + "- Teleport");
                    }
                    if (Config.featWarps()) {
                        sender.sendMessage(ChatColor.GOLD + "- Warps");
                    }
                    if (Config.featSpawn()) {
                        sender.sendMessage(ChatColor.GOLD + "- Spawn");
                    }
                    if (Config.featRTP()) {
                        sender.sendMessage(ChatColor.GOLD + "- RandomTP");
                    }
                    if (Config.featHomes()) {
                        sender.sendMessage(ChatColor.GOLD + "- Homes");
                    }
                    return false;
                } else if (args[0].equalsIgnoreCase("teleport")) {
                    if (Config.featTP()) {
                        sender.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "Teleport help");
                        sender.sendMessage(ChatColor.GOLD + "- /tpa <player> - Sends a request to teleport to the player.");
                        sender.sendMessage(ChatColor.GOLD + "- /tpahere <player> - Sends a request to the player to teleport to you");
                        sender.sendMessage(ChatColor.GOLD + "- /tpaccept - Accepts a player's teleport request.");
                        sender.sendMessage(ChatColor.GOLD + "- /tpdeny - Declines a player's teleport request.");
                        sender.sendMessage(ChatColor.GOLD + "- /tpcancel - Lets you cancel the request you have sent to a player.");
                        sender.sendMessage(ChatColor.GOLD + "- /tpon - Enables teleport requests to you.");
                        sender.sendMessage(ChatColor.GOLD + "- /tpoff - Disables teleport requests to you.");
                        sender.sendMessage(ChatColor.GOLD + "- /tpblock <player> - Blocks the player so that they cannot send you teleport requests anymore.");
                        sender.sendMessage(ChatColor.GOLD + "- /tpunblock <player> - Unblocks the player so that they can send you teleport requests.");
                        if (sender.hasPermission("tbh.tp.admin.help")) {
                            sender.sendMessage(ChatColor.GOLD + "- /tpo <player> - Instantly teleports you to the player.");
                            sender.sendMessage(ChatColor.GOLD + "- /tpohere <player> - Instantly teleports the player to you.");
                            sender.sendMessage(ChatColor.GOLD + "- /tpall - Sends a teleport request to every online player to teleport to you.");
                            return false;
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ERROR: " + ChatColor.RED + "The feature " + ChatColor.GOLD + "Teleport " + ChatColor.RED + "is disabled!");
                        return false;
                    }
                } else if (args[0].equalsIgnoreCase("warps")) {
                    if (Config.featWarps()) {
                        sender.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "Warps help");
                        sender.sendMessage(ChatColor.GOLD + "- /warp <warp name> - Teleports you to an existing warp point.");
                        sender.sendMessage(ChatColor.GOLD + "- /warps - Gives you a list of warps.");
                        if (sender.hasPermission("tbh.tp.admin.help")) {
                            sender.sendMessage(ChatColor.GOLD + "- /warp set <warp name> - Sets a warp point at your location.");
                            sender.sendMessage(ChatColor.GOLD + "- /warp delete <warp name> - Deletes an existing warp point.");
                            return false;
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ERROR: " + ChatColor.RED + "The feature " + ChatColor.GOLD + "Warps " + ChatColor.RED + "is disabled!");
                        return false;
                    }
                } else if (args[0].equalsIgnoreCase("Spawn")) {
                    if (Config.featSpawn()) {
                        sender.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "Spawn help");
                        sender.sendMessage(ChatColor.GOLD + "- /spawn - Teleports you to the spawn point.");
                        if (sender.hasPermission("tbh.tp.admin.help")) {
                            sender.sendMessage(ChatColor.GOLD + "- /setspawn - Sets a spawn point at your location.");
                            return false;
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ERROR: " + ChatColor.RED + "The feature " + ChatColor.GOLD + "Spawn " + ChatColor.RED + "is disabled!");
                        return false;
                    }
                } else if (args[0].equalsIgnoreCase("RandomTP")) {
                    if (Config.featRTP()) {
                        sender.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "RandomTP help");
                        sender.sendMessage(ChatColor.GOLD + "- /rtp - Teleports you to a random location.");
                        return false;
                    } else {
                        sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ERROR: " + ChatColor.RED + "The feature " + ChatColor.GOLD + "RandomTP " + ChatColor.RED + "is disabled!");
                        return false;
                    }
                } else if (args[0].equalsIgnoreCase("Homes")) {
                    if (Config.featHomes()) {
                        sender.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "Homes help");
                        sender.sendMessage(ChatColor.GOLD + "- /sethome <home name> - Sets a home point at your location.");
                        sender.sendMessage(ChatColor.GOLD + "- /delhome <home name> - Deletes a home point you've set.");
                        sender.sendMessage(ChatColor.GOLD + "- /home <home name> - Teleports you to your home.");
                        sender.sendMessage(ChatColor.GOLD + "- /homes - Gives you a list of homes you've set.");
                        if (sender.hasPermission("tbh.tp.admin.help")) {
                            sender.sendMessage(ChatColor.GOLD + "- /sethome <player> <home name> - Sets a home point at your location for the player.");
                            sender.sendMessage(ChatColor.GOLD + "- /delhome <player> <home name> - Deletes a home point of a player.");
                            sender.sendMessage(ChatColor.GOLD + "- /home <player> <home name> - Teleports you to a home point a player has set.");
                            sender.sendMessage(ChatColor.GOLD + "- /homes <player> - Gives you a list of homes of a player.");
                            return false;
                        }
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ERROR: " + ChatColor.RED + "This category does not exist! Type /athelp to get a list of existing categories.");
                    return false;
                }

            }
        } return false;

    }
}
