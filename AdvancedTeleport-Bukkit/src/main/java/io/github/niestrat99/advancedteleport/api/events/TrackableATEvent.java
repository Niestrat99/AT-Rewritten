package io.github.niestrat99.advancedteleport.api.events;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

public abstract class TrackableATEvent extends CancellableATEvent {

    @Nullable
    private CommandSender sender;

    public TrackableATEvent(@Nullable CommandSender sender) {
        this.sender = sender;
    }

    @Nullable
    public CommandSender getSender() {
        return sender;
    }

    public void setSender(@Nullable CommandSender sender) {
        this.sender = sender;
    }
}
