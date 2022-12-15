package io.github.niestrat99.advancedteleport.commands;

import io.github.niestrat99.advancedteleport.config.CustomMessages;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the base implementation for a command in AT.
 */
public interface IATCommand extends TabExecutor {

    /**
     * Determines whether the command can continue after performing basic checks.<br>
     * This will check if the feature for the command is enabled, and if the sender<br>
     * has permission.
     *
     * @param sender the command sender running the command.
     * @return true if the sender can run the command, false if not.
     */
    default boolean canProceed(@NotNull CommandSender sender) {
        // Make sure the required feature is enabled
        if (!getRequiredFeature()) {
            CustomMessages.sendMessage(sender, "Error.featureDisabled");
            return false;
        }

        // If it's enabled, check for permission
        return sender.hasPermission(getPermission());
    }

    boolean getRequiredFeature();

    @NotNull String getPermission();
}
