package com.destroystokyo.paper.util.misc;

import net.minecraft.server.level.ServerPlayer;

public class PlayerDistanceTrackingAreaMap extends DistanceTrackingAreaMap<ServerPlayer> {

    public PlayerDistanceTrackingAreaMap() {
        super();
    }

    public PlayerDistanceTrackingAreaMap(final PooledLinkedHashSets<ServerPlayer> pooledHashSets) {
        super(pooledHashSets);
    }

    public PlayerDistanceTrackingAreaMap(final PooledLinkedHashSets<ServerPlayer> pooledHashSets, final ChangeCallback<ServerPlayer> addCallback,
                                         final ChangeCallback<ServerPlayer> removeCallback, final DistanceChangeCallback<ServerPlayer> distanceChangeCallback) {
        super(pooledHashSets, addCallback, removeCallback, distanceChangeCallback);
    }

    @Override
    protected PooledLinkedHashSets.PooledObjectLinkedOpenHashSet<ServerPlayer> getEmptySetFor(final ServerPlayer player) {
        return player.cachedSingleHashSet;
    }
}
