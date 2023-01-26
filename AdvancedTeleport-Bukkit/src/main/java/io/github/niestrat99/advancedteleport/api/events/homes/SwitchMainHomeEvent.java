package io.github.niestrat99.advancedteleport.api.events.homes;

import io.github.niestrat99.advancedteleport.api.Home;
import io.github.niestrat99.advancedteleport.api.events.TrackableATEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The event fired when the main home of a player switches home.
 */
public final class SwitchMainHomeEvent extends TrackableATEvent {

    private static final HandlerList handlers = new HandlerList();
    private final @Nullable Home oldMainHome;
    private @NotNull Home newMainHome;

    @Contract(pure = true)
    public SwitchMainHomeEvent(
        @Nullable final Home oldMainHome,
        @NotNull final Home newMainHome,
        @Nullable final CommandSender sender
    ) {
        super(sender);
        this.oldMainHome = oldMainHome;
        this.newMainHome = newMainHome;
    }

    /**
     * Gets the current - becoming old - main home of the player.
     *
     * @return the current main home.
     */
    @Contract(pure = true)
    public @Nullable Home getOldMainHome() {
        return oldMainHome;
    }

    /**
     * Gets the new main home of the player.
     *
     * @return the new main home.
     */
    @Contract(pure = true)
    public @NotNull Home getNewMainHome() {
        return newMainHome;
    }

    /**
     * Sets the new main home of the player.
     *
     * @param newMainHome the new main home question.
     */
    @Contract(pure = true)
    public void setNewMainHome(@NotNull final Home newMainHome) {
        this.newMainHome = newMainHome;
    }

    @Override
    @Contract(pure = true)
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static @NotNull HandlerList getHandlerList() {
        return handlers;
    }
}
