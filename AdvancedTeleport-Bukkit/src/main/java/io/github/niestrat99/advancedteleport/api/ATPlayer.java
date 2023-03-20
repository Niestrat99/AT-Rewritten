package io.github.niestrat99.advancedteleport.api;

import com.google.common.collect.ImmutableMap;
import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.data.CancelledEventException;
import io.github.niestrat99.advancedteleport.api.events.ATTeleportEvent;
import io.github.niestrat99.advancedteleport.api.events.homes.HomeCreateEvent;
import io.github.niestrat99.advancedteleport.api.events.homes.HomeDeleteEvent;
import io.github.niestrat99.advancedteleport.api.events.homes.SwitchMainHomeEvent;
import io.github.niestrat99.advancedteleport.api.events.players.PreviousLocationChangeEvent;
import io.github.niestrat99.advancedteleport.api.events.players.ToggleTeleportationEvent;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.MainConfig;
import io.github.niestrat99.advancedteleport.managers.CooldownManager;
import io.github.niestrat99.advancedteleport.managers.MovementManager;
import io.github.niestrat99.advancedteleport.managers.ParticleManager;
import io.github.niestrat99.advancedteleport.managers.PluginHookManager;
import io.github.niestrat99.advancedteleport.payments.PaymentManager;
import io.github.niestrat99.advancedteleport.sql.BlocklistManager;
import io.github.niestrat99.advancedteleport.sql.HomeSQLManager;
import io.github.niestrat99.advancedteleport.sql.PlayerSQLManager;
import io.github.thatsmusic99.configurationmaster.api.ConfigSection;
import io.papermc.lib.PaperLib;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

/**
 * A wrapper class used to represent a player. An ATPlayer stores information such as their homes, the players they
 * have blocked, whether they have teleportation enabled, their main home, and previous location.
 *
 * @since v5.4
 */
public class ATPlayer {

    private static final @NotNull HashMap<String, ATPlayer> players = new HashMap<>();
    protected @NotNull UUID uuid;
    private final @NotNull PendingData<LinkedHashMap<String, @NotNull Home>> homes;
    private final @NotNull PendingData<HashMap<UUID, @NotNull BlockInfo>> blockedUsers;
    private final @NotNull PendingData<Boolean> isTeleportationEnabled;
    private final @NotNull PendingData<@Nullable String> mainHome;
    private final @NotNull PendingData<@Nullable Location> previousLoc;

    /**
     * Internal use only.
     */
    public ATPlayer(@NotNull Player player) {
        this(player.getUniqueId(), player.getName());
    }

    /**
     * Internal use only.
     */
    @ApiStatus.Internal
    public ATPlayer(
        @NotNull final UUID uuid,
        @NotNull final String name
    ) {
        this.uuid = uuid;

        // Set up homes data
        this.homes = new PendingData<>(CompletableFuture.supplyAsync(() -> HomeSQLManager.get().getHomes(uuid.toString()), CoreClass.async)
                .thenApplyAsync(list -> {

                    if (getBedSpawn() != null && MainConfig.get().ADD_BED_TO_HOMES.get()) {
                        list.put("bed", getBedSpawn());
                    }

                    return list;
                }, CoreClass.async));

        // Set up the main home data
        this.mainHome = new PendingData<>(CompletableFuture.supplyAsync(() -> PlayerSQLManager.get().getMainHome(name), CoreClass.async)
                .thenApplyAsync(home -> {
                    reorganiseHomes();
                    return home;
                }, CoreClass.async));

        // Get blocked users
        this.blockedUsers = new PendingData<>(CompletableFuture.supplyAsync(() -> BlocklistManager.get().getBlockedPlayers(uuid.toString()), CoreClass.async));
        this.previousLoc = new PendingData<>(CompletableFuture.supplyAsync(() -> PlayerSQLManager.get().getPreviousLocation(name), CoreClass.async));
        this.isTeleportationEnabled = new PendingData<>(CompletableFuture.supplyAsync(() -> PlayerSQLManager.get().isTeleportationOn(uuid), CoreClass.async));

        if (PluginHookManager.get().floodgateEnabled()) {
            org.geysermc.floodgate.api.FloodgateApi api = org.geysermc.floodgate.api.FloodgateApi.getInstance();
            if (api == null) {
                CoreClass.getInstance().getLogger().severe("Detected the floodgate plugin, but it seems to be out of date. Please use floodgate v2.");
                return;
            }
            if (api.isFloodgateId(uuid)) this.uuid = api.getPlayer(uuid).getCorrectUniqueId();
        }


        players.put(name.toLowerCase(), this);
    }

