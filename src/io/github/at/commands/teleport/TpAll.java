package io.github.at.commands.teleport;

import io.github.at.config.Config;
import io.github.at.config.CustomMessages;
import io.github.at.config.TpBlock;
import io.github.at.events.CooldownManager;
import io.github.at.main.CoreClass;
import io.github.at.utilities.DistanceLimiter;
import io.github.at.utilities.TPRequest;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class TpAll implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (sender instanceof Player) {
            if (Config.isFeatureEnabled("teleport")) {
                if (sender.hasPermission("at.admin.all")) {
                    Player player = (Player) sender;
                    UUID playerUuid = player.getUniqueId();
                    if (CooldownManager.getCooldown().containsKey(playerUuid)) {
                        sender.sendMessage(CustomMessages.getString("Error.onCooldown").replaceAll("\\{time}", String.valueOf(Config.commandCooldown())));
                        return false;
                    }
                    int players = 0;
                    for (Player target : Bukkit.getOnlinePlayers()) {
                        if (target != player) {
                            UUID targetUuid = target.getUniqueId();
                            if (TpOff.getTpOff().contains(targetUuid)) {
                                continue;
                            }
                            if (TpBlock.getBlockedPlayers(target).contains(playerUuid)) {
                                continue;
                            }
                            if (!DistanceLimiter.canTeleport(player.getLocation(), target.getLocation(), "tpahere") && !target.hasPermission("at.admin.bypass.distance-limit")) {
                                continue;
                            }
                            players++;
                            target.sendMessage(CustomMessages.getString("Info.tpaRequestHere")
                                    .replaceAll("\\{player}", sender.getName())
                                    .replaceAll("\\{lifetime}", String.valueOf(Config.requestLifetime())));
                            BukkitRunnable run = new BukkitRunnable() {
                                @Override
                                public void run() {
                                    TPRequest.removeRequest(TPRequest.getRequestByReqAndResponder(target, player));
                                }
                            };
                            run.runTaskLater(CoreClass.getInstance(), Config.requestLifetime() * 20); // 60 seconds
                            TPRequest request = new TPRequest(player, target, run, TPRequest.TeleportType.TPA_HERE); // Creates a new teleport request.
                            TPRequest.addRequest(request);
                            BukkitRunnable cooldowntimer = new BukkitRunnable() {
                                @Override
                                public void run() {
                                    CooldownManager.getCooldown().remove(playerUuid);
                                }
                            };
                            CooldownManager.getCooldown().put(player.getUniqueId(), cooldowntimer);
                            cooldowntimer.runTaskLater(CoreClass.getInstance(), Config.commandCooldown() * 20); // 20 ticks = 1 second
                        }
                    }
                    if (players > 0) {
                        player.sendMessage(CustomMessages.getString("Info.tpallRequestSent").replaceAll("\\{amount}", String.valueOf(players)));
                    } else {
                        player.sendMessage(CustomMessages.getString("Error.noRequestsSent"));
                    }
                }

            }
        } else {
            sender.sendMessage(CustomMessages.getString("Error.notAPlayer"));
        }
        return false;
    }
}
