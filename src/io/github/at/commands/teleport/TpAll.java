package io.github.at.commands.teleport;

import io.github.at.config.Config;
import io.github.at.config.CustomMessages;
import io.github.at.config.TpBlock;
import io.github.at.events.CooldownManager;
import io.github.at.main.Main;
import io.github.at.utilities.DistanceLimiter;
import io.github.at.utilities.TPRequest;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class TpAll implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (sender instanceof Player) {
            if (Config.isFeatureEnabled("teleport")) {
                if (sender.hasPermission("at.admin.all")) {
                    Player player = (Player) sender;
                    if (CooldownManager.getCooldown().containsKey(player)) {
                        sender.sendMessage(ChatColor.RED + "This command has a cooldown of " + Config.commandCooldown() + " seconds each use - Please wait!");
                        return false;
                    }
                    int players = 0;
                    for (Player target : Bukkit.getOnlinePlayers()) {
                        if (target != player) {
                            if (TpOff.getTpOff().contains(target)) {
                                continue;
                            }
                            if (TpBlock.getBlockedPlayers(target).contains(player)) {
                                continue;
                            }
                            if (!DistanceLimiter.canTeleport(player.getLocation(), target.getLocation(), "tpahere") && !target.hasPermission("at.admin.bypass.distance-limit")) {
                                continue;
                            }
                            players++;
                            target.sendMessage(CustomMessages.getString("Info.tpaRequestHere")
                                    .replaceAll("\\{player}", target.getName())
                                    .replaceAll("\\{lifetime}", String.valueOf(Config.requestLifetime())));
                            BukkitRunnable run = new BukkitRunnable() {
                                @Override
                                public void run() {
                                    TPRequest.removeRequest(TPRequest.getRequestByReqAndResponder(target, player));
                                }
                            };
                            run.runTaskLater(Main.getInstance(), Config.requestLifetime() * 20); // 60 seconds
                            TPRequest request = new TPRequest(player, target, run, TPRequest.TeleportType.TPA_HERE); // Creates a new teleport request.
                            TPRequest.addRequest(request);
                            BukkitRunnable cooldowntimer = new BukkitRunnable() {
                                @Override
                                public void run() {
                                    CooldownManager.getCooldown().remove(player);
                                }
                            };
                            CooldownManager.getCooldown().put(player, cooldowntimer);
                            cooldowntimer.runTaskLater(Main.getInstance(), Config.commandCooldown() * 20); // 20 ticks = 1 second
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
