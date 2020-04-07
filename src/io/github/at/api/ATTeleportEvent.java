package io.github.at.api;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ATTeleportEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private Location toLoc;
    private Location fromLoc;
    private String locName;
    private TeleportType type;
    private boolean cancelled = false;

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

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        cancelled = b;
    }

    public enum TeleportType {

        TPA(true),
        TPAHERE(true),
        TPALL(false),
        HOME(false),
        WARP(true),
        SPAWN(true),
        TPR(false), // It actually is restricted, but it handles this within the command instead
        BACK(true),
        TPO(false),
        TPLOC(false);

        private boolean restricted;

        TeleportType(boolean restricted) {
            this.restricted = restricted;
        }

        public boolean isRestricted() {
            return restricted;
        }
    }
}
