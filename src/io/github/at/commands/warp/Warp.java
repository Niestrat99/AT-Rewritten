package io.github.at.commands.warp;

import io.github.at.config.Config;
import io.github.at.config.Warps;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;

public class Warp implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (Config.featWarps()) {
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("set")) {
                    if (sender.hasPermission("tbh.tp.admin.warpset")) {
                        if (sender instanceof Player) {
                            Player player = (Player) sender;
                            Location warp = player.getLocation();
                            if (args.length > 1) {
                                try {
                                    Warps.setWarp(args[1], warp);
                                    sender.sendMessage(ChatColor.GREEN + "Successfully created the warp " + ChatColor.YELLOW + args[1] + ChatColor.GREEN + "!");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            } else {
                                sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ERROR:" + ChatColor.RED + "You have to give this warp a name!");
                                return false;
                            }
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ERROR:" + ChatColor.RED + " You do not have permission to use this command!");
                        return false;
                    }
                } else if (args[0].equalsIgnoreCase("delete")) {
                    if (sender.hasPermission("tbh.tp.admin.warpdel")) {
                        if (args.length > 1) {
                            if (Warps.getWarps().containsKey(args[1])) {
                                try {
                                    sender.sendMessage(ChatColor.GREEN + "Successfully deleted the warp " + ChatColor.YELLOW + args[1] + ChatColor.GREEN + "!");
                                    Warps.delWarp(args[1]);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ERROR:" + ChatColor.RED + " This warp doesn't exist!");
                            }
                        } else {
                            sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ERROR:" + ChatColor.RED + " You have to include the warp's name!");
                            return false;
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ERROR:" + ChatColor.RED + " You do not have permission to use this command!");
                        return false;
                    }
                } else if (sender.hasPermission("tbh.tp.member.warp")) {
                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        if (Warps.getWarps().containsKey(args[0])) {
                            player.teleport(Warps.getWarps().get(args[0]));
                            sender.sendMessage(ChatColor.GREEN + "Successfully teleported to " + ChatColor.YELLOW + args[0] + ChatColor.GREEN + "!");
                        } else {
                            sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ERROR:" + ChatColor.RED + " This warp doesn't exist!");
                            return false;
                        }
                    }
                }
            } else {
                sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ERROR:" + ChatColor.RED + "You need to include an argument or warp name!");
                return false;
            }

        } else {
            sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ERROR: " + ChatColor.RED + "The feature " + ChatColor.GOLD + "WarpsCommand " + ChatColor.RED + "is disabled!");
            return false;
        }
        return false;
    }
}
