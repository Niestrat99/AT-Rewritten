package io.github.niestrat99.advancedteleport.api.events.warps;

import io.github.niestrat99.advancedteleport.api.Warp;
import io.github.niestrat99.advancedteleport.api.events.TrackableATEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The event fired when a warp is deleted.
 */
public final class WarpDeleteEvent extends TrackableATEvent {

    private static final HandlerList handlers = new HandlerList();
    private final @NotNull Warp warp;

    @Contract(pure = true)
    public WarpDeleteEvent(
        @NotNull final Warp warp,
        @Nullable final CommandSender sender
    ) {
        super(sender);
        this.warp = warp;
    }

    /**
     * Returns the warp being deleted.
     *
     * @return the warp being deleted.
     */
    @Contract(pure = true)
    public @NotNull Warp getWarp() {
        return warp;
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
