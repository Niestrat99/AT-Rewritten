package io.github.niestrat99.advancedteleport.api.events.spawn;

import io.github.niestrat99.advancedteleport.api.events.TrackableATEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * The event fired when a spawnpoint is mirrored to another world.
 */
public final class SpawnMirrorEvent extends TrackableATEvent {

    private String fromWorld;
    private @NotNull String toWorld;
    private static final HandlerList handlers = new HandlerList();

    @Contract(pure = true)
    public SpawnMirrorEvent(
        @NotNull final String fromWorld,
        @NotNull final String toWorld,
        @Nullable final CommandSender sender
    ) throws IllegalArgumentException {
        super(sender);
        if (fromWorld.isEmpty()) throw new IllegalArgumentException("The from-world name must not be empty.");
        if (toWorld.isEmpty()) throw new IllegalArgumentException("The to-world name must not be empty.");

        this.fromWorld = fromWorld;
        this.toWorld = toWorld;
    }

    /**
     * Gives the name of the world that the spawn is getting mirrored from.
     *
     * @return the provided world the spawn is being mirrored from.
     */
    @Contract(pure = true)
    public @NotNull String getFromWorld() {
        return fromWorld;
    }

    /**
     * Gives the name of the world that the spawn is getting mirrored to.
     *
     * @return the provided world the spawn is being mirrored to.
     */
    @Contract(pure = true)
    public @NotNull String getToWorld() {
        return toWorld;
    }

    /**
     * Sets the world for the spawn to be mirrored from.
     *
     * @param fromWorld the name of the world.
     * @throws NullPointerException     if the spawn name is null.
     * @throws IllegalArgumentException if the spawn name is empty.
     */
    @Contract(pure = true)
    public void setFromWorld(@NotNull final String fromWorld) throws IllegalArgumentException {
        if (fromWorld.isEmpty()) throw new IllegalArgumentException("The world name must not be empty.");

        this.fromWorld = fromWorld;
    }

    /**
     * Sets the world for the spawn to be mirrored to.
     *
     * @param toWorld the name of the world.
     * @throws NullPointerException     if the spawn name is null.
     * @throws IllegalArgumentException if the spawn name is empty.
     */
    @Contract(pure = true)
    public void setToWorld(@NotNull final String toWorld) throws IllegalArgumentException {
        Objects.requireNonNull(toWorld, "The world name must not be null.");
        if (toWorld.isEmpty()) throw new IllegalArgumentException("The world name must not be empty.");

        this.toWorld = toWorld;
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
