package io.github.at.commands.home;

import io.github.at.config.Config;
import io.github.at.config.CustomMessages;
import io.github.at.config.Homes;
import io.github.at.events.MovementManager;
import io.github.at.events.TeleportTrackingManager;
import io.github.at.main.Main;
import io.github.at.utilities.DistanceLimiter;
import io.github.at.utilities.PaymentManager;
import org.bukkit.Bukkit;
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
            if (sender.hasPermission("at.member.home")) {
                if (sender instanceof Player) {
                    Player player = (Player)sender;
                    if (args.length>0) {
                        if (Bukkit.getPlayer(args[0]) != null) {
                            if (sender.hasPermission("at.admin.home")) {
                                if (args.length > 1) {
                                    Player target = Bukkit.getOfflinePlayer(args[0]).getPlayer();
                                    try {
                                        if (Homes.getHomes(target).containsKey(args[1])) {
                                            Location tlocation = Homes.getHomes(target).get(args[1]);
                                            TeleportTrackingManager.getLastLocations().put(player, player.getLocation());
                                            player.teleport(tlocation);
                                            sender.sendMessage(CustomMessages.getString("Info.teleportingToHomeOther")
                                                    .replaceAll("\\{player}", target.getName())
                                                    .replaceAll("\\{home}", args[1]));
                                            return false;
                                        } else if (args[1].equalsIgnoreCase("bed")) {
                                            Location location = player.getBedSpawnLocation();
                                            if (location == null) {
                                                player.sendMessage(CustomMessages.getString("Error.noBedHomeOther").replaceAll("\\{player}", target.getName()));
                                                return false;
                                            }

                                        } else if (args[1].equalsIgnoreCase("list")) {
                                            if (sender.hasPermission("at.admin.homes")) {
                                                StringBuilder hlist = new StringBuilder();
                                                hlist.append(CustomMessages.getString("Info.homesOther").replaceAll("\\{player}", player.getName()));
                                                if (Bukkit.getPlayer(args[0]) != null) {
                                                    try {
                                                        if (Homes.getHomes(player).size()>0) {
                                                            for (String home: Homes.getHomes(player).keySet()) {
                                                                hlist.append(home + ", ");
                                                            }
                                                        } else {
                                                            sender.sendMessage(CustomMessages.getString("Error.noHomesOther").replaceAll("\\{player}", player.getName()));
                                                            return false;
                                                        }

                                                    } catch (NullPointerException ex) {
                                                        sender.sendMessage(CustomMessages.getString("Error.noHomesOther").replaceAll("\\{player}", player.getName()));
                                                        return false;
                                                    }
                                                    sender.sendMessage(hlist.toString());
                                                    return false;
                                                }
                                            }
                                        } else {
                                            sender.sendMessage(CustomMessages.getString("Error.noSuchHome"));
                                            return false;
                                        }
                                    } catch (NullPointerException ex) {
                                        Location tlocation = Homes.getHomes(target).get(args[1]);
                                        TeleportTrackingManager.getLastLocations().put(player, player.getLocation());
                                        player.teleport(tlocation);
                                        sender.sendMessage(CustomMessages.getString("Teleport.teleportingToHomeOther")
                                                .replaceAll("\\{player}", target.getName().replaceAll("\\{home}", args[1])));
                                        return false;
                                    }
                                } else {
                                    sender.sendMessage(CustomMessages.getString("Error.noHomeInput"));
                                    return false;
                                }
                            }
                        }
                        if (PaymentManager.canPay("home", player)) {
                            try {
                                if (Homes.getHomes(player).containsKey(args[0])) {
                                    Location location = Homes.getHomes(player).get(args[0]);
                                    TeleportTrackingManager.getLastLocations().put(player, player.getLocation());
                                    teleport(player, location, args[0]);
                                    return false;
                                } else if (args[0].equalsIgnoreCase("bed")) {
                                    Location location = player.getBedSpawnLocation();
                                    if (location == null) {
                                        player.sendMessage(CustomMessages.getString("Error.noBedHome"));
                                        return false;
                                    }
                                    TeleportTrackingManager.getLastLocations().put(player, player.getLocation());
                                    teleport(player, location, args[0]);
                                    return false;

                                } else if (args[0].equalsIgnoreCase("list")) {
                                    StringBuilder hlist = new StringBuilder();
                                    hlist.append(CustomMessages.getString("Info.homes"));
                                    try {
                                        if (Homes.getHomes(player).size()>0){
                                            for (String home: Homes.getHomes(player).keySet()) {
                                                hlist.append(home + ", ");
                                            }
                                        } else {
                                            sender.sendMessage(CustomMessages.getString("Error.noHomes"));
                                            return false;
                                        }
                                    } catch (NullPointerException ex) { // If a player has never set any homes
                                        sender.sendMessage(CustomMessages.getString("Error.noHomes"));
                                        return false;
                                    }
                                    sender.sendMessage(hlist.toString());
                                } else {
                                    sender.sendMessage(CustomMessages.getString("Error.noSuchHome"));
                                }
                            } catch (NullPointerException ex) {
                                Location location = Homes.getHomes(player).get(args[0]);
                                teleport(player, location, args[0]);
                                return false;
                            }
                        }

                    } else {
                        sender.sendMessage(CustomMessages.getString("Error.noHomeInput"));
                        return false;
                    }
                } else {
                    sender.sendMessage(CustomMessages.getString("Error.notAPlayer"));
                }
            }
        } else {
            sender.sendMessage(CustomMessages.getString("Error.featureDisabled"));
            return false;
        }
        return false;
    }

    private void teleport(Player player, Location loc, String name) {
        if (!DistanceLimiter.canTeleport(player.getLocation(), loc, "home") && !player.hasPermission("at.admin.bypass.distance-limit")) {
            player.sendMessage(CustomMessages.getString("Error.tooFarAway"));
            return;
        }
        if (PaymentManager.canPay("home", player)) {
            if (Config.getTeleportTimer("home") > 0) {
                BukkitRunnable movementtimer = new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.sendMessage(CustomMessages.getString("Teleport.teleportingToHome").replaceAll("\\{home}",name));
                        TeleportTrackingManager.getLastLocations().put(player, player.getLocation());
                        player.teleport(loc);
                        MovementManager.getMovement().remove(player);
                        PaymentManager.withdraw("home", player);
                    }
                };
                MovementManager.getMovement().put(player, movementtimer);
                movementtimer.runTaskLater(Main.getInstance(), Config.getTeleportTimer("home") * 20);
                player.sendMessage(CustomMessages.getString("Teleport.eventBeforeTP").replaceAll("\\{countdown}", String.valueOf(Config.getTeleportTimer("home"))));

            } else {
                player.sendMessage(CustomMessages.getString("Teleport.teleportingToHome").replaceAll("\\{home}",name));
                TeleportTrackingManager.getLastLocations().put(player, player.getLocation());
                player.teleport(loc);
                PaymentManager.withdraw("home", player);
            }
        }

    }

}
