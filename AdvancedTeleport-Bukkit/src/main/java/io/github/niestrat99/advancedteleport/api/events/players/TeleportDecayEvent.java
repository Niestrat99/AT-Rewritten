package io.github.niestrat99.advancedteleport.api.events.players;

import io.github.niestrat99.advancedteleport.api.TeleportRequestType;
import io.github.niestrat99.advancedteleport.api.events.CancellableATEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class TeleportDecayEvent extends CancellableATEvent {

    private static final HandlerList handlers = new HandlerList();
    @NotNull
    private final Player receivingPlayer;
    @NotNull
    private final Player sendingPlayer;
    @NotNull
    private final TeleportRequestType requestType;

    public TeleportDecayEvent(@NotNull Player receivingPlayer, @NotNull Player sendingPlayer, @NotNull TeleportRequestType requestType) {
        this.receivingPlayer = receivingPlayer;
        this.sendingPlayer = sendingPlayer;
        this.requestType = requestType;
    }

    @NotNull
    public Player getReceivingPlayer() {
        return receivingPlayer;
    }

    @NotNull
    public Player getSendingPlayer() {
        return sendingPlayer;
    }

    @NotNull
    public TeleportRequestType getRequestType() {
        return requestType;
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
