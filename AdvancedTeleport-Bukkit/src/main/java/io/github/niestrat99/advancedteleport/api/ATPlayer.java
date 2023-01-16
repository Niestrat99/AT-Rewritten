package io.github.niestrat99.advancedteleport.api;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.events.ATTeleportEvent;
import io.github.niestrat99.advancedteleport.api.events.homes.*;
import io.github.niestrat99.advancedteleport.api.events.players.PreviousLocationChangeEvent;
import io.github.niestrat99.advancedteleport.api.events.players.ToggleTeleportationEvent;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.managers.CooldownManager;
import io.github.niestrat99.advancedteleport.managers.MovementManager;
import io.github.niestrat99.advancedteleport.managers.ParticleManager;
import io.github.niestrat99.advancedteleport.payments.PaymentManager;
import io.github.niestrat99.advancedteleport.sql.BlocklistManager;
import io.github.niestrat99.advancedteleport.sql.HomeSQLManager;
import io.github.niestrat99.advancedteleport.sql.PlayerSQLManager;
import io.github.niestrat99.advancedteleport.sql.SQLManager;
import io.github.thatsmusic99.configurationmaster.api.ConfigSection;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.geysermc.floodgate.api.FloodgateApi;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

/**
 * A wrapper class used to represent a player. An ATPlayer stores information such as their homes, the players they
 * have blocked, whether they have teleportation enabled, their main home, and previous location.
 */
public class ATPlayer {

    protected @NotNull UUID uuid;
    private @NotNull LinkedHashMap<String, Home> homes;
    private @NotNull HashMap<UUID, BlockInfo> blockedUsers;
    private boolean isTeleportationEnabled;
    private @Nullable String mainHome;
    private @Nullable Location previousLoc;

    private static final HashMap<String, ATPlayer> players = new HashMap<>();

    /**
     * Internal use only.
     */
    public ATPlayer(@NotNull Player player) {
        this(player.getUniqueId(), player.getName());
    }

    /**
     * Internal use only.
     */
    // TODO - handle Citizens NPCs correctly, UUIDs may be null
    public ATPlayer(@NotNull UUID uuid, @Nullable String name) {
        this.homes = new LinkedHashMap<>();
        this.blockedUsers = new HashMap<>();
        this.uuid = uuid;
        if (name == null) return;

        BlocklistManager.get().getBlockedPlayers(uuid.toString(), (list) -> this.blockedUsers = list);
        HomeSQLManager.get().getHomes(uuid.toString(), list -> {
            this.homes = list;

            // Do this after to be safe
            PlayerSQLManager.get().getMainHome(name, result -> {
                if (result != null && !result.isEmpty()) {
                    setMainHome(result); // TODO: error thrown here
                }
            });
            // Add the bed spawn home
            if (getBedSpawn() != null && NewConfig.get().ADD_BED_TO_HOMES.get()) {
                homes.put("bed", getBedSpawn());
            }
        });

        PlayerSQLManager.get().isTeleportationOn(uuid, result -> this.isTeleportationEnabled = result);
        PlayerSQLManager.get().getPreviousLocation(name, result -> this.previousLoc = result);

        players.put(name.toLowerCase(), this);
    }

    /**
     * Gets the Bukkit player object representing this ATPlayer.
     *
     * @return the Bukkit player representing this ATPlayer. This is null if the player is not online.
     */
    public @Nullable Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    /**
     * Gets the offline Bukkit player object representing this ATPlayer.
     *
     * @return the offline Bukkit player representing this ATPlayer.
     */
    public @NotNull OfflinePlayer getOfflinePlayer() {
        return Bukkit.getOfflinePlayer(uuid);
    }

    @Deprecated
    @ApiStatus.Internal
    public void teleport(ATTeleportEvent event, String command, String teleportMsg, int warmUp)  {
        teleport(event, command, teleportMsg);
    }

