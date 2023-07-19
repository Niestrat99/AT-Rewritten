package io.github.niestrat99.advancedteleport.api.events.spawn;

import io.github.niestrat99.advancedteleport.api.events.TrackableATEvent;
import io.github.niestrat99.advancedteleport.api.Spawn;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 * @since v6.0.0
 */
public class SpawnMoveEvent extends TrackableATEvent {

    private static final HandlerList handlers = new HandlerList();
    private final @NotNull Spawn spawn;
    private @NotNull Location newLocation;


    public SpawnMoveEvent(
            @NotNull Spawn spawn,
            @NotNull Location newLocation,
            @Nullable CommandSender sender
    ) {
        super(sender);
        this.spawn = spawn;
        this.newLocation = newLocation;
    }

    @Contract(pure = true)
    public @NotNull Spawn getSpawn() {
        return spawn;
    }

    @Contract(pure = true)
    public @NotNull Location getNewLocation() {
        return newLocation;
    }

    @Contract(pure = true)
    public void setNewLocation(@NotNull Location newLocation) {
        this.newLocation = newLocation;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
}
