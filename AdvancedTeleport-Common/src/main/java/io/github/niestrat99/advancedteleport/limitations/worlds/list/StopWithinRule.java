package io.github.niestrat99.advancedteleport.limitations.worlds.list;

import io.github.niestrat99.advancedteleport.limitations.worlds.WorldRule;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class StopWithinRule extends WorldRule {

    public StopWithinRule(@NotNull final String worldRule) {
        super(worldRule);
    }

    @Override
    public boolean canTeleport(@NotNull final Player player, @NotNull final Location toLoc) {
        return !player.getWorld().getName().equals(toLoc.getWorld().getName());
    }
}
