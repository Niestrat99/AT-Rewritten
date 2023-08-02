package io.github.niestrat99.advancedteleport.commands.misc;

import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.commands.ATCommand;
import io.github.niestrat99.advancedteleport.commands.PlayerCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TpConfirm extends ATCommand implements PlayerCommand {

    @Override
    public boolean getRequiredFeature() {
        return true;
    }

    @Override
    public @NotNull String getPermission() {
        return "at.member.confirm";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        // Get the player
        ATPlayer player = ATPlayer.getPlayer((Player) sender);
        if (!player.isAwaitingConfirmation()) {
            CustomMessages.sendMessage(sender, "Error.notAwaitingConfirmation");
            return true;
        }

        // Confirm!
        CustomMessages.sendMessage(sender, "Info.confirmedTeleportation");
        player.confirm();
        return true;
    }
}
