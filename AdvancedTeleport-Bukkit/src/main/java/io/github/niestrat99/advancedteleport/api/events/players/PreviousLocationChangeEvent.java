package io.github.niestrat99.advancedteleport.api.events.players;

import io.github.niestrat99.advancedteleport.api.events.CancellableATEvent;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * The event fired when a player's previous location changes, whether it is through /back or just teleporting.
 */
public class PreviousLocationChangeEvent extends CancellableATEvent {

    private static final HandlerList handlers = new HandlerList();
    private final @Nullable Location oldLocation;
    private @Nullable Location newLocation;
    private @NotNull OfflinePlayer player;

    public PreviousLocationChangeEvent(@NotNull OfflinePlayer player, @Nullable Location newLocation, @Nullable Location oldLocation) {
        Objects.requireNonNull(player, "The player must not be null.");
        // Location checks
        Objects.requireNonNull(newLocation, "The new location must not be null.");
        if (!newLocation.isWorldLoaded()) throw new IllegalStateException("The new location's world is not loaded.");
        // Assign everything
        this.player = player;
        this.newLocation = newLocation;
        this.oldLocation = oldLocation;
    }

    /**
     * Gets the player whose previous location is changing.
     *
     * @return the player whose previous location is changing.
     */
    @NotNull
    public OfflinePlayer getPlayer() {
        return player;
    }

    /**
     * Gets the new location that will become the player's previous location. This is not where the player currently is.
     *
     * @return the new location that is to be the previous location. Can be null if modified
     */
    @Nullable
    public Location getNewLocation() {
        return newLocation;
    }

    /**
     * Gets the current previous location of the player.
     *
     * @return the current previous location, or null if there was none prior.
     */
    @Nullable
    public Location getOldLocation() {
        return oldLocation;
    }

    /**
     * Sets the location that will become the player's previous location.
     *
     * @param newLocation the location to become the player's previous location. Can be null.
     */
    public void setNewLocation(@Nullable Location newLocation) {
        this.newLocation = newLocation;
    }

    /**
     * Sets the player that will have their previous location changed.
     *
     * @param player the player to change to.
     * @throws NullPointerException if player is null.
     */
    public void setPlayer(@NotNull OfflinePlayer player) {
        Objects.requireNonNull(player, "The player must not be null.");
        this.player = player;
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
