package io.github.niestrat99.advancedteleport.api;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.data.ATException;
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

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class AdvancedTeleportAPI {

    public static CompletableFuture<Void> setWarp(@NotNull String name, @Nullable CommandSender creator, @NotNull Location location) {
        // Null checks
        Objects.requireNonNull(location, "The warp location must not be null.");
        if (!location.isWorldLoaded()) throw new IllegalArgumentException("The world the warp is being set in must be loaded.");

        // Create an event.
        WarpCreateEvent event = new WarpCreateEvent(name, creator, location);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return ATException.failedFuture(event);

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

    public static HashMap<String, Warp> getWarps() {
        return new HashMap<>(Warp.getWarps());
    }

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

    public static CompletableFuture<Void> removeSpawn(@NotNull String name, @Nullable CommandSender sender) {
        SpawnRemoveEvent event = new SpawnRemoveEvent(name, sender);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return CompletableFuture.completedFuture(null);

        // Remove the warp
        return CompletableFuture.runAsync(() -> Spawn.get().removeSpawn(name));
    }

    public static CompletableFuture<Void> mirrorSpawn(@NotNull String fromWorld, @NotNull String toWorld, @Nullable CommandSender sender) {
        SpawnMirrorEvent event = new SpawnMirrorEvent(fromWorld, toWorld, sender);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return CompletableFuture.completedFuture(null);

        // Do the mirroring
        return CompletableFuture.runAsync(() -> Spawn.get().mirrorSpawn(fromWorld, toWorld));
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
