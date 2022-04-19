package io.github.niestrat99.advancedteleport.api.events.warps;

import io.github.niestrat99.advancedteleport.api.Warp;
import io.github.niestrat99.advancedteleport.api.events.CancellableATEvent;
import org.bukkit.Location;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class WarpMoveEvent extends CancellableATEvent {

    private static final HandlerList handlers = new HandlerList();
    private Warp warp;
    private Location location;

    public WarpMoveEvent(Warp warp, Location location) {
        this.warp = warp;
        this.location = location;
    }

    public Warp getWarp() {
        return warp;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
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
