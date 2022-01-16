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
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            CustomMessages.sendMessage(sender, "Error.notAPlayer");
            return true;
        }
        if (!NewConfig.get().USE_BASIC_TELEPORT_FEATURES.get()) {
            CustomMessages.sendMessage(sender, "Error.featureDisabled");
            return true;
        }
        if (!sender.hasPermission("at.admin.all")) {
            CustomMessages.sendMessage(sender, "Error.noPermission");
            return true;
        }
        Player player = (Player) sender;
        int cooldown = CooldownManager.secondsLeftOnCooldown("tpahere", player);
        if (cooldown > 0) {
            CustomMessages.sendMessage(sender, "Error.onCooldown", "{time}", String.valueOf(cooldown));
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
                CustomMessages.sendMessage(target, "Info.tpaRequestHere", "{player}", sender.getName(), "{lifetime}", String.valueOf(requestLifetime));

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
            CustomMessages.sendMessage(player, "Info.tpallRequestSent", "{amount}", String.valueOf(players));
        } else {
            CustomMessages.sendMessage(player, "Error.noRequestsSent");
        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        return null;
    }
}
