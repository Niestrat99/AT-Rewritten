package io.github.niestrat99.advancedteleport.api;

import io.github.niestrat99.advancedteleport.sql.SQLManager;
import io.github.niestrat99.advancedteleport.sql.WarpSQLManager;
import org.bukkit.Location;
import org.fusesource.hawtjni.runtime.Callback;

import java.util.HashMap;
import java.util.UUID;

public class Warp {

    private UUID creator;
    private String name;
    private Location location;
    private long createdTime;
    private long updatedTime;
    private String createdTimeFormatted;
    private String updatedTimeFormatted;

    private static HashMap<String, Warp> warps = new HashMap<>();

    public Warp(UUID creator, String name, Location location, long createdTime, long updatedTime) {

    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
    }

    public UUID getCreator() {
        return creator;
    }

    public void setLocation(Location location, SQLManager.SQLCallback<Boolean> callback) {
        this.location = location;
        this.updatedTime = System.currentTimeMillis();

        WarpSQLManager.get().moveWarp(location, name, callback);
    }

    public static HashMap<String, Warp> getWarps() {
        return warps;
    }
}
