package io.github.niestrat99.advancedteleport.commands.warp;

import io.github.niestrat99.advancedteleport.config.Warps;
import io.github.niestrat99.advancedteleport.api.ATTeleportEvent;
import io.github.niestrat99.advancedteleport.config.Config;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.events.MovementManager;
import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.utilities.DistanceLimiter;
import io.github.niestrat99.advancedteleport.utilities.PaymentManager;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
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
                                if (args[1].matches("^[a-zA-Z0-9]+$")) {
                                    try {
                                        Warps.setWarp(args[1], warp);
                                        sender.sendMessage(CustomMessages.getString("Info.setWarp").replaceAll("\\{warp}", args[1]));
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    sender.sendMessage(CustomMessages.getString("Error.invalidName"));
                                }
                            } else {
                                sender.sendMessage(CustomMessages.getString("Error.noWarpInput"));
                            }
                        }
                    } else {
                        sender.sendMessage(CustomMessages.getString("Error.noPermission"));
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
                        }
                    } else {
                        sender.sendMessage(CustomMessages.getString("Error.noPermission"));
                    }
                } else if (sender.hasPermission("at.member.warp")) {
                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        if (Warps.getWarps().containsKey(args[0])) {
                            if (MovementManager.getMovement().containsKey(player.getUniqueId())) {
                                player.sendMessage(CustomMessages.getString("Error.onCountdown"));
                                return true;
                            }
                            Location warp = Warps.getWarps().get(args[0]);
                            warp(warp, player, args[0]);
                        } else {
                            sender.sendMessage(CustomMessages.getString("Error.noSuchWarp"));
                        }
                    } else {
                        sender.sendMessage(CustomMessages.getString("Error.notAPlayer"));
                    }
                } else {
                    sender.sendMessage(CustomMessages.getString("Error.noPermission"));
                }
            } else {
                sender.sendMessage(CustomMessages.getString("Error.noWarpInput"));
            }

        } else {
            sender.sendMessage(CustomMessages.getString("Error.featureDisabled"));
        }
        return true;
    }

    public static void warp(Location loc, Player player, String name) {
        if (!DistanceLimiter.canTeleport(player.getLocation(), loc, "warp") && !player.hasPermission("at.admin.bypass.distance-limit")) {
            player.sendMessage(CustomMessages.getString("Error.tooFarAway"));
            return;
        }

        boolean found = false;
        if (player.hasPermission("at.member.warp.*")) found = true;
        for (PermissionAttachmentInfo permission : player.getEffectivePermissions()) {
            if (permission.getPermission().equalsIgnoreCase("at.member.warp." + name)) {
                found = permission.getValue();
                break;
            }
        }
        if (!found) {
            player.sendMessage(CustomMessages.getString("Error.noPermissionWarp").replaceAll("\\{warp}", name));
            return;
        }
        ATTeleportEvent event = new ATTeleportEvent(player, loc, player.getLocation(), name, ATTeleportEvent.TeleportType.WARP);
        if (!event.isCancelled()) {
            if (PaymentManager.canPay("warp", player)) {

                if (Config.getTeleportTimer("warp") > 0 && !player.hasPermission("at.admin.bypass.timer")) {
                    BukkitRunnable movementtimer = new BukkitRunnable() {
                        @Override
                        public void run() {
                            player.teleport(loc);
                            MovementManager.getMovement().remove(player.getUniqueId());
                            player.sendMessage(CustomMessages.getString("Teleport.teleportingToWarp").replaceAll("\\{warp}", name));
                            PaymentManager.withdraw("warp", player);

                        }
                    };
                    MovementManager.getMovement().put(player.getUniqueId(), movementtimer);
                    movementtimer.runTaskLater(CoreClass.getInstance(), Config.getTeleportTimer("warp")*20);
                    player.sendMessage(CustomMessages.getEventBeforeTPMessage().replaceAll("\\{countdown}" , String.valueOf(Config.getTeleportTimer("warp"))));

                } else {
                    player.teleport(loc);
                    PaymentManager.withdraw("warp", player);
                    player.sendMessage(CustomMessages.getString("Teleport.teleportingToWarp").replaceAll("\\{warp}", name));
                }
            }
        }
    }
}
