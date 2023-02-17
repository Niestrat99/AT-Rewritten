package io.github.niestrat99.advancedteleport.api.events.spawn;

import io.github.niestrat99.advancedteleport.api.events.TrackableATEvent;
import io.github.niestrat99.advancedteleport.api.spawn.Spawn;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SpawnMoveEvent extends TrackableATEvent {

    private static final HandlerList handlers = new HandlerList();
    private final @NotNull Spawn spawn;
    private @NotNull Location newLocation;


    protected SpawnMoveEvent(
            @NotNull Spawn spawn,
            @NotNull Location newLocation,
            @Nullable CommandSender sender
    ) {
        super(sender);
        this.spawn = spawn;
        this.newLocation = newLocation;
    }


    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
}
