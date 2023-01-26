package io.github.niestrat99.advancedteleport.commands.core;

import io.github.niestrat99.advancedteleport.commands.SubATCommand;
import io.github.niestrat99.advancedteleport.commands.core.map.SetIconCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public final class MapCommand extends SubATCommand {

    private final HashMap<String, SubATCommand> subMapCommands;

    public MapCommand() {
        subMapCommands = new HashMap<>();
        subMapCommands.put("seticon", new SetIconCommand());
    }

    @Override
    @Contract(value = "_, _, _, _ -> false", pure = true)
    public boolean onCommand(
        @NotNull final CommandSender sender,
        @NotNull final Command command,
        @NotNull final String s,
        @NotNull final String[] args
) {
        return false;
    }
}
