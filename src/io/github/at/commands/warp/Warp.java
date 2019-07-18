package io.github.at.commands.warp;

import io.github.at.config.Config;
import io.github.at.config.CustomMessages;
import io.github.at.config.Warps;
import io.github.at.events.MovementManager;
import io.github.at.main.Main;
import io.github.at.utilities.PaymentManager;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;

public class Warp implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (Config.isFeatureEnabled("warps")) {
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("set")) {
                    if (sender.hasPermission("at.admin.warpset")) {
                        if (sender instanceof Player) {
                            Player player = (Player) sender;
                            Location warp = player.getLocation();
                            if (args.length > 1) {
                                try {
                                    Warps.setWarp(args[1], warp);
                                    sender.sendMessage(CustomMessages.getString("Info.setWarp").replaceAll("\\{warp}", args[1]));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            } else {
                                sender.sendMessage(CustomMessages.getString("Error.noWarpInput"));
                                return false;
                            }
                        }
                    } else {
                        sender.sendMessage(CustomMessages.getString("Error.noPermission"));
                        return false;
                    }
                } else if (args[0].equalsIgnoreCase("delete")) {
                    if (sender.hasPermission("at.admin.warpdel")) {
                        if (args.length > 1) {
                            if (Warps.getWarps().containsKey(args[1])) {
                                try {
                                    Warps.delWarp(args[1]);
                                    sender.sendMessage(CustomMessages.getString("Info.deletedWarp").replaceAll("\\{warp}", args[1]));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                sender.sendMessage(CustomMessages.getString("Error.noSuchWarp"));
                            }
                        } else {
                            sender.sendMessage(CustomMessages.getString("Error.noWarpInput"));
                            return false;
                        }
                    } else {
                        sender.sendMessage(CustomMessages.getString("Error.noPermission"));
                        return false;
                    }
                } else if (sender.hasPermission("at.member.warp")) {
                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        if (Warps.getWarps().containsKey(args[0])) {
                            if (PaymentManager.canPay("warp", player)) {
                                Location warp = Warps.getWarps().get(args[0]);
                                if (Config.getTeleportTimer("warp") > 0) {
                                    BukkitRunnable movementtimer = new BukkitRunnable() {
                                        @Override
                                        public void run() {
                                            player.teleport(warp);
                                            MovementManager.getMovement().remove(player);
                                            player.sendMessage(CustomMessages.getString("Teleport.eventTeleport"));
                                            PaymentManager.withdraw("warp", player);

                                        }
                                    };
                                    MovementManager.getMovement().put(player, movementtimer);
                                    movementtimer.runTaskLater(Main.getInstance(), Config.getTeleportTimer("tpahere")*20);
                                    player.sendMessage(CustomMessages.getString("Teleport.eventBeforeTP").replaceAll("\\{countdown}" , String.valueOf(Config.getTeleportTimer("tpahere"))));

                                } else {
                                    player.teleport(warp);
                                    PaymentManager.withdraw("warp", player);
                                    sender.sendMessage(CustomMessages.getString("Teleport.teleportingToWarp").replaceAll("\\{warp}", args[0]));
                                }
                            }
                        } else {
                            sender.sendMessage(CustomMessages.getString("Error.noSuchWarp"));
                            return false;
                        }
                    } else {
                        sender.sendMessage(CustomMessages.getString("Error.notAPlayer"));
                    }
                } else {
                    sender.sendMessage(CustomMessages.getString("Error.noPermission"));
                    return false;
                }
            } else {
                sender.sendMessage(CustomMessages.getString("Error.noWarpInput"));
                return false;
            }

        } else {
            sender.sendMessage(CustomMessages.getString("Error.featureDisabled"));
            return false;
        }
        return false;
    }
}
