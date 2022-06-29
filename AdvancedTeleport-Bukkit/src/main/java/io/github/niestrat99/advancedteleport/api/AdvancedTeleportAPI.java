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

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class AdvancedTeleportAPI {

    public static CompletableFuture<Boolean> setWarp(@NotNull String name, @Nullable CommandSender creator, @NotNull Location location) {
        Objects.requireNonNull(location, "The warp location must not be null.");
        if (!location.isWorldLoaded()) throw new IllegalArgumentException("The world the warp is being set in must be loaded.");
        // Create an event.
        WarpCreateEvent event = new WarpCreateEvent(name, creator, location);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return CompletableFuture.completedFuture(false);
        // Create the warp object.
        Warp warp = new Warp(event.getSender() instanceof Player ? ((Player) event.getSender()).getUniqueId() : null,
                event.getName(), event.getLocation(), System.currentTimeMillis(), System.currentTimeMillis());
        // Get registering
        return CompletableFuture.supplyAsync(() -> {
            FlattenedCallback<Boolean> callback = new FlattenedCallback<>();
            Warp.registerWarp(warp);
            WarpSQLManager.get().addWarp(warp, callback);
            return callback.data;
        }, CoreClass.async).thenApplyAsync(data -> {
            WarpPostCreateEvent postEvent = new WarpPostCreateEvent(warp);
            Bukkit.getPluginManager().callEvent(postEvent);
            return data;
        }, CoreClass.sync);
    }

    public static HashMap<String, Warp> getWarps() {
        return new HashMap<>(Warp.getWarps());
    }

    public static CompletableFuture<Boolean> setSpawn(@NotNull String name, @Nullable CommandSender sender, @NotNull Location location) {
        Objects.requireNonNull(location, "The spawn location must not be null.");
        if (!location.isWorldLoaded()) throw new IllegalArgumentException("The world the spawn is being set in must be loaded.");
        // Create an event.
        SpawnCreateEvent event = new SpawnCreateEvent(name, sender, location);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return CompletableFuture.completedFuture(false);
        // Get registering
        return CompletableFuture.supplyAsync(() -> {
            AdvancedTeleportAPI.setSpawn(event.getName(), sender, event.getLocation());
            return true;
        });
    }

    public static CompletableFuture<Boolean> setMainSpawn(@NotNull String newName, @Nullable CommandSender sender) {
        // Create an event
        SwitchMainSpawnEvent event = new SwitchMainSpawnEvent(Spawn.get().getMainSpawn(), newName, sender);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return CompletableFuture.completedFuture(false);
        // Get switching
        return CompletableFuture.supplyAsync(() -> {
            Location spawn = Spawn.get().getSpawn(newName);
            Spawn.get().setMainSpawn(newName, spawn);
            return true;
        });
    }

    public static CompletableFuture<Boolean> removeSpawn(@NotNull String name, @Nullable CommandSender sender) {
        SpawnRemoveEvent event = new SpawnRemoveEvent(name, sender);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return CompletableFuture.completedFuture(false);
        // Remove the warp
        return CompletableFuture.supplyAsync(() -> {
            Spawn.get().removeSpawn(name);
            return true;
        });
    }

    public static CompletableFuture<Boolean> mirrorSpawn(@NotNull String fromWorld, @NotNull String toWorld, @Nullable CommandSender sender) {
        SpawnMirrorEvent event = new SpawnMirrorEvent(fromWorld, toWorld, sender);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return CompletableFuture.completedFuture(false);
        // Do the mirroring
        return CompletableFuture.supplyAsync(() ->
                Spawn.get().mirrorSpawn(fromWorld, toWorld).equals("Info.mirroredSpawn"));
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
