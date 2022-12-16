package io.github.niestrat99.advancedteleport.api.events.players;

import io.github.niestrat99.advancedteleport.api.events.TrackableATEvent;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The event fired when a player's teleportation status has been changed.
 */
public final class ToggleTeleportationEvent extends TrackableATEvent {

    private static final HandlerList handlers = new HandlerList();
    private final @NotNull OfflinePlayer player;
    private boolean enabled;
    private boolean statusChanging;

    @Contract(pure = true)
    public ToggleTeleportationEvent(
        @Nullable final CommandSender sender,
        @NotNull final OfflinePlayer player,
        final boolean enabled,
        final boolean statusChanging
    ) {
        super(sender);
        this.player = player;
        this.enabled = enabled;
        this.statusChanging = statusChanging;
    }

    /**
     * Returns the player who is having their teleportation status changed.
     *
     * @return the player that is having their teleportation status changed.
     */
    @Contract(pure = true)
    public @NotNull OfflinePlayer getPlayer() {
        return player;
    }

    /**
     * Returns the status that the player's teleportation status is being set to.
     *
     * @return true if the player's teleportation is going to be enabled, false if it is being disabled.
     */
    @Contract(pure = true)
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets the status of the player's teleportation after the event, as long as it is uncancelled.
     *
     * @param enabled true to enable the player teleportation, false to disable it.
     */
    @Contract(pure = true)
    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) statusChanging = !statusChanging;
        this.enabled = enabled;
    }

    /**
     * Returns whether the player's teleportation status is changing from what it currently is.
     *
     * @return true if the status is changing (i.e. true to false, false to true) or false if it is not.
     */
    @Contract(pure = true)
    public boolean isStatusChanging() {
        return statusChanging;
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
