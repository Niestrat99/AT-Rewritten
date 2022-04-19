package io.github.niestrat99.advancedteleport.api.events.homes;

import io.github.niestrat99.advancedteleport.api.events.TrackableATEvent;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * The event fired when a player - or admin - creates a new home
 */
public class HomeCreateEvent extends TrackableATEvent {

    @NotNull
    private OfflinePlayer player;
    @NotNull
    private String name;
    @NotNull
    private Location location;
    private static final HandlerList handlers = new HandlerList();

    public HomeCreateEvent(@NotNull OfflinePlayer player, @NotNull String name, @NotNull Location location, @Nullable Player creator) {
        super(creator);
        // Home name checks
        Objects.requireNonNull(name, "The home name must not be null.");
        if (name.isEmpty()) throw new IllegalArgumentException("The home name must not be empty.");
        // Player checks
        Objects.requireNonNull(player, "The player must not be null.");
        // Location checks
        Objects.requireNonNull(location, "The location must not be null.");
        if (!location.isWorldLoaded()) throw new IllegalStateException("The location's world is not loaded.");

        this.player = player;
        this.name = name;
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

    @NotNull
    public OfflinePlayer getPlayer() {
        return player;
    }

    @NotNull
    public Location getLocation() {
        return location;
    }

    @NotNull
    public String getName() {
        return name;
    }

    public void setName(@NotNull String name) {
        Objects.requireNonNull(name, "The home name must not be null.");
        if (name.isEmpty()) throw new IllegalArgumentException("The home name must not be empty.");
        this.name = name;
    }

    public void setLocation(@NotNull Location location) {
        Objects.requireNonNull(location, "The location must not be null.");
        if (!location.isWorldLoaded()) throw new IllegalStateException("The location's world is not loaded.");
        this.location = location;
    }

    public void setPlayer(@NotNull OfflinePlayer player) {
        Objects.requireNonNull(player, "The player must not be null.");
        this.player = player;
    }
}
