package io.github.niestrat99.advancedteleport.api.events.homes;

import io.github.niestrat99.advancedteleport.api.Home;
import io.github.niestrat99.advancedteleport.api.events.TrackableATEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SwitchMainHomeEvent extends TrackableATEvent {

    private static final HandlerList handlers = new HandlerList();
    @Nullable
    private final Home oldMainHome;
    @NotNull
    private Home newMainHome;

    public SwitchMainHomeEvent(@Nullable Home oldMainHome, @NotNull Home newMainHome, @Nullable CommandSender sender) {
        super(sender);
        this.oldMainHome = oldMainHome;
        this.newMainHome = newMainHome;
    }

    @Nullable
    public Home getOldMainHome() {
        return oldMainHome;
    }

    @NotNull
    public Home getNewMainHome() {
        return newMainHome;
    }

    public void setNewMainHome(@NotNull Home newMainHome) {
        this.newMainHome = newMainHome;
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
