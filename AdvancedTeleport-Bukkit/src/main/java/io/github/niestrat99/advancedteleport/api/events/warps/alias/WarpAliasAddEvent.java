package io.github.niestrat99.advancedteleport.api.events.warps.alias;

import io.github.niestrat99.advancedteleport.api.Warp;
import io.github.niestrat99.advancedteleport.api.events.TrackableATEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WarpAliasAddEvent extends TrackableATEvent {

    private static final HandlerList handlers = new HandlerList();
    private final @NotNull Warp warp;
    private @NotNull String name;

    public WarpAliasAddEvent(
            @NotNull final String name,
            @NotNull final Warp warp,
            @Nullable final CommandSender sender
    ) {
        super(sender);

        this.name = name;
        this.warp = warp;
    }

    @Contract(pure = true)
    public @NotNull Warp getWarp() {
        return warp;
    }

    @Contract(pure = true)
    public @NotNull String getName() {
        return name;
    }

    @Contract(pure = true)
    public void setName(@NotNull String name) {
        this.name = name;
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
