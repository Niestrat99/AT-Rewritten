package io.github.niestrat99.advancedteleport.api.data;

import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class UnloadedWorldException extends ATException {
    public UnloadedWorldException(
        @NotNull final World world,
        @Nullable final String message
    ) {
        super("World [%s] | Message [%s]".formatted(world.getName(), message));
    }
}
