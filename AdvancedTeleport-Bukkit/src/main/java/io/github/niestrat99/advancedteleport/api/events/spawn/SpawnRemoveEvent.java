package io.github.niestrat99.advancedteleport.api.events.spawn;

import io.github.niestrat99.advancedteleport.api.events.TrackableATEvent;
import io.github.niestrat99.advancedteleport.api.spawn.Spawn;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** The event fired when a spawnpoint is removed. */
public final class SpawnRemoveEvent extends TrackableATEvent {

    @NotNull private static final HandlerList handlers = new HandlerList();
    @NotNull private final Spawn spawn;

    @Contract(pure = true)
    public SpawnRemoveEvent(@NotNull final Spawn spawn, @Nullable final CommandSender sender) {
        super(sender);
        this.spawn = spawn;
    }

    /**
     * Returns the spawnpoint being deleted.
     *
     * @return the spawn being deleted.
     */
    @Contract(pure = true)
    public @NotNull Spawn getSpawn() {
        return spawn;
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
