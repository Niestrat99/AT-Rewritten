package io.github.niestrat99.advancedteleport.api.events.spawn;

import io.github.niestrat99.advancedteleport.api.spawn.Spawn;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class SpawnPostCreateEvent extends Event {

    @NotNull private static final HandlerList handlers = new HandlerList();
    @NotNull private final Spawn spawn;

    public SpawnPostCreateEvent(@NotNull Spawn spawn) {
        this.spawn = spawn;
    }

    @Contract(pure = true)
    public @NotNull Spawn getSpawn() {
        return spawn;
    }

    @Contract(pure = true)
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    @Contract(pure = true)
    public static @NotNull HandlerList getHandlerList() {
        return handlers;
    }
}
