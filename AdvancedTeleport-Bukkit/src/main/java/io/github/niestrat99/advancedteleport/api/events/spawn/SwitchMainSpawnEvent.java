package io.github.niestrat99.advancedteleport.api.events.spawn;

import io.github.niestrat99.advancedteleport.api.events.TrackableATEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * The event fired when the main spawnpoint is switched.
 */
public class SwitchMainSpawnEvent extends TrackableATEvent {

    private static final HandlerList handlers = new HandlerList();
    @Nullable
    private final String oldMainSpawn;
    @NotNull
    private String newMainSpawn;

    public SwitchMainSpawnEvent(@Nullable String oldMainSpawn, @NotNull String newMainSpawn, @Nullable CommandSender sender) {
        super(sender);
        this.oldMainSpawn = oldMainSpawn;
        this.newMainSpawn = newMainSpawn;
    }

    /**
     * Gets the current - becoming old - main spawn of the player.
     *
     * @return the current main spawn.
     */
    @Nullable
    public String getOldMainSpawn() {
        return oldMainSpawn;
    }

    /**
     * Gets the new main spawn of the player.
     *
     * @return the new main spawn.
     */
    @NotNull
    public String getNewMainSpawn() {
        return newMainSpawn;
    }

    /**
     * Sets the new main spawn of the player.
     *
     * @param newMainSpawn the new main spawn question.
     */
    public void setNewMainSpawn(@NotNull String newMainSpawn) {
        Objects.requireNonNull(newMainSpawn, "The new main spawn cannot be null.");
        this.newMainSpawn = newMainSpawn;
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
