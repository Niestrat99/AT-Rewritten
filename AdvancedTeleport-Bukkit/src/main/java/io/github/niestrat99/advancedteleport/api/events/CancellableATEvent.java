package io.github.niestrat99.advancedteleport.api.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

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
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Set the event to be cancelled - true cancels the event, false doesn't.
     *
     * @param b whether to cancel the event.
     */
    @Override
    public void setCancelled(boolean b) {
        cancelled = b;
    }
}