    /**
     * Gets the offline Bukkit player object representing this ATPlayer.
     *
     * @return the offline Bukkit player representing this ATPlayer.
     */
    @Contract(pure = true)
    public @NotNull OfflinePlayer getOfflinePlayer() {
        return Bukkit.getOfflinePlayer(uuid);
    }

    /*
     * BLOCKING FUNCTIONALITY
     */
    @Contract(pure = true)
    public @NotNull UUID uuid() {
        return uuid;
    }

    @Deprecated
    @ApiStatus.Internal
    public void teleport(
        ATTeleportEvent event,
        String command,
        String teleportMsg,
        int warmUp
    ) {
        teleport(event, command, teleportMsg);
    }

    @ApiStatus.Internal
    public void teleport(
        @NotNull final ATTeleportEvent event,
        @NotNull final String command,
        @NotNull final String teleportMsg
    ) {
        Player player = event.getPlayer();
        int warmUp = getWarmUp(command);
        if (event.isCancelled()) return;
        if (!PaymentManager.getInstance().canPay(command, player)) return;

        // If the cooldown is to be applied after request or accept (they are the same in the case of /tpr), apply it now
        String cooldownConfig = MainConfig.get().APPLY_COOLDOWN_AFTER.get();

        if (cooldownConfig.equalsIgnoreCase("request") || cooldownConfig.equalsIgnoreCase("accept")) {
            CooldownManager.addToCooldown(command, player);
        }

        // If there's a movement timer, apply it - otherwise, teleport them immediately
        if (warmUp > 0 && !player.hasPermission("at.admin.bypass.timer")) {
            MovementManager.createMovementTimer(player, event.getToLocation(), command, teleportMsg, warmUp,
                 Placeholder.unparsed("home", event.getLocName()),
                 Placeholder.unparsed("warp", event.getLocName())
            );
        } else {
            ParticleManager.onTeleport(player, command);
            PaperLib.teleportAsync(player, event.getToLocation(), PlayerTeleportEvent.TeleportCause.COMMAND).whenComplete((result, err) -> {

                // If we didn't succeed, let the player know.
                if (!result) {
                    CustomMessages.sendMessage(player, "Error.teleportFailed");
                    return;
                }

                // Let the player know they have been teleported and withdraw any money.
                CustomMessages.sendMessage(player, teleportMsg,
                        Placeholder.unparsed("home", event.getLocName()),
                        Placeholder.unparsed("warp", event.getLocName())
                );
                PaymentManager.getInstance().withdraw(command, player);
            });
        }
    }
    
    /**
     * Returns whether teleportation is enabled for the player. This allows the player to receive teleportation requests
     * if set to true.
     *
     * @return true if teleportation is enabled, false if it is disabled.
     */
    @Contract(pure = true)
    public boolean isTeleportationEnabled() {
        return Boolean.TRUE.equals(isTeleportationEnabled.data);
    }

    /**
     * Gets the Bukkit player object representing this ATPlayer.
     *
     * @return the Bukkit player representing this ATPlayer. This is null if the player is not online.
     */
    @Contract(pure = true)
    public @Nullable Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    /**
     * Toggles teleportation for the player, setting it to a specific status.
     *
     * @param teleportationEnabled true to enable teleportation, false to disable it.
     * @return a completable future of whether the action failed or succeeded.
     */
    public @NotNull CompletableFuture<Void> setTeleportationEnabled(boolean teleportationEnabled) {
        return setTeleportationEnabled(teleportationEnabled, null);
    }

    /**
     * Toggles teleportation for the player, setting it to a specific status.
     *
     * @param teleportationEnabled true to enable teleportation, false to disable it.
     * @param sender               the command sender that triggered the action.
     * @return a completable future of whether the action failed or succeeded.
     */
    public @NotNull CompletableFuture<Void> setTeleportationEnabled(
        final boolean teleportationEnabled,
        @Nullable final CommandSender sender
    ) {
        return AdvancedTeleportAPI.validateEvent(new ToggleTeleportationEvent(
                sender,
                getOfflinePlayer(),
                teleportationEnabled,
                isTeleportationEnabled() ^ teleportationEnabled
        ), event -> {
            this.isTeleportationEnabled.data = event.isEnabled();
            return CompletableFuture.runAsync(() -> PlayerSQLManager.get().setTeleportationOn(uuid, teleportationEnabled), CoreClass.async);
        });
    }

