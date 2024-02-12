package io.github.niestrat99.advancedteleport.api.events.warps.alias;

import io.github.niestrat99.advancedteleport.api.Warp;
import io.github.niestrat99.advancedteleport.api.events.TrackableATEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WarpAliasRemoveEvent  extends TrackableATEvent {

    private static final HandlerList handlers = new HandlerList();
    private final @NotNull Warp warp;
    private final @NotNull String oldAlias;

    public WarpAliasRemoveEvent(
            @NotNull final String oldAlias,
            @NotNull final Warp warp,
            @Nullable final CommandSender sender
    ) {
        super(sender);

        this.oldAlias = oldAlias;
        this.warp = warp;
    }

    @Contract(pure = true)
    public @NotNull Warp getWarp() {
        return warp;
    }

    @Contract(pure = true)
    public @NotNull String getOldAlias() {
        return oldAlias;
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
