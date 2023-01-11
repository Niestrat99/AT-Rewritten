package io.github.niestrat99.advancedteleport.api.events.homes;

import io.github.niestrat99.advancedteleport.api.Home;
import io.github.niestrat99.advancedteleport.api.events.TrackableATEvent;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * The event that is fired when a home is moved.
 */
public class HomeMoveEvent extends TrackableATEvent {

    private static final HandlerList handlers = new HandlerList();
    private final @NotNull Home home;
    private @NotNull Location location;

    public HomeMoveEvent(@NotNull Home home, @NotNull Location location, @Nullable CommandSender sender) {
        super(sender);
        this.home = home;
        this.location = location;
    }

    /**
     * Gives the home being moved.
     *
     * @return the home being moved.
     */
    @NotNull
    public Home getHome() {
        return home;
    }

    /**
     * Gives the location that the home is moving to.
     *
     * @return the location the home is moving to.
     */
    @NotNull
    public Location getLocation() {
        return location;
    }

    /**
     * Sets the location that the home is moving to.
     *
     * @param location the new location to set to.
     */
    public void setLocation(@NotNull Location location) {
        // Location checks
        Objects.requireNonNull(location, "The new location must not be null.");
        if (!location.isWorldLoaded()) throw new IllegalStateException("The new location's world is not loaded.");
        this.location = location;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
