package io.github.niestrat99.advancedteleport.api;

import io.github.niestrat99.advancedteleport.api.events.homes.HomeMoveEvent;
import io.github.niestrat99.advancedteleport.sql.HomeSQLManager;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/** Represents a player's private teleportation point, known as a home. */
public final class Home implements NamedLocation {

    // The UUID of the home owner.
    private final @NotNull UUID owner;
    // The name of the home.
    private final @NotNull String name;
    private final @NotNull String createdTimeFormatted;
    private final @NotNull SimpleDateFormat format;
    private final long createdTime;
    private @NotNull String updatedTimeFormatted;
    private @NotNull Location location;
    private long updatedTime;

    /**
     * Creates a home object. Please note this does not add a home to the saved data; instead, use
     * {@link ATPlayer#addHome(String, Location, org.bukkit.entity.Player)}.
     *
     * @param owner The owner of the house.
     * @param name The name of the house.
     * @param location Where the house is located.
     * @param createdTime When the house was created in milliseconds.
     * @param updatedTime When the house was last updated in milliseconds.
     */
    @Contract(pure = true)
    public Home(
            @NotNull final UUID owner,
            @NotNull final String name,
            @NotNull final Location location,
            final long createdTime,
            final long updatedTime) {
        this.name = name;
        this.owner = owner;
        this.location = location instanceof WorldlessLocation ? location : new WorldlessLocation(location, location.getWorld().getName());
        this.createdTime = createdTime;
        this.updatedTime = updatedTime;

        this.format = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
        this.createdTimeFormatted = format.format(new Date(createdTime));
        this.updatedTimeFormatted = format.format(new Date(updatedTime));
    }

    /**
     * Returns the name of the house.
     *
     * @return the name of the house.
     */
    @Contract(pure = true)
    public @NotNull String getName() {
        return name;
    }

    /**
     * Returns the location of the house.
     *
     * @return the location of the house.
     */
    @Contract(pure = true)
    public @NotNull Location getLocation() {
        return location;
    }

    /**
     * Returns the UUID of the home's owner. The actual player object can be fetched using {@link
     * org.bukkit.Bukkit#getPlayer(UUID)}.
     *
     * @return the home's owner UUID.
     */
    @Contract(pure = true)
    public @NotNull UUID getOwner() {
        return owner;
    }

    /**
     * Sets the location of the house to a different location. This also updates the last updated
     * timestamp.
     *
     * @param location The new location that the home will be set to.
     * @return a completable void of the task.
     */
    public @NotNull CompletableFuture<Void> move(
            @NotNull final Location location, @Nullable final CommandSender sender) {

        // Validate the event
        return AdvancedTeleportAPI.validateEvent(
                new HomeMoveEvent(this, location, sender),
                event -> {
                    this.location = event.getLocation();

                    this.updatedTime = System.currentTimeMillis();
                    this.updatedTimeFormatted = format.format(new Date(updatedTime));

                    HomeSQLManager.get().moveHome(location, owner, name, false);
                    return null;
                });
    }

    /**
     * Gets the last updated time in milliseconds, starting from the 1st January 1970.
     *
     * @return the last updated time.
     */
    @Contract(pure = true)
    public long getUpdatedTime() {
        return updatedTime;
    }

    /**
     * Gets the time the house was created in milliseconds, starting from the 1st January 1970.
     *
     * @return the time the house was created in milliseconds.
     */
    @Contract(pure = true)
    public long getCreatedTime() {
        return createdTime;
    }

    /**
     * Gets the formatted timestamp of when the home was created. This is formatted as dd MMM yyyy
     * HH:mm:ss.
     *
     * @return the formatted timestamp of when the home was created.
     */
    @Contract(pure = true)
    public @NotNull String getCreatedTimeFormatted() {
        return createdTimeFormatted;
    }

    /**
     * Gets the formatted timestamp of when the home was last updated. This is formatted as dd MMM
     * yyyy HH:mm:ss.
     *
     * @return the formatted timestamp of when the home was last update.
     */
    @Contract(pure = true)
    public @NotNull String getUpdatedTimeFormatted() {
        return updatedTimeFormatted;
    }
}
