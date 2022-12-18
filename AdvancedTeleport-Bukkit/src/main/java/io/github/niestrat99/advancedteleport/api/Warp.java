package io.github.niestrat99.advancedteleport.api;

import io.github.niestrat99.advancedteleport.api.data.ATException;
import io.github.niestrat99.advancedteleport.api.events.warps.WarpDeleteEvent;
import io.github.niestrat99.advancedteleport.api.events.warps.WarpMoveEvent;
import io.github.niestrat99.advancedteleport.sql.WarpSQLManager;
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


    private final @Nullable UUID creator;

    private final @NotNull String name;

    private @NotNull Location location;
    private final long createdTime;
    private long updatedTime;
    private final @NotNull String createdTimeFormatted;
    private @NotNull String updatedTimeFormatted;
    private final @NotNull SimpleDateFormat format;
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
    public @NotNull String getName() {
        return name;
    }

    /**
     * Returns the location of the warp.
     *
     * @return the location of the warp.
     */
    public @NotNull Location getLocation() {
        return location;
    }

    /**
     * Returns the UUID of the warp's creator.
     *
     * @return the UUID of the warp's creator.
     */
    public @Nullable UUID getCreator() {
        return creator;
    }


    /**
     * Sets the location of the warp.
     *
     * @param location the new location of the warp.
     * @return a completable future of whether the action failed or succeeded.
     */
    public CompletableFuture<Boolean> setLocation(@NotNull final Location location) {
        return setLocation(location, null);
    }

    /**
     * Sets the location of the warp. This will fire WarpMoveEvent.
     *
     * @param location the new location of the warp.
     * @param sender the command sender who triggered the action.
     * @return a completable future of whether the action failed or succeeded.
     */
    public CompletableFuture<Boolean> setLocation(@NotNull Location location, @Nullable CommandSender sender) {
        WarpMoveEvent event = new WarpMoveEvent(this, location, sender);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return CompletableFuture.completedFuture(false);

        // Set the variables.
        this.location = location;
        this.updatedTime = System.currentTimeMillis();

        // The warp was updated, so update the timestamp and update it in the database.
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
    public @NotNull String getCreatedTimeFormatted() {
        return createdTimeFormatted;
    }

    /**
     * Returns the formatted time the warp was last updated at.
     *
     * @return the time the warp was last updated at in the format dd MMM yyyy HH:mm:ss
     */
    public @NotNull String getUpdatedTimeFormatted() {
        return updatedTimeFormatted;
    }

    public static void registerWarp(Warp warp) {
        warps.put(warp.name, warp);
    }

    /**
     * Deletes a specified warp.
     *
     * @param sender the command sender that called for the action. Can be null.
     * @return a completable future of whether the action failed or succeeded.
     */
    public @NotNull CompletableFuture<Void> delete(@Nullable CommandSender sender) {

        // Creates the event.
        WarpDeleteEvent event = new WarpDeleteEvent(this, sender);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return ATException.failedFuture(event);

        // Removes the warp in cache.
        warps.remove(name);

        // Remove the warp in the database.
        return CompletableFuture.runAsync(() -> {
            AdvancedTeleportAPI.FlattenedCallback<Boolean> callback = new AdvancedTeleportAPI.FlattenedCallback<>();
            WarpSQLManager.get().removeWarp(name, callback);
        });
    }

    /**
     * Deletes a specified warp.
     *
     * @return a completable future of whether the action failed or succeeded.
     */
    public CompletableFuture<Void> delete() {
        return delete((CommandSender) null);
    }

    static HashMap<String, Warp> warps() {
        return warps;
    }
}
