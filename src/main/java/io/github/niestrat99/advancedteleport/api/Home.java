package io.github.niestrat99.advancedteleport.api;

import org.bukkit.Location;

import java.util.UUID;

public class Home {

    private UUID owner;
    private String name;
    private Location location;
    private long createdTime;
    private long updatedTime;
    private String createdTimeFormatted;
    private String updatedTimeFormatted;

    public Home(UUID owner, String name, Location location, long createdTime, long updatedTime) {
        this.name = name;
        this.owner = owner;
        this.location = location;
        this.createdTime = createdTime;
        this.updatedTime = updatedTime;
    }

    public Location getLocation() {
        return location;
    }

    public String getName() {
        return name;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
