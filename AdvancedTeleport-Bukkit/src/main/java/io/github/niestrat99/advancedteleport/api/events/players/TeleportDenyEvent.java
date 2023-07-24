package io.github.niestrat99.advancedteleport.api.events.players;

import io.github.niestrat99.advancedteleport.api.TeleportRequestType;
import io.github.niestrat99.advancedteleport.api.events.CancellableATEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/** The event fired when a teleportation request is denied. */
public final class TeleportDenyEvent extends CancellableATEvent {

    private static final HandlerList handlers = new HandlerList();
    private final @NotNull Player receivingPlayer;
    private final @NotNull Player sendingPlayer;
    private final @NotNull TeleportRequestType requestType;

    @Contract(pure = true)
    public TeleportDenyEvent(
            @NotNull final Player receivingPlayer,
            @NotNull final Player sendingPlayer,
            @NotNull final TeleportRequestType requestType) {
        this.receivingPlayer = receivingPlayer;
        this.sendingPlayer = sendingPlayer;
        this.requestType = requestType;
    }

    /**
     * The player that is receiving the teleport request, i.e. the player who denied it.
     *
     * @return the player that denied the teleport request.
     */
    @Contract(pure = true)
    public @NotNull Player getReceivingPlayer() {
        return receivingPlayer;
    }

    /**
     * The player that sent the teleport request in the first place.
     *
     * @return the player that sent the original teleport request.
     */
    @Contract(pure = true)
    public @NotNull Player getSendingPlayer() {
        return sendingPlayer;
    }

    /**
     * Gets the type of request represented by the teleport request.
     *
     * @return TPA if the request was created via /tpa, or TPAHERE if the request was created via
     *     /tpahere.
     */
    @Contract(pure = true)
    public @NotNull TeleportRequestType getRequestType() {
        return requestType;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    @Contract(pure = true)
    public static @NotNull HandlerList getHandlerList() {
        return handlers;
    }
}
