package io.github.niestrat99.advancedteleport.hooks.claims;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.hooks.ClaimPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.RegisteredServiceProvider;

public class WorldGuardClaimHook extends ClaimPlugin {

    private RegionContainer container;

    @Override
    public boolean canUse(World world) {
        if (!NewConfig.get().PROTECT_CLAIM_LOCATIONS.get() ||
            !Bukkit.getPluginManager().isPluginEnabled("WorldGuard")
        ) return false;

        RegisteredServiceProvider<WorldGuard> provider = Bukkit.getServer().getServicesManager().getRegistration(WorldGuard.class);
        if (provider == null) return false;
        container = provider.getProvider().getPlatform().getRegionContainer();
        return container.get(BukkitAdapter.adapt(world)) != null;
    }

    @Override
    public boolean isClaimed(Location location) {
        if (location.getWorld() == null) return false;
        RegionQuery query = container.createQuery();
        return query.getApplicableRegions(BukkitAdapter.adapt(location)).size() > 0;
    }
}
