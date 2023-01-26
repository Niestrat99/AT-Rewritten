package io.github.niestrat99.advancedteleport.api.events;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

/**
 * An event that has a trackable executor, such as an admin creating a warp, or a player setting a home for someone
 * else, etc.
 */
public abstract class TrackableATEvent extends CancellableATEvent {

    private @Nullable CommandSender sender;

    @Contract(pure = true)
    protected TrackableATEvent(@Nullable final CommandSender sender) {
        this.sender = sender;
    }

    /**
     * Gives the entity/command sender who caused the event to happen.
     *
     * @return the command sender that caused the event to happen.
     */
    @Contract(pure = true)
    public @Nullable CommandSender getSender() {
        return sender;
    }

    /**
     * Sets the entity/command sender to something else. Can be null.
     *
     * @param sender the command sender that triggered the event.
     */
    @Contract(pure = true)
    public void setSender(@Nullable final CommandSender sender) {
        this.sender = sender;
    }
}
