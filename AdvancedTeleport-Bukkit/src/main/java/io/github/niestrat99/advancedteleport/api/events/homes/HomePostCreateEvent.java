package io.github.niestrat99.advancedteleport.api.events.homes;

import io.github.niestrat99.advancedteleport.api.Home;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * The event that gets the home object after it has been created and fully registered properly.
 */
public class HomePostCreateEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    @NotNull
    private final Home home;

    public HomePostCreateEvent(@NotNull Home home) {
        this.home = home;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * Gives the home object in question.
     *
     * @return the home that has been created.
     */
    @NotNull
    public Home getHome() {
        return home;
    }
}
