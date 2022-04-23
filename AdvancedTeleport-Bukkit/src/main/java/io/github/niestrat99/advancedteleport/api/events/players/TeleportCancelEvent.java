package io.github.niestrat99.advancedteleport.api.events.players;

import io.github.niestrat99.advancedteleport.api.TeleportRequestType;
import io.github.niestrat99.advancedteleport.api.events.CancellableATEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * The event fired when someone cancels their teleportation request.
 */
public class TeleportCancelEvent extends CancellableATEvent {

    private static final HandlerList handlers = new HandlerList();
    @NotNull
    private final Player receivingPlayer;
    @NotNull
    private final Player sendingPlayer;
    @NotNull
    private final TeleportRequestType requestType;

    public TeleportCancelEvent(@NotNull Player receivingPlayer, @NotNull Player sendingPlayer, @NotNull TeleportRequestType requestType) {
        this.receivingPlayer = receivingPlayer;
        this.sendingPlayer = sendingPlayer;
        this.requestType = requestType;
    }

    /**
     * The player that was receiving the teleport request.
     *
     * @return the player that was initially receiving the teleport request.
     */
    @NotNull
    public Player getReceivingPlayer() {
        return receivingPlayer;
    }

    /**
     * The player that sent the teleport request in the first place, but is now cancelling it.
     *
     * @return the player that sent the original teleport request.
     */
    @NotNull
    public Player getSendingPlayer() {
        return sendingPlayer;
    }

    /**
     * Gets the type of request represented by the teleport request.
     *
     * @return TPA if the request was created via /tpa, or TPAHERE if the request was created via /tpahere.
     */
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
