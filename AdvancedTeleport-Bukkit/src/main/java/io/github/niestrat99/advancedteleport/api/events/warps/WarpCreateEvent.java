package io.github.niestrat99.advancedteleport.api.events.warps;

import io.github.niestrat99.advancedteleport.api.events.TrackableATEvent;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The event fired when a warp is created.
 */
public final class WarpCreateEvent extends TrackableATEvent {

    private static final HandlerList handlers = new HandlerList();
    private @NotNull String name;
    private @NotNull Location location;

    @Contract(pure = true)
    public WarpCreateEvent(
        @NotNull final String name,
        @Nullable final CommandSender sender,
        @NotNull final Location location
    ) throws IllegalArgumentException, IllegalStateException {
        super(sender);
        if (name.isEmpty()) throw new IllegalArgumentException("The warp name must not be empty.");
        if (!location.isWorldLoaded()) throw new IllegalStateException("The location's world is not loaded.");

        this.name = name;
        this.location = location;
    }

    /**
     * Gives the name of the warp to be created.
     *
     * @return the provided name.
     */
    @Contract(pure = true)
    public @NotNull String getName() {
        return name;
    }

    /**
     * Gives the location of the warp to be created.
     *
     * @return the provided location.
     */
    @Contract(pure = true)
    public @NotNull Location getLocation() {
        return location;
    }

    /**
     * Sets the name of the warp.
     *
     * @param name the name to be used.
     * @throws NullPointerException if the warp name is null.
     * @throws IllegalArgumentException if the warp name is empty.
     */
    @Contract(pure = true)
    public void setName(@NotNull final String name) throws IllegalArgumentException {
        if (name.isEmpty()) throw new IllegalArgumentException("The warp name must not be empty.");

        this.name = name;
    }

    /**
     * Sets the location of the warp.
     *
     * @param location the location to be used.
     * @throws NullPointerException if the location is null.
     * @throws IllegalStateException if the location's world is unloaded.
     */
    @Contract(pure = true)
    public void setLocation(@NotNull final Location location) throws IllegalStateException {
        if (!location.isWorldLoaded()) throw new IllegalStateException("The location's world is not loaded.");
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
