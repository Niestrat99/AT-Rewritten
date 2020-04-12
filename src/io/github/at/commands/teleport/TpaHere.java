package io.github.at.commands.teleport;

import io.github.at.config.Config;
import io.github.at.config.CustomMessages;
import io.github.at.config.TpBlock;
import io.github.at.events.CooldownManager;
import io.github.at.events.MovementManager;
import io.github.at.main.CoreClass;
import io.github.at.utilities.DistanceLimiter;
import io.github.at.utilities.PaymentManager;
import io.github.at.utilities.TPRequest;
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
                    if (CooldownManager.getCooldown().containsKey(playerUuid)) {
                        sender.sendMessage(CustomMessages.getString("Error.onCooldown").replaceAll("\\{time}", String.valueOf(Config.commandCooldown())));
                        return true;
                    }
                    if (MovementManager.getMovement().containsKey(playerUuid)) {
                        player.sendMessage(CustomMessages.getString("Error.onCountdown"));
                        return true;
                    }
                    if (args.length > 0) {
                        if (args[0].equalsIgnoreCase(player.getName())){
                            sender.sendMessage(CustomMessages.getString("Error.requestSentToSelf"));
                            return true;
                        }
                        Player target = Bukkit.getPlayer(args[0]);
                        if (target == null) {
                            sender.sendMessage(CustomMessages.getString("Error.noSuchPlayer"));
                            return true;
                        } else {
                            UUID targetUuid = target.getUniqueId();
                            if (TpOff.getTpOff().contains(targetUuid)) {
                                sender.sendMessage(CustomMessages.getString("Error.tpOff").replaceAll("\\{player}", target.getName()));
                                return true;
                            }
                            if (TpBlock.getBlockedPlayers(target).contains(playerUuid)) {
                                sender.sendMessage(CustomMessages.getString("Error.tpBlock").replaceAll("\\{player}", target.getName()));
                                return true;
                            }
                            if (TPRequest.getRequestByReqAndResponder(target, player) != null) {
                                sender.sendMessage(CustomMessages.getString("Error.alreadySentRequest").replaceAll("\\{player}", target.getName()));
                                return true;
                            }
                            if (!DistanceLimiter.canTeleport(player.getLocation(), target.getLocation(), "tpahere") && !target.hasPermission("at.admin.bypass.distance-limit")) {
                                player.sendMessage(CustomMessages.getString("Error.tooFarAway"));
                                return true;
                            }
                            if (PaymentManager.canPay("tpahere", player)) {
                                sender.sendMessage(CustomMessages.getString("Info.requestSent")
                                        .replaceAll("\\{player}", target.getName())
                                        .replaceAll("\\{lifetime}", String.valueOf(Config.requestLifetime())));
                                target.sendMessage(CustomMessages.getString("Info.tpaRequestHere")
                                        .replaceAll("\\{player}", sender.getName())
                                        .replaceAll("\\{lifetime}", String.valueOf(Config.requestLifetime())));
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
