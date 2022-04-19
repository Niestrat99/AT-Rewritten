package io.github.niestrat99.advancedteleport.api.events.warps;

import io.github.niestrat99.advancedteleport.api.Warp;
import io.github.niestrat99.advancedteleport.api.events.TrackableATEvent;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WarpMoveEvent extends TrackableATEvent {

    private static final HandlerList handlers = new HandlerList();
    @NotNull
    private Warp warp;
    @NotNull
    private Location location;

    public WarpMoveEvent(@NotNull Warp warp, @NotNull Location location, @Nullable CommandSender sender) {
        super(sender);
        this.warp = warp;
        this.location = location;
    }

    @NotNull
    public Warp getWarp() {
        return warp;
    }

    public void setLocation(@NotNull Location location) {
        this.location = location;
    }

    @NotNull
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
