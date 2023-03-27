package io.github.niestrat99.advancedteleport.hooks.claims;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.hooks.ClaimPlugin;
import me.angeschossen.lands.api.LandsIntegration;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

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

        // Get the lands integration
        if (lands == null) {
            lands = LandsIntegration.of(CoreClass.getInstance());
        }

        // Returns true if the lands is active in the world.
        return true;
    }

    @Override
    @Contract(pure = true)
    public boolean isClaimed(@NotNull final Location location) {
        final var chunk = location.getChunk();
        return lands.getLandByChunk(chunk.getWorld(), chunk.getX(), chunk.getZ()) != null;
    }
}
