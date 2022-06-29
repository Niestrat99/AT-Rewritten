package io.github.niestrat99.advancedteleport.hooks;

import io.github.niestrat99.advancedteleport.api.Home;
import io.github.niestrat99.advancedteleport.api.Warp;
import org.bukkit.Location;

public abstract class MapPlugin {

    public abstract boolean canEnable();

    public abstract void enable();

    public abstract void addWarp(Warp warp);

    public abstract void addHome(Home home);

    public abstract void addSpawn(String name, Location location);

    public enum TeleportPoint {
        WARP,
        HOME,
        SPAWN
    }
}
