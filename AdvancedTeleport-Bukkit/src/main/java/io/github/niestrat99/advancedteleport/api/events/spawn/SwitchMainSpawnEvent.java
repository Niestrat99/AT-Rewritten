package io.github.niestrat99.advancedteleport.api.events.spawn;

import io.github.niestrat99.advancedteleport.api.events.TrackableATEvent;
import io.github.niestrat99.advancedteleport.api.Spawn;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** The event fired when the main spawnpoint is switched. */
public final class SwitchMainSpawnEvent extends TrackableATEvent {

    private static final HandlerList handlers = new HandlerList();
    private final @Nullable Spawn oldMainSpawn;
    private @Nullable Spawn newMainSpawn;

    @Contract(pure = true)
    public SwitchMainSpawnEvent(
            @Nullable final Spawn oldMainSpawn,
            @Nullable final Spawn newMainSpawn,
            @Nullable final CommandSender sender) {
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
    public @Nullable Spawn getOldMainSpawn() {
        return oldMainSpawn;
    }

    /**
     * Gets the new main spawn of the player.
     *
     * @return the new main spawn.
     */
    @Contract(pure = true)
    public @Nullable Spawn getNewMainSpawn() {
        return newMainSpawn;
    }

    /**
     * Sets the new main spawn of the player.
     *
     * @param newMainSpawn the new main spawn question.
     */
    @Contract(pure = true)
    public void setNewMainSpawn(@Nullable final Spawn newMainSpawn)
            throws IllegalArgumentException {
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
