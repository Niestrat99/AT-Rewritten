package io.github.niestrat99.advancedteleport.api.events.warps;

import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public class WarpCreateEvent extends Event implements Cancellable {

    private boolean cancelled = false;
    private String name;
    private UUID creator;
    private Location location;
    private static final HandlerList handlers = new HandlerList();

    public WarpCreateEvent(String name, UUID creator, Location location) {
        this.name = name;
        this.creator = creator;
        this.location = location;
    }

    @NotNull
    public String getName() {
        return name;
    }

    public UUID getCreator() {
        return creator;
    }

    public Location getLocation() {
        return location;
    }

    public void setName(@NotNull String name) {
        Objects.requireNonNull(name, "The warp name must not be null.");
        if (name.isEmpty()) throw new IllegalArgumentException("The warp name must not be empty.");

        this.name = name;
    }

    public void setCreator(@Nullable UUID creator) {
        this.creator = creator;
    }

    public void setLocation(@NotNull Location location) {
        this.location = location;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
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
