package io.github.niestrat99.advancedteleport.api.events;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Fired when a player is teleporting using AT.
 */
public final class ATTeleportEvent extends CancellableATEvent {

    private static final HandlerList handlers = new HandlerList();
    @NotNull private final Player player;
    @NotNull private Location toLoc;
    @NotNull private final Location fromLoc;
    @NotNull private final String locName;
    @NotNull private final TeleportType type;

    @Contract(pure = true)
    public ATTeleportEvent(
        @NotNull final Player player,
        @NotNull final Location toLoc,
        @NotNull final Location fromLoc,
        @NotNull final String locName,
        @NotNull final TeleportType type
    ) {
        this.player = player;
        this.toLoc = toLoc;
        this.fromLoc = fromLoc;
        this.locName = locName;
        this.type = type;
        Bukkit.getPluginManager().callEvent(this); // Calls the event as it is initiated // TODO: Remove this
    }

    @Override
    @Contract(pure = true)
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    @Contract(pure = true)
    public static @NotNull HandlerList getHandlerList() {
        return handlers;
    }

    @Contract(pure = true)
    public @NotNull Location getFromLocation() {
        return fromLoc;
    }

    @Contract(pure = true)
    public @NotNull Location getToLocation() {
        return toLoc;
    }

    @Contract(pure = true)
    public @NotNull Player getPlayer() {
        return player;
    }

    @Contract(pure = true)
    public @NotNull TeleportType getType() {
        return type;
    }

    @Contract(pure = true)
    public @NotNull String getLocName() {
        return locName;
    }

    @Contract(pure = true)
    public void setToLocation(@NotNull final Location toLoc) {
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

        private final boolean restricted;
        private final String name;

        TeleportType(
            final boolean restricted,
            @NotNull final String name
        ) {
            this.restricted = restricted;
            this.name = name;
        }

        @Contract(pure = true)
        public boolean isRestricted() {
            return restricted;
        }

        @Contract(pure = true)
        public @NotNull String getName() {
            return name;
        }
    }
}
