package io.github.niestrat99.advancedteleport.commands.teleport;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.commands.ATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.managers.CooldownManager;
import io.github.niestrat99.advancedteleport.utilities.ConditionChecker;
import io.github.niestrat99.advancedteleport.utilities.TPRequest;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TpAll implements ATCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (sender instanceof Player) {
            if (NewConfig.get().USE_BASIC_TELEPORT_FEATURES.get()) {
                if (sender.hasPermission("at.admin.all")) {
                    Player player = (Player) sender;
                    int cooldown = CooldownManager.secondsLeftOnCooldown("tpahere", player);
                    if (cooldown > 0) {
                        sender.sendMessage(CustomMessages.getString("Error.onCooldown").replaceAll("\\{time}", String.valueOf(cooldown)));
                        return true;
                    }
                    int players = 0;
                    int requestLifetime = NewConfig.get().REQUEST_LIFETIME.get();
                    for (Player target : Bukkit.getOnlinePlayers()) {
                        if (target != player) {
                            if (!ConditionChecker.canTeleport(player, target, "tpahere").isEmpty()) {
                                continue;
                            }
                            players++;
                            target.sendMessage(CustomMessages.getString("Info.tpaRequestHere")
                                    .replaceAll("\\{player}", sender.getName())
                                    .replaceAll("\\{lifetime}", String.valueOf(requestLifetime)));
                            BukkitRunnable run = new BukkitRunnable() {
                                @Override
                                public void run() {
                                    TPRequest.removeRequest(TPRequest.getRequestByReqAndResponder(target, player));
                                }
                            };
                            run.runTaskLater(CoreClass.getInstance(), requestLifetime * 20); // 60 seconds
                            TPRequest request = new TPRequest(player, target, run, TPRequest.TeleportType.TPAHERE); // Creates a new teleport request.
                            TPRequest.addRequest(request);
                            // Cooldown for tpall is always applied after request
                            CooldownManager.addToCooldown("tpahere", player);
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
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        return null;
    }
}
