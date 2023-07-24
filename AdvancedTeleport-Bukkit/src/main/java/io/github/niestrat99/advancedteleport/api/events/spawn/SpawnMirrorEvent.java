package io.github.niestrat99.advancedteleport.api.events.spawn;

import io.github.niestrat99.advancedteleport.api.events.TrackableATEvent;
import io.github.niestrat99.advancedteleport.api.Spawn;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** The event fired when a spawnpoint is mirrored to another world. */
public final class SpawnMirrorEvent extends TrackableATEvent {

    private static final HandlerList handlers = new HandlerList();
    private @NotNull Spawn fromSpawn;
    private @Nullable Spawn toSpawn;

    @Contract(pure = true)
    public SpawnMirrorEvent(
            @NotNull final Spawn fromSpawn,
            @Nullable final Spawn toSpawn,
            @Nullable final CommandSender sender)
            throws IllegalArgumentException {
        super(sender);

        this.fromSpawn = fromSpawn;
        this.toSpawn = toSpawn;
    }

    /**
     * Gives the name of the world that the spawn is getting mirrored from.
     *
     * @return the provided world the spawn is being mirrored from.
     */
    @Contract(pure = true)
    public @NotNull Spawn getSourceSpawn() {
        return fromSpawn;
    }

    /**
     * Gives the name of the world that the spawn is getting mirrored to.
     *
     * @return the provided world the spawn is being mirrored to.
     */
    @Contract(pure = true)
    public @Nullable Spawn getDestinationSpawn() {
        return toSpawn;
    }

    /**
     * Sets the world for the spawn to be mirrored from.
     *
     * @param fromSpawn the name of the world.
     * @throws NullPointerException if the spawn name is null.
     * @throws IllegalArgumentException if the spawn name is empty.
     */
    @Contract(pure = true)
    public void setFromSpawn(@NotNull final Spawn fromSpawn) throws IllegalArgumentException {

        this.fromSpawn = fromSpawn;
    }

    /**
     * Sets the world for the spawn to be mirrored to.
     *
     * @param toSpawn the name of the world.
     * @throws NullPointerException if the spawn name is null.
     * @throws IllegalArgumentException if the spawn name is empty.
     */
    @Contract(pure = true)
    public void setDestinationSpawn(@NotNull final Spawn toSpawn) throws IllegalArgumentException {
        if (toSpawn == fromSpawn)
            throw new IllegalArgumentException(
                    "The source spawn and destination spawn must differ!");

        this.toSpawn = toSpawn;
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
