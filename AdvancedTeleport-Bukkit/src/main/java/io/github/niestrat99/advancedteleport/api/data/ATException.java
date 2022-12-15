package io.github.niestrat99.advancedteleport.api.data;

import java.util.concurrent.CompletableFuture;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public sealed class ATException extends Exception permits CancelledEventException {
    @Nullable private final transient CommandSender sender;

    protected ATException(
        @Nullable final CommandSender sender,
        @Nullable final String message
    ) {
        super(message);
        this.sender = sender;
    }

    protected ATException(@Nullable final String message) {
        this(null, message);
    }

    protected ATException(@Nullable final CommandSender sender) {
        this(sender, null);
    }

    public @Nullable CommandSender sender() {
        return sender;
    }

    @Contract(pure = true)
    public @NotNull <T> CompletableFuture<T> future() {
        return CompletableFuture.failedFuture(this);
    }

    @Contract(pure = true)
    public static <T> @NotNull CompletableFuture<T> failedFuture(Cancellable event) {
        return CancelledEventException.of(event).future();
    }

    @Contract(pure = true)
    public static <T> @NotNull CompletableFuture<T> failedFuture(
        @Nullable final CommandSender sender,
        @NotNull final String message
    ) {
        return new ATException(message).future();
    }
}
