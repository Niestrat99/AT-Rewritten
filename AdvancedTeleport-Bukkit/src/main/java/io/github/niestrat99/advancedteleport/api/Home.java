package io.github.niestrat99.advancedteleport.api;

import io.github.niestrat99.advancedteleport.sql.HomeSQLManager;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Represents a player's private teleportation point, known as a home.
 */
public class Home implements NamedLocation {

    // The UUID of the home owner.
    private final @NotNull UUID owner;
    // The name of the home.
    private final @NotNull String name;
    private final @NotNull String createdTimeFormatted;
    private final @NotNull SimpleDateFormat format;
    // When the home was made.
    private final long createdTime;
    // When the home was last updated.
    private long updatedTime;
    private @NotNull String updatedTimeFormatted;
    // The location of the home.
    private @NotNull Location location;

    /**
     * Creates a home object. Please note this does not add a home to the saved data; instead, use {@link ATPlayer#addHome(String, Location, org.bukkit.entity.Player)}.
     *
     * @param owner The owner of the house.
     * @param name The name of the house.
     * @param location Where the house is located.
     * @param createdTime When the house was created in milliseconds.
     * @param updatedTime When the house was last updated in milliseconds.
     */
    public Home(@NotNull UUID owner, @NotNull String name, @NotNull Location location, long createdTime, long updatedTime) {
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
    @NotNull
    public Location getLocation() {
        return location;
    }

    /**
     * Returns the name of the house.
     *
     * @return the name of the house.
     */
    @NotNull
    public String getName() {
        return name;
    }

    /**
     * Returns the UUID of the home's owner. The actual player object can be fetched using {@link org.bukkit.Bukkit#getPlayer(UUID)}.
     *
     * @return the home's owner UUID.
     */
    @NotNull
    public UUID getOwner() {
        return owner;
    }

    /**
     * Sets the location of the house. This also updates the last updated timestamp.
     *
     * @param location The new location that the home will be set to.
     * @deprecated use {@link Home#move(Location)} instead.
     */
    @Deprecated
    public void setLocation(@NotNull Location location) {
        move(location);
    }

    /**
     * Sets the location of the house to a different location. This also updates the last updated timestamp.
     *
     * @param location The new location that the home will be set to.
     * @return true if the move succeeded, false if it failed.
     */
    public CompletableFuture<Boolean> move(@NotNull Location location) {
        this.location = location;

        this.updatedTime = System.currentTimeMillis();
        this.updatedTimeFormatted = format.format(new Date(updatedTime));

        return CompletableFuture.supplyAsync(() -> {
            AdvancedTeleportAPI.FlattenedCallback<Boolean> callback = new AdvancedTeleportAPI.FlattenedCallback<>();
            HomeSQLManager.get().moveHome(location, owner, name, callback);
            return callback.data;
        });

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

    /**
     * Gets the formatted timestamp of when the home was created. This is formatted as dd MMM yyyy HH:mm:ss.
     *
     * @return the formatted timestamp of when the home was created.
     */
    @NotNull
    public String getCreatedTimeFormatted() {
        return createdTimeFormatted;
    }

    /**
     * Gets the formatted timestamp of when the home was last updated. This is formatted as dd MMM yyyy HH:mm:ss.
     *
     * @return the formatted timestamp of when the home was last update.
     */
    @NotNull
    public String getUpdatedTimeFormatted() {
        return updatedTimeFormatted;
    }
}
