package io.github.niestrat99.advancedteleport.commands.core;

import io.github.niestrat99.advancedteleport.commands.SubATCommand;
import io.github.niestrat99.advancedteleport.commands.core.map.*;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public final class MapCommand extends SubATCommand {

    private static final HashMap<String, SubATCommand> subMapCommands = new HashMap<>();

    public MapCommand() {
        subMapCommands.put("seticon", new SetIconCommand());
        subMapCommands.put("setclicktooltip", new SetClickTooltipCommand());
        subMapCommands.put("sethovertooltip", new SetHoverTooltipCommand());
        subMapCommands.put("setsize", new SetSizeCommand());
        subMapCommands.put("setvisible", new SetVisibleCommand());
    }

    @Override
    @Contract(value = "_, _, _, _ -> false", pure = true)
    public boolean onCommand(
        @NotNull final CommandSender sender,
        @NotNull final Command command,
        @NotNull final String s,
        @NotNull final String[] args
    ) {

        // If there's no arguments, stop there
        if (args.length == 0) {
            Bukkit.dispatchCommand(sender, "at help map");
            return false;
        }

        // Get the subcommand in question
        SubATCommand subATCommand = subMapCommands.get(args[0].toLowerCase());
        if (subATCommand == null) {
            Bukkit.dispatchCommand(sender, "at help map");
            return false;
        }

        // Check permissions
        if (!sender.hasPermission("at.member.core.map." + args[0].toLowerCase())) {
            Bukkit.dispatchCommand(sender, "at help map");
            return false;
        }

        // Execute
        return subATCommand.onCommand(sender, command, s, Arrays.copyOfRange(args, 1, args.length));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        List<String> results = new ArrayList<>();
        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], subMapCommands.keySet().stream().filter(key -> sender.hasPermission("at.member.core.map." + key)).toList(), results);
            return results;
        }

        // Get the command completion from the subcommand
        SubATCommand subATCommand = subMapCommands.get(args[0].toLowerCase());
        if (subATCommand == null) return results;

        return subATCommand.onTabComplete(sender, command, s, Arrays.copyOfRange(args, 1, args.length));
    }

    public static HashMap<String, SubATCommand> getSubMapCommands() {
        return subMapCommands;
    }
}
