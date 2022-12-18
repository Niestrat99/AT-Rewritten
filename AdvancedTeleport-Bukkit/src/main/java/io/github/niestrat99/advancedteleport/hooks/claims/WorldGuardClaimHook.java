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
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class WorldGuardClaimHook extends ClaimPlugin<Plugin, WorldGuard> {

    private RegionContainer container;

    public WorldGuardClaimHook() {
        super("WorldGuard", WorldGuard.class);
    }

    @Override
    @Contract(pure = true)
    public boolean canUse(@NotNull final World world) {
        if (!super.canUse(world)) return false;

        return this.provider().map(provider -> {
            container = provider.getPlatform().getRegionContainer();
            return container.get(BukkitAdapter.adapt(world)) != null;
        }).orElse(false);
    }

    @Override
    @Contract(pure = true)
    public boolean isClaimed(@NotNull final Location location) {
        RegionQuery query = container.createQuery();
        return query.getApplicableRegions(BukkitAdapter.adapt(location)).size() > 0;
    }
}
