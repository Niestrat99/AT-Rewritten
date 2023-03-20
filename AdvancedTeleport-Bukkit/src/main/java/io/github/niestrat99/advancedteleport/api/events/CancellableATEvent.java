package io.github.niestrat99.advancedteleport.api.events;

import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Contract;

/**
 * Represents an AT event that can be cancelled.
 */
public abstract class CancellableATEvent extends Event implements Cancellable {

    private boolean cancelled = false;

    /**
     * If the event has been cancelled.
     *
     * @return true if the event has been cancelled, false if it has not been.
     */
    @Override
    @Contract(pure = true)
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Set the event to be cancelled - true cancels the event, false doesn't.
     *
     * @param newState whether to cancel the event.
     */
    @Override
    @Contract(pure = true)
    public void setCancelled(boolean newState) {
        cancelled = newState;
    }

    @Contract(pure = true)
    public boolean callEvent() {
        Bukkit.getPluginManager().callEvent(this);
        return !isCancelled();
    }
}
