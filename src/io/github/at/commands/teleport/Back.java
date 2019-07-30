package io.github.at.commands.teleport;

import io.github.at.config.Config;
import io.github.at.config.CustomMessages;
import io.github.at.config.LastLocations;
import io.github.at.events.MovementManager;
import io.github.at.events.TeleportTrackingManager;
import io.github.at.main.Main;
import io.github.at.utilities.DistanceLimiter;
import io.github.at.utilities.PaymentManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class Back implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if (Config.isFeatureEnabled("teleport")) {
            if (sender.hasPermission("at.member.back")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    Location loc = TeleportTrackingManager.getLastLocation(player);
                    if (loc == null) {
                        loc = LastLocations.getLocation(player);
                        if (loc == null) {
                            sender.sendMessage(CustomMessages.getString("Error.noLocation"));
                            return false;
                        }
                    }
                    while (loc.getBlock().getType() != Material.AIR && loc.getBlock().getType() != Material.WATER) {
                        loc.add(0.0, 1.0, 0.0);
                    }
                    if (!DistanceLimiter.canTeleport(player.getLocation(), loc, "back") && !player.hasPermission("at.admin.bypass.distance-limit")) {
                        player.sendMessage(CustomMessages.getString("Error.tooFarAway"));
                        return false;
                    }
                    Location finalLoc = loc;
                    if (PaymentManager.canPay("back", player)) {
                        if (Config.getTeleportTimer("back") > 0) {
                            BukkitRunnable movementtimer = new BukkitRunnable() {
                                @Override
                                public void run() {
                                    TeleportTrackingManager.getLastLocations().put(player, player.getLocation());
                                    player.teleport(finalLoc);
                                    MovementManager.getMovement().remove(player);
                                    player.sendMessage(CustomMessages.getString("Teleport.teleportingToLastLoc"));
                                    PaymentManager.withdraw("back", player);

                                }
                            };
                            MovementManager.getMovement().put(player, movementtimer);
                            movementtimer.runTaskLater(Main.getInstance(), Config.getTeleportTimer("back")*20);
                            player.sendMessage(CustomMessages.getString("Teleport.eventBeforeTP").replaceAll("\\{countdown}" , String.valueOf(Config.getTeleportTimer("back"))));

                        } else {
                            TeleportTrackingManager.getLastLocations().put(player, player.getLocation());
                            player.teleport(loc);
                            PaymentManager.withdraw("back", player);
                            player.sendMessage(CustomMessages.getString("Teleport.teleportingToLastLoc"));
                        }
                    }
                } else {
                    sender.sendMessage(CustomMessages.getString("Error.notAPlayer"));
                }
            } else {
                sender.sendMessage(CustomMessages.getString("Error.noPermission"));
                return false;
            }
        } else {
            sender.sendMessage(CustomMessages.getString("Error.featureDisabled"));
            return false;
        }
        return false;
    }
}
