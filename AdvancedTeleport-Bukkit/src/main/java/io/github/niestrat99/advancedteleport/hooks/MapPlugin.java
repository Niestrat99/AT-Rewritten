package io.github.niestrat99.advancedteleport.hooks;

import io.github.niestrat99.advancedteleport.api.Warp;

public abstract class MapPlugin {

    public abstract boolean canEnable();

    public abstract void enable();

    public abstract void addWarp(Warp warp);

    public enum TeleportPoint {
        WARP,
        HOME,
        SPAWN
    }
}
