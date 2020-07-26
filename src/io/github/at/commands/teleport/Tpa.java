package io.github.at.commands.teleport;

import io.github.at.config.Config;
import io.github.at.config.CustomMessages;
import io.github.at.events.CooldownManager;
import io.github.at.events.MovementManager;
import io.github.at.main.CoreClass;
import io.github.at.utilities.ConditionChecker;
import io.github.at.utilities.PaymentManager;
import io.github.at.utilities.TPRequest;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;


public class Tpa implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (Config.isFeatureEnabled("teleport")) {
                if (sender.hasPermission("at.member.tpa")) {
                    UUID playerUuid = player.getUniqueId();
                    if (CooldownManager.getCooldown().containsKey(playerUuid) && !player.hasPermission("at.admin.bypass.cooldown")) {
                        sender.sendMessage(CustomMessages.getString("Error.onCooldown").replaceAll("\\{time}", String.valueOf(Config.commandCooldown())));
                        return true;
                    }
                    if (MovementManager.getMovement().containsKey(playerUuid)) {
                        player.sendMessage(CustomMessages.getString("Error.onCountdown"));
                        return true;
                    }
                    if (args.length > 0) {
                        Player target = Bukkit.getPlayer(args[0]);
                        String result = ConditionChecker.canTeleport(player, target, "tpa");
                        if (result.isEmpty()) {
                            if (PaymentManager.canPay("tpa", player)) {
                                sender.sendMessage(CustomMessages.getString("Info.requestSent")
                                        .replaceAll("\\{player}", target.getName())
                                        .replaceAll("\\{lifetime}", String.valueOf(Config.requestLifetime())));

                                CoreClass.playSound("tpa", "requestSent", player);

                                target.sendMessage(CustomMessages.getString("Info.tpaRequestReceived")
                                        .replaceAll("\\{player}", sender.getName())
                                        .replaceAll("\\{lifetime}", String.valueOf(Config.requestLifetime())));

                                CoreClass.playSound("tpa", "requestReceived", target);

                                BukkitRunnable run = new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        sender.sendMessage(CustomMessages.getString("Error.requestExpired").replaceAll("\\{player}", target.getName()));
                                        TPRequest.removeRequest(TPRequest.getRequestByReqAndResponder(target, player));
                                    }
                                };
                                run.runTaskLater(CoreClass.getInstance(), Config.requestLifetime()*20); // 60 seconds
                                TPRequest request = new TPRequest(player, target, run, TPRequest.TeleportType.TPA); // Creates a new teleport request.
                                TPRequest.addRequest(request);
                                BukkitRunnable cooldowntimer = new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        CooldownManager.getCooldown().remove(playerUuid);
                                    }
                                };
                                CooldownManager.getCooldown().put(playerUuid, cooldowntimer);
                                cooldowntimer.runTaskLater(CoreClass.getInstance(), Config.commandCooldown()*20); // 20 ticks = 1 second
                                return true;
                            }
                        } else {
                            player.sendMessage(result);
                            return true;
                        }
                    } else {
                        sender.sendMessage(CustomMessages.getString("Error.noPlayerInput"));
                        return true;
                    }
                }
                return true;
            }
        } else {
            sender.sendMessage(CustomMessages.getString("Error.notAPlayer"));
        }
        return true;
    }
}
