package io.github.niestrat99.advancedteleport.api.events.warps;

import io.github.niestrat99.advancedteleport.api.Warp;
import io.github.niestrat99.advancedteleport.api.events.TrackableATEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The event fired when a warp is deleted.
 */
public class WarpDeleteEvent extends TrackableATEvent {

    @NotNull
    private final Warp warp;
    private static final HandlerList handlers = new HandlerList();

    public WarpDeleteEvent(@NotNull Warp warp, @Nullable CommandSender sender) {
        super(sender);
        this.warp = warp;
    }

    /**
     * Returns the warp being deleted.
     *
     * @return the warp being deleted.
     */
    @NotNull
    public Warp getWarp() {
        return warp;
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
