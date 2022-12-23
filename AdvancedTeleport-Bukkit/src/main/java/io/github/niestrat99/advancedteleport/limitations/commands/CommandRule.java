package io.github.niestrat99.advancedteleport.limitations.commands;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public abstract class CommandRule {

    protected List<String> worlds;
    protected boolean inclusive;

    protected CommandRule(@NotNull String commandRule) {
        worlds = new ArrayList<>();
        if (commandRule.contains(":")) {
            inclusive = true;
            commandRule = commandRule.replaceFirst(":", "");
        } else if (commandRule.contains("!")) {
            commandRule = commandRule.replaceFirst("!", "");
        }
        worlds.addAll(Arrays.asList(commandRule.split(",")));
    }

    public abstract boolean canTeleport(
        @NotNull final Player player,
        @NotNull final Location toLoc
    );

}
