package io.github.niestrat99.advancedteleport.api.events.homes;

import io.github.niestrat99.advancedteleport.api.Home;
import io.github.niestrat99.advancedteleport.api.events.TrackableATEvent;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The event that is fired when a home is moved.
 */
public class HomeMoveEvent extends TrackableATEvent {

    private static final HandlerList handlers = new HandlerList();
    @NotNull private final Home home;
    @NotNull private Location location;

    @Contract(pure = true)
    public HomeMoveEvent(
        @NotNull final Home home,
        @NotNull final Location location,
        @Nullable final CommandSender sender
    ) {
        super(sender);
        this.home = home;
        this.location = location;
    }

    /**
     * Gives the home being moved.
     *
     * @return the home being moved.
     */
    @Contract(pure = true)
    public @NotNull Home getHome() {
        return home;
    }

    /**
     * Gives the location that the home is moving to.
     *
     * @return the location the home is moving to.
     */
    @Contract(pure = true)
    public @NotNull Location getLocation() {
        return location;
    }

    /**
     * Sets the location that the home is moving to.
     *
     * @param location the new location to set to.
     */
    @Contract(pure = true)
    public void setLocation(@NotNull final Location location) {
        if (!location.isWorldLoaded()) throw new IllegalStateException("The new location's world is not loaded.");
        this.location = location;
    }

    @Override
    @Contract(pure = true)
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    @Contract(pure = true)
    public static @NotNull HandlerList getHandlerList() {
        return handlers;
    }
}
