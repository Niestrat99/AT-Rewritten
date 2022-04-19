package io.github.niestrat99.advancedteleport.hooks.claims;

import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.hooks.ClaimPlugin;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.RegisteredServiceProvider;

public class GriefPreventionClaimHook extends ClaimPlugin {
    private GriefPrevention griefPrevention;

    @Override
    public boolean canUse(World world) {
        if (!NewConfig.get().PROTECT_CLAIM_LOCATIONS.get() ||
            !Bukkit.getPluginManager().isPluginEnabled("GriefPrevention")
        ) return false;

        RegisteredServiceProvider<GriefPrevention> provider = Bukkit.getServer().getServicesManager().getRegistration(GriefPrevention.class);
        if (provider == null) return false;
        griefPrevention = provider.getProvider();
        return griefPrevention.claimsEnabledForWorld(world);
    }

    @Override
    public boolean isClaimed(Location location) {
        return griefPrevention.dataStore.getClaimAt(location, false, null) != null;
    }
}
