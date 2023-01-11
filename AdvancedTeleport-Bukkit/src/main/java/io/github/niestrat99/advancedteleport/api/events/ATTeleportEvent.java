package io.github.niestrat99.advancedteleport.api.events;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired when a player is teleporting using AT.
 */
public class ATTeleportEvent extends CancellableATEvent {

    private static final HandlerList handlers = new HandlerList();
    private final @NotNull Player player;
    private final @NotNull Location fromLoc;
    private final @NotNull TeleportType type;
    private final @NotNull String locName;
    private @NotNull Location toLoc;

    public ATTeleportEvent(@NotNull Player player, @NotNull Location toLoc, @NotNull Location fromLoc,
                           @NotNull String locName, @NotNull TeleportType type) {
        this.player = player;
        this.toLoc = toLoc;
        this.fromLoc = fromLoc;
        this.locName = locName;
        this.type = type;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    // Also needs this, useless but ok
    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Location getFromLocation() {
        return fromLoc;
    }

    @NotNull
    public Location getToLocation() {
        return toLoc;
    }

    @NotNull
    public Player getPlayer() {
        return player;
    }

    @NotNull
    public TeleportType getType() {
        return type;
    }

    @NotNull
    public String getLocName() {
        return locName;
    }

    public void setToLocation(@NotNull Location toLoc) {
        this.toLoc = toLoc;
    }

    public enum TeleportType {

        TPA(true, "tpa"),
        TPAHERE(true, "tpahere"),
        HOME(true, "home"),
        WARP(true, "warp"),
        SPAWN(true, "spawn"),
        TPR(false, "tpr"), // It actually is restricted, but it handles this within the command instead
        BACK(true, "back"),
        TPLOC(false, "tploc");

        private boolean restricted;
        private String name;

        TeleportType(boolean restricted, String name) {
            this.restricted = restricted;
            this.name = name;
        }

        public boolean isRestricted() {
            return restricted;
        }

        public String getName() {
            return name;
        }
    }
}
