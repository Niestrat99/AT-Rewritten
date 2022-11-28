package io.github.niestrat99.advancedteleport.commands;

import io.github.niestrat99.advancedteleport.config.CustomMessages;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the base implementation for a command in AT.
 */
public interface ATCommand extends TabExecutor {

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

    String getPermission();

    default Void handleCommandFeedback(Throwable ex, CommandSender sender, String success, String failure, String... placeholders) {
        // If an error occurred, send the error and print the stacktrace
        if (ex != null) {
            CustomMessages.sendMessage(sender, failure, placeholders);
            ex.printStackTrace();
            return null;
        }

        // Otherwise, just send the success message
        CustomMessages.sendMessage(sender, success, placeholders);
        return null;
    }

    @Nullable
    @Override
    default List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        List<String> results = new ArrayList<>();
        List<String> players = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (sender instanceof Player && ((Player) sender).canSee(player)) {
                players.add(player.getName());
            }
        }
        StringUtil.copyPartialMatches(args[args.length - 1], players, results);
        return results;
    }
}