    @ApiStatus.Internal
    public void teleport(ATTeleportEvent event, String command, String teleportMsg) {
        Player player = event.getPlayer();
        int warmUp = getWarmUp(command);
        if (event.isCancelled()) return;
        if (!PaymentManager.getInstance().canPay(command, player)) return;
                
        // If the cooldown is to be applied after request or accept (they are the same in the case of /tpr), apply it now
        String cooldownConfig = NewConfig.get().APPLY_COOLDOWN_AFTER.get();

        if (cooldownConfig.equalsIgnoreCase("request") || cooldownConfig.equalsIgnoreCase("accept")) {
            CooldownManager.addToCooldown(command, player);
        }
        
        // If there's a movement timer, apply it - otherwise, teleport them immediately
        if (warmUp > 0 && !player.hasPermission("at.admin.bypass.timer")) {
            MovementManager.createMovementTimer(player, event.getToLocation(), command, teleportMsg, warmUp,
                    "{home}", event.getLocName(), "{warp}", event.getLocName());
        } else {
            ParticleManager.onTeleport(player, command);
            PaperLib.teleportAsync(player, event.getToLocation(), PlayerTeleportEvent.TeleportCause.COMMAND);
            CustomMessages.sendMessage(player, teleportMsg, "{home}", event.getLocName(), "{warp}",
                    event.getLocName());
            PaymentManager.getInstance().withdraw(command, player);
        }
    }
    
    /**
     * Returns whether teleportation is enabled for the player. This allows the player to receive teleportation requests
     * if set to true.
     *
     * @return true if teleportation is enabled, false if it is disabled.
     */
    public boolean isTeleportationEnabled() {
        return isTeleportationEnabled;
    }

    /**
     * Toggles teleportation for the player, setting it to a specific status.
     *
     * @param teleportationEnabled true to enable teleportation, false to disable it.
     * @param callback what to do after teleportation has been changed.
     * @deprecated use {@link #setTeleportationEnabled(boolean)} instead.
     */
    @Deprecated
    public void setTeleportationEnabled(boolean teleportationEnabled, SQLManager.SQLCallback<Boolean> callback) {
        setTeleportationEnabled(teleportationEnabled);
        callback.onSuccess(true);
    }

    /**
     * Toggles teleportation for the player, setting it to a specific status.
     *
     * @param teleportationEnabled true to enable teleportation, false to disable it.
     * @return a completable future of whether the action failed or succeeded.
     */
    public CompletableFuture<Void> setTeleportationEnabled(boolean teleportationEnabled) {
        return setTeleportationEnabled(teleportationEnabled, (CommandSender) null);
    }

    /**
     * Toggles teleportation for the player, setting it to a specific status.
     *
     * @param teleportationEnabled true to enable teleportation, false to disable it.
     * @param sender the command sender that triggered the action.
     * @return a completable future of whether the action failed or succeeded.
     */
    public CompletableFuture<Void> setTeleportationEnabled(boolean teleportationEnabled, CommandSender sender) {
        ToggleTeleportationEvent event = new ToggleTeleportationEvent(sender, getOfflinePlayer(), teleportationEnabled,
                isTeleportationEnabled ^ teleportationEnabled);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return CompletableFuture.completedFuture(null);

        this.isTeleportationEnabled = teleportationEnabled;
        return CompletableFuture.runAsync(() -> {
            AdvancedTeleportAPI.FlattenedCallback<Boolean> callback = new AdvancedTeleportAPI.FlattenedCallback<>();
            PlayerSQLManager.get().setTeleportationOn(uuid, teleportationEnabled, callback);
        });
    }

    /*
     * BLOCKING FUNCTIONALITY
     */

    /**
     * Checks to see if this player has blocked a specified player.
     *
     * @param otherPlayer The player that this one theoretically blocked.
     * @return true is otherPlayer is blocked, false if otherwise.
     */
    public boolean hasBlocked(OfflinePlayer otherPlayer) {
        return blockedUsers.containsKey(otherPlayer.getUniqueId());
    }

    /**
     * Gets the information regarding the block relationship between this player and someone else. This only gets
     * information if this player has blocked the other, not vice versa. To do this, get the ATPlayer object of the
     * other player and check if they have blocked this player using {@link ATPlayer#hasBlocked(OfflinePlayer)}.
     *
     * @param otherPlayer The other player.
     * @return A BlockInfo object if this player has blocked the other player, but null if they haven't.
     */
    public @Nullable BlockInfo getBlockInfo(OfflinePlayer otherPlayer) {
        return blockedUsers.get(otherPlayer.getUniqueId());
    }

