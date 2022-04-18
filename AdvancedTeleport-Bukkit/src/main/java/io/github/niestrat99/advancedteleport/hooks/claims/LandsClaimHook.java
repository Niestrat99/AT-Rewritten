package io.github.niestrat99.advancedteleport.hooks.claims;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.hooks.ClaimPlugin;
import me.angeschossen.lands.api.integration.LandsIntegration;
import me.angeschossen.lands.api.land.LandArea;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class LandsClaimHook extends ClaimPlugin {

    private LandsIntegration lands;

    @Override
    public boolean canUse(World world) {
        if (!NewConfig.get().PROTECT_CLAIM_LOCATIONS.get()) return false;
        if (!Bukkit.getPluginManager().isPluginEnabled("Lands")) return false;
        if (lands == null) {
            lands = new LandsIntegration(CoreClass.getInstance());
        }
        return lands.getLandWorld(world) != null; // Returns true if the lands is active in the world.
    }

    @Override
    public boolean isClaimed(Location location) {
        return lands.isClaimed(location);
    }

    @Override
    public boolean isPermitted(
            Location location,
            Player player
    ) {
        LandArea land = lands.getArea(location);
        if (land == null) return true;
        if (land.isTrusted(player.getUniqueId())) return true; // TODO: Check if this method includes the owner.
        return land.getOwnerUID() == player.getUniqueId();
    }
}
