package io.github.niestrat99.advancedteleport.commands;

import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.managers.CommandManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CoreCommand extends ATCommand {

    @Override
    public boolean onCommand(
        @NotNull final CommandSender sender,
        @NotNull final Command cmd,
        @NotNull final String s,
        @NotNull final String[] args
    ) {

        // Grab the help subcommand in case we need it
        SubATCommand help = CommandManager.subcommands.get("help");
        if (!sender.hasPermission("at.member.core")) {
            CustomMessages.sendMessage(sender, "Error.noPermission");
            return true;
        }
        if (args.length == 0) {
            help.onCommand(sender, cmd, s, args);
            return true;
        }
        String command = args[0].toLowerCase();
        if (!CommandManager.subcommands.containsKey(command)) {
            help.onCommand(sender, cmd, s, args);
            return true;
        }
        if (sender.hasPermission("at.member.core." + command)) {
            CommandManager.subcommands.get(command).onCommand(sender, cmd, s, Arrays.copyOfRange(args, 1, args.length));
        } else {
            CustomMessages.sendMessage(sender, "Error.noPermission");
        }

        return true;
    }

    @Override
    public boolean getRequiredFeature() {
        return true;
    }

    @Override
    public @NotNull String getPermission() {
        return "at.member.core";
    }

    @Override
    public @Nullable List<String> onTabComplete(
        @NotNull final CommandSender sender,
        @NotNull final Command cmd,
        @NotNull final String s,
        @NotNull final String[] args
    ) {
        if (args.length > 1) {
            String command = args[0].toLowerCase();
            if (!CommandManager.subcommands.containsKey(command)) {
                return null;
            }
            return CommandManager.subcommands.get(command).onTabComplete(sender, cmd, s, Arrays.copyOfRange(args, 1, args.length));
        }
        List<String> availableCommands = new ArrayList<>();
        List<String> chosenCommands = new ArrayList<>();
        for (String command : CommandManager.subcommands.keySet()) {
            if (sender.hasPermission("at.member.core." + command)) {
                availableCommands.add(command);
            }
        }
        StringUtil.copyPartialMatches(args[0], availableCommands, chosenCommands);
        return chosenCommands;
    }
}
