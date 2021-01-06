package io.github.niestrat99.advancedteleport.commands.warp;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.ATTeleportEvent;
import io.github.niestrat99.advancedteleport.config.Config;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.config.Warps;
import io.github.niestrat99.advancedteleport.events.CooldownManager;
import io.github.niestrat99.advancedteleport.events.MovementManager;
import io.github.niestrat99.advancedteleport.payments.PaymentManager;
import io.papermc.lib.PaperLib;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;

public class Warp implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (NewConfig.getInstance().USE_WARPS.get()) {
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
                        int cooldown = CooldownManager.secondsLeftOnCooldown("warp", player);
                        if (cooldown > 0) {
                            sender.sendMessage(CustomMessages.getString("Error.onCooldown").replaceAll("\\{time}", String.valueOf(cooldown)));
                            return true;
                        }
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
            if (PaymentManager.getInstance().canPay("warp", player)) {
                int warmUp = NewConfig.getInstance().WARM_UPS.WARP.get();
                if (warmUp > 0 && !player.hasPermission("at.admin.bypass.timer")) {
                    MovementManager.createMovementTimer(player, loc, "warp", "Teleport.teleportingToWarp", warmUp, "\\{warp}", name);
                    // If the cooldown is to be applied after request or accept (they are the same in the case of /warp), apply it now
                    String cooldownConfig = NewConfig.getInstance().APPLY_COOLDOWN_AFTER.get();
                    if(cooldownConfig.equalsIgnoreCase("request") || cooldownConfig.equalsIgnoreCase("accept")) {
                        CooldownManager.addToCooldown("warp", player);
                    }
                } else {
                    PaymentManager.getInstance().withdraw("warp", player);
                    PaperLib.teleportAsync(player, loc, PlayerTeleportEvent.TeleportCause.COMMAND);
                    player.sendMessage(CustomMessages.getString("Teleport.teleportingToWarp").replaceAll("\\{warp}", name));
                }
            }
        }
    }
}
