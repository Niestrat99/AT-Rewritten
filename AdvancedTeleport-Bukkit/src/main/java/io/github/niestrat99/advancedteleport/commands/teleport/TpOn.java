package io.github.niestrat99.advancedteleport.commands.teleport;

import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.commands.PlayerCommand;
import io.github.niestrat99.advancedteleport.commands.TeleportATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class TpOn extends TeleportATCommand implements PlayerCommand {

    @Override
    public boolean onCommand(
        @NotNull final CommandSender sender,
        @NotNull final Command command,
        @NotNull final String s,
        @NotNull final String[] args
    ) {
        // If there is no permission or the feature is disabled, stop there
        if (!canProceed(sender)) return true;
        Player player = (Player) sender;
        ATPlayer atPlayer = ATPlayer.getPlayer(player);
        if (!atPlayer.isTeleportationEnabled()) {
            atPlayer.setTeleportationEnabled(true, sender).thenAcceptAsync(callback ->
                CustomMessages.sendMessage(sender, "Info.tpOn"));
        } else {
            CustomMessages.sendMessage(sender, "Error.alreadyOn");
        }
        return true;
    }

    @Override
    public @NotNull String getPermission() {
        return "at.member.on";
    }
}