    /**
     * Checks to see if this player has blocked a specified player.
     *
     * @param otherPlayer The player that this one theoretically blocked.
     * @return true is otherPlayer is blocked, false if otherwise.
     */
    @Contract(pure = true)
    public boolean hasBlocked(@NotNull final OfflinePlayer otherPlayer) {
        return hasBlocked(otherPlayer.getUniqueId());
    }

    /**
     * Checks to see if this player has blocked a specified player.
     *
     * @param otherPlayer The player that this one theoretically blocked.
     * @return true is otherPlayer is blocked, false if otherwise.
     */
    @Contract(pure = true)
    public boolean hasBlocked(@NotNull final UUID otherPlayer) {
        return blockedUsers.data != null && blockedUsers.data.containsKey(otherPlayer);
    }

    /**
     * Gets the information regarding the block relationship between this player and someone else. This only gets
     * information if this player has blocked the other, not vice versa. To do this, get the ATPlayer object of the
     * other player and check if they have blocked this player using {@link ATPlayer#hasBlocked(OfflinePlayer)}.
     *
     * @param otherPlayer The other player.
     * @return A BlockInfo object if this player has blocked the other player, but null if they haven't.
     */
    @Contract(pure = true)
    public @Nullable BlockInfo getBlockInfo(@NotNull final OfflinePlayer otherPlayer) {
        return blockedUsers.data != null ? blockedUsers.data.get(otherPlayer.getUniqueId()) : null;
    }

    /**
     * Makes this ATPlayer block another player, stopping the other player from sending teleportation requests to them.
     *
     * @param otherPlayer the player being blocked.
     * @return a completable future of whether the action failed or succeeded.
     */
    public @NotNull CompletableFuture<Void> blockUser(@NotNull final OfflinePlayer otherPlayer) {
        return blockUser(otherPlayer.getUniqueId(), null);
    }

    /**
     * Makes this ATPLayer block another player with the specified UUID with a given reason, stopping the other player
     * from sending teleportation requests to them.
     *
     * @param otherUUID the player's UUID to be blocked.
     * @param reason    the reason the player has been blocked. Can be null.
     * @return a completable future of whether the action failed or succeeded.
     */
    public @NotNull CompletableFuture<Void> blockUser(
        @NotNull final UUID otherUUID,
        @Nullable final String reason
    ) {

        // Add it to the internal data store
        blockedUsers.getData().thenApplyAsync(list ->
                list.put(otherUUID, new BlockInfo(uuid, otherUUID, reason, System.currentTimeMillis())));

        // Add the entry to the SQL database.
        return CompletableFuture.runAsync(() ->
                BlocklistManager.get().blockUser(uuid.toString(), otherUUID.toString(), reason), CoreClass.async);
    }

    /**
     * Makes this ATPlayer block another player with a specified reason, stopping the other player from sending
     * teleportation requests to them.
     *
     * @param otherPlayer the player being blocked.
     * @param reason      the reason the player has been blocked. Can be null.
     * @return a completable future of whether the action failed or succeeded.
     */
    public @NotNull CompletableFuture<Void> blockUser(
        @NotNull final OfflinePlayer otherPlayer,
        @Nullable final String reason
    ) {
        return blockUser(otherPlayer.getUniqueId(), reason);
    }

    /**
     * Makes this player unblock a player with the specified UUID.
     *
     * @param otherUUID the UUID of the player to be unblocked.
     * @return a completable future of whether the action failed or succeeded.
     */
    public @NotNull CompletableFuture<Void> unblockUser(@NotNull final UUID otherUUID) {
        blockedUsers.getData().thenApplyAsync(list -> list.remove(otherUUID));

        return CompletableFuture.runAsync(() ->
                BlocklistManager.get().unblockUser(uuid.toString(), otherUUID.toString()), CoreClass.async);
    }

