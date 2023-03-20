package io.github.niestrat99.advancedteleport.api.events.homes;

import io.github.niestrat99.advancedteleport.api.Home;
import io.github.niestrat99.advancedteleport.api.events.TrackableATEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The event fired when a home is deleted.
 */
public final class HomeDeleteEvent extends TrackableATEvent {

    private static final HandlerList handlers = new HandlerList();
    private final @NotNull Home home;

    @Contract(pure = true)
    public HomeDeleteEvent(
        @NotNull final Home home,
        @Nullable final CommandSender sender
    ) {
        super(sender);
        this.home = home;
    }

    /**
     * Gives the home object in question.
     *
     * @return the home being deleted.
     */
    @Contract(pure = true)
    public @NotNull Home getHome() {
        return home;
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
