package io.github.niestrat99.advancedteleport.hooks.claims;

import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.hooks.ClaimPlugin;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class GriefPreventionClaimHook extends ClaimPlugin {
    private GriefPrevention griefPrevention;

    @Override
    public boolean canUse(World world) {
        if (!NewConfig.get().PROTECT_CLAIM_LOCATIONS.get()) return false;
        if (!Bukkit.getPluginManager().isPluginEnabled("GriefPrevention")) return false;
        RegisteredServiceProvider<GriefPrevention> provider = Bukkit.getServer().getServicesManager().getRegistration(GriefPrevention.class);
        if (provider == null) return false;
        griefPrevention = provider.getProvider();
        return griefPrevention.claimsEnabledForWorld(world);
    }

    @Override
    public boolean isClaimed(Location location) {
        return griefPrevention.dataStore.getClaimAt(location, false, null) != null;
    }

    @Override
    public boolean isPermitted(
            Location location,
            Player player
    ) {
        PlayerData data = griefPrevention.dataStore.getPlayerData(player.getUniqueId());
        Claim claim = griefPrevention.dataStore.getClaimAt(location, false, data.lastClaim);
        if (claim == null) return true;
        return claim.checkPermission(player, ClaimPermission.Build, null) == null;

    }
}