    /**
     * Returns a hashmap of homes, where the key is the home name, and the value is the home object.
     *
     * @return a hashmap of homes.
     */
    @Contract(pure = true)
    public @NotNull ImmutableMap<String, Home> getHomes() {
        return ImmutableMap.copyOf(homes.data == null ? new HashMap<>() : homes.data);
    }

    @Contract(pure = true)
    public @NotNull CompletableFuture<ImmutableMap<String, Home>> getHomesAsync() {
        return homes.getData().thenApplyAsync(ImmutableMap::copyOf);
    }

    /**
     * Adds a home to the player's home list.
     *
     * @param name     the name of the home.
     * @param location the location of the home.
     * @return a completable future of whether the action failed or succeeded.
     */
    public @NotNull CompletableFuture<Void> addHome(
        @NotNull final String name,
        @NotNull final Location location
    ) {
        return addHome(name, location, getPlayer(), true);
    }

    /**
     * Adds a home to the player's home list.
     *
     * @param name the name of the home.
     * @param location the location of the home.
     * @param creator the player who created the home.
     * @return a completable future of whether the action failed or succeeded.
     */
    public @NotNull CompletableFuture<Void> addHome(
        @NotNull final String name,
        @NotNull final Location location,
        @NotNull final Player creator
    ) {
        return addHome(name, location, creator, true);
    }

    /**
     * Adds a home to the player's home list.
     *
     * @param name     the name of the home.
     * @param location the location of the home.
     * @param creator  the player who created the home.
     * @param async    true if the home is to be added asynchronously, false if not.
     * @return a completable future of whether the action failed or succeeded.
     */
    public @NotNull CompletableFuture<Void> addHome(
        @NotNull final String name,
        @NotNull final Location location,
        @Nullable final Player creator,
        final boolean async
    ) {

        // If the home exists, move it instead
        if (hasHome(name)) return moveHome(name, location);

        return AdvancedTeleportAPI.validateEvent(new HomeCreateEvent(
            getOfflinePlayer(),
            name,
            location,
            creator
        ), event -> {
            homes.getData().thenApplyAsync(list -> {
                    list.put(name,
                            new Home(
                                    event.getPlayer().getUniqueId(),
                                    event.getName(), event.getLocation(),
                                    System.currentTimeMillis(),
                                    System.currentTimeMillis()
                            )
                    );

                    homes.data = list;
                    return list;
            });

            return CompletableFuture.runAsync(() -> HomeSQLManager.get().addHome(location, uuid, name, async), CoreClass.async);
        });
    }

    /**
     * Whether the player has a home with the specified name.
     *
     * @param name The name of the home.
     * @return true if the player has a home named as specified, false if they do not.
     */
    public boolean hasHome(@NotNull final String name) {
        return homes.data != null && homes.data.containsKey(name);
    }

    /**
     * Moves a specified home to a new location.
     *
     * @param name        the name of the home.
     * @param newLocation the new location of the home.
     * @return a completable future of whether the action failed or succeeded.
     */
    public @NotNull CompletableFuture<Void> moveHome(
        @NotNull final String name,
        @NotNull final Location newLocation
    ) {
        return moveHome(name, newLocation, null);
    }

    /**
     * Moves a specified home to a new location.
     *
     * @param name        the name of the home.
     * @param newLocation the new location of the home.
     * @return a completable future of whether the action failed or succeeded.
     */
    public @NotNull CompletableFuture<Void> moveHome(
        @NotNull final String name,
        @NotNull final Location newLocation,
        @Nullable final CommandSender sender
    ) {
        return this.homes.getData().thenApplyAsync(homes -> {

            Home home = homes.get(name);
            if (home == null)
                throw new NullPointerException("Context [%s] | Message [%s]".formatted(sender, "Missing home: " + name));

            return home;
        }, CoreClass.sync).thenAcceptAsync(home -> home.move(newLocation, sender));
    }

    /**
     * Removes a specified home.
     *
     * @param name the name of the home.
     * @return a completable future of whether the action failed or succeeded.
     */
    public @NotNull CompletableFuture<Void> removeHome(@NotNull final String name) {
        return removeHome(name, null);
    }

