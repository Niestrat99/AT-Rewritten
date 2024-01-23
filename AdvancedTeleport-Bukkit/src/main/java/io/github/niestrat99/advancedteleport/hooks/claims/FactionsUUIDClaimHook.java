package io.github.niestrat99.advancedteleport.hooks.claims;

import com.massivecraft.factions.*;
import io.github.niestrat99.advancedteleport.hooks.ClaimPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class FactionsUUIDClaimHook extends ClaimPlugin<Plugin, FactionsPlugin> {

    public FactionsUUIDClaimHook() {
        super("Factions", FactionsPlugin.class);
    }

    @Override
    public boolean isClaimed(@NotNull Location location) {

        // Check if there's a faction
        final FLocation fLocation = new FLocation(location);
        final Faction faction = Board.getInstance().getFactionAt(fLocation);
        return faction != null;
    }

    @Override
    public boolean canAccess(@NotNull Player player, @NotNull Location location) {

        // Check if there's a faction
        final FLocation fLocation = new FLocation(location);
        final Faction faction = Board.getInstance().getFactionAt(fLocation);
        if (faction == null) return true;
        if (faction.isWilderness()) return true;

        // Check if the player has access
        final FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);
        return faction.getFPlayers().contains(fPlayer);
    }
}
