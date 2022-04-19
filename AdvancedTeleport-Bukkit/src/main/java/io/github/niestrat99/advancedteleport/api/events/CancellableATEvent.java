package io.github.niestrat99.advancedteleport.api.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public abstract class CancellableATEvent extends Event implements Cancellable {

    private boolean cancelled = false;

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        cancelled = b;
    }
}
