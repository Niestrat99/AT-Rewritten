package io.github.niestrat99.advancedteleport.api.events;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

/**
 * Fired when a player is teleporting using AT.
 */
public class ATTeleportEvent extends CancellableATEvent {

    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private Location toLoc;
    private Location fromLoc;
    private String locName;
    private TeleportType type;

    public ATTeleportEvent(Player player, Location toLoc, Location fromLoc, String locName, TeleportType type) {
        this.player = player;
        this.toLoc = toLoc;
        this.fromLoc = fromLoc;
        this.locName = locName;
        this.type = type;
        Bukkit.getPluginManager().callEvent(this); // Calls the event as it is initiated
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

    public Location getToLocation() {
        return toLoc;
    }

    public Player getPlayer() {
        return player;
    }

    public TeleportType getType() {
        return type;
    }

    public String getLocName() {
        return locName;
    }

    public void setToLocation(Location toLoc) {
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
