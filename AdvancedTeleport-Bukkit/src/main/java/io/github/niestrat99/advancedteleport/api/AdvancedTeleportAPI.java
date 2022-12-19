package io.github.niestrat99.advancedteleport.api;

import com.google.common.collect.ImmutableMap;
import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.data.ATException;
import io.github.niestrat99.advancedteleport.api.events.CancellableATEvent;
import io.github.niestrat99.advancedteleport.api.events.spawn.SpawnCreateEvent;
import io.github.niestrat99.advancedteleport.api.events.spawn.SpawnMirrorEvent;
import io.github.niestrat99.advancedteleport.api.events.spawn.SpawnRemoveEvent;
import io.github.niestrat99.advancedteleport.api.events.spawn.SwitchMainSpawnEvent;
import io.github.niestrat99.advancedteleport.api.events.warps.WarpCreateEvent;
import io.github.niestrat99.advancedteleport.api.events.warps.WarpPostCreateEvent;
import io.github.niestrat99.advancedteleport.config.Spawn;
import io.github.niestrat99.advancedteleport.sql.SQLManager;
import io.github.niestrat99.advancedteleport.sql.WarpSQLManager;
import java.util.Optional;
import java.util.function.Function;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * The class used for accessing common counterparts of the plugin.
 */
public final class AdvancedTeleportAPI {
    private AdvancedTeleportAPI() {}

    @ApiStatus.Internal
    public static <T extends CancellableATEvent> @NotNull CompletableFuture<Void> validateEvent(
        @NotNull final T event,
        @NotNull final Function<T, CompletableFuture<Void>> validatedEvent
    ) {
        if (event.callEvent()) {
            return validatedEvent.apply(event);
        } else return ATException.failedFuture(event);
    }

    static @NotNull Optional<Player> maybePlayer(@Nullable final CommandSender sender) {
        if (sender instanceof Player player) {
            return Optional.of(player);
        } else return Optional.empty();
    }

    /**
     * Sets a warp at a given location. This will call
     *
     * @param name The name of the warp.
     * @param creator The creator of the warp.
     * @param location The location of the warp.
     * @return a completable future action of the saved warp.
     * @throws IllegalArgumentException if the world of the warp is not loaded.
     */
    public static @NotNull CompletableFuture<Void> setWarp(
        @NotNull final String name,
        @Nullable final CommandSender creator,
        @NotNull final Location location
    ) {
        // Null checks
        Objects.requireNonNull(location, "The warp location must not be null.");
        if (!location.isWorldLoaded()) return ATException.failedFuture("The world the warp is being set in must be loaded.");

        return validateEvent(new WarpCreateEvent(name, creator, location), event -> {
            final var warp = new Warp(
                maybePlayer(event.getSender()).map(Player::getUniqueId).orElse(null),
                event.getName(),
                event.getLocation(),
                System.currentTimeMillis(), System.currentTimeMillis()
            );

            return CompletableFuture.runAsync(() -> {
                FlattenedCallback<Boolean> callback = new FlattenedCallback<>();
                Warp.registerWarp(warp);
                WarpSQLManager.get().addWarp(warp, callback);
            }, CoreClass.async).thenAcceptAsync(data -> {
                WarpPostCreateEvent postEvent = new WarpPostCreateEvent(warp);
                Bukkit.getPluginManager().callEvent(postEvent);
            }, CoreClass.sync);
        });
    }

    /**
     * Returns a warp object by its name.
     *
     * @param name the name of the warp.
     * @return the registered warp object, or null if it does not exist.
     */
    @Nullable
    public static Warp getWarp(@NotNull String name) {

        // Null check
        Objects.requireNonNull(name, "The warp name must not be null.");

        // Returns the warp
        return getWarps().get(name);
    }

    public static boolean isWarpSet(@NotNull String name) {

        // Null check
        Objects.requireNonNull(name, "The warp name must not be null.");

        // Return the result
        return getWarps().containsKey(name);
    }

    /**
     * Returns a hashmap of warps. The keys are the names of the warps, and the corresponding value is the warp objects.
     * Cannot be directly modified.
     *
     * @return a cloned hashmap of warps.
     */
    @Contract(pure = true)
    public static @NotNull ImmutableMap<String, Warp> getWarps() {
        return ImmutableMap.copyOf(Warp.warps());
    }

    /**
     * Sets a spawnpoint at a specific location.
     *
     * @param name the name/ID of the spawnpoint.
     * @param sender the creator of the spawnpoint.
     * @param location the location of the warp.
     * @return a completable future action of the saved spawnpoint.
     */
    public static @NotNull CompletableFuture<Void> setSpawn(
            @NotNull final String name,
            @Nullable final CommandSender sender,
            @NotNull final Location location
    ) {

        // Null checks
        Objects.requireNonNull(location, "The spawn location must not be null.");
        if (!location.isWorldLoaded()) return ATException.failedFuture(location.getWorld(), "The world the spawn is being set in must be loaded.");

        return validateEvent(new SpawnCreateEvent(name, sender, location), event -> CompletableFuture.runAsync(() -> {
                AdvancedTeleportAPI.setSpawn(event.getName(), sender, event.getLocation());
        }, CoreClass.async));
    }

    /**
     * Sets the main spawn.
     *
     * @param newName the ID of the spawnpoint.
     * @param sender the player/command sender setting the main spawnpoint.
     * @return a completable future action of the new main spawn.
     */

    public static @NotNull CompletableFuture<Void> setMainSpawn(
            @NotNull final String newName,
            @Nullable final CommandSender sender
    ) {
        return validateEvent(new SwitchMainSpawnEvent(Spawn.get().getMainSpawn(), newName, sender), event -> CompletableFuture.runAsync(() -> {
            final var spawn = Spawn.get().getSpawn(newName);
            Spawn.get().setMainSpawn(newName, spawn);
        }, CoreClass.async));
    }

    /**
     * Removes a given spawnpoint.
     *
     * @param name the name of the spawnpoint.
     * @param sender the player/command sender who removed the spawnpoint.
     * @return a completable future action of the new main spawn.
     */
    public static @NotNull CompletableFuture<Void> removeSpawn(
            @NotNull final String name,
            @Nullable final CommandSender sender
    ) {
        return validateEvent(new SpawnRemoveEvent(name, sender), event -> CompletableFuture.runAsync(() -> {
            Spawn.get().removeSpawn(event.getSpawnName());
        }, CoreClass.async));
    }

    /**
     * Mirrors a world's spawnpoint to a different spawnpoint.
     *
     * @param fromWorld the world to be mirrored from.
     * @param toSpawnID the spawn ID to be mirrored to.
     * @param sender the player/command sender making this change.
     * @return a completable future action of the new main spawn.
     */
    public static @NotNull CompletableFuture<Void> mirrorSpawn(
            @NotNull final String fromWorld,
            @NotNull final String toSpawnID,
            @Nullable final CommandSender sender
    ) {
        return validateEvent(new SpawnMirrorEvent(fromWorld, toSpawnID, sender), event -> CompletableFuture.runAsync(() -> {
            Spawn.get().mirrorSpawn(event.getFromWorld(), event.getToWorld());
        }, CoreClass.async));
    }

    static class FlattenedCallback<D> implements SQLManager.SQLCallback<D> {
        D data;

        @Override
        public void onSuccess(D data) {
            this.data = data;
        }

        @Override
        public void onFail() {
            this.data = null;
        }
    }
}
