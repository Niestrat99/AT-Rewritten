package io.github.niestrat99.advancedteleport.api;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.events.ATTeleportEvent;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.managers.CooldownManager;
import io.github.niestrat99.advancedteleport.managers.MovementManager;
import io.github.niestrat99.advancedteleport.payments.PaymentManager;
import io.github.niestrat99.advancedteleport.sql.BlocklistManager;
import io.github.niestrat99.advancedteleport.sql.HomeSQLManager;
import io.github.niestrat99.advancedteleport.sql.PlayerSQLManager;
import io.github.niestrat99.advancedteleport.sql.SQLManager;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.geysermc.floodgate.api.FloodgateApi;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * A wrapper class used to represent a player. An ATPlayer stores information such as their homes, the players they
 * have blocked, whether they have teleportation enabled, their main home, and previous location.
 */
public class ATPlayer {

    private UUID uuid;
    @NotNull
    private LinkedHashMap<String, Home> homes;
    @NotNull
    private HashMap<UUID, BlockInfo> blockedUsers;
    private boolean isTeleportationEnabled;
    private String mainHome;
    private Location previousLoc;

    private static final HashMap<String, ATPlayer> players = new HashMap<>();

    /**
     * @param player
     */
    public ATPlayer(Player player) {
        this(player.getUniqueId(), player.getName());
    }

