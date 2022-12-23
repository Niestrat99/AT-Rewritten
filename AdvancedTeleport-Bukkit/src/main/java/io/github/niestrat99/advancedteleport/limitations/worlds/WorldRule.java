package io.github.niestrat99.advancedteleport.limitations.worlds;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public abstract class WorldRule {

    protected List<String> worlds;
    protected boolean inclusive = false;

    protected WorldRule(@NotNull String worldRule) {
        worlds = new ArrayList<>();
        if (worldRule.isEmpty()) return;
        if (worldRule.contains(":")) {
            inclusive = true;
            worldRule = worldRule.replaceFirst(":", "");
        } else if (worldRule.contains("!")) {
            worldRule = worldRule.replaceFirst("!", "");
        }
        worlds.addAll(Arrays.asList(worldRule.split(",")));
    }

    public abstract boolean canTeleport(
        @NotNull final Player player,
        @NotNull final Location toLoc
    );
}