    /**
     * Removes a specified home.
     *
     * @param name   the name of the home.
     * @param sender the command sender that triggered the event.
     * @return a completable future of whether the action failed or succeeded.
     */
    public @NotNull CompletableFuture<Void> removeHome(
        @NotNull final String name,
        @Nullable final CommandSender sender
    ) {
        return homes.getData().thenAcceptAsync(list -> {

            Home home = list.get(name);
            if (home == null) return;

            HomeDeleteEvent event = new HomeDeleteEvent(home, sender);
            if (!event.callEvent()) throw new RuntimeException(CancelledEventException.of(event));

            list.remove(event.getHome().getName());
            homes.data = list;
            HomeSQLManager.get().removeHome(uuid, event.getHome().getName());
        }, CoreClass.sync);
    }

    /**
     * Returns a specified home object from the name.
     *
     * @param name the name of the home.
     * @return the home object itself, null if it doesn't exist.
     * @throws NullPointerException if name is null.
     */
    @Contract(pure = true)
    public @Nullable Home getHome(@NotNull final String name) {
        return homes.data == null ? null : homes.data.get(name);
    }

    /**
     * Gets the bed home object of the player.
     *
     * @return if the player has a bed spawn set, return the home object, else return null.
     */
    @Contract(pure = true)
    public @Nullable Home getBedSpawn() {
        if (getOfflinePlayer().getBedSpawnLocation() != null) {
            return new Home(uuid, "bed", getOfflinePlayer().getBedSpawnLocation(), -1, -1);
        }

        return null;
    }

    /**
     * Whether the player has a main home.
     *
     * @return true if the player has a main home that exists, false if not.
     */
    @Contract(pure = true)
    public boolean hasMainHome() {
        return homes.data != null && mainHome.data != null && homes.data.containsKey(mainHome.data);
    }

    /**
     * Returns the main home of the player as a home object.
     *
     * @return the main home as a home object, or null if it does not exist.
     */
    @Contract(pure = true)
    public @Nullable Home getMainHome() {
        if (homes.data == null || mainHome.data == null) return null;
        return homes.data.get(mainHome.data);
    }

    /**
     * Sets the main home of the player.
     *
     * @param name the name of the home to be used.
     * @return a completable future of whether the action failed or succeeded.
     */
    @Contract(pure = true)
    public @NotNull CompletableFuture<Void> setMainHome(@NotNull final String name) {
        return setMainHome(name, null);
    }

    /**
     * Sets the main home of the player.
     *
     * @param name the name of the home to be used.
     * @param sender the command sender that triggered the event.
     * @return a completable future of whether the action failed or succeeded.
     */
    @Contract(pure = true)
    public @NotNull CompletableFuture<Void> setMainHome(
            @NotNull final String name,
            @Nullable final CommandSender sender
    ) {

        return homes.getData().thenAcceptAsync(list -> {

            Home home = list.get(name);
            if (home == null)
                throw new NullPointerException("Context [%s] | Message [%s]".formatted(sender, "Missing home: " + name));

            reorganiseHomes();

            SwitchMainHomeEvent event = new SwitchMainHomeEvent(mainHome.data == null ? null : list.get(mainHome.data), home, sender);
            if (!event.callEvent()) return;

            this.mainHome.data = event.getNewMainHome().getName();

            PlayerSQLManager.get().setMainHome(uuid, event.getNewMainHome().getName());
        }, CoreClass.sync);
    }

    private void reorganiseHomes() {

        this.homes.getData().thenAcceptAsync(homes -> {

            final var homesList = homes;

            this.mainHome.getData().thenAcceptAsync(mainHome -> {

                if (!homesList.containsKey(mainHome)) return;

                final var tempHomes = new LinkedHashMap<String, Home>();

                tempHomes.put(mainHome, homesList.get(mainHome));
                homesList.keySet().stream()
                        .filter(home -> !home.equals(mainHome))
                        .forEach(home -> tempHomes.put(home, homes.get(home)));

                this.homes.data = tempHomes;
                this.mainHome.data = mainHome;
            });
        });
    }

