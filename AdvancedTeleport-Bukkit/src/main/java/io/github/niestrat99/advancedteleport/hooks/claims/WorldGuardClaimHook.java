package io.github.niestrat99.advancedteleport.hooks.claims;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;

import io.github.niestrat99.advancedteleport.hooks.ClaimPlugin;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class WorldGuardClaimHook extends ClaimPlugin<Plugin, WorldGuard> {

    private RegionContainer container;

    public WorldGuardClaimHook() {
        super("WorldGuard", WorldGuard.class);
    }

    @Override
    @Contract(pure = true)
    public boolean canUse(@NotNull final World world) {

        // Ensures claim avoidance is enabled
        if (!super.canUse(world)) return false;

        container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        return container.get(BukkitAdapter.adapt(world)) != null;
    }

    @Override
    @Contract(pure = true)
    public boolean isClaimed(@NotNull final Location location) {
        RegionQuery query = container.createQuery();
        return query.getApplicableRegions(BukkitAdapter.adapt(location)).size() > 0;
    }

    @Override
    public boolean canAccess(@NotNull Player player, @NotNull Location location) {
        RegionQuery query = container.createQuery();
        return query.testState(BukkitAdapter.adapt(location), WorldGuardPlugin.inst().wrapPlayer(player), Flags.ENTRY);
    }
}
