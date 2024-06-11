package io.github.niestrat99.advancedteleport.api;

import com.google.common.collect.ImmutableMap;
import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.data.ATException;
import io.github.niestrat99.advancedteleport.api.events.CancellableATEvent;
import io.github.niestrat99.advancedteleport.api.events.spawn.*;
import io.github.niestrat99.advancedteleport.api.events.warps.WarpCreateEvent;
import io.github.niestrat99.advancedteleport.api.events.warps.WarpPostCreateEvent;
import io.github.niestrat99.advancedteleport.config.MainConfig;
import io.github.niestrat99.advancedteleport.managers.NamedLocationManager;
import io.github.niestrat99.advancedteleport.managers.RTPManager;
import io.github.niestrat99.advancedteleport.managers.SignManager;
import io.github.niestrat99.advancedteleport.sql.MetadataSQLManager;
import io.github.niestrat99.advancedteleport.sql.SpawnSQLManager;
import io.github.niestrat99.advancedteleport.sql.WarpSQLManager;

import io.github.niestrat99.advancedteleport.utilities.RandomTPAlgorithms;
import io.papermc.lib.PaperLib;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/** The class used for accessing common counterparts of the plugin. */
public final class AdvancedTeleportAPI {
    private AdvancedTeleportAPI() {}

