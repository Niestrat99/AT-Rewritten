package io.github.niestrat99.advancedteleport.api.events.warps;

import io.github.niestrat99.advancedteleport.api.events.CancellableATEvent;
import io.github.niestrat99.advancedteleport.api.events.TrackableATEvent;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public class WarpCreateEvent extends TrackableATEvent {

    private String name;
    private Location location;
    private static final HandlerList handlers = new HandlerList();

    public WarpCreateEvent(@NotNull String name, @Nullable CommandSender sender, @NotNull Location location) {
        super(sender);
        // Name checks
        Objects.requireNonNull(name, "The warp name must not be null.");
        if (name.isEmpty()) throw new IllegalArgumentException("The warp name must not be empty.");

        this.name = name;
        this.location = location;
    }

    @NotNull
    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
    }

    public void setName(@NotNull String name) {
        Objects.requireNonNull(name, "The warp name must not be null.");
        if (name.isEmpty()) throw new IllegalArgumentException("The warp name must not be empty.");

        this.name = name;
    }

    public void setLocation(@NotNull Location location) {
        this.location = location;
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
