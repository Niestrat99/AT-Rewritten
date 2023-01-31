package io.github.niestrat99.advancedteleport.api.events.spawn;

import io.github.niestrat99.advancedteleport.api.events.TrackableATEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The event fired when the main spawnpoint is switched.
 */
public final class SwitchMainSpawnEvent extends TrackableATEvent {

    private static final HandlerList handlers = new HandlerList();
    private final @Nullable String oldMainSpawn;
    private @NotNull String newMainSpawn;

    @Contract(pure = true)
    public SwitchMainSpawnEvent(
        @Nullable final String oldMainSpawn,
        @NotNull final String newMainSpawn,
        @Nullable final CommandSender sender
    ) {
        super(sender);
        this.oldMainSpawn = oldMainSpawn;
        this.newMainSpawn = newMainSpawn;
    }

    /**
     * Gets the current - becoming old - main spawn of the player.
     *
     * @return the current main spawn.
     */
    @Contract(pure = true)
    public @Nullable String getOldMainSpawn() {
        return oldMainSpawn;
    }

    /**
     * Gets the new main spawn of the player.
     *
     * @return the new main spawn.
     */
    @Contract(pure = true)
    public @NotNull String getNewMainSpawn() {
        return newMainSpawn;
    }

    /**
     * Sets the new main spawn of the player.
     *
     * @param newMainSpawn the new main spawn question.
     */
    @Contract(pure = true)
    public void setNewMainSpawn(@NotNull final String newMainSpawn) throws IllegalArgumentException {
        if (newMainSpawn.isEmpty()) throw new IllegalArgumentException("The new main spawn must not be empty.");
        this.newMainSpawn = newMainSpawn;
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
