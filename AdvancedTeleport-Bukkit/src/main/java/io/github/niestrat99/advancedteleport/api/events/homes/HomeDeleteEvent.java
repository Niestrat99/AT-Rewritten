package io.github.niestrat99.advancedteleport.api.events.homes;

import io.github.niestrat99.advancedteleport.api.Home;
import io.github.niestrat99.advancedteleport.api.events.TrackableATEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The event fired when a home is deleted.
 */
public class HomeDeleteEvent extends TrackableATEvent {

    private static final HandlerList handlers = new HandlerList();
    private final @NotNull Home home;

    public HomeDeleteEvent(@NotNull Home home, @Nullable CommandSender sender) {
        super(sender);
        this.home = home;
    }

    /**
     * Gives the home object in question.
     *
     * @return the home being deleted.
     */
    @NotNull
    public Home getHome() {
        return home;
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
