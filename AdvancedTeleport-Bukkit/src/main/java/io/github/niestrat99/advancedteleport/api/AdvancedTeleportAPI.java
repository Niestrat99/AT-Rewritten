package io.github.niestrat99.advancedteleport.api;

import com.google.common.collect.ImmutableMap;
import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.data.ATException;
import io.github.niestrat99.advancedteleport.api.events.CancellableATEvent;
import io.github.niestrat99.advancedteleport.api.events.spawn.*;
import io.github.niestrat99.advancedteleport.api.events.warps.alias.WarpAliasAddEvent;
import io.github.niestrat99.advancedteleport.api.events.warps.WarpCreateEvent;
import io.github.niestrat99.advancedteleport.api.events.warps.WarpPostCreateEvent;
import io.github.niestrat99.advancedteleport.api.events.warps.alias.WarpAliasRemoveEvent;
import io.github.niestrat99.advancedteleport.config.MainConfig;
import io.github.niestrat99.advancedteleport.managers.NamedLocationManager;
import io.github.niestrat99.advancedteleport.managers.RTPManager;
import io.github.niestrat99.advancedteleport.sql.MetadataSQLManager;
import io.github.niestrat99.advancedteleport.sql.SpawnSQLManager;
import io.github.niestrat99.advancedteleport.sql.WarpSQLManager;

import io.github.niestrat99.advancedteleport.utilities.RandomTPAlgorithms;
import io.papermc.lib.PaperLib;
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

import java.util.*;
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
     * Attempts to fetch a warp through standard means or through an alias whilst checking player permissions.
     *
     * @param name the name of the warp/alias.
     * @param player the player to be teleported.
     * @param usingSign true if the player is using a sign to teleport.
     * @return a warp that the user can teleport to, null if the warp does not exist or the player does not have permissions.
     */
    @Contract(pure = true)
    public static @Nullable Warp fetchWarp(@NotNull String name, @Nullable Player player, boolean usingSign) {

        // Set up warp prefix
        final String WARP_PREFIX = "at.member.warp." + (usingSign ? "sign." : "");

        // See if there's a warp alias to go by
        Map<String, List<String>> aliases = NamedLocationManager.get().getWarpAliases();
        List<String> warps = aliases.getOrDefault(name, new ArrayList<>());
        if (warps != null && !warps.isEmpty()) {

            // Get all the aliases
            Iterator<String> warpsIterator = warps.listIterator();
            List<String> validWarps = new ArrayList<>();
            while (warpsIterator.hasNext()) {

                // Check permissions
                String warpName = warpsIterator.next();

                // No player? No problem
                if (player == null) {
                    validWarps.add(warpName);
                    continue;
                }

                // If the player has the wildcard permission, automatically add
                if (player.hasPermission(WARP_PREFIX + "*") || player.hasPermission(WARP_PREFIX + name.toLowerCase() + ".*")) {
                    validWarps.add(warpName);
                    continue;
                }

                // If the player has explicit permission to the warp, use that
                String fullPermission = WARP_PREFIX + name.toLowerCase() + "." + warpName.toLowerCase();
                if (player.isPermissionSet(fullPermission)) {
                    if (player.hasPermission(fullPermission)) {
                        validWarps.add(warpName);
                    }
                }
            }

            // Only continue if there is a valid random warp to pick from
            while (!validWarps.isEmpty()) {

                // IRS: tax time :)
                // Me: How much?
                // IRS: secret :3
                // Me: why?
                // IRS: just guess ;p
                String warpName = validWarps.get(new Random().nextInt(validWarps.size()));
                // Me: $600?
                Warp warp = getWarp(warpName);

                // IRS: jail :(
                if (warp == null) {
                    validWarps.remove(warpName);
                    continue;
                }

                return warp;
            }
        }

        // Otherwise, fetch the warp
        Warp warp = getWarp(name);
        if (player == null) return warp;

        // Since the player exists, ensure they have permission
        boolean found = player.hasPermission(WARP_PREFIX + "*");
        if (player.isPermissionSet(WARP_PREFIX + name.toLowerCase())) {
            found = player.hasPermission(WARP_PREFIX + name.toLowerCase());
        }

        // If they don't have permission, stop there
        return found ? warp : null;
    }

    public static boolean canAccessWarp(@NotNull Player player, @NotNull String name, boolean usingSign) {

        // Set up warp prefix
        final String WARP_PREFIX = "at.member.warp." + (usingSign ? "sign." : "");

        // If it's an alias, check individual warps
        if (getWarpAliases().containsKey(name)) {
            if (player.hasPermission(WARP_PREFIX + "*") || player.hasPermission(WARP_PREFIX + name.toLowerCase() + ".*"))
                return true;

            for (String warpName : getWarpAliases(name)) {
                String fullPermission = WARP_PREFIX + name.toLowerCase() + "." + warpName.toLowerCase();
                if (player.isPermissionSet(fullPermission) && player.hasPermission(fullPermission)) return true;
            }
        }

        // If it's a normal warp, make sure it exists
        Warp warp = getWarp(name);
        if (warp == null) return false;

        return player.hasPermission(WARP_PREFIX + "*") ||
                (player.isPermissionSet(WARP_PREFIX + name.toLowerCase())
                        && player.hasPermission(WARP_PREFIX + name.toLowerCase()));
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

    @Contract(pure = true)
    public static @NotNull Map<String, List<String>> getWarpAliases() {
        return NamedLocationManager.get().getWarpAliases();
    }

    @Contract(pure = true)
    public static @NotNull List<String> getWarpAliases(@NotNull String name) {
        return getWarpAliases().getOrDefault(name, new ArrayList<>());
    }

    @Contract(pure = true)
    public static boolean isAlias(@NotNull String alias, @NotNull String name) {
        return getWarpAliases(alias).contains(name);
    }

    @Contract(pure = true)
    public static CompletableFuture<Boolean> addWarpAlias(
            @NotNull final String alias,
            @NotNull final Warp warp,
            @Nullable final CommandSender sender
    ) {

        // Make sure we don't dupe aliases
        if (isAlias(alias, warp.getName()))
            throw new IllegalStateException(warp.getName() + " is already an alias of " + alias + "!");

        return validateEvent(new WarpAliasAddEvent(alias, warp, sender), event -> {

            // Add the alias internally
            NamedLocationManager.get().addWarpAlias(event.getName(), event.getWarp().getName());

            // Update it in the metadata table
            return MetadataSQLManager.get().addWarpMetadata(event.getWarp().getName(), "alias", event.getName(), false);
        });
    }

    @Contract(pure = true)
    public static CompletableFuture<Boolean> removeWarpAlias(
            @NotNull final String alias,
            @NotNull final Warp warp,
            @Nullable final CommandSender sender
    ) {

        // Not an alias? Say something
        if (isAlias(alias, warp.getName()))
            throw new IllegalStateException(warp.getName() + " is not an alias of " + alias + "!");

        return validateEvent(new WarpAliasRemoveEvent(alias, warp, sender), event -> {

            // Remove the alias internally, so it doesn't appear in the plugin
            NamedLocationManager.get().removeWarpAlias(event.getOldAlias(), event.getWarp().getName());

            return MetadataSQLManager.get().deleteWarpMetadata(event.getWarp().getName(), "alias", event.getOldAlias());
        });
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
}