    /**
     * Makes this ATPlayer block another player, stopping the other player from sending teleportation requests to them.
     *
     * @param otherPlayer the player being blocked.
     * @param callback what to do after the player has been blocked.
     * @deprecated use {@link #blockUser(OfflinePlayer)} instead.
     */
    @Deprecated
    public void blockUser(@NotNull OfflinePlayer otherPlayer, SQLManager.SQLCallback<Boolean> callback) {
        blockUser(otherPlayer, (String) null);
        callback.onSuccess(true);
    }

    /**
     * Makes this ATPlayer block another player with a specified reason, stopping the other player from sending
     * teleportation requests to them.
     *
     * @param otherPlayer the player being blocked.
     * @param reason the reason the player has been blocked. Can be null.
     * @param callback what to do after the player has been blocked.
     * @deprecated use {@link #blockUser(OfflinePlayer, String)} instead.
     */
    @Deprecated
    public void blockUser(@NotNull OfflinePlayer otherPlayer, @Nullable String reason,
                          SQLManager.SQLCallback<Boolean> callback) {
        blockUser(otherPlayer.getUniqueId(), reason);
        callback.onSuccess(true);
    }

    /**
     * Makes this ATPLayer block another player with the specified UUID with a given reason, stopping the other player
     * from sending teleportation requests to them.
     *
     * @param otherUUID the player's UUID to be blocked.
     * @param reason the reason the player has been blocked. Can be null.
     * @param callback what to do after the player has been blocked.
     * @deprecated use {@link #blockUser(UUID, String)} instead.
     */
    @Deprecated
    public void blockUser(@NotNull UUID otherUUID, @Nullable String reason, SQLManager.SQLCallback<Boolean> callback) {
        blockUser(otherUUID, reason);
        callback.onSuccess(true);
    }

    /**
     * Makes this ATPlayer block another player, stopping the other player from sending teleportation requests to them.
     *
     * @param otherPlayer the player being blocked.
     * @return a completable future of whether the action failed or succeeded.
     */
    public CompletableFuture<Void> blockUser(@NotNull OfflinePlayer otherPlayer) {
        return blockUser(otherPlayer.getUniqueId(), null);
    }

    /**
     * Makes this ATPlayer block another player with a specified reason, stopping the other player from sending
     * teleportation requests to them.
     *
     * @param otherPlayer the player being blocked.
     * @param reason the reason the player has been blocked. Can be null.
     * @return a completable future of whether the action failed or succeeded.
     */
    public CompletableFuture<Void> blockUser(@NotNull OfflinePlayer otherPlayer, @Nullable String reason) {
        return blockUser(otherPlayer.getUniqueId(), reason);
    }

    /**
     * Makes this ATPLayer block another player with the specified UUID with a given reason, stopping the other player
     * from sending teleportation requests to them.
     *
     * @param otherUUID the player's UUID to be blocked.
     * @param reason the reason the player has been blocked. Can be null.
     * @return a completable future of whether the action failed or succeeded.
     */
    public CompletableFuture<Void> blockUser(@NotNull UUID otherUUID, @Nullable String reason) {

        // Add the user to the list of blocked users.
        blockedUsers.put(otherUUID, new BlockInfo(uuid, otherUUID, reason, System.currentTimeMillis()));

        // Add the entry to the SQL database.
        return CompletableFuture.runAsync(() -> {
            AdvancedTeleportAPI.FlattenedCallback<Boolean> callback = new AdvancedTeleportAPI.FlattenedCallback<>();
            BlocklistManager.get().blockUser(uuid.toString(), otherUUID.toString(), reason, callback);
        }, CoreClass.async);
    }

    /**
     * Makes this player unblock a player with the specified UUID.
     *
     * @param otherUUID the UUID of the player to be unblocked.
     * @param callback what to do after the player has been unblocked.
     * @deprecated use {@link #unblockUser(UUID)} instead.
     */
    @Deprecated
    public void unblockUser(@NotNull UUID otherUUID, SQLManager.SQLCallback<Boolean> callback) {
        unblockUser(otherUUID);
        callback.onSuccess(true);
    }

    /**
     * Makes this player unblock a player with the specified UUID.
     *
     * @param otherUUID the UUID of the player to be unblocked.
     * @return a completable future of whether the action failed or succeeded.
     */
    public CompletableFuture<Void> unblockUser(@NotNull UUID otherUUID) {
        blockedUsers.remove(otherUUID);

        return CompletableFuture.runAsync(() -> {
            AdvancedTeleportAPI.FlattenedCallback<Boolean> callback = new AdvancedTeleportAPI.FlattenedCallback<>();
            BlocklistManager.get().unblockUser(uuid.toString(), otherUUID.toString(), callback);
        });
    }

