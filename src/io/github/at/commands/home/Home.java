package io.github.at.commands.home;

import io.github.at.config.Config;
import io.github.at.config.CustomMessages;
import io.github.at.config.Homes;
import io.github.at.events.MovementManager;
import io.github.at.main.Main;
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

                                        } else {
                                            sender.sendMessage(CustomMessages.getString("Error.noSuchHome"));
                                            return false;
                                        }
                                    } catch (NullPointerException ex) {
                                        Location tlocation = Homes.getHomes(target).get(args[1]);
                                        player.teleport(tlocation);
                                        sender.sendMessage(CustomMessages.getString("Info.teleportingToHomeOther")
                                                .replaceAll("\\{player}", target.getName().replaceAll("\\{home}", args[1])));
                                        return false;
                                    }
                                } else {
                                    sender.sendMessage(CustomMessages.getString("Error.noHomeInput"));
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
                                    player.sendMessage(CustomMessages.getString("Error.noBedHome"));
                                    return false;
                                }
                                teleport(player, location);
                                return false;

                            } else {
                                sender.sendMessage(CustomMessages.getString("Error.noSuchHome"));
                            }
                        } catch (NullPointerException ex) {
                            Location location = Homes.getHomes(player).get(args[0]);
                            teleport(player, location);
                            return false;
                        }
                    } else {
                        sender.sendMessage(CustomMessages.getString("Error.noHomeInput"));
                        return false;
                    }
                }
            }
        } else {
            sender.sendMessage(CustomMessages.getString("Error.featureDisabled"));
            return false;
        }
        return false;
    }

    private void teleport(Player player, Location loc) {
        if (PaymentManager.canPay("home", player)) {
            if (Config.getTeleportTimer("home") > 0) {
                BukkitRunnable movementtimer = new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.sendMessage(CustomMessages.getString("Info.teleportingToHome"));
                        player.teleport(loc);
                        MovementManager.getMovement().remove(player);
                        PaymentManager.withdraw("home", player);
                    }
                };
                MovementManager.getMovement().put(player, movementtimer);
                movementtimer.runTaskLater(Main.getInstance(), Config.teleportTimer() * 20);
                player.sendMessage(CustomMessages.getString("Teleport.eventBeforeTP").replaceAll("\\{countdown}", String.valueOf(Config.teleportTimer())));

            } else {
                player.sendMessage(CustomMessages.getString("Info.teleportingToHome"));
                player.teleport(loc);
                PaymentManager.withdraw("home", player);
            }
        }

    }

}
