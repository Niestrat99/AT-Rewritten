package io.github.niestrat99.advancedteleport.api.events.spawn;

import io.github.niestrat99.advancedteleport.api.events.TrackableATEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * The event fired when a spawnpoint is mirrored to another world.
 */
public class SpawnMirrorEvent extends TrackableATEvent {

    private String fromWorld;
    @NotNull
    private String toWorld;
    private static final HandlerList handlers = new HandlerList();

    public SpawnMirrorEvent(@NotNull String fromWorld, @NotNull String toWorld, @Nullable CommandSender sender) {
        super(sender);

        // Name checks
        Objects.requireNonNull(fromWorld, "The from-world name must not be null.");
        if (fromWorld.isEmpty()) throw new IllegalArgumentException("The from-world name must not be empty.");
        Objects.requireNonNull(toWorld, "The to-world name must not be null.");
        if (toWorld.isEmpty()) throw new IllegalArgumentException("The to-world name must not be empty.");

        this.fromWorld = fromWorld;
        this.toWorld = toWorld;
    }

    /**
     * Gives the name of the world that the spawn is getting mirrored from.
     *
     * @return the provided world the spawn is being mirrored from.
     */
    @NotNull
    public String getFromWorld() {
        return fromWorld;
    }

    /**
     * Gives the name of the world that the spawn is getting mirrored to.
     *
     * @return the provided world the spawn is being mirrored to.
     */
    @NotNull
    public String getToWorld() {
        return toWorld;
    }

    /**
     * Sets the world for the spawn to be mirrored from.
     *
     * @param fromWorld the name of the world.
     * @throws NullPointerException     if the spawn name is null.
     * @throws IllegalArgumentException if the spawn name is empty.
     */
    public void setFromWorld(@NotNull String fromWorld) {
        Objects.requireNonNull(fromWorld, "The world name must not be null.");
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
    public void setToWorld(@NotNull String toWorld) {
        Objects.requireNonNull(toWorld, "The world name must not be null.");
        if (toWorld.isEmpty()) throw new IllegalArgumentException("The world name must not be empty.");

        this.toWorld = toWorld;
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