    /*
     * HOMES FUNCTIONALITY
     */

    /**
     * Returns a hashmap of homes, where the key is the home name, and the value is the home object.
     *
     * @return a hashmap of homes.
     */
    @Unmodifiable
    public HashMap<String, Home> getHomes() {
        return new HashMap<>(homes);
    }

    /**
     * Adds a home to the player's home list.
     *
     * @param name the name of the home.
     * @param location the location of the home.
     * @param callback what to do after the home has been added.
     * @deprecated use {@link #addHome(String, Location)} instead.
     */
    @Deprecated
    public void addHome(@NotNull String name, @NotNull Location location, SQLManager.SQLCallback<Boolean> callback) {
        addHome(name, location, getPlayer(), true);
        if (callback != null) callback.onSuccess(true);
    }

    /**
     * Adds a home to the player's home list.
     *
     * @param name the name of the home.
     * @param location the location of the home.
     * @return a completable future of whether the action failed or succeeded.
     */
    public CompletableFuture<Void> addHome(String name, Location location) {
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
    public CompletableFuture<Void> addHome(String name, Location location, Player creator) {
        return addHome(name, location, creator, true);
    }

    /**
     * Adds a home to the player's home list.
     *
     * @param name the name of the home.
     * @param location the location of the home.
     * @param creator the player who created the home.
     * @param async true if the home is to be added asynchronously, false if not.
     * @return a completable future of whether the action failed or succeeded.
     */
    public CompletableFuture<Void> addHome(String name, Location location, Player creator, boolean async) {

        // If the home exists, move it instead
        if (hasHome(name)) return moveHome(name, location);

        // Creates the event to be fired
        HomeCreateEvent event = new HomeCreateEvent(getOfflinePlayer(), name, location, creator);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return CompletableFuture.completedFuture(null);

        // Cache the home
        Home home = new Home(event.getPlayer().getUniqueId(), event.getName(), event.getLocation(),
                System.currentTimeMillis(), System.currentTimeMillis());
        homes.put(name, home);

        // Store it in the database
        return CompletableFuture.runAsync(() -> {
            AdvancedTeleportAPI.FlattenedCallback<Boolean> callback = new AdvancedTeleportAPI.FlattenedCallback<>();
            HomeSQLManager.get().addHome(location, uuid, name, callback, async);
            if (!callback.data) return;

            // Create the post-create home event
            HomePostCreateEvent otherEvent = new HomePostCreateEvent(home);
            Bukkit.getPluginManager().callEvent(otherEvent);
        });
    }

    /**
     * Moves a specified home to a new location.
     *
     * @param name the name of the home.
     * @param newLocation the new location of the home.
     * @param callback what to do after the home has been moved.
     * @deprecated use {@link #moveHome(String, Location)} instead.
     */
    @Deprecated
    public void moveHome(String name, Location newLocation, SQLManager.SQLCallback<Boolean> callback) {
        moveHome(name, newLocation, callback, true);
    }

    /**
     * Moves a specified home to a new location.
     *
     * @param name the name of the home.
     * @param newLocation the new location of the home.
     * @param callback what to do after the home has been moved.
     * @param async true if the action should be done asynchronously, false if not.
     * @deprecated use {@link #moveHome(String, Location)} instead.
     */
    public void moveHome(String name, Location newLocation, SQLManager.SQLCallback<Boolean> callback, boolean async) {
        homes.get(name).move(newLocation);
        HomeSQLManager.get().moveHome(newLocation, uuid, name, callback, async);
        moveHome(name, newLocation);
        if (callback != null) callback.onSuccess(true);
    }

    /**
     * Moves a specified home to a new location.
     *
     * @param name the name of the home.
     * @param newLocation the new location of the home.
     * @return a completable future of whether the action failed or succeeded.
     */
    public CompletableFuture<Void> moveHome(String name, Location newLocation) {
        return moveHome(name, newLocation, (CommandSender) null);
    }

    /**
     * Moves a specified home to a new location.
     *
     * @param name the name of the home.
     * @param newLocation the new location of the home.
     * @return a completable future of whether the action failed or succeeded.
     */
    public CompletableFuture<Void> moveHome(String name, Location newLocation, CommandSender sender) {

        // If the home doesn't exist, it didn't happen
        if (!homes.containsKey(name)) return CompletableFuture.completedFuture(null);

        // Create an event to be called
        HomeMoveEvent event = new HomeMoveEvent(homes.get(name), newLocation, sender);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return CompletableFuture.completedFuture(null);

        // Move the home
        return event.getHome().move(event.getLocation());
    }

    /**
     * Removes a specified home.
     *
     * @param name the name of the home.
     * @param callback what to do after the home has been added.
     * @deprecated use {@link #removeHome(String)} instead.
     */
    @Deprecated
    public void removeHome(String name, SQLManager.SQLCallback<Boolean> callback) {
        removeHome(name);
        callback.onSuccess(true);
    }

    /**
     * Removes a specified home.
     *
     * @param name the name of the home.
     * @return a completable future of whether the action failed or succeeded.
     */
    public CompletableFuture<Void> removeHome(String name) {
        return removeHome(name, (CommandSender) null);
    }

    /**
     * Removes a specified home.
     *
     * @param name the name of the home.
     * @param sender the command sender that triggered the event.
     * @return a completable future of whether the action failed or succeeded.
     */
    public CompletableFuture<Void> removeHome(String name, CommandSender sender) {

        // Create an event to be called
        HomeDeleteEvent event = new HomeDeleteEvent(homes.get(name), sender);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return CompletableFuture.completedFuture(null);

        // Remove it from the cache
        homes.remove(event.getHome().getName());

        // Remove the home from the database
        return CompletableFuture.runAsync(() -> {
            AdvancedTeleportAPI.FlattenedCallback<Boolean> callback = new AdvancedTeleportAPI.FlattenedCallback<>();
            HomeSQLManager.get().removeHome(uuid, event.getHome().getName(), callback);
        });
    }

    /**
     * Returns a specified home object from the name.
     *
     * @param name the name of the home.
     * @return the home object itself, null if it doesn't exist.
     * @throws NullPointerException if name is null.
     */
    public Home getHome(@NotNull String name) {
        Objects.requireNonNull(name, "Home name cannot be null.");
        return homes.get(name);
    }

    /**
     * Gets the bed home object of the player.
     *
     * @return if the player has a bed spawn set, return the home object, else return null.
     */
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
    public boolean hasMainHome() {
        return mainHome != null && !mainHome.isEmpty() && homes.containsKey(mainHome);
    }

    /**
     * Returns the main home of the player as a home object.
     *
     * @return the main home as a home object, or null if it does not exist.
     */
    public Home getMainHome() {
        return mainHome == null ? null : homes.get(mainHome);
    }

    /**
     * Sets the main home of the player.
     *
     * @param name the name of the home to be used.
     * @return a completable future of whether the action failed or succeeded.
     */
    public CompletableFuture<Boolean> setMainHome(String name) {
        return setMainHome(name, (CommandSender) null);
    }

    /**
     * Sets the main home of the player.
     *
     * @param name the name of the home to be used.
     * @param sender the command sender that triggered the event.
     * @return a completable future of whether the action failed or succeeded.
     */
    public CompletableFuture<Boolean> setMainHome(String name, CommandSender sender) {

        // If the home doesn't exist, indicate the action has failed.
        if (!homes.containsKey(name)) return CompletableFuture.completedFuture(false);

        // Create the event to be called
        SwitchMainHomeEvent event = new SwitchMainHomeEvent(homes.get(mainHome), homes.get(name), sender);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return CompletableFuture.completedFuture(false);

        this.mainHome = name;
        LinkedHashMap<String, Home> tempHomes = new LinkedHashMap<>();
        tempHomes.put(name, homes.get(name));
        for (String home : homes.keySet()) {
            if (home.equals(name)) continue;
            tempHomes.put(home, homes.get(home));
        }
        homes = tempHomes;

        return CompletableFuture.supplyAsync(() -> {
            AdvancedTeleportAPI.FlattenedCallback<Boolean> callback = new AdvancedTeleportAPI.FlattenedCallback<>();
            PlayerSQLManager.get().setMainHome(uuid, name, callback);
            return callback.data;
        });
    }

    /**
     * Sets the main home of the player.
     *
     * @param name the name of the home to be used.
     * @param callback what to do after the home has been added.
     * @deprecated use {@link #setMainHome(String)} instead.
     */
    @Deprecated
    public void setMainHome(String name, SQLManager.SQLCallback<Boolean> callback) {
        setMainHome(name);
        callback.onSuccess(true);
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
        int maxHomes = NewConfig.get().DEFAULT_HOMES_LIMIT.get();

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
                    if (data[0].equals(getPlayer().getWorld().getName()) && data[1].matches("^[0-9]+$")) {
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
                if (!endNode.matches("^[0-9]+$")) continue;
                int homes = Integer.parseInt(endNode);
                if (maxHomes < homes) {
                    maxHomes = homes;
                }

            }
        }
        return maxHomes;
    }

