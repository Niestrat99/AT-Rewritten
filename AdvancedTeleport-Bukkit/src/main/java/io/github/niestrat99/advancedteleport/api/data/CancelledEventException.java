package io.github.niestrat99.advancedteleport.api.data;

import io.github.niestrat99.advancedteleport.api.events.TrackableATEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CancelledEventException extends ATException {
    private CancelledEventException(@Nullable final CommandSender sender, @Nullable final String message) {
        super(sender, message);
    }

    @Contract(pure = true)
    public static @NotNull CancelledEventException of(Cancellable event) {
        if (event instanceof TrackableATEvent atEvent) {
            return new CancelledEventException(atEvent.getSender(), "The event " + event.getClass().getSimpleName() + " was cancelled.");
        } else return new CancelledEventException(null, "The event " + event.getClass().getSimpleName() + " was cancelled.");
    }
}
