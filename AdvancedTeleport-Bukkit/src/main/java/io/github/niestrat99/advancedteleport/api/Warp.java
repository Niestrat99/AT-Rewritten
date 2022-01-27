package io.github.niestrat99.advancedteleport.api;

import io.github.niestrat99.advancedteleport.api.events.warps.WarpDeleteEvent;
import io.github.niestrat99.advancedteleport.api.events.warps.WarpMoveEvent;
import io.github.niestrat99.advancedteleport.fanciful.sql.SQLManager;
import io.github.niestrat99.advancedteleport.fanciful.sql.WarpSQLManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Represents a warp point.
 */
public class Warp implements NamedLocation {

    @Nullable
    private final UUID creator;
    @NotNull
    private final String name;
    @NotNull
    private Location location;
    private final long createdTime;
    private long updatedTime;
    @NotNull
    private final String createdTimeFormatted;
    @NotNull
    private String updatedTimeFormatted;
    @NotNull
    private final SimpleDateFormat format;

    private static final HashMap<String, Warp> warps = new HashMap<>();

    /**
     * Creates a warp object, but does not formally register it. To register a warp, use {@link AdvancedTeleportAPI#setWarp(String, CommandSender, Location)}.
     *
     * @param creator the creator of the warp. Can be null.
     * @param name the name of the warp.
     * @param location the location of the warp.
     * @param createdTime the time in milliseconds when the warp was created.
     * @param updatedTime the time in milliseconds when the warp was updated.
     */
    public Warp(@Nullable UUID creator, @NotNull String name, @NotNull Location location, long createdTime, long updatedTime) {
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

    /**
     * Returns the name of the warp.
     *
     * @return the name of the warp.
     */
    @NotNull
    public String getName() {
        return name;
    }

    /**
     * Returns the location of the warp.
     *
     * @return the location of the warp.
     */
    @NotNull
    public Location getLocation() {
        return location;
    }

    /**
     * Returns the UUID of the warp's creator.
     *
     * @return the UUID of the warp's creator.
     */
    @Nullable
    public UUID getCreator() {
        return creator;
    }

    /**
     * Sets the location of the warp.
     *
     * @param location the new location of the warp.
     * @param callback what to do after the warp has been moved.
     * @deprecated Replaced with {@link Warp#setLocation(Location, CommandSender)}
     */
    @Deprecated
    public void setLocation(Location location, SQLManager.SQLCallback<Boolean> callback) {
        setLocation(location);
        callback.onSuccess(true);
    }

    /**
     * Sets the location of the warp.
     *
     * @param location the new location of the warp.
     * @return a completable future of whether the action failed or succeeded.
     */
    public CompletableFuture<Boolean> setLocation(@NotNull Location location) {
        return setLocation(location, (CommandSender) null);
    }

    /**
     * Sets the location of the warp.
     *
     * @param location the new location of the warp.
     * @param sender the command sender who triggered the action.
     * @return a completable future of whether the action failed or succeeded.
     */
    public CompletableFuture<Boolean> setLocation(@NotNull Location location, @Nullable CommandSender sender) {
        WarpMoveEvent event = new WarpMoveEvent(this, location, sender);
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

    /**
     * Returns the time the warp was created at in milliseconds.
     *
     * @return the time in milliseconds the warp was created.
     */
    public long getCreatedTime() {
        return createdTime;
    }

    /**
     * Returns the time the warp was last updated at in milliseconds.
     *
     * @return the time in milliseconds the warp was last updated.
     */
    public long getUpdatedTime() {
        return updatedTime;
    }

    /**
     * Returns the formatted time the warp was created at.
     *
     * @return the time the warp was created at in the format dd MMM yyyy HH:mm:ss
     */
    @NotNull
    public String getCreatedTimeFormatted() {
        return createdTimeFormatted;
    }

    /**
     * Returns the formatted time the warp was last updated at.
     *
     * @return the time the warp was last updated at in the format dd MMM yyyy HH:mm:ss
     */
    @NotNull
    public String getUpdatedTimeFormatted() {
        return updatedTimeFormatted;
    }

    /**
     * Returns all registered warps.
     *
     * @return a hashmap where the key is the warp names, and the value is the warp objects themselves.
     * @deprecated use {@link AdvancedTeleportAPI#getWarps()} instead, this will eventually become internal use only.
     */
    @Deprecated
    public static HashMap<String, Warp> getWarps() {
        return new HashMap<>(warps);
    }

    public static void registerWarp(Warp warp) {
        warps.put(warp.name, warp);
    }

    /**
     * Deletes a specified warp.
     *
     * @param callback what to do after the warp has been moved.
     * @deprecated use {@link Warp#delete(CommandSender)} or {@link Warp#delete()} instead.
     */
    @Deprecated
    public void delete(SQLManager.SQLCallback<Boolean> callback) {
        delete((CommandSender) null);
        callback.onSuccess(true);
    }

    /**
     * Deletes a specified warp.
     *
     * @param sender the command sender that called for the action. Can be null.
     * @return a completable future of whether the action failed or succeeded.
     */
    public CompletableFuture<Boolean> delete(@Nullable CommandSender sender) {
        WarpDeleteEvent event = new WarpDeleteEvent(this, sender);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return CompletableFuture.completedFuture(false);

        warps.remove(name);
        return CompletableFuture.supplyAsync(() -> {
            AdvancedTeleportAPI.FlattenedCallback<Boolean> callback = new AdvancedTeleportAPI.FlattenedCallback<>();
            WarpSQLManager.get().removeWarp(name, callback);
            return callback.data;
        });
    }

    /**
     * Deletes a specified warp.
     *
     * @return a completable future of whether the action failed or succeeded.
     */
    public CompletableFuture<Boolean> delete() {
        return delete((CommandSender) null);
    }
}
