package io.github.niestrat99.advancedteleport.commands.teleport;

import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.commands.TeleportATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ToggleTP extends TeleportATCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s,
                             @NotNull String[] args) {
        if (!canProceed(sender)) return true;
        if (!(sender instanceof Player)) {
            CustomMessages.sendMessage(sender, "Error.notAPlayer");
            return true;
        }
        if (args.length > 0) {
            if (sender.hasPermission("at.admin.toggletp")) {
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    CustomMessages.sendMessage(sender, "Error.noSuchPlayer");
                    return true;
                }
                ATPlayer atPlayer = ATPlayer.getPlayer(target);
                if (atPlayer.isTeleportationEnabled()) {
                    atPlayer.setTeleportationEnabled(false, sender).thenAcceptAsync(result -> {
                        CustomMessages.sendMessage(sender, "Info.tpAdminOff");
                        CustomMessages.sendMessage(target, "Info.tpOff");
                    });
                } else {
                    atPlayer.setTeleportationEnabled(false, sender).thenAcceptAsync(result -> {
                        CustomMessages.sendMessage(sender, "Info.tpAdminOn");
                        CustomMessages.sendMessage(target, "Info.tpOn");
                    });
                }
                return true;
            }
        }
        Player player = (Player) sender;
        ATPlayer atPlayer = ATPlayer.getPlayer(player);
        if (atPlayer.isTeleportationEnabled()) {
            atPlayer.setTeleportationEnabled(false, sender).thenAcceptAsync(callback -> CustomMessages.sendMessage(sender, "Info.tpOff"));
        } else {
            atPlayer.setTeleportationEnabled(true, sender).thenAcceptAsync(callback -> CustomMessages.sendMessage(sender, "Info.tpOn"));
        }

        return true;
    }

    @Override
    public @NotNull String getPermission() {
        return "at.member.toggletp";
    }
}
