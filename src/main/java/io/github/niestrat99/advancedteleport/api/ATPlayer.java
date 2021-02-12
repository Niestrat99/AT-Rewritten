package io.github.niestrat99.advancedteleport.api;

import io.github.niestrat99.advancedteleport.config.NewConfig;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.jetbrains.annotations.NotNull;
import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.sql.BlocklistManager;
import io.github.niestrat99.advancedteleport.sql.HomeSQLManager;
import io.github.niestrat99.advancedteleport.sql.PlayerSQLManager;
import io.github.niestrat99.advancedteleport.sql.SQLManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ATPlayer {

    private final UUID uuid;
    private LinkedHashMap<String, Home> homes;
    private HashMap<UUID, BlockInfo> blockedUsers;
    private boolean isTeleportationEnabled;

    private static final HashMap<String, ATPlayer> players = new HashMap<>();

    public ATPlayer(Player player) {
        this(player.getUniqueId(), player.getName());
    }

    public ATPlayer(UUID uuid, String name) {
        this.uuid = uuid;

        BlocklistManager.get().getBlockedPlayers(uuid.toString(), (list) -> this.blockedUsers = list);
        HomeSQLManager.get().getHomes(uuid.toString(), list -> this.homes = list);
        PlayerSQLManager.get().isTeleportationOn(uuid, result -> this.isTeleportationEnabled = result);

        if (getPlayer() != null
                && getPlayer().getBedSpawnLocation() != null
                && !getHomes().containsKey("bed")) {
            addHome("bed", getPlayer().getBedSpawnLocation(), null);
        }
        players.put(name, this);
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public OfflinePlayer getOfflinePlayer() {
        return Bukkit.getOfflinePlayer(uuid);
    }

    public void teleport(Location location) {

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
     *
     * @param otherPlayer
     * @return
     */
    public boolean hasBlocked(OfflinePlayer otherPlayer) {
        return blockedUsers.containsKey(otherPlayer.getUniqueId());
    }

    public BlockInfo getBlockInfo(OfflinePlayer otherPlayer) {
        return blockedUsers.get(otherPlayer.getUniqueId());
    }

    public void blockUser(@NotNull OfflinePlayer otherPlayer, SQLManager.SQLCallback<Boolean> callback) {
        blockUser(otherPlayer, null, callback);
    }

    public void blockUser(@NotNull OfflinePlayer otherPlayer, @Nullable String reason, SQLManager.SQLCallback<Boolean> callback) {
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
        if (getPlayer().getBedSpawnLocation() != null) {
            return new Home(uuid, "bed", getPlayer().getBedSpawnLocation(), -1, -1);
        }
        return null;
    }

    // Used to get the permission for how many homes a player can have.
    // If there is no permission, then it's assumed that the number of homes they can have is limitless (-1).
    // E.g.: at.member.homes.5
    // at.member.homes.40
    // at.member.homes.100000
    public int getHomesLimit() {
        int maxHomes = NewConfig.get().DEFAULT_HOMES_LIMIT.get();
        for (PermissionAttachmentInfo permission : getPlayer().getEffectivePermissions()) {
            if (permission.getPermission().startsWith("at.member.homes.")) {
                if (permission.getValue()) {
                    String perm = permission.getPermission();
                    String ending = perm.substring(perm.lastIndexOf(".") + 1);
                    if (ending.equalsIgnoreCase("unlimited")) return -1;
                    if (!ending.matches("^[0-9]+$")) continue;
                    int homes = Integer.parseInt(ending);
                    if (maxHomes < homes) {
                        maxHomes = homes;
                    }
                }
            }
        }
        return maxHomes;
    }

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

    public boolean hasHome(String name) {
        return homes.containsKey(name);
    }

    public boolean canSetMoreHomes() {
        return getHomesLimit() == -1 || homes.size() < getHomesLimit();
    }

    @NotNull
    public static ATPlayer getPlayer(Player player) {
        return players.containsKey(player.getName()) ? players.get(player.getName()) : new ATPlayer(player);
    }

    @NotNull
    public static ATPlayer getPlayer(OfflinePlayer player) {
        return players.containsKey(player.getName()) ? players.get(player.getName()) : new ATPlayer(player.getUniqueId(), player.getName());
    }

    @Nullable
    public static ATPlayer getPlayer(String name) {
        if (players.containsKey(name)) {
            return players.get(name);
        }
        Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), () -> {
            OfflinePlayer player = Bukkit.getOfflinePlayer(name);
            new ATPlayer(player.getUniqueId(), player.getName());
        });
        return null;
    }

    public static void removePlayer(Player player) {
        players.remove(player.getName());
    }

}
