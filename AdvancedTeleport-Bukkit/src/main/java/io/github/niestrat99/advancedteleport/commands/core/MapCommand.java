package io.github.niestrat99.advancedteleport.commands.core;

import io.github.niestrat99.advancedteleport.commands.SubATCommand;
import io.github.niestrat99.advancedteleport.commands.core.map.*;
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

    private final HashMap<String, SubATCommand> subMapCommands;

    public MapCommand() {
        subMapCommands = new HashMap<>();
        subMapCommands.put("seticon", new SetIconCommand());
        subMapCommands.put("setclicktooltip", new SetClickTooltipCommand());
        subMapCommands.put("sethidden", new SetHiddenCommand());
        subMapCommands.put("sethovertooltip", new SetHoverTooltipCommand());
        subMapCommands.put("setsize", new SetSizeCommand());
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
        if (args.length == 0) return false;

        // Get the subcommand in question
        SubATCommand subATCommand = this.subMapCommands.get(args[0].toLowerCase());
        if (subATCommand == null) {

            return false;
        }

        // Execute
        return subATCommand.onCommand(sender, command, s, Arrays.copyOfRange(args, 1, args.length));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        List<String> results = new ArrayList<>();
        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], this.subMapCommands.keySet(), results);
            return results;
        }

        // Get the command completion from the subcommand
        SubATCommand subATCommand = this.subMapCommands.get(args[0].toLowerCase());
        if (subATCommand == null) return results;

        return subATCommand.onTabComplete(sender, command, s, Arrays.copyOfRange(args, 1, args.length));
    }
}
