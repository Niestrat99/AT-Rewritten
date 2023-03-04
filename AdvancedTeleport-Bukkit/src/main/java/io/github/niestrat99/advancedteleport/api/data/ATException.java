package io.github.niestrat99.advancedteleport.api.data;

import java.util.concurrent.CompletableFuture;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ATException extends Exception {
    @Nullable private final transient CommandSender sender;

    @Contract(pure = true)
    protected ATException(
        @Nullable final CommandSender sender,
        @Nullable final String message
    ) {
        super("Context [%s] | Message [%s]".formatted(sender == null ? "null" : sender.getName(), message));
        this.sender = sender;
    }

    @Contract(pure = true)
    protected ATException(@Nullable final String message) {
        this(null, message);
    }

    @Contract(pure = true)
    protected ATException(@Nullable final CommandSender sender) {
        this(sender, null);
    }

    @Contract(pure = true)
    public @Nullable CommandSender sender() {
        return sender;
    }

    @Contract(pure = true)
    public @NotNull <T> CompletableFuture<T> future() {
        return CompletableFuture.failedFuture(this);
    }

    @Contract(value = "_ -> new", pure = true)
    public static <T> @NotNull CompletableFuture<T> failedFuture(Cancellable event) {
        return CancelledEventException.of(event).future();
    }

    @Contract(value = "_, _ -> new", pure = true)
    public static <T> @NotNull CompletableFuture<T> failedFuture(
        @Nullable final CommandSender sender,
        @NotNull final String message
    ) {
        return failed(sender, message).future();
    }

    @Contract(value = "_, _ -> new", pure = true)
    public static @NotNull ATException failed(
            @Nullable final CommandSender sender,
            @NotNull final String message
    ) {
        return new ATException(sender, message);
    }

    @Contract(value = "_ -> new", pure = true)
    public static <T> @NotNull CompletableFuture<T> failedFuture(@NotNull final String message) {
        return new ATException(message).future();
    }

    @Contract(value = "_, _ -> new", pure = true)
    public static <T> @NotNull CompletableFuture<T> failedFuture(
        @NotNull final World world,
        @NotNull final String message
    ) {
        return new UnloadedWorldException(world, message).future();
    }
}
