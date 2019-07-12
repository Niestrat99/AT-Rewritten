package io.github.at.commands.home;

import io.github.at.config.Config;
import io.github.at.config.CustomMessages;
import io.github.at.config.Homes;
import io.github.at.events.MovementManager;
import io.github.at.main.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class Home implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (Config.isFeatureEnabled("homes")) {
            if (sender.hasPermission("tbh.tp.member.home")) {
                if (sender instanceof Player) {
                    Player player = (Player)sender;
                    if (args.length>0) {
                        if (Bukkit.getPlayer(args[0]) != null) {
                            if (sender.hasPermission("tbh.tp.admin.home")) {
                                if (args.length > 1) {
                                    Player target = Bukkit.getOfflinePlayer(args[0]).getPlayer();
                                    try {
                                        if (Homes.getHomes(target).containsKey(args[1])) {
                                            Location tlocation = Homes.getHomes(target).get(args[1]);
                                            player.teleport(tlocation);
                                            sender.sendMessage(ChatColor.GREEN + "Successfully teleported you to " + ChatColor.GOLD + args[0] + ChatColor.GREEN + "'s home!");
                                            return false;
                                        } else if (args[1].equalsIgnoreCase("bed")) {
                                            Location location = player.getBedSpawnLocation();
                                            if (location == null) {
                                                player.sendMessage(ChatColor.RED + "This player doesn't have any bed spawn set!");
                                                return false;
                                            }

                                        } else {
                                            sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ERROR: " + ChatColor.RED + "This home does not exist!");
                                            return false;
                                        }
                                    } catch (NullPointerException ex) {
                                        Location tlocation = Homes.getHomes(target).get(args[1]);
                                        player.teleport(tlocation);
                                        sender.sendMessage(ChatColor.GREEN + "Successfully teleported you to " + ChatColor.GOLD + args[0] + ChatColor.GREEN + "'s home!");
                                        return false;
                                    }
                                } else {
                                    sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ERROR: " + ChatColor.RED + "You have to include the home name!");
                                    return false;
                                }
                            }
                        }
                        try {
                            if (Homes.getHomes(player).containsKey(args[0])) {
                                Location location = Homes.getHomes(player).get(args[0]);
                                teleport(player, location);
                                return false;
                            } else if (args[0].equalsIgnoreCase("bed")) {
                                Location location = player.getBedSpawnLocation();
                                if (location == null) {
                                    player.sendMessage(ChatColor.RED + "You don't have any bed spawn set!");
                                    return false;
                                }
                                teleport(player, location);
                                return false;

                            } else {
                                sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ERROR: " + ChatColor.RED + "This home does not exist!");
                            }
                        } catch (NullPointerException ex) {
                            Location location = Homes.getHomes(player).get(args[0]);
                            teleport(player, location);
                            return false;
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

    private void teleport(Player player, Location loc) {
        BukkitRunnable movementtimer = new BukkitRunnable() {
            @Override
            public void run() {
                player.teleport(loc);
                MovementManager.getMovement().remove(player);
                player.sendMessage(ChatColor.GREEN + "Successfully teleported to your home!");
            }
        };
        MovementManager.getMovement().put(player, movementtimer);
        movementtimer.runTaskLater(Main.getInstance(), Config.teleportTimer() * 20);
        player.sendMessage(CustomMessages.getString("Teleport.eventBeforeTP").replaceAll("\\{countdown}", String.valueOf(Config.teleportTimer())));

    }

}
