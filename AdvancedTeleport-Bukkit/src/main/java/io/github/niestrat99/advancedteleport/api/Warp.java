package io.github.niestrat99.advancedteleport.api;

import io.github.niestrat99.advancedteleport.api.events.warps.WarpDeleteEvent;
import io.github.niestrat99.advancedteleport.api.events.warps.WarpMoveEvent;
import io.github.niestrat99.advancedteleport.sql.WarpSQLManager;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Represents a warp point.
 */
public final class Warp implements NamedLocation {

    @Nullable private final UUID creator;
    @NotNull private final String name;
    @NotNull private Location location;
    private final long createdTime;
    private long updatedTime;
    @NotNull private final String createdTimeFormatted;
    @NotNull private String updatedTimeFormatted;
    @NotNull private final SimpleDateFormat format;

    private static final HashMap<String, Warp> warps = new HashMap<>();

    /**
     * Creates a warp object, but does not formally register it. To register a warp, use {@link AdvancedTeleportAPI#setWarp(String, CommandSender, Location)}.
     *
     * @param creator The creator of the warp. Can be null.
     * @param name The name of the warp.
     * @param location The location of the warp.
     * @param createdTime The time in milliseconds when the warp was created.
     * @param updatedTime The time in milliseconds when the warp was updated.
     */
    public Warp(
        @Nullable final UUID creator,
        @NotNull final String name,
        @NotNull final Location location,
        final long createdTime,
        final long updatedTime
    ) {
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
    @Contract(pure = true)
    public @NotNull String getName() {
        return name;
    }

    /**
     * Returns the location of the warp.
     *
     * @return the location of the warp.
     */
    @Contract(pure = true)
    public @NotNull Location getLocation() {
        return location;
    }

    /**
     * Returns the UUID of the warp's creator.
     *
     * @return the UUID of the warp's creator.
     */
    @Contract(pure = true)
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
    @Contract(pure = true)
    public long getCreatedTime() {
        return createdTime;
    }

    /**
     * Returns the time the warp was last updated at in milliseconds.
     *
     * @return the time in milliseconds the warp was last updated.
     */
    @Contract(pure = true)
    public long getUpdatedTime() {
        return updatedTime;
    }

    /**
     * Returns the formatted time the warp was created at.
     *
     * @return The time the warp was created at in the format dd MMM yyyy HH:mm:ss.
     */
    @Contract(pure = true)
    public @NotNull String getCreatedTimeFormatted() {
        return createdTimeFormatted;
    }

    /**
     * Returns the formatted time the warp was last updated at.
     *
     * @return The time the warp was last updated at in the format dd MMM yyyy HH:mm:ss.
     */
    @Contract(pure = true)
    public @NotNull String getUpdatedTimeFormatted() {
        return updatedTimeFormatted;
    }

    @Contract(pure = true)
    public static void registerWarp(@NotNull final Warp warp) {
        warps.put(warp.name, warp);
    }

    /**
     * Deletes a specified warp.
     *
     * @param sender the command sender that called for the action. Can be null.
     * @return a completable future of whether the action failed or succeeded.
     */
    public @NotNull CompletableFuture<Void> delete(@Nullable CommandSender sender) {
        WarpDeleteEvent event = new WarpDeleteEvent(this, sender);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return ATException.failedFuture(event);

        warps.remove(name);
        return CompletableFuture.runAsync(() -> {
            AdvancedTeleportAPI.FlattenedCallback<Boolean> callback = new AdvancedTeleportAPI.FlattenedCallback<>();
            WarpSQLManager.get().removeWarp(name, callback);
        });
    }

    /**
     * Deletes a specified warp.
     *
     * @return A completable future of whether the action failed or succeeded.
     */
    @Contract(pure = true)
    public @NotNull CompletableFuture<Void> delete() {
        return delete(null);
    }

    @ApiStatus.Internal
    @Contract(pure = true)
    static @NotNull HashMap<String, Warp> warps() {
        return warps;
    }
}
