package io.github.niestrat99.advancedteleport.api.events.warps;

import io.github.niestrat99.advancedteleport.api.Warp;
import io.github.niestrat99.advancedteleport.api.events.TrackableATEvent;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The event fired when a warp is moved.
 */
public final class WarpMoveEvent extends TrackableATEvent {

    private static final HandlerList handlers = new HandlerList();
    private final @NotNull Warp warp;
    private @NotNull Location location;

    @Contract(pure = true)
    public WarpMoveEvent(
        @NotNull final Warp warp,
        @NotNull final Location location,
        @Nullable final CommandSender sender
    ) {
        super(sender);
        this.warp = warp;
        this.location = location;
    }

    /**
     * Gives the warp to be moved.
     *
     * @return the warp to be moved.
     */
    @Contract(pure = true)
    public @NotNull Warp getWarp() {
        return warp;
    }

    /**
     * Gets the location the warp is moving to.
     *
     * @return the upcoming location of the warp.
     */
    @Contract(pure = true)
    public @NotNull Location getLocation() {
        return location;
    }

    /**
     * Sets the location the warp is being moved to.
     *
     * @param location the location to be moved to.
     * @throws NullPointerException  if the location is null.
     * @throws IllegalStateException if the location's world is not loaded.
     */
    @Contract(pure = true)
    public void setLocation(@NotNull final Location location) throws IllegalStateException {
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
