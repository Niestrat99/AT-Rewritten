package io.github.niestrat99.advancedteleport.api.events;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

/**
 * An event that has a trackable executor, such as an admin creating a warp, or a player setting a home for someone
 * else, etc.
 */
public abstract class TrackableATEvent extends CancellableATEvent {

    private @Nullable CommandSender sender;

    public TrackableATEvent(@Nullable CommandSender sender) {
        this.sender = sender;
    }

    /**
     * Gives the entity/command sender who caused the event to happen.
     *
     * @return the command sender that caused the event to happen.
     */
    @Nullable
    public CommandSender getSender() {
        return sender;
    }

    /**
     * Sets the entity/command sender to something else. Can be null.
     *
     * @param sender the command sender that triggered the event.
     */
    public void setSender(@Nullable CommandSender sender) {
        this.sender = sender;
    }
}
