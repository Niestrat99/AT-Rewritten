package io.github.niestrat99.advancedteleport.api.events.spawn;

import io.github.niestrat99.advancedteleport.api.events.TrackableATEvent;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** The event fired when a new spawnpoint is created. */
public final class SpawnCreateEvent extends TrackableATEvent {

    private static final HandlerList handlers = new HandlerList();
    private @NotNull String name;
    private @NotNull Location location;

    @Contract(pure = true)
    public SpawnCreateEvent(
            @NotNull final String name,
            @Nullable final CommandSender sender,
            @NotNull final Location location)
            throws IllegalArgumentException, IllegalStateException {
        super(sender);

        // Name checks
        if (name.isEmpty()) throw new IllegalArgumentException("The spawn name must not be empty.");
        // Location checks
        if (!location.isWorldLoaded())
            throw new IllegalStateException("The location's world is not loaded.");

        this.name = name;
        this.location = location;
    }

    /**
     * Gives the name of the spawn to be created.
     *
     * @return the provided name.
     */
    @Contract(pure = true)
    public @NotNull String getName() {
        return name;
    }

    /**
     * Sets the name of the spawn.
     *
     * @param name the name to be used.
     * @throws NullPointerException if the spawn name is null.
     * @throws IllegalArgumentException if the spawn name is empty.
     */
    @Contract(pure = true)
    public void setName(@NotNull final String name) throws IllegalArgumentException {
        if (name.isEmpty()) throw new IllegalArgumentException("The spawn name must not be empty.");

        this.name = name;
    }

    /**
     * Gives the location of the spawn to be created.
     *
     * @return the provided location.
     */
    @Contract(pure = true)
    public @NotNull Location getLocation() {
        return location;
    }

    /**
     * Sets the location of the spawn.
     *
     * @param location the location to be used.
     * @throws NullPointerException if the location is null.
     * @throws IllegalStateException if the location's world is unloaded.
     */
    @Contract(pure = true)
    public void setLocation(@NotNull final Location location) throws IllegalStateException {
        if (!location.isWorldLoaded())
            throw new IllegalStateException("The location's world is not loaded.");
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
