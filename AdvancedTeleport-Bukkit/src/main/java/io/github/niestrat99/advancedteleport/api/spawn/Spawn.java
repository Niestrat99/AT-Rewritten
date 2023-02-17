package io.github.niestrat99.advancedteleport.api.spawn;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.AdvancedTeleportAPI;
import io.github.niestrat99.advancedteleport.api.NamedLocation;
import io.github.niestrat99.advancedteleport.api.events.spawn.SpawnMirrorEvent;
import io.github.niestrat99.advancedteleport.api.events.spawn.SpawnRemoveEvent;
import io.github.niestrat99.advancedteleport.managers.NamedLocationManager;
import io.github.niestrat99.advancedteleport.sql.MetadataSQLManager;
import io.github.niestrat99.advancedteleport.sql.SpawnSQLManager;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class Spawn implements NamedLocation {

    @NotNull private final String name;
    @Nullable private final UUID creator;
    @NotNull private Location location;
    @Nullable private Spawn mirroringSpawn;
    private final boolean requiresPermission;
    private final long createdTime;
    private long updatedTime;

    public Spawn(
            @NotNull String name,
            @NotNull Location location
    ) {
        this(name, location, null, true);
    }

    public Spawn(
            @NotNull String name,
            @NotNull Location location,
            boolean requiresPermission
    ) {
        this(name, location, null, requiresPermission);
    }

    public Spawn(
            @NotNull String name,
            @NotNull Location location,
            @Nullable UUID creator
    ) {
        this(name, location, creator, true);
    }

    public Spawn(
            @NotNull String name,
            @NotNull Location location,
            @Nullable UUID creator,
            boolean requiresPermission
    ) {
        this(name, location, null, creator, requiresPermission, System.currentTimeMillis(), System.currentTimeMillis());
    }

    public Spawn(
            @NotNull String name,
            @NotNull Location location,
            @Nullable Spawn mirroringSpawn,
            @Nullable UUID creator,
            boolean requiresPermission,
            final long createdTime,
            final long updatedTime
    ) {
        this.name = name;
        this.location = location;
        this.creator = creator;
        this.requiresPermission = requiresPermission;
        this.mirroringSpawn = mirroringSpawn;

        this.createdTime = createdTime;
        this.updatedTime = updatedTime;
    }

    @Contract(pure = true)
    public @NotNull String getName() {
        return name;
    }

    @Contract(pure = true)
    public @NotNull Location getLocation() {
        return location;
    }

    @Contract(pure = true)
    public CompletableFuture<Void> setLocation(@NotNull Location location, @Nullable CommandSender sender) {

        // If the event was cancelled, stop there
        return AdvancedTeleportAPI.validateEvent()
    }

    @Contract(pure = true)
    public @Nullable UUID getCreatorUUID() {
        return creator;
    }

    @Contract(pure = true)
    public boolean requiresPermission() {
        return requiresPermission;
    }

    @Contract(pure = true)
    public boolean canAccess(Player teleportingPlayer) {
        return !requiresPermission || teleportingPlayer.hasPermission("at.member.spawn." + name)
                && teleportingPlayer.hasPermission("at.member.spawn." + name);
    }

    @Contract(pure = true)
    public @Nullable Spawn getMirroringSpawn() {
        return mirroringSpawn;
    }


    /**
     * Mirrors this spawnpoint to a different spawnpoint if this one cannot be accessed.
     *
     * @param mirroringSpawn the spawn to be mirrored to.
     * @param sender the player/command sender making this change.
     * @return a completable future action of the new main spawn.
     */
    @Contract(pure = true)
    public CompletableFuture<Spawn> setMirroringSpawn(@Nullable Spawn mirroringSpawn, @Nullable CommandSender sender) {

        // See if the event passes first
        return AdvancedTeleportAPI.validateEvent(new SpawnMirrorEvent(this, mirroringSpawn, sender), event -> {

            // Set the mirroring spawn
            this.mirroringSpawn = event.getDestinationSpawn();

            // Update it in the database
            return MetadataSQLManager.get().mirrorSpawn(this, event.getDestinationSpawn())
                    .thenApplyAsync(result -> event.getDestinationSpawn());
        });
    }

    @Contract(pure = true)
    public CompletableFuture<Void> delete(@Nullable CommandSender sender) {

        // See if the event passes first
        return AdvancedTeleportAPI.validateEvent(new SpawnRemoveEvent(this, sender), event -> {

            // If this is the main spawn, then remove that too
            if (AdvancedTeleportAPI.getMainSpawn() == this) {
                return AdvancedTeleportAPI.setMainSpawn(null, sender).thenAcceptAsync(nullSpawn -> {
                    NamedLocationManager.get().removeSpawn(this);
                    SpawnSQLManager.get().removeSpawn(name).join();
                }, CoreClass.async);
            }

            // Remove the spawn
            NamedLocationManager.get().removeSpawn(this);
            return CompletableFuture.runAsync(() -> SpawnSQLManager.get().removeSpawn(name), CoreClass.async);
        });
    }
}
