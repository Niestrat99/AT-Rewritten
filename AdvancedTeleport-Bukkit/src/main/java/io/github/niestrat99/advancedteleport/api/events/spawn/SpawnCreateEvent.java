package io.github.niestrat99.advancedteleport.api.events.spawn;

import io.github.niestrat99.advancedteleport.api.events.TrackableATEvent;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * The event fired when a new spawnpoint is created.
 */
public class SpawnCreateEvent extends TrackableATEvent {

    @NotNull
    private String name;
    @NotNull
    private Location location;
    private static final HandlerList handlers = new HandlerList();

    public SpawnCreateEvent(@NotNull String name, @Nullable CommandSender sender, @NotNull Location location) {
        super(sender);
        // Name checks
        Objects.requireNonNull(name, "The spawn name must not be null.");
        if (name.isEmpty()) throw new IllegalArgumentException("The spawn name must not be empty.");
        // Location checks
        Objects.requireNonNull(location, "The location must not be null.");
        if (!location.isWorldLoaded()) throw new IllegalStateException("The location's world is not loaded.");

        this.name = name;
        this.location = location;
    }

    /**
     * Gives the name of the spawn to be created.
     *
     * @return the provided name.
     */
    @NotNull
    public String getName() {
        return name;
    }

    /**
     * Gives the location of the spawn to be created.
     *
     * @return the provided location.
     */
    @NotNull
    public Location getLocation() {
        return location;
    }

    /**
     * Sets the name of the spawn.
     *
     * @param name the name to be used.
     * @throws NullPointerException if the spawn name is null.
     * @throws IllegalArgumentException if the spawn name is empty.
     */
    public void setName(@NotNull String name) {
        Objects.requireNonNull(name, "The spawn name must not be null.");
        if (name.isEmpty()) throw new IllegalArgumentException("The spawn name must not be empty.");

        this.name = name;
    }

    /**
     * Sets the location of the spawn.
     *
     * @param location the location to be used.
     * @throws NullPointerException if the location is null.
     * @throws IllegalStateException if the location's world is unloaded.
     */
    public void setLocation(@NotNull Location location) {
        // Location checks
        Objects.requireNonNull(location, "The location must not be null.");
        if (!location.isWorldLoaded()) throw new IllegalStateException("The location's world is not loaded.");
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