    @Range(from = 0, to = Integer.MAX_VALUE)
    public int getCooldown(@NotNull String command) {
        return getMin("at.member.cooldown", command, NewConfig.get().CUSTOM_COOLDOWNS.get(), NewConfig.get().COOLDOWNS.valueOf(command).get());
    }

    @Range(from = 0, to = Integer.MAX_VALUE)
    public int getWarmUp(@NotNull String command) {
        return getMin("at.member.timer", command, NewConfig.get().CUSTOM_WARM_UPS.get(), NewConfig.get().WARM_UPS.valueOf(command).get());
    }

    @Range(from = 0, to = Integer.MAX_VALUE)
    public int getDistanceLimitation(@Nullable String command) {
        return determineValue("at.member.distance", command, command == null ? NewConfig.get().MAXIMUM_TELEPORT_DISTANCE.get()
                : NewConfig.get().DISTANCE_LIMITS.valueOf(command).get(), NewConfig.get().CUSTOM_DISTANCE_LIMITS.get(), Math::max);
    }

    @Range(from = 0, to = Integer.MAX_VALUE)
    private int getMin(String permission, String command, ConfigSection customSection, int defaultValue) {
        return determineValue(permission, command, defaultValue, customSection, Math::min);
    }

    @Range(from = 0, to = Integer.MAX_VALUE)
    private int determineValue(String permission, String command, int defaultValue, ConfigSection customSection, BiFunction<Integer, Integer, Integer> consumer) {
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
            if (!cooldown.matches("^[0-9]+$")) continue;
            if (!changed) {
                min = Integer.parseInt(cooldown);
                changed = true;
                continue;
            }
            min = consumer.apply(min, Integer.parseInt(cooldown));
        }
        return min;
    }

    private List<String> getDynamicPermission(String prefix) {
        prefix = prefix.endsWith(".") ? prefix : prefix + ".";

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
    public boolean canAccessHome(@NotNull Home home) {

        // If the homes limit is -1, it's unlimited
        if (getHomesLimit() == -1) return true;

        // If we don't deny home access if the home limit has already been exceeded, allow them access
        if (!NewConfig.get().DENY_HOMES_IF_OVER_LIMIT.get()) return true;

        // If the home exists, ensure the index is below the homes index.
        if (homes.containsValue(home)) {
            List<Home> homes = new ArrayList<>(this.homes.values());
            int index = homes.indexOf(home);
            return index < getHomesLimit();
        }

        // If they don't have a home though, stop there
        return false;
    }

    /**
     * Whether the player has a home with the specified name.
     *
     * @param name The name of the home.
     * @return true if the player has a home named as specified, false if they do not.
     */
    public boolean hasHome(@NotNull String name) {
        return homes.containsKey(name);
    }

    /**
     * Whether the player can set more homes. If {@link ATPlayer#getHomesLimit()} returns -1, then they can set
     * unlimited homes. If it isn't, then the number of homes the player has is compared to the homes limit. If it is
     * fewer than the homes limit, they can set more homes.
     *
     * @return true if the player can set more homes, false if they can not.
     */
    public boolean canSetMoreHomes() {
        return getHomesLimit() == -1 || homes.size() < getHomesLimit();
    }

    /**
     * Gets an instance of an ATPlayer by using the player object.
     *
     * @param player the player to get an ATPlayer instance of.
     * @return an ATPlayer object representing the player.
     * @throws NullPointerException if the player is null.
     */
    public static @NotNull ATPlayer getPlayer(@NotNull Player player) {

        // Null checks
        Objects.requireNonNull(player, "Player must not be null.");

        // If the player is cached, return the cached player
        if (players.containsKey(player.getName().toLowerCase())) return players.get(player.getName().toLowerCase());

        // If floodgate is on the server, see if they're a Bedrock player - if so, initiate them as a Floodgate player
        if (Bukkit.getServer().getPluginManager().getPlugin("floodgate") != null && Bukkit.getServer().getPluginManager().isPluginEnabled("floodgate")) {
            FloodgateApi api = FloodgateApi.getInstance();
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
     * @throws NullPointerException if the player or their name is null.
     */
    public static @NotNull ATPlayer getPlayer(@NotNull OfflinePlayer player) {
        Objects.requireNonNull(player, "Player must not be null.");
        String name = player.getName();
        Objects.requireNonNull(name, "Player name must not be null.");
        return players.containsKey(name.toLowerCase()) ? players.get(name.toLowerCase()) :
                new ATPlayer(player.getUniqueId(), name);
    }

    /**
     * Gets an instance of an ATPlayer by using their name.
     *
     * @param name the player name to get an ATPlayer instance of.
     * @return an ATPlayer object representing the player, but null if they haven't immediately loaded.
     */
    @SuppressWarnings("deprecation") // for Bukkit#getOfflinePlayer
    public static @Nullable ATPlayer getPlayer(@NotNull String name) {

        // If the player is cached, just return it
        if (players.containsKey(name.toLowerCase())) {
            return players.get(name.toLowerCase());
        }

        // Create the player object on an alternative thread
        Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), () -> {
            OfflinePlayer player = Bukkit.getOfflinePlayer(name);
            new ATPlayer(player.getUniqueId(), player.getName());
        });
        return null;
    }

    /**
     * Gets an instance of an ATPlayer by using their name.
     *
     * @param name the player name to get an ATPlayer instance of.
     * @return an ATPlayer object representing the player within a CompletableFuture.
     */

    @SuppressWarnings("deprecation") // for Bukkit#getOfflinePlayer
    public static @NotNull CompletableFuture<ATPlayer> getPlayerFuture(@NotNull String name) {

        // If the player is cached, just return it
        if (players.containsKey(name.toLowerCase())) {
            return CompletableFuture.completedFuture(players.get(name.toLowerCase()));
        }

        // Create the player object on an alternative thread
        return CompletableFuture.supplyAsync(() -> {
            OfflinePlayer player = Bukkit.getOfflinePlayer(name);
            return new ATPlayer(player.getUniqueId(), player.getName());
        }, CoreClass.async).thenApplyAsync(player -> player, CoreClass.sync);
    }

    /**
     * Internal use only
     */
    @ApiStatus.Internal
    public static void removePlayer(Player player) {
        players.remove(player.getName());
    }

    /**
     * Internal use only
     */
    @ApiStatus.Internal
    public static boolean isPlayerCached(String name) {
        return players.containsKey(name.toLowerCase());
    }

    /**
     * Gets the previous location of the player.
     *
     * @return the location the player was last at before teleporting. Can be null if they literally never teleported
     * before.
     */
    public @Nullable Location getPreviousLocation() {
        return previousLoc;
    }

    /**
     * Sets the player's previous location.
     *
     * @param previousLoc the new previous location to use.
     * @param callback what to do after the home has been added.
     * @deprecated use {@link #setPreviousLocation(Location)} instead.
     */
    @Deprecated
    public void setPreviousLocation(@Nullable Location previousLoc, SQLManager.SQLCallback<Boolean> callback) {
        setPreviousLocation(previousLoc);
        callback.onSuccess(true);
    }

    /**
     * Sets the player's previous location.
     *
     * @param previousLoc the new previous location to use.
     * @return a completable future of whether the action failed or succeeded.
     */
    public CompletableFuture<Boolean> setPreviousLocation(@Nullable Location previousLoc) {

        // Create the previous location change event
        PreviousLocationChangeEvent event = new PreviousLocationChangeEvent(getOfflinePlayer(), previousLoc,
                this.previousLoc);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return CompletableFuture.completedFuture(false);

        // Save the new location in the SQL database
        return CompletableFuture.supplyAsync(() -> {
            AdvancedTeleportAPI.FlattenedCallback<Boolean> callback = new AdvancedTeleportAPI.FlattenedCallback<>();
            PlayerSQLManager.get().setPreviousLocation(getOfflinePlayer().getName(), previousLoc, null);
            return callback.data;
        });
    }
}
