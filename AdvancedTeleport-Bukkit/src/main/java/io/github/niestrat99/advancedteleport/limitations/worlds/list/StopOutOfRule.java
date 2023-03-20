package io.github.niestrat99.advancedteleport.limitations.worlds.list;

import io.github.niestrat99.advancedteleport.limitations.worlds.WorldRule;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class StopOutOfRule extends WorldRule {

    public StopOutOfRule(@NotNull final String worldRule) {
        super(worldRule);
    }

    @Override
    public boolean canTeleport(
        @NotNull final Player player,
        @NotNull final Location toLoc
    ) {
        String toWorld = toLoc.getWorld().getName();
        // Applies to all worlds
        if (worlds.isEmpty()) {
            return player.getWorld().getName().equals(toWorld);
        }

        // If inclusive (1) and contains world (0), allow
        // If not inclusive (0) and contains world (0), deny
        // If not inclusive (0) and doesn't contain world (1), allow
        // If inclusive (1) and doesn't contain world (0), deny
        return (inclusive ^ worlds.contains(toWorld)) || player.getWorld().getName().equals(toWorld);
    }
}
