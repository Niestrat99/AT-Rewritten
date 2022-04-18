package io.github.niestrat99.advancedteleport.hooks.claims;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.hooks.ClaimPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class WorldGuardClaimHook extends ClaimPlugin {

    private RegionContainer container;

    @Override
    public boolean canUse(World world) {
        if (!NewConfig.get().PREVENT_CLAIM_LOCATIONS.get()) return false;
        if (!Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) return false;
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

    @Override
    public boolean isPermitted(
            Location location,
            Player player
    ) {
        if (location.getWorld() == null) return false;
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        RegionQuery query = container.createQuery();
        return query.testState(BukkitAdapter.adapt(location), localPlayer, Flags.BUILD);
    }
}
