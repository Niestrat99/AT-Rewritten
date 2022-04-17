package io.github.niestrat99.advancedteleport.api;

import io.github.niestrat99.advancedteleport.sql.SQLManager;
import org.bukkit.Location;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Represents a player's private teleportation point, known as a home.
 */
public class Home {

    // The UUID of the home owner.
    private UUID owner;
    // The name of the home.
    private String name;
    // The location of the home.
    private Location location;
    // When the home was made.
    private long createdTime;
    // When the home was last updated.
    private long updatedTime;
    private String createdTimeFormatted;
    private String updatedTimeFormatted;
    private final SimpleDateFormat format;

    /**
     * Creates a home object. Please note this does not add a home to the saved data; instead, use {@link ATPlayer#addHome(String, Location, SQLManager.SQLCallback)}.
     *
     * @param owner The owner of the house.
     * @param name The name of the house.
     * @param location Where the house is located.
     * @param createdTime When the house was created in milliseconds.
     * @param updatedTime When the house was last updated in milliseconds.
     */
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

    /**
     * Returns the location of the house.
     *
     * @return the location of the house.
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Returns the name of the house.
     *
     * @return the name of the house.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the UUID of the home's owner. The actual player object can be fetched using {@link org.bukkit.Bukkit#getPlayer(UUID)}.
     *
     * @return the home's owner UUID.
     */
    public UUID getOwner() {
        return owner;
    }

    /**
     * Sets the location of the house. This also updates the last updated timestamp.
     *
     * @param location The new location that the home will be set to.
     */
    public void setLocation(Location location) {
        this.location = location;

        this.updatedTime = System.currentTimeMillis();
        this.updatedTimeFormatted = format.format(new Date(updatedTime));
    }

    /**
     * Gets the last updated time in milliseconds, starting from the 1st January 1970.
     *
     * @return the last updated time.
     */
    public long getUpdatedTime() {
        return updatedTime;
    }

    /**
     * Gets the time the house was created in milliseconds, starting from the 1st January 1970.
     *
     * @return the time the house was created in milliseconds.
     */
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
