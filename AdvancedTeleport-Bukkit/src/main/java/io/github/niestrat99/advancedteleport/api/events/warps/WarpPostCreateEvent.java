package io.github.niestrat99.advancedteleport.api.events.warps;

import io.github.niestrat99.advancedteleport.api.Warp;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class WarpPostCreateEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final @NotNull Warp warp;

    public WarpPostCreateEvent(@NotNull Warp warp) {
        this.warp = warp;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @NotNull
    public Warp getWarp() {
        return warp;
    }
}