    /**
     * Used to get the permission for how many homes a player can have.
     * <p>
     * If there is no permission, then it's assumed that the number of homes they can have is limitless (-1).
     * <p>
     * If they have at.member.homes.unlimited, then well, they have unlimited homes, what were you expecting, a
     * plasma TV?
     * <p>
     * e.g.
     * - at.member.homes.5
     * - at.member.homes.40
     * - at.member.homes.100000
     */
    public int getHomesLimit() {
        int maxHomes = MainConfig.get().DEFAULT_HOMES_LIMIT.get();

        // Whether or not the limit is being overriden by a per-world homes limit
        boolean worldSpecific = false;

        // Player is offline, we'll assume an admin is getting the homes
        if (getPlayer() == null) return -1;
        for (PermissionAttachmentInfo permission : getPlayer().getEffectivePermissions()) {
            if (permission.getValue() && permission.getPermission().startsWith("at.member.homes.")) {

                // Get the permission and all data following the base permission
                String perm = permission.getPermission();
                String endNode = perm.substring("at.member.homes.".length());

                // If there's a world included
                // If not, make sure there's no world limit overriding
                if (endNode.lastIndexOf(".") != -1) {
                    String[] data = endNode.split("\\.");

                    // Make sure it's in the same world
                    if (data[0].equals(getPlayer().getWorld().getName()) && data[1].matches("^\\d+$")) {
                        int homes = Integer.parseInt(data[1]);

                        // If there isn't already a world limit overriding this one, make it do so.
                        // Otherwise, make sure this limit actually changes something
                        if (!worldSpecific) {
                            maxHomes = homes;
                            worldSpecific = true;
                        } else if (maxHomes < homes) {
                            maxHomes = homes;
                        }
                    }
                } else if (worldSpecific) {
                    continue;
                }
                if (endNode.equalsIgnoreCase("unlimited")) return -1;
                if (!endNode.matches("^\\d+$")) continue;
                int homes = Integer.parseInt(endNode);
                if (maxHomes < homes) {
                    maxHomes = homes;
                }

            }
        }
        return maxHomes;
    }

    @Range(from = 0, to = Integer.MAX_VALUE)
    @Contract(pure = true)
    public int getCooldown(@NotNull final String command) {
        return getMin("at.member.cooldown", command, MainConfig.get().CUSTOM_COOLDOWNS.get(), MainConfig.get().COOLDOWNS.valueOf(command).get());
    }

    @Range(from = 0, to = Integer.MAX_VALUE)
    @Contract(pure = true)
    public int getWarmUp(@NotNull final String command) {
        return getMin("at.member.timer", command, MainConfig.get().CUSTOM_WARM_UPS.get(), MainConfig.get().WARM_UPS.valueOf(command).get());
    }

    @Range(from = 0, to = Integer.MAX_VALUE)
    @Contract(pure = true)
    public int getDistanceLimitation(@Nullable final String command) {
        return determineValue("at.member.distance", command, command == null ? MainConfig.get().MAXIMUM_TELEPORT_DISTANCE.get()
                : MainConfig.get().DISTANCE_LIMITS.valueOf(command).get(), MainConfig.get().CUSTOM_DISTANCE_LIMITS.get(), Math::max);
    }

    @Range(from = 0, to = Integer.MAX_VALUE)
    @Contract(pure = true)
    private int getMin(
        @NotNull final String permission,
        @Nullable final String command,
        @NotNull final ConfigSection customSection,
        final int defaultValue
    ) {
        return determineValue(permission, command, defaultValue, customSection, Math::min);
    }

    @Range(from = 0, to = Integer.MAX_VALUE)
    @Contract(pure = true)
    private int determineValue(
        @NotNull final String permission,
        @Nullable final String command,
        final int defaultValue,
        @NotNull final ConfigSection customSection,
        @NotNull final BiFunction<Integer, Integer, Integer> consumer
    ) {
        List<String> cooldowns = new ArrayList<>();

        // If the player is null, return the default value
        if (getPlayer() == null) return defaultValue;

        // Get the custom section keys
        for (String key : customSection.getKeys(false)) {
            String value = customSection.getString(key);
            String worldName = getPlayer().getWorld().getName().toLowerCase(Locale.ENGLISH);
            if (!getPlayer().hasPermission(permission + "." + key)
                    && !getPlayer().hasPermission(permission + "." + command + "." + key)
                    && !getPlayer().hasPermission(permission + "." + worldName + "." + key)
                    && !getPlayer().hasPermission(permission + "." + command + "." + worldName + "." + key)) continue;

            // Make sure there's only one value
            cooldowns.clear();
            cooldowns.add(value);
        }

        // If no cooldowns have been specified,
        if (cooldowns.isEmpty()) {
            if (command == null) {
                cooldowns = getDynamicPermission(permission);
            } else {
                cooldowns = getDynamicPermission(permission + "." + command);
                if (cooldowns.isEmpty()) cooldowns = getDynamicPermission(permission);
            }
        } else {
            return Integer.parseInt(cooldowns.get(0));
        }


        int min = defaultValue;
        boolean changed = false;
        for (String cooldown : cooldowns) {
            if (!cooldown.matches("^\\d+$")) continue;
            if (!changed) {
                min = Integer.parseInt(cooldown);
                changed = true;
                continue;
            }
            min = consumer.apply(min, Integer.parseInt(cooldown));
        }
        return min;
    }

