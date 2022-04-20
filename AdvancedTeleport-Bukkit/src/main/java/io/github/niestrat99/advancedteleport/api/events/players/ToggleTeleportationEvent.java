package io.github.niestrat99.advancedteleport.api.events.players;

import io.github.niestrat99.advancedteleport.api.events.TrackableATEvent;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ToggleTeleportationEvent extends TrackableATEvent {

    @NotNull
    private OfflinePlayer player;
    private boolean enabled;
    private boolean statusChanging;

    public ToggleTeleportationEvent(@Nullable CommandSender sender, @NotNull OfflinePlayer player, boolean enabled, boolean statusChanging) {
        super(sender);
        this.player = player;
        this.enabled = enabled;
        this.statusChanging = statusChanging;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return null;
    }
}
