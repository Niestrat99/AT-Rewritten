package io.github.niestrat99.advancedteleport.commands.teleport;

import io.github.niestrat99.advancedteleport.api.TeleportRequest;
import io.github.niestrat99.advancedteleport.api.TeleportRequestType;
import io.github.niestrat99.advancedteleport.commands.PlayerCommand;
import io.github.niestrat99.advancedteleport.commands.TeleportATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.MainConfig;
import io.github.niestrat99.advancedteleport.folia.CancellableRunnable;
import io.github.niestrat99.advancedteleport.folia.RunnableManager;
import io.github.niestrat99.advancedteleport.managers.CooldownManager;
import io.github.niestrat99.advancedteleport.utilities.ConditionChecker;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class TpAll extends TeleportATCommand implements PlayerCommand {

    @Override
    public boolean onCommand(
            @NotNull final CommandSender sender,
            @NotNull final Command command,
            @NotNull final String s,
            @NotNull final String[] args) {
        if (!canProceed(sender)) return true;

        Player player = (Player) sender;
        int cooldown = CooldownManager.secondsLeftOnCooldown("tpahere", player);
        if (cooldown > 0) {
            CustomMessages.sendMessage(
                    sender,
                    "Error.onCooldown",
                    Placeholder.unparsed("time", String.valueOf(cooldown)));
            return true;
        }
        int players = 0;
        int requestLifetime = MainConfig.get().REQUEST_LIFETIME.get();
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (target == player) continue;
            if (ConditionChecker.canTeleport(player, target, "tpahere") != null) {
                continue;
            }
            players++;
            CustomMessages.sendMessage(
                    target,
                    "Info.tpaRequestHere",
                    Placeholder.unparsed("player", sender.getName()),
                    Placeholder.unparsed("lifetime", String.valueOf(requestLifetime)));

            CancellableRunnable run = RunnableManager.setupRunnerDelayed(() ->
                    TeleportRequest.removeRequest(TeleportRequest.getRequestByReqAndResponder(target, player)), requestLifetime * 20L);

            TeleportRequest request = new TeleportRequest(player, target, run, TeleportRequestType.TPAHERE);
            // Creates a new teleport request.
            TeleportRequest.addRequest(request);
            // Cooldown for tpall is always applied after request
            CooldownManager.addToCooldown("tpahere", player, player.getWorld());
        }
        if (players > 0) {
            CustomMessages.sendMessage(
                    player,
                    "Info.tpallRequestSent",
                    Placeholder.unparsed("amount", String.valueOf(players)));
        } else {
            CustomMessages.sendMessage(player, "Error.noRequestsSent");
        }
        return true;
    }

    @Override
    public @NotNull String getPermission() {
        return "at.admin.all";
    }

    @Override
    public @Nullable List<String> onTabComplete(
            @NotNull final CommandSender sender,
            @NotNull final Command command,
            @NotNull final String s,
            @NotNull final String[] args) {
        return null;
    }
}
