package io.github.niestrat99.advancedteleport.api.events.homes;

import io.github.niestrat99.advancedteleport.api.events.TrackableATEvent;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The event fired when a player - or admin - creates a new home.
 */
public final class HomeCreateEvent extends TrackableATEvent {

    private static final HandlerList handlers = new HandlerList();
    private @NotNull OfflinePlayer player;
    private @NotNull String name;
    private @NotNull Location location;

    @Contract(pure = true)
    public HomeCreateEvent(
        @NotNull final OfflinePlayer player,
        @NotNull final String name,
        @NotNull final Location location,
        @Nullable final Player creator
    ) throws IllegalArgumentException, IllegalStateException {
        super(creator);

        if (name.isEmpty()) throw new IllegalArgumentException("The home name must not be empty.");
        if (!location.isWorldLoaded()) throw new IllegalStateException("The location's world is not loaded.");

        this.player = player;
        this.name = name;
        this.location = location;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    @Contract(pure = true)
    public static @NotNull HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * Gets the owner of the home.
     *
     * @return the owner of the home.
     */
    @Contract(pure = true)
    public @NotNull OfflinePlayer getPlayer() {
        return player;
    }

    /**
     * Gets the location of the home.
     *
     * @return the location of the home.
     */
    @Contract(pure = true)
    public @NotNull Location getLocation() {
        return location;
    }

    /**
     * Gets the name of the home.
     *
     * @return the name of the home.
     */
    @Contract(pure = true)
    public @NotNull String getName() {
        return name;
    }

    /**
     * Sets the name of the home.
     *
     * @param name the new name to be used.
     * @throws NullPointerException if the name is null.
     * @throws IllegalArgumentException if the name is empty.
     */
    @Contract(pure = true)
    public void setName(@NotNull final String name) throws IllegalArgumentException {
        if (name.isEmpty()) throw new IllegalArgumentException("The home name must not be empty.");
        this.name = name;
    }

    /**
     * Sets the location of the home.
     *
     * @param location the new location of the home.
     * @throws NullPointerException if the location is null.
     * @throws IllegalStateException if the location's world isn't loaded.
     */
    @Contract(pure = true)
    public void setLocation(@NotNull final Location location) throws IllegalStateException {
        if (!location.isWorldLoaded()) throw new IllegalStateException("The location's world is not loaded.");
        this.location = location;
    }

    /**
     * Sets the owner of the home.
     *
     * @param player the new owner of the home.
     * @throws NullPointerException if the player is null.
     */
    @Contract(pure = true)
    public void setPlayer(@NotNull final OfflinePlayer player) {
        this.player = player;
    }
}
