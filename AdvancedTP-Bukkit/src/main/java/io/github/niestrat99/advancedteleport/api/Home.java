package io.github.niestrat99.advancedteleport.api;

import org.bukkit.Location;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class Home {

    private UUID owner;
    private String name;
    private Location location;
    private long createdTime;
    private long updatedTime;
    private String createdTimeFormatted;
    private String updatedTimeFormatted;
    private SimpleDateFormat format;

    public Home(UUID owner, String name, Location location, long createdTime, long updatedTime) {
        this.name = name;
        this.owner = owner;
        this.location = location;
        this.createdTime = createdTime;
        this.updatedTime = updatedTime;

        this.format = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
        this.createdTimeFormatted = format.format(new Date(createdTime));
        this.updatedTimeFormatted = format.format(new Date(updatedTime));
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

        this.updatedTime = System.currentTimeMillis();
        this.updatedTimeFormatted = format.format(new Date(updatedTime));
    }

    public long getUpdatedTime() {
        return updatedTime;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public String getCreatedTimeFormatted() {
        return createdTimeFormatted;
    }

    public String getUpdatedTimeFormatted() {
        return updatedTimeFormatted;
    }
}
