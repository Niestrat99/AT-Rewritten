package io.github.niestrat99.advancedteleport.commands.teleport;

import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.commands.PlayerCommand;
import io.github.niestrat99.advancedteleport.commands.TeleportATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ToggleTP extends TeleportATCommand implements PlayerCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s,
                             @NotNull String[] args) {

        // If the command can't proceed, stop there
        if (!canProceed(sender)) return true;

        // If more than one argument has been specified, see if the sender can change the teleportation state of another player
        if (args.length > 0 && sender.hasPermission("at.admin.toggletp")) {

            // Get the target player - if it doesn't exist, stop
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                CustomMessages.sendMessage(sender, "Error.noSuchPlayer");
                return true;
            }

            //
            ATPlayer atPlayer = ATPlayer.getPlayer(target);
            if (atPlayer.isTeleportationEnabled()) {
                atPlayer.setTeleportationEnabled(false, sender).thenAcceptAsync(result -> {
                    CustomMessages.sendMessage(sender, "Info.tpAdminOff");
                    CustomMessages.sendMessage(target, "Info.tpOff");
                });
            } else {
                atPlayer.setTeleportationEnabled(true, sender).thenAcceptAsync(result -> {
                    CustomMessages.sendMessage(sender, "Info.tpAdminOn");
                    CustomMessages.sendMessage(target, "Info.tpOn");
                });
            }
            return true;

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
    public String getPermission() {
        return "at.member.toggletp";
    }
}
