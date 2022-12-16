package io.github.niestrat99.advancedteleport.api.events.spawn;

import io.github.niestrat99.advancedteleport.api.events.TrackableATEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The event fired when a spawnpoint is removed.
 */
public final class SpawnRemoveEvent extends TrackableATEvent {

    @NotNull private final String name;
    private static final HandlerList handlers = new HandlerList();

    @Contract(pure = true)
    public SpawnRemoveEvent(
        @NotNull final String name,
        @Nullable final CommandSender sender
    ) {
        super(sender);
        this.name = name;
    }

    /**
     * Returns the spawnpoint being deleted.
     *
     * @return the name of the spawn being deleted.
     */
    @Contract(pure = true)
    public @NotNull String getSpawnName() {
        return name;
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
