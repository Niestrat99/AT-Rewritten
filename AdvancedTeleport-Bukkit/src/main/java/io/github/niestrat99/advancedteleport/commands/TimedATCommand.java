package io.github.niestrat99.advancedteleport.commands;

import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.managers.CooldownManager;
import io.github.niestrat99.advancedteleport.managers.MovementManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface TimedATCommand extends PlayerCommand {

    @Override
    default boolean canProceed(@NotNull CommandSender sender) {

        // If the base checks don't pass, stop there
        if (!PlayerCommand.super.canProceed(sender)) return false;

        // Get the player in question
        Player player = (Player) sender;

        // If the player is on a cooldown, stop there
        int cooldown = CooldownManager.secondsLeftOnCooldown(getSection(), player);
        if (cooldown > 0) {
            CustomMessages.sendMessage(sender, "Error.onCooldown", "time", String.valueOf(cooldown));
            return true;
        }

        // If the player is on a movement timer, stop there
        if (MovementManager.getMovement().containsKey(player.getUniqueId())) {
            CustomMessages.sendMessage(player, "Error.onCountdown");
            return true;
        }

        return true;
    }

    @NotNull
    String getSection();
}
