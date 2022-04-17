package io.github.niestrat99.advancedteleport.api;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.events.warps.WarpCreateEvent;
import io.github.niestrat99.advancedteleport.sql.SQLManager;
import io.github.niestrat99.advancedteleport.sql.WarpSQLManager;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class AdvancedTeleportAPI {

    public static CompletableFuture<Boolean> setWarp(@NotNull String name, @Nullable UUID creator, @NotNull Location location) {
        Objects.requireNonNull(location, "The warp location must not be null.");
        if (!location.isWorldLoaded()) throw new IllegalArgumentException("The world the warp is being set in must be loaded.");
        // Create an event.
        WarpCreateEvent event = new WarpCreateEvent(name, creator, location);
        if (event.isCancelled()) return CompletableFuture.completedFuture(false);
        // Create the warp object.
        Warp warp = new Warp(event.getCreator(), event.getName(), event.getLocation(), System.currentTimeMillis(), System.currentTimeMillis());
        // Get registering
        return CompletableFuture.supplyAsync(() -> {
            FlattenedCallback<Boolean> callback = new FlattenedCallback<>();
            WarpSQLManager.get().addWarp(warp, callback);
            return callback.data;
        }, CoreClass.async).thenApplyAsync(data -> data, CoreClass.sync);
    }

    public static HashMap<String, Warp> getWarps() {
        return new HashMap<>(Warp.getWarps());
    }

    static class FlattenedCallback<D> implements SQLManager.SQLCallback<D> {
        D data;

        @Override
        public void onSuccess(D data) {
            this.data = data;
        }
    }
}
