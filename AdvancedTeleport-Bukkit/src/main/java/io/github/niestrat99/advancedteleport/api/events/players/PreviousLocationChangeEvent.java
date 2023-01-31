package io.github.niestrat99.advancedteleport.api.events.players;

import io.github.niestrat99.advancedteleport.api.events.CancellableATEvent;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The event fired when a player's previous location changes, whether it is through /back or just teleporting.
 */
public final class PreviousLocationChangeEvent extends CancellableATEvent {

    private static final HandlerList handlers = new HandlerList();
    private final @Nullable Location oldLocation;
    private @Nullable Location newLocation;
    private @NotNull OfflinePlayer player;

    @Contract(pure = true)
    public PreviousLocationChangeEvent(
        @NotNull final OfflinePlayer player,
        @NotNull final Location newLocation,
        @Nullable final Location oldLocation
    ) throws IllegalArgumentException {
        if (!newLocation.isWorldLoaded()) throw new IllegalStateException("The new location's world is not loaded.");
        this.player = player;
        this.newLocation = newLocation;
        this.oldLocation = oldLocation;
    }

    /**
     * Gets the player whose previous location is changing.
     *
     * @return the player whose previous location is changing.
     */
    @Contract(pure = true)
    public @NotNull OfflinePlayer getPlayer() {
        return player;
    }

    /**
     * Sets the player that will have their previous location changed.
     *
     * @param player the player to change to.
     * @throws NullPointerException if player is null.
     */
    @Contract(pure = true)
    public void setPlayer(@NotNull final OfflinePlayer player) {
        this.player = player;
    }

    /**
     * Gets the new location that will become the player's previous location. This is not where the player currently is.
     *
     * @return the new location that is to be the previous location. Can be null if modified.
     */
    @Contract(pure = true)
    public @Nullable Location getNewLocation() {
        return newLocation;
    }

    /**
     * Sets the location that will become the player's previous location.
     *
     * @param newLocation the location to become the player's previous location. Can be null.
     */
    @Contract(pure = true)
    public void setNewLocation(@Nullable final Location newLocation) {
        this.newLocation = newLocation;
    }

    /**
     * Gets the current previous location of the player.
     *
     * @return the current previous location, or null if there was none prior.
     */
    @Contract(pure = true)
    public @Nullable Location getOldLocation() {
        return oldLocation;
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
