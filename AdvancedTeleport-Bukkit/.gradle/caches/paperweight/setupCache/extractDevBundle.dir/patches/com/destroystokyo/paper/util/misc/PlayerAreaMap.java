package com.destroystokyo.paper.util.misc;

import net.minecraft.server.level.ServerPlayer;

/**
 * @author Spottedleaf
 */
public final class PlayerAreaMap extends AreaMap<ServerPlayer> {

    public PlayerAreaMap() {
        super();
    }

    public PlayerAreaMap(final PooledLinkedHashSets<ServerPlayer> pooledHashSets) {
        super(pooledHashSets);
    }

    public PlayerAreaMap(final PooledLinkedHashSets<ServerPlayer> pooledHashSets, final ChangeCallback<ServerPlayer> addCallback,
                         final ChangeCallback<ServerPlayer> removeCallback) {
        this(pooledHashSets, addCallback, removeCallback, null);
    }

    public PlayerAreaMap(final PooledLinkedHashSets<ServerPlayer> pooledHashSets, final ChangeCallback<ServerPlayer> addCallback,
                         final ChangeCallback<ServerPlayer> removeCallback, final ChangeSourceCallback<ServerPlayer> changeSourceCallback) {
        super(pooledHashSets, addCallback, removeCallback, changeSourceCallback);
    }

    @Override
    protected PooledLinkedHashSets.PooledObjectLinkedOpenHashSet<ServerPlayer> getEmptySetFor(final ServerPlayer player) {
        return player.cachedSingleHashSet;
    }
}
