package io.github.niestrat99.advancedteleport.api.data;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

public final class UnloadedWorldException extends ATException {
    private UnloadedWorldException(
        @Nullable final CommandSender sender,
        @Nullable final String message
    ) {
        super(sender, message);
    }

    private UnloadedWorldException(@Nullable final String message) {
        super(message);
    }

    private UnloadedWorldException(@Nullable final CommandSender sender) {
        super(sender);
    }
}
