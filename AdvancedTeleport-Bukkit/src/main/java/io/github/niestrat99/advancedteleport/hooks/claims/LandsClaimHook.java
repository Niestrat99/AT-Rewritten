package io.github.niestrat99.advancedteleport.hooks.claims;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.hooks.ClaimPlugin;
import me.angeschossen.lands.api.integration.LandsIntegration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class LandsClaimHook extends ClaimPlugin {

    private LandsIntegration lands;

    @Override
    public boolean canUse(World world) {
        if (!NewConfig.get().PROTECT_CLAIM_LOCATIONS.get() ||
            !Bukkit.getPluginManager().isPluginEnabled("Lands")
        ) return false;

        if (lands == null) {
            lands = new LandsIntegration(CoreClass.getInstance());
        }
        return lands.getLandWorld(world) != null; // Returns true if the lands is active in the world.
    }

    @Override
    public boolean isClaimed(Location location) {
        final var chunk = location.getChunk();
        return lands.isClaimed(chunk.getWorld(), chunk.getX(), chunk.getZ());
    }
}
