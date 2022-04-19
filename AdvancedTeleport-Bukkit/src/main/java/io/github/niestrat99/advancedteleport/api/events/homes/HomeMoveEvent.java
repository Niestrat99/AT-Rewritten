package io.github.niestrat99.advancedteleport.api.events.homes;

import io.github.niestrat99.advancedteleport.api.Home;
import io.github.niestrat99.advancedteleport.api.events.TrackableATEvent;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HomeMoveEvent extends TrackableATEvent {

    private static final HandlerList handlers = new HandlerList();
    @NotNull
    private final Home home;
    @NotNull
    private Location location;

    public HomeMoveEvent(@NotNull Home home, @NotNull Location location, @Nullable CommandSender sender) {
        super(sender);
        this.home = home;
        this.location = location;
    }

    @NotNull
    public Home getHome() {
        return home;
    }

    @NotNull
    public Location getLocation() {
        return location;
    }

    public void setLocation(@NotNull Location location) {
        this.location = location;
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
