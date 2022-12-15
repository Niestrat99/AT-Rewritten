package io.github.niestrat99.advancedteleport.commands.core;

import io.github.niestrat99.advancedteleport.commands.SubATCommand;
import io.github.niestrat99.advancedteleport.commands.core.map.SetIconCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class MapCommand extends SubATCommand {

    private final HashMap<String, SubATCommand> subMapCommands;

    public MapCommand() {
        subMapCommands = new HashMap<>();
        subMapCommands.put("seticon", new SetIconCommand());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s,
                             @NotNull String[] args) {
        return false;
    }
}
