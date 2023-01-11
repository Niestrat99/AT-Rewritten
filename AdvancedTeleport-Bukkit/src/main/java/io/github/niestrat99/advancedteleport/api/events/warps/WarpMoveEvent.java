package io.github.niestrat99.advancedteleport.api.events.warps;

import io.github.niestrat99.advancedteleport.api.Warp;
import io.github.niestrat99.advancedteleport.api.events.TrackableATEvent;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * The event fired when a warp is moved.
 */
public class WarpMoveEvent extends TrackableATEvent {

    private static final HandlerList handlers = new HandlerList();
    private final @NotNull Warp warp;
    private @NotNull Location location;

    public WarpMoveEvent(@NotNull Warp warp, @NotNull Location location, @Nullable CommandSender sender) {
        super(sender);
        this.warp = warp;
        this.location = location;
    }

    /**
     * Gives the warp to be moved.
     *
     * @return the warp to be moved.
     */
    @NotNull
    public Warp getWarp() {
        return warp;
    }

    /**
     * Sets the location the warp is being moved to.
     *
     * @param location the location to be moved to.
     * @throws NullPointerException if the location is null.
     * @throws IllegalStateException if the location's world is not loaded.
     */
    public void setLocation(@NotNull Location location) {
        // Location checks
        Objects.requireNonNull(location, "The new location must not be null.");
        if (!location.isWorldLoaded()) throw new IllegalStateException("The new location's world is not loaded.");
        this.location = location;
    }

    /**
     * Gets the location the warp is moving to.
     *
     * @return the upcoming location of the warp.
     */
    @NotNull
    public Location getLocation() {
        return location;
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
