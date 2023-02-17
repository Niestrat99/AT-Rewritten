package io.github.niestrat99.advancedteleport.api.spawn;

import org.bukkit.Location;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class MirroredSpawn extends Spawn {

    @NotNull private Spawn spawn;

    public MirroredSpawn(@NotNull String name, @NotNull Spawn spawn) {
        this(name, spawn.getLocation(), spawn, null, true);
    }

    public MirroredSpawn(@NotNull String name, @Nullable Location location, @NotNull Spawn spawn) {
        this(name, location, spawn, null, true);
    }

    public MirroredSpawn(@NotNull String name, @Nullable Location location, @NotNull Spawn spawn, @Nullable UUID creator) {
        this(name, location, spawn, creator, true);
    }

    public MirroredSpawn(@NotNull String name, @Nullable Location location, @NotNull Spawn spawn, boolean requiresPermission) {
        this(name, location, spawn, null, requiresPermission);
    }

    public MirroredSpawn(@NotNull String name, @Nullable Location location, @NotNull Spawn spawn, @Nullable UUID creator, boolean requiresPermission) {
        super(name, location == null ? spawn.getLocation() : location, creator, requiresPermission);

        this.spawn = spawn;
    }

    @Contract(pure = true)
    public Spawn getDestinationSpawn() {
        return spawn;
    }

    @Contract(pure = true)
    public void setDestinationSpawn(@NotNull Spawn spawn) {
        this.spawn = spawn;
    }
}
