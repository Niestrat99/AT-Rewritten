package io.github.niestrat99.advancedteleport.commands.teleport;

import io.github.niestrat99.advancedteleport.utilities.ConditionChecker;
import io.github.niestrat99.advancedteleport.utilities.TPRequest;
import io.github.niestrat99.advancedteleport.config.Config;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.events.CooldownManager;
import io.github.niestrat99.advancedteleport.events.MovementManager;
import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.utilities.PaymentManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class TpaHere implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (sender instanceof Player){
            if (Config.isFeatureEnabled("teleport")) {
                if (sender.hasPermission("at.member.here")) {
                    Player player = (Player) sender;
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
                        String result = ConditionChecker.canTeleport(player, target, "tpahere");
                        if (result.isEmpty()) {
                            if (PaymentManager.canPay("tpahere", player)) {
                                sender.sendMessage(CustomMessages.getString("Info.requestSent")
                                        .replaceAll("\\{player}", target.getName())
                                        .replaceAll("\\{lifetime}", String.valueOf(Config.requestLifetime())));

                                CoreClass.playSound("tpahere", "requestSent", player);

                                target.sendMessage(CustomMessages.getString("Info.tpaRequestHere")
                                        .replaceAll("\\{player}", sender.getName())
                                        .replaceAll("\\{lifetime}", String.valueOf(Config.requestLifetime())));

                                CoreClass.playSound("tpahere", "requestReceived", target);

                                BukkitRunnable run = new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        sender.sendMessage(CustomMessages.getString("Error.requestExpired").replaceAll("\\{player}", target.getName()));
                                        TPRequest.removeRequest(TPRequest.getRequestByReqAndResponder(target, player));
                                    }
                                };
                                run.runTaskLater(CoreClass.getInstance(), Config.requestLifetime() * 20); // 60 seconds
                                TPRequest request = new TPRequest(player, target, run, TPRequest.TeleportType.TPAHERE); // Creates a new teleport request.
                                TPRequest.addRequest(request);
                                BukkitRunnable cooldowntimer = new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        CooldownManager.getCooldown().remove(playerUuid);
                                    }
                                };
                                CooldownManager.getCooldown().put(playerUuid, cooldowntimer);
                                cooldowntimer.runTaskLater(CoreClass.getInstance(), Config.commandCooldown() * 20); // 20 ticks = 1 second
                                return true;
                            }
                        } else {
                            sender.sendMessage(result);
                            return true;
                        }
                    } else {
                        sender.sendMessage(CustomMessages.getString("Error.noPlayerInput"));
                        return true;
                    }

                }
            }
        } else {
        sender.sendMessage(CustomMessages.getString("Error.notAPlayer"));
    }
        return true;
    }
}
