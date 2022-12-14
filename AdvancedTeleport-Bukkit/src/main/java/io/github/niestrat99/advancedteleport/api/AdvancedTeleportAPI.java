package io.github.niestrat99.advancedteleport.api;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.events.spawn.SpawnCreateEvent;
import io.github.niestrat99.advancedteleport.api.events.spawn.SpawnMirrorEvent;
import io.github.niestrat99.advancedteleport.api.events.spawn.SpawnRemoveEvent;
import io.github.niestrat99.advancedteleport.api.events.spawn.SwitchMainSpawnEvent;
import io.github.niestrat99.advancedteleport.api.events.warps.WarpCreateEvent;
import io.github.niestrat99.advancedteleport.api.events.warps.WarpPostCreateEvent;
import io.github.niestrat99.advancedteleport.config.Spawn;
import io.github.niestrat99.advancedteleport.sql.SQLManager;
import io.github.niestrat99.advancedteleport.sql.WarpSQLManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * The class used for accessing common counterparts of the plugin.
 */
public class AdvancedTeleportAPI {

    /**
     * Sets a warp at a given location. This will call
     *
     * @param name The name of the warp.
     * @param creator The creator of the warp.
     * @param location The location of the warp.
     * @return a completable future action of the saved warp.
     * @throws IllegalArgumentException if the world of the warp is not loaded.
     */
    public static CompletableFuture<Void> setWarp(@NotNull String name, @Nullable CommandSender creator, @NotNull Location location) {
        // Null checks
        Objects.requireNonNull(location, "The warp location must not be null.");
        if (!location.isWorldLoaded()) throw new IllegalArgumentException("The world the warp is being set in must be loaded.");

        // Create an event.
        WarpCreateEvent event = new WarpCreateEvent(name, creator, location);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return CompletableFuture.completedFuture(null);

        // Create the warp object.
        Warp warp = new Warp(event.getSender() instanceof Player ? ((Player) event.getSender()).getUniqueId() : null,
                event.getName(), event.getLocation(), System.currentTimeMillis(), System.currentTimeMillis());

        // Get registering
        return CompletableFuture.supplyAsync(() -> {
            FlattenedCallback<Boolean> callback = new FlattenedCallback<>();
            Warp.registerWarp(warp);
            WarpSQLManager.get().addWarp(warp, callback);
            return callback.data;
        }, CoreClass.async).thenAcceptAsync(data -> {
            WarpPostCreateEvent postEvent = new WarpPostCreateEvent(warp);
            Bukkit.getPluginManager().callEvent(postEvent);
        }, CoreClass.sync);
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
    @Unmodifiable
    public static HashMap<String, Warp> getWarps() {
        return new HashMap<>(Warp.getWarps());
    }

    /**
     * Sets a spawnpoint at a specific location.
     *
     * @param name the name/ID of the spawnpoint.
     * @param sender the creator of the spawnpoint.
     * @param location the location of the warp.
     * @return a completable future action of the saved spawnpoint.
     */
    public static CompletableFuture<Void> setSpawn(@NotNull String name, @Nullable CommandSender sender, @NotNull Location location) {

        // Null checks
        Objects.requireNonNull(location, "The spawn location must not be null.");
        if (!location.isWorldLoaded()) throw new IllegalArgumentException("The world the spawn is being set in must be loaded.");

        // Create an event.
        SpawnCreateEvent event = new SpawnCreateEvent(name, sender, location);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return CompletableFuture.completedFuture(null);

        // Get registering
        return CompletableFuture.runAsync(() -> AdvancedTeleportAPI.setSpawn(event.getName(), sender, event.getLocation()));
    }

    /**
     * Sets the main spawn.
     *
     * @param newName the ID of the spawnpoint.
     * @param sender the player/command sender setting the main spawnpoint.
     * @return a completable future action of the new main spawn.
     */
    public static CompletableFuture<Void> setMainSpawn(@NotNull String newName, @Nullable CommandSender sender) {

        // Create an event
        SwitchMainSpawnEvent event = new SwitchMainSpawnEvent(Spawn.get().getMainSpawn(), newName, sender);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return CompletableFuture.completedFuture(null);

        // Get switching
        return CompletableFuture.runAsync(() -> {
            Location spawn = Spawn.get().getSpawn(newName);
            Spawn.get().setMainSpawn(newName, spawn);
        });
    }

    /**
     * Removes a given spawnpoint.
     *
     * @param name the name of the spawnpoint.
     * @param sender the player/command sender who removed the spawnpoint.
     * @return a completable future action of the new main spawn.
     */
    public static CompletableFuture<Void> removeSpawn(@NotNull String name, @Nullable CommandSender sender) {

        // Create an event
        SpawnRemoveEvent event = new SpawnRemoveEvent(name, sender);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return CompletableFuture.completedFuture(null);

        // Remove the warp
        return CompletableFuture.runAsync(() -> Spawn.get().removeSpawn(name));
    }

    /**
     * Mirrors a world's spawnpoint to a different spawnpoint.
     *
     * @param fromWorld the world to be mirrored from.
     * @param toSpawnID the spawn ID to be mirrored to.
     * @param sender the player/command sender making this change.
     * @return a completable future action of the new main spawn.
     */
    public static CompletableFuture<Void> mirrorSpawn(@NotNull String fromWorld, @NotNull String toSpawnID, @Nullable CommandSender sender) {

        // Create an event
        SpawnMirrorEvent event = new SpawnMirrorEvent(fromWorld, toSpawnID, sender);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return CompletableFuture.completedFuture(null);

        // Do the mirroring
        return CompletableFuture.runAsync(() -> Spawn.get().mirrorSpawn(fromWorld, toSpawnID));
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
