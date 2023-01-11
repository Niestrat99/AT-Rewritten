package io.github.niestrat99.advancedteleport.api.events.homes;

import io.github.niestrat99.advancedteleport.api.Home;
import io.github.niestrat99.advancedteleport.api.events.TrackableATEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * The event fired when the main home of a player switches home.
 */
public class SwitchMainHomeEvent extends TrackableATEvent {

    private static final HandlerList handlers = new HandlerList();
    private final @Nullable Home oldMainHome;
    private @NotNull Home newMainHome;

    public SwitchMainHomeEvent(@Nullable Home oldMainHome, @NotNull Home newMainHome, @Nullable CommandSender sender) {
        super(sender);
        this.oldMainHome = oldMainHome;
        this.newMainHome = newMainHome;
    }

    /**
     * Gets the current - becoming old - main home of the player.
     *
     * @return the current main home.
     */
    @Nullable
    public Home getOldMainHome() {
        return oldMainHome;
    }

    /**
     * Gets the new main home of the player.
     *
     * @return the new main home.
     */
    @NotNull
    public Home getNewMainHome() {
        return newMainHome;
    }

    /**
     * Sets the new main home of the player.
     *
     * @param newMainHome the new main home question.
     */
    public void setNewMainHome(@NotNull Home newMainHome) {
        Objects.requireNonNull(newMainHome, "The new main home cannot be null.");
        this.newMainHome = newMainHome;
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