    private @NotNull List<String> getDynamicPermission(@NotNull final String rawPrefix) {
        final var prefix = rawPrefix.endsWith(".") ? rawPrefix : rawPrefix + ".";
        // If the player is offline, return nothing
        if (getPlayer() == null) return new ArrayList<>();

        // Whether the limit is being overridden by a per-world homes limit
        boolean worldSpecific = false;

        // Track values - String is the value after the permissions
        List<String> results = new ArrayList<>();

        // Go through each permission
        for (PermissionAttachmentInfo permission : getPlayer().getEffectivePermissions()) {

            // If the permission is granted, and it's the one we want
            if (permission.getValue() && permission.getPermission().startsWith(prefix)) {

                // Get the permission and all data following the base permission
                String perm = permission.getPermission();
                String endNode = perm.substring(prefix.length());

                // If there's a world included
                // If not, make sure there's no world limit overriding
                if (endNode.lastIndexOf(".") != -1) {
                    String[] data = endNode.split("\\.");

                    // Make sure it's in the same world
                    if (data[0].equals(getPlayer().getWorld().getName())) {
                        if (!worldSpecific) results.clear();
                        worldSpecific = true;
                        results.add(data[1]);
                    }
                } else if (worldSpecific) {
                    continue;
                }
                results.add(endNode);
            }
        }
        return results;
    }

    /**
     * Whether the player can access a specified home or not. A player may lose home access if
     * `deny-homes-if-over-limit`
     * is set to true in the config.yml file, and if they used to have a higher homes limit than they currently have.
     *
     * @param home The home having access checked.
     * @return true if the player can access the home, false if they cannot.
     */
    public boolean canAccessHome(@NotNull final Home home) {

        // If the homes limit is -1, it's unlimited
        if (getHomesLimit() == -1) return true;

        // If we don't deny home access if the home limit has already been exceeded, allow them access
        if (!MainConfig.get().DENY_HOMES_IF_OVER_LIMIT.get()) return true;

        // If the home exists, ensure the index is below the homes index.
        if (homes.data != null && homes.data.containsValue(home)) {
            List<Home> homes = new ArrayList<>(this.homes.data.values());
            int index = homes.indexOf(home);
            return index < getHomesLimit();
        }

        // If they don't have a home though, stop there
        return false;
    }

    /**
     * Whether the player can set more homes. If {@link ATPlayer#getHomesLimit()} returns -1, then they can set
     * unlimited homes. If it isn't, then the number of homes the player has is compared to the homes limit. If it is
     * fewer than the homes limit, they can set more homes.
     *
     * @return true if the player can set more homes, false if they can not.
     */
    public boolean canSetMoreHomes() {
        return getHomesLimit() == -1 || (homes.data != null && homes.data.size() < getHomesLimit());
    }

    /**
     * Gets an instance of an ATPlayer by using the player object.
     *
     * @param player the player to get an ATPlayer instance of.
     * @return an ATPlayer object representing the player.
     */
    public static @NotNull ATPlayer getPlayer(@NotNull final Player player) {

        // If the player is cached, return the cached player
        if (players.containsKey(player.getName().toLowerCase())) return players.get(player.getName().toLowerCase());

        // If floodgate is on the server, see if they're a Bedrock player - if so, initiate them as a Floodgate player
        if (PluginHookManager.get().floodgateEnabled()) {
            org.geysermc.floodgate.api.FloodgateApi api = org.geysermc.floodgate.api.FloodgateApi.getInstance();
            if (api == null) {
                CoreClass.getInstance().getLogger().severe("Detected the floodgate plugin, but it seems to be out of date. Please use floodgate v2.");
                return new ATPlayer(player);
            }
            if (api.isFloodgateId(player.getUniqueId())) return new ATFloodgatePlayer(player);
        }
        return new ATPlayer(player);
    }

