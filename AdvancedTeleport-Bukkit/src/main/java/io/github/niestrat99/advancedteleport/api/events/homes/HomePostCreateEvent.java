package io.github.niestrat99.advancedteleport.api.events.homes;

import io.github.niestrat99.advancedteleport.api.Home;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * The event that gets the home object after it has been created and fully registered properly.
 */
public final class HomePostCreateEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    @NotNull private final Home home;

    @Contract(pure = true)
    public HomePostCreateEvent(@NotNull final Home home) {
        this.home = home;
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
    public @NotNull Home getHome() {
        return home;
    }
}
