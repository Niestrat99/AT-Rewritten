package io.github.niestrat99.advancedteleport.api.events.spawn;

import io.github.niestrat99.advancedteleport.api.events.TrackableATEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The event fired when a spawnpoint is removed.
 */
public class SpawnRemoveEvent extends TrackableATEvent {

    @NotNull
    private final String name;
    private static final HandlerList handlers = new HandlerList();

    public SpawnRemoveEvent(@NotNull String name, @Nullable CommandSender sender) {
        super(sender);
        this.name = name;
    }

    /**
     * Returns the spawnpoint being deleted.
     *
     * @return the name of the spawn being deleted.
     */
    @NotNull
    public String getSpawnName() {
        return name;
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