    /**
     * Gets an instance of an ATPlayer by using the player object.
     *
     * @param player the player to get an ATPlayer instance of.
     * @return an ATPlayer object representing the player.
     * @throws NullPointerException if the player, their UUID or their name is null.
     */
    public static @NotNull ATPlayer getPlayer(@NotNull final OfflinePlayer player) {
        String name = player.getName();

        // Perform null checks
        Objects.requireNonNull(name, "Player name must not be null.");
        Objects.requireNonNull(player.getUniqueId(), "Player UUID must not be null.");

        // If the player is cached, get the cached object, otherwise create a new one.
        return players.containsKey(name.toLowerCase()) ? players.get(name.toLowerCase()) :
                new ATPlayer(player.getUniqueId(), name);
    }

    /**
     * Gets an instance of an ATPlayer by using their name.
     *
     * @param name the player name to get an ATPlayer instance of.
     * @return an ATPlayer object representing the player, but null if they haven't immediately loaded.
     */
    public static @Nullable ATPlayer getPlayer(@NotNull final String name) {

        // If the player is cached, just return it
        if (players.containsKey(name.toLowerCase())) {
            return players.get(name.toLowerCase());
        }

        // Create the player object on an alternative thread
        AdvancedTeleportAPI.getOfflinePlayer(name).whenComplete((player, err) -> new ATPlayer(player.getUniqueId(), name));

        return null;
    }

    /**
     * Gets an instance of an ATPlayer by using their name.
     *
     * @param name the player name to get an ATPlayer instance of.
     * @return an ATPlayer object representing the player within a CompletableFuture.
     */
    @SuppressWarnings("deprecation") // for Bukkit#getOfflinePlayer
    public static @NotNull CompletableFuture<ATPlayer> getPlayerFuture(@NotNull final String name) {

        // If the player is cached, just return it
        if (players.containsKey(name.toLowerCase())) {
            return CompletableFuture.completedFuture(players.get(name.toLowerCase()));
        }

        // Create the player object on an alternative thread
        return AdvancedTeleportAPI.getOfflinePlayer(name).thenApplyAsync(player -> new ATPlayer(player.getUniqueId(), name), CoreClass.sync);
    }

    /**
     * Internal use only
     */
    @ApiStatus.Internal
    @Contract(pure = true)
    public static void removePlayer(@NotNull final Player player) {
        players.remove(player.getName());
    }

    /**
     * Internal use only
     */
    @ApiStatus.Internal
    @Contract(pure = true)
    public static boolean isPlayerCached(@NotNull final String name) {
        return players.containsKey(name.toLowerCase());
    }

    /**
     * Gets the previous location of the player.
     *
     * @return the location the player was last at before teleporting. Can be null if they literally never teleported
     * before.
     */
    @Contract(pure = true)
    public @Nullable Location getPreviousLocation() {
        return previousLoc.data;
    }

    /**
     * Sets the player's previous location.
     *
     * @param previousLoc the new previous location to use.
     * @return a completable future of whether the action failed or succeeded.
     */
    public @NotNull CompletableFuture<Void> setPreviousLocation(@NotNull final Location previousLoc) {
        return AdvancedTeleportAPI.validateEvent(new PreviousLocationChangeEvent(
            getOfflinePlayer(),
            previousLoc,
            this.previousLoc.data
        ), event -> CompletableFuture.runAsync(() -> PlayerSQLManager.get().setPreviousLocation(getOfflinePlayer().getName(), previousLoc)));
    }

    private static class PendingData<T> {
        private final CompletableFuture<T> future;
        private @Nullable T data;

        public PendingData(CompletableFuture<T> future) {
            this.future = future;

            // Wait for the data to go through
            this.future.thenApplyAsync(data -> this.data = data);
        }

        public CompletableFuture<T> getFuture() {
            return future;
        }

        public CompletableFuture<T> getData() {

            // If the data has been fetched, return it
            if (data != null) return CompletableFuture.completedFuture(data);

            // Otherwise return the future to wait on
            return this.future;
        }
    }
}
