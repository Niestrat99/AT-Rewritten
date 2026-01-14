package io.github.niestrat99.advancedteleport.hooks.claims;

import io.github.niestrat99.advancedteleport.hooks.ClaimPlugin;

import me.ryanhamshire.GriefPrevention.GriefPrevention;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class GriefPreventionClaimHook extends ClaimPlugin<Plugin, GriefPrevention> {
    private GriefPrevention griefPrevention;

    @Contract(pure = true)
    public GriefPreventionClaimHook() {
        super("GriefPrevention", GriefPrevention.class);
    }

    @Override
    @Contract(pure = true)
    public boolean canUse(@NotNull final World world) {
        if (!super.canUse(world)) return false;

        // Ensures there's a world border set in the world
        return this.provider()
                .map(
                        provider -> {
                            this.griefPrevention = provider;
                            return provider.claimsEnabledForWorld(world);
                        })
                .orElse(false);
    }

    @Override
    @Contract(pure = true)
    public boolean isClaimed(@NotNull final Location location) {
        return griefPrevention.dataStore.getClaimAt(location, false, null) != null;
    }
}
