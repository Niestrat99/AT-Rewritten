package io.github.niestrat99.advancedteleport.api.events.warps;

import io.github.niestrat99.advancedteleport.api.Warp;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class WarpPostCreateEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final @NotNull Warp warp;

    @Contract(pure = true)
    public WarpPostCreateEvent(@NotNull final Warp warp) {
        this.warp = warp;
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

    @Contract(pure = true)
    public @NotNull Warp getWarp() {
        return warp;
    }
}