    public ATPlayer(@Nullable UUID uuid, @Nullable String name) {
        if (uuid == null || name == null) return;

        this.uuid = uuid;
        if (Bukkit.getServer().getPluginManager().getPlugin("floodgate") != null && Bukkit.getServer().getPluginManager().isPluginEnabled("floodgate")) {
            FloodgateApi api = FloodgateApi.getInstance();
            if (api.isFloodgateId(uuid)) this.uuid = api.getPlayer(uuid).getCorrectUniqueId();
        }
        this.homes = new LinkedHashMap<>();
        this.blockedUsers = new HashMap<>();

        BlocklistManager.get().getBlockedPlayers(uuid.toString(), (list) -> this.blockedUsers = list);
        HomeSQLManager.get().getHomes(uuid.toString(), list -> {
            this.homes = list;
            // Do this after to be safe
            PlayerSQLManager.get().getMainHome(name, result -> {
                if (result != null && !result.isEmpty()) {
                    setMainHome(result, null);
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

    @Nullable
    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public OfflinePlayer getOfflinePlayer() {
        return Bukkit.getOfflinePlayer(uuid);
    }

    public void teleport(ATTeleportEvent event, String command, String teleportMsg, int warmUp) {
        Player player = event.getPlayer();
        if (!event.isCancelled()) {
            if (PaymentManager.getInstance().canPay(command, player)) {
                // If the cooldown is to be applied after request or accept (they are the same in the case of /tpr),
                // apply it now
                String cooldownConfig = NewConfig.get().APPLY_COOLDOWN_AFTER.get();

                if (cooldownConfig.equalsIgnoreCase("request") || cooldownConfig.equalsIgnoreCase("accept")) {
                    CooldownManager.addToCooldown(command, player);
                }

                if (warmUp > 0 && !player.hasPermission("at.admin.bypass.timer")) {
                    MovementManager.createMovementTimer(player, event.getToLocation(), command, teleportMsg, warmUp,
                            "{home}", event.getLocName(), "{warp}", event.getLocName());
                } else {
                    PaperLib.teleportAsync(player, event.getToLocation(), PlayerTeleportEvent.TeleportCause.COMMAND);
                    CustomMessages.sendMessage(player, teleportMsg, "{home}", event.getLocName(), "{warp}",
                            event.getLocName());
                    PaymentManager.getInstance().withdraw(command, player);
                }
            }

        }
    }

    public boolean isTeleportationEnabled() {
        return isTeleportationEnabled;
    }

    public void setTeleportationEnabled(boolean teleportationEnabled, SQLManager.SQLCallback<Boolean> callback) {
        isTeleportationEnabled = teleportationEnabled;
        PlayerSQLManager.get().setTeleportationOn(uuid, teleportationEnabled, callback);
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
    @Nullable
    public BlockInfo getBlockInfo(OfflinePlayer otherPlayer) {
        return blockedUsers.get(otherPlayer.getUniqueId());
    }

    public void blockUser(@NotNull OfflinePlayer otherPlayer, SQLManager.SQLCallback<Boolean> callback) {
        blockUser(otherPlayer, null, callback);
    }

    public void blockUser(@NotNull OfflinePlayer otherPlayer, @Nullable String reason,
                          SQLManager.SQLCallback<Boolean> callback) {
        blockUser(otherPlayer.getUniqueId(), reason, callback);
    }

    public void blockUser(@NotNull UUID otherUUID, @Nullable String reason, SQLManager.SQLCallback<Boolean> callback) {
        // Add the user to the list of blocked users.
        blockedUsers.put(otherUUID, new BlockInfo(uuid, otherUUID, reason, System.currentTimeMillis()));
        // Add the entry to the SQL database.
        BlocklistManager.get().blockUser(uuid.toString(), otherUUID.toString(), reason, callback);
    }

    public void unblockUser(@NotNull UUID otherUUID, SQLManager.SQLCallback<Boolean> callback) {
        blockedUsers.remove(otherUUID);

        BlocklistManager.get().unblockUser(uuid.toString(), otherUUID.toString(), callback);
    }

    /*
     * HOMES FUNCTIONALITY
     */

    public HashMap<String, Home> getHomes() {
        return homes;
    }

    public void addHome(String name, Location location, SQLManager.SQLCallback<Boolean> callback) {
        if (hasHome(name)) {
            moveHome(name, location, callback);
            return;
        }
        homes.put(name, new Home(uuid, name, location, System.currentTimeMillis(), System.currentTimeMillis()));
        HomeSQLManager.get().addHome(location, uuid, name, callback);
    }

    public void moveHome(String name, Location newLocation, SQLManager.SQLCallback<Boolean> callback) {
        homes.get(name).setLocation(newLocation);
        HomeSQLManager.get().moveHome(newLocation, uuid, name, callback);
    }

    public void removeHome(String name, SQLManager.SQLCallback<Boolean> callback) {
        homes.remove(name);
        HomeSQLManager.get().removeHome(uuid, name, callback);
    }

    public Home getHome(String name) {
        return homes.get(name);
    }

    public Home getBedSpawn() {
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

    public Home getMainHome() {
        return homes.get(mainHome);
    }

    public void setMainHome(String name, SQLManager.SQLCallback<Boolean> callback) {
        if (!homes.containsKey(name)) return;
        this.mainHome = name;
        LinkedHashMap<String, Home> tempHomes = new LinkedHashMap<>();
        tempHomes.put(name, homes.get(name));
        for (String home : homes.keySet()) {
            if (home.equals(name)) continue;
            tempHomes.put(home, homes.get(home));
        }
        homes = tempHomes;

        PlayerSQLManager.get().setMainHome(uuid, name, callback);
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

    /**
     * Whether the player can access a specified home or not. A player may lose home access if `deny-homes-if-over-limit`
     * is set to true in the config.yml file, and if they used to have a higher homes limit than they currently have.
     *
     * @param home The home having access checked.
     * @return true if the player can access the home, false if they cannot.
     */
    public boolean canAccessHome(Home home) {
        if (getHomesLimit() == -1) return true;
        if (!NewConfig.get().DENY_HOMES_IF_OVER_LIMIT.get()) return true;
        if (homes.containsValue(home)) {
            List<Home> homes = new ArrayList<>(this.homes.values());
            int index = homes.indexOf(home);
            return index < getHomesLimit();
        }
        return false;
    }

    /**
     * Whether the player has a home with the specified name.
     *
     * @param name The name of the home.
     * @return true if the player has a home named as specified, false if they do not.
     */
    public boolean hasHome(String name) {
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

    @NotNull
    public static ATPlayer getPlayer(Player player) {
        return players.containsKey(player.getName().toLowerCase()) ? players.get(player.getName().toLowerCase()) :
                new ATPlayer(player);
    }

    @NotNull
    public static ATPlayer getPlayer(OfflinePlayer player) {
        return players.containsKey(player.getName().toLowerCase()) ? players.get(player.getName().toLowerCase()) :
                new ATPlayer(player.getUniqueId(), player.getName());
    }

    @Nullable
    public static ATPlayer getPlayer(@NotNull String name) {
        if (players.containsKey(name.toLowerCase())) {
            return players.get(name.toLowerCase());
        }
        Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), () -> {
            OfflinePlayer player = Bukkit.getOfflinePlayer(name);
            new ATPlayer(player.getUniqueId(), player.getName());
        });
        return null;
    }

    @NotNull
    public static CompletableFuture<ATPlayer> getPlayerFuture(String name) {
        if (players.containsKey(name.toLowerCase())) {
            return CompletableFuture.completedFuture(players.get(name.toLowerCase()));
        }
        return CompletableFuture.supplyAsync(() -> {
            OfflinePlayer player = Bukkit.getOfflinePlayer(name);
            return new ATPlayer(player.getUniqueId(), player.getName());
        }, CoreClass.async).thenApplyAsync(player -> player, CoreClass.sync);
    }

    public static void removePlayer(Player player) {
        players.remove(player.getName());
    }

    public Location getPreviousLocation() {
        return previousLoc;
    }

    public void setPreviousLocation(Location previousLoc) {
        this.previousLoc = previousLoc;
        PlayerSQLManager.get().setPreviousLocation(getOfflinePlayer().getName(), previousLoc, null);
    }
}
