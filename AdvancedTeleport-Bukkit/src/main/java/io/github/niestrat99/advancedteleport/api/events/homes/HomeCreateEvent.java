package io.github.niestrat99.advancedteleport.api.events.homes;

import io.github.niestrat99.advancedteleport.api.Home;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class HomeCreateEvent extends Event implements Cancellable {

    private boolean cancelled = false;
    private Player player;
    private Home home;
    private boolean isMainHome;
    private static final HandlerList handlers = new HandlerList();

    public HomeCreateEvent(Player player, Home home, boolean isMainHome) {
        this.player = player;
        this.home = home;
        this.isMainHome = isMainHome;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        cancelled = b;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Player getPlayer() {
        return player;
    }

    public Home getHome() {
        return home;
    }

    public boolean isMainHome() {
        return isMainHome;
    }

    public void setMainHome(boolean flag) {
        this.isMainHome = flag;
    }
}
