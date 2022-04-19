package io.github.niestrat99.advancedteleport.api;

import io.github.niestrat99.advancedteleport.api.events.warps.WarpDeleteEvent;
import io.github.niestrat99.advancedteleport.api.events.warps.WarpMoveEvent;
import io.github.niestrat99.advancedteleport.sql.SQLManager;
import io.github.niestrat99.advancedteleport.sql.WarpSQLManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class Warp implements NamedLocation {

    private UUID creator;
    private String name;
    private Location location;
    private long createdTime;
    private long updatedTime;
    private String createdTimeFormatted;
    private String updatedTimeFormatted;
    private SimpleDateFormat format;

    private static HashMap<String, Warp> warps = new HashMap<>();

    public Warp(UUID creator, String name, Location location, long createdTime, long updatedTime) {
        Objects.requireNonNull(name, "The warp name must not be null.");
        Objects.requireNonNull(location, "The warp location must not be null.");
        if (name.isEmpty()) throw new IllegalArgumentException("The warp name must not be empty.");

        this.name = name;
        this.location = location;
        this.creator = creator;
        this.createdTime = createdTime;
        this.updatedTime = updatedTime;

        this.format = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
        this.createdTimeFormatted = format.format(new Date(createdTime));
        this.updatedTimeFormatted = format.format(new Date(updatedTime));
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
    }

    public UUID getCreator() {
        return creator;
    }

    @Deprecated
    public void setLocation(Location location, SQLManager.SQLCallback<Boolean> callback) {
        setLocation(location);
        callback.onSuccess(true);
    }

    public CompletableFuture<Boolean> setLocation(Location location) {
        WarpMoveEvent event = new WarpMoveEvent(this, location);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return CompletableFuture.completedFuture(false);

        this.location = location;
        this.updatedTime = System.currentTimeMillis();

        this.updatedTimeFormatted = format.format(new Date(updatedTime));
        return CompletableFuture.supplyAsync(() -> {
            AdvancedTeleportAPI.FlattenedCallback<Boolean> callback = new AdvancedTeleportAPI.FlattenedCallback<>();
            WarpSQLManager.get().moveWarp(location, name, callback);
            return callback.data;
        });
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public long getUpdatedTime() {
        return updatedTime;
    }

    public static HashMap<String, Warp> getWarps() {
        return new HashMap<>(warps);
    }

    static void registerWarp(Warp warp) {
        warps.put(warp.name, warp);
    }

    @Deprecated
    public void delete(SQLManager.SQLCallback<Boolean> callback) {
        delete();
        callback.onSuccess(true);
    }

    public CompletableFuture<Boolean> delete() {
        WarpDeleteEvent event = new WarpDeleteEvent(this);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return CompletableFuture.completedFuture(false);

        warps.remove(name);
        return CompletableFuture.supplyAsync(() -> {
            AdvancedTeleportAPI.FlattenedCallback<Boolean> callback = new AdvancedTeleportAPI.FlattenedCallback<>();
            WarpSQLManager.get().removeWarp(name, callback);
            return callback.data;
        });
    }
}
