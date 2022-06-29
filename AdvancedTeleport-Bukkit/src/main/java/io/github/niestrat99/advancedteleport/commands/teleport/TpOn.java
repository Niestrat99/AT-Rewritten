package io.github.niestrat99.advancedteleport.commands.teleport;

import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.commands.TeleportATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TpOn extends TeleportATCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // If there is no permission or the feature is disabled, stop there
        if (!canProceed(sender)) return true;
        if (!(sender instanceof Player)) {
            CustomMessages.sendMessage(sender, "Error.notAPlayer");
            return true;
        }
        Player player = (Player)sender;
        ATPlayer atPlayer = ATPlayer.getPlayer(player);
        if (!atPlayer.isTeleportationEnabled()) {
            atPlayer.setTeleportationEnabled(true, sender).thenAcceptAsync(callback -> CustomMessages.sendMessage(sender, "Info.tpOn"));
        } else {
            CustomMessages.sendMessage(sender, "Error.alreadyOn");
        }
        return true;
    }

    @Override
    public String getPermission() {
        return "at.member.on";
    }
}
