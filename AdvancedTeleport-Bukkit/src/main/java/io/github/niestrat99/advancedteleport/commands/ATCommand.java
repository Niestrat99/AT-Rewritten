package io.github.niestrat99.advancedteleport.commands;

import io.github.niestrat99.advancedteleport.config.CustomMessages;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the base implementation for a command in AT.
 */
public abstract class ATCommand implements IATCommand {


    public Void handleCommandFeedback(
        @Nullable final Throwable err,
        @NotNull final CommandSender sender,
        @NotNull final String success,
        @NotNull final String failure,
        @NotNull TagResolver... placeholders
    ) {
        // If an error occurred, send the error and print the stacktrace
        if (err != null) {
            CustomMessages.sendMessage(sender, failure, placeholders);
            err.printStackTrace();
            return null;
        }

        // Otherwise, just send the success message
        CustomMessages.sendMessage(sender, success, placeholders);
        return null;
    }

    @Override
    public @Nullable List<String> onTabComplete(
        @NotNull final CommandSender sender,
        @NotNull final Command command,
        @NotNull final String s,
        @NotNull final String[] args
    ) {
        final var checkVisibility = sender instanceof Player;
        final var players = Bukkit.getOnlinePlayers().stream() // TODO - see how performance intensive this is (love streams)
            .filter(player -> !checkVisibility || ((Player) sender).canSee(player))
            .map(Player::getName).toList();

        return StringUtil.copyPartialMatches(args[args.length - 1], players, new ArrayList<>());
    }
}
