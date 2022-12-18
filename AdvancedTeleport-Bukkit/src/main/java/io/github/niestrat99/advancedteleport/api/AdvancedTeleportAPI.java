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
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

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

    public static @NotNull CompletableFuture<Void> setWarp(
        @NotNull final String name,
        @Nullable final CommandSender creator,
        @NotNull final Location location
    ) {
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

    public static ImmutableMap<String, Warp> getWarps() {
        return ImmutableMap.copyOf(Warp.warps());
    }

    public static @NotNull CompletableFuture<Void> setSpawn(
        @NotNull final String name,
        @Nullable final CommandSender sender,
        @NotNull final Location location
    ) {
        Objects.requireNonNull(location, "The spawn location must not be null.");
        if (!location.isWorldLoaded()) return ATException.failedFuture("The world the spawn is being set in must be loaded.");

        return validateEvent(new SpawnCreateEvent(name, sender, location), event ->  CompletableFuture.runAsync(() -> {
                AdvancedTeleportAPI.setSpawn(event.getName(), sender, event.getLocation());
        }, CoreClass.async));
    }

    public static @NotNull CompletableFuture<Void> setMainSpawn(
        @NotNull final String newName,
        @Nullable final CommandSender sender
    ) {
        return validateEvent(new SwitchMainSpawnEvent(Spawn.get().getMainSpawn(), newName, sender), event -> CompletableFuture.runAsync(() -> {
            final var spawn = Spawn.get().getSpawn(newName);
            Spawn.get().setMainSpawn(newName, spawn);
        }, CoreClass.async));
    }

    public static @NotNull CompletableFuture<Void> removeSpawn(
        @NotNull final String name,
        @Nullable final CommandSender sender
    ) {
        return validateEvent(new SpawnRemoveEvent(name, sender), event -> CompletableFuture.runAsync(() -> {
            Spawn.get().removeSpawn(event.getSpawnName());
        }, CoreClass.async));
    }

    public static @NotNull CompletableFuture<Void> mirrorSpawn(
        @NotNull final String fromWorld,
        @NotNull final String toWorld,
        @Nullable final CommandSender sender
    ) {
        return validateEvent(new SpawnMirrorEvent(fromWorld, toWorld, sender), event -> CompletableFuture.runAsync(() -> {
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