    public static @NotNull CompletableFuture<@NotNull OfflinePlayer> getOfflinePlayer(
            @NotNull final String name) {
        return CompletableFuture.supplyAsync(() -> Bukkit.getOfflinePlayer(name), CoreClass.async);
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
    public static @NotNull CompletableFuture<Warp> setWarp(
            @NotNull final String name,
            @Nullable final CommandSender creator,
            @NotNull final Location location) {

        // Null checks
        Objects.requireNonNull(location, "The warp location must not be null.");
        if (!location.isWorldLoaded())
            return ATException.failedFuture("The world the warp is being set in must be loaded.");

        return validateEvent(
                new WarpCreateEvent(name, creator, location),
                event -> {

                    // Create the warp object
                    final Warp warp =
                            new Warp(
                                    maybePlayer(event.getSender())
                                            .map(Player::getUniqueId)
                                            .orElse(null),
                                    event.getName(),
                                    event.getLocation(),
                                    System.currentTimeMillis(),
                                    System.currentTimeMillis());

                    // Add the warp
                    NamedLocationManager.get().registerWarp(warp);
                    return CompletableFuture.runAsync(() -> WarpSQLManager.get().addWarp(warp))
                            .thenApplyAsync(
                                    ignored -> {

                                        // Call the event
                                        final WarpPostCreateEvent postCreateEvent =
                                                new WarpPostCreateEvent(warp);
                                        Bukkit.getServer()
                                                .getPluginManager()
                                                .callEvent(postCreateEvent);

                                        return warp;
                                    },
                                    CoreClass.sync);
                });
    }

    @ApiStatus.Internal
    public static <T extends CancellableATEvent, R> @NotNull CompletableFuture<R> validateEvent(
            @NotNull final T event,
            @NotNull final Function<T, CompletableFuture<R>> validatedEvent) {
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

    /**
     * Returns a hashmap of warps. The keys are the names of the warps, and the corresponding value
     * is the warp objects. Cannot be directly modified.
     *
     * @return a cloned hashmap of warps.
     */
    @Contract(pure = true)
    public static @NotNull ImmutableMap<String, Warp> getWarps() {
        return NamedLocationManager.get().getWarps();
    }

    public static boolean isWarpSet(@NotNull String name) {

        // Null check
        Objects.requireNonNull(name, "The warp name must not be null.");

        // Return the result
        return getWarps().containsKey(name);
    }

    /**
     * Sets a spawnpoint at a specific location.
     *
     * @param name the name/ID of the spawnpoint.
     * @param sender the creator of the spawnpoint.
     * @param location the location of the warp.
     * @return a completable future action of the saved spawnpoint.
     */
    public static @NotNull CompletableFuture<Spawn> setSpawn(
            @NotNull final String name,
            @Nullable final CommandSender sender,
            @NotNull final Location location) {

        // Null checks
        Objects.requireNonNull(location, "The spawn location must not be null.");
        if (!location.isWorldLoaded())
            return ATException.failedFuture(
                    location.getWorld(), "The world the spawn is being set in must be loaded.");

        return validateEvent(
                new SpawnCreateEvent(name, sender, location),
                event -> {

                    // Set up the spawn, register it, and send it off
                    Spawn spawn =
                            new Spawn(
                                    name,
                                    location,
                                    maybePlayer(event.getSender())
                                            .map(Player::getUniqueId)
                                            .orElse(null));
                    NamedLocationManager.get().registerSpawn(spawn);

                    // If there is no main spawn yet, make it this one too
                    if (NamedLocationManager.get().getMainSpawn() == null) {
                        Bukkit.getScheduler().runTask(CoreClass.getInstance(),
                                () -> AdvancedTeleportAPI.setMainSpawn(spawn, sender));
                    }

                    // Add it to the database.
                    return SpawnSQLManager.get()
                            .addSpawn(spawn)
                            .thenApplyAsync(
                                    x -> {

                                        // Call the post-create event to indicate success
                                        Bukkit.getPluginManager()
                                                .callEvent(new SpawnPostCreateEvent(spawn));
                                        return spawn;
                                    },
                                    CoreClass.sync);
                });
    }

    /**
     * Sets the main spawn.
     *
     * @param newSpawn the spawnpoint to be made the main one.
     * @param sender the player/command sender setting the main spawnpoint.
     * @return a completable future action of the new main spawn.
     */
    public static @NotNull CompletableFuture<Spawn> setMainSpawn(
            @Nullable final Spawn newSpawn, @Nullable final CommandSender sender) {
        return validateEvent(
                new SwitchMainSpawnEvent(AdvancedTeleportAPI.getMainSpawn(), newSpawn, sender),
                event ->
                        CompletableFuture.supplyAsync(
                                () -> {

                                    // Set the main spawn
                                    NamedLocationManager.get()
                                            .setMainSpawn(event.getNewMainSpawn());

                                    // Update it internally
                                    MetadataSQLManager.get().deleteMainSpawn().join();
                                    if (event.getNewMainSpawn() != null)
                                        MetadataSQLManager.get()
                                                .addSpawnMetadata(
                                                        event.getNewMainSpawn().getName(),
                                                        "main_spawn",
                                                        "true");

                                    return event.getNewMainSpawn();
                                }));
    }

    public static @Nullable Spawn getSpawn(@NotNull String name) {
        return NamedLocationManager.get().getSpawn(name);
    }

    public static @NotNull Spawn getSpawn(@NotNull World world) {
        return getDestinationSpawn(world, null);
    }

    public static @NotNull Spawn getDestinationSpawn(
            @NotNull World world, @Nullable Player player) {
        return NamedLocationManager.get().getSpawn(world, player);
    }

    public static @Nullable Spawn getMainSpawn() {
        return NamedLocationManager.get().getMainSpawn();
    }

    public static @NotNull ImmutableMap<String, Spawn> getSpawns() {
        return NamedLocationManager.get().getSpawns();
    }

    public static @NotNull CompletableFuture<@NotNull Location> getRandomLocation(
            @NotNull World world, @NotNull Player player) {

        if (MainConfig.get().RAPID_RESPONSE.get() && PaperLib.isPaper()) {

            // Attempt to get a nearby location urgently.
            Location nextLoc = RTPManager.getLocationUrgently(world);
            if (nextLoc != null) return CompletableFuture.completedFuture(nextLoc);

            // If one is not found, then look for another location.
            return RTPManager.getNextAvailableLocation(world);
        } else {

            // Otherwise get it from the Random TP algorithms
            return CompletableFuture.supplyAsync(
                    () -> RandomTPAlgorithms.getAlgorithms().get("binary").fire(player, world),
                    CoreClass.async);
        }
    }

    /**
     * Fetches a sign with a particular name.
     *
     * @param sign the name of the sign, e.g. "warps", "home".
     * @return the sign associated with the specified name, or null if one does not exist.
     */
    public static @Nullable ATSign getSign(final @NotNull String sign) {
        return SignManager.get().getSign(sign);
    }

    /**
     * Fetches a sign by a component display name.
     *
     * @param component the component to check against.
     * @return the sign associated with the display name.
     */
    public static @Nullable ATSign getSignByDisplayName(final @NotNull Component component) {
        return SignManager.get().getSignByDisplayName(component);
    }

    /**
     * Fetches a sign by a component display name, only comparing the component content and not styles.
     *
     * @param component the component to check against.
     * @return the sign associated with the display name.
     */
    public static @Nullable ATSign getSignByFlatDisplayName(final @NotNull TextComponent component) {
        return SignManager.get().getSignByFlatDisplayName(component);
    }

    /**
     * Fetches a sign by its legacy-formatted name.
     *
     * @param string the legacy-formatted name to check against.
     * @return the sign associated with the name.
     */
    public static @Nullable ATSign getSignByLegacyName(final @NotNull String string) {
        return SignManager.get().getSignByDisplayName(LegacyComponentSerializer.legacySection().deserialize(string));
    }

    /**
     * Registers a sign with a specified name.
     *
     * @param name the ID of the sign.
     * @param sign the sign itself.
     */
    public static void registerSign(final @NotNull String name, final @NotNull ATSign sign) {
        SignManager.get().register(name, sign);
    }
}
