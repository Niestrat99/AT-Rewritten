package io.github.niestrat99.advancedteleport.hooks.claims;

import dev.kitteh.factions.*;
import dev.kitteh.factions.permissible.Relation;
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
        return !isPublicZone(getFaction(location));
    }

    @Override
    public boolean canAccess(@NotNull Player player, @NotNull Location location) {
        final Faction faction = getFaction(location);
        if (isPublicZone(faction)) return true;

        final FPlayer fPlayer = FPlayers.fPlayers().get(player);
        final Relation relation = faction.relationTo(fPlayer);

        return relation.isMember() || relation.isAlly() || relation.isTruce();
    }

    private Faction getFaction(@NotNull Location location) {
        final FLocation fLocation = new FLocation(location);
        return Board.board().factionAt(fLocation);
    }

    private boolean isPublicZone(Faction faction) {
        return faction.isWilderness() || faction.isSafeZone() || faction.isWarZone();
    }
}
