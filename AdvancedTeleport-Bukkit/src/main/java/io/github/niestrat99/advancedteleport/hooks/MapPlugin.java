package io.github.niestrat99.advancedteleport.hooks;

import io.github.niestrat99.advancedteleport.api.Home;
import io.github.niestrat99.advancedteleport.api.Warp;
import org.bukkit.Location;

import java.io.InputStream;

public abstract class MapPlugin {

    public abstract boolean canEnable();

    public abstract void enable();

    public abstract void addWarp(Warp warp);

    public abstract void addHome(Home home);

    public abstract void addSpawn(String name, Location location);

    public abstract void removeWarp(Warp warp);

    public abstract void removeHome(Home home);

    public abstract void removeSpawn(String name);

    public abstract void moveWarp(Warp warp);

    public abstract void moveHome(Home home);

    public abstract void moveSpawn(String name, Location location);

    public abstract void registerImage(String name, InputStream stream);

    public enum TeleportPoint {
        WARP,
        HOME,
        SPAWN
    }
}
