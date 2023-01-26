package io.github.niestrat99.advancedteleport.commands;

import io.github.niestrat99.advancedteleport.config.CustomMessages;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface PlayerCommand extends IATCommand {

    @Override
    default boolean canProceed(@NotNull CommandSender sender) {

        // If the base checks fail, stop there
        if (!IATCommand.super.canProceed(sender)) return false;

        // If it's not a player, stop there
        if (!(sender instanceof Player)) {
            CustomMessages.sendMessage(sender, "Error.notAPlayer");
            return false;
        }

        // We good!
        return true;
    }
}
