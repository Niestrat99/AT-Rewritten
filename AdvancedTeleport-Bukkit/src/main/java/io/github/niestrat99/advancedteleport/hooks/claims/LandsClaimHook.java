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

        // Ensures claim avoidance is enabled
        if (!NewConfig.get().PROTECT_CLAIM_LOCATIONS.get() ||
            !Bukkit.getPluginManager().isPluginEnabled("Lands")) return false;

        // Get the lands integration
        if (lands == null) {
            lands = new LandsIntegration(CoreClass.getInstance());
        }

        // Returns true if the lands is active in the world.
        return lands.getLandWorld(world) != null;
    }

    @Override
    public boolean isClaimed(Location location) {
        return lands.isClaimed(location);
    }
}
