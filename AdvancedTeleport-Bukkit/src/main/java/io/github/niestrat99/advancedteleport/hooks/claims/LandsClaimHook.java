package io.github.niestrat99.advancedteleport.hooks.claims;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.hooks.ClaimPlugin;
import me.angeschossen.lands.api.integration.LandsIntegration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class LandsClaimHook extends ClaimPlugin<Plugin, Void> { // Stupid lands doesn't have a provider class

    private LandsIntegration lands;

    @Contract(pure = true)
    public LandsClaimHook() {
        super("Lands");
    }

    @Override
    @Contract(pure = true)
    public boolean canUse(@NotNull final World world) {
        if (!super.canUse(world)) return false;

        if (lands == null) {
            lands = new LandsIntegration(CoreClass.getInstance());
        }

        return lands.getLandWorld(world) != null;
    }

    @Override
    @Contract(pure = true)
    public boolean isClaimed(@NotNull final Location location) {
        final var chunk = location.getChunk();
        return lands.isClaimed(chunk.getWorld(), chunk.getX(), chunk.getZ());
    }
}
