package io.github.niestrat99.advancedteleport.api;

import com.sun.istack.internal.NotNull;
import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.sql.BlocklistManager;
import io.github.niestrat99.advancedteleport.sql.HomeSQLManager;
import io.github.niestrat99.advancedteleport.sql.SQLManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ATPlayer {

    private UUID uuid;
    private HashMap<String, Home> homes;
    private HashMap<UUID, BlockInfo> blockedUsers;

    private static HashMap<String, ATPlayer> players = new HashMap<>();

    public ATPlayer(Player player) {
        this(player.getUniqueId(), player.getName());
    }

    public ATPlayer(UUID uuid, String name) {
        this.uuid = uuid;

        BlocklistManager.get().getBlockedPlayers(uuid.toString(), (list) -> this.blockedUsers = list);
        HomeSQLManager.get().getHomes(uuid.toString(), list -> this.homes = list);

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

    /*
     * BLOCKING FUNCTIONALITY
     */

    /**
     *
     * @param otherPlayer
     * @return
     */
    public boolean isBlocked(OfflinePlayer otherPlayer) {
        return blockedUsers.containsKey(otherPlayer.getUniqueId());
    }

    public BlockInfo getBlockInfo(OfflinePlayer otherPlayer) {
        return blockedUsers.get(otherPlayer.getUniqueId());
    }

    public void blockUser(@NotNull OfflinePlayer otherPlayer) {
        blockUser(otherPlayer, null);
    }

    public void blockUser(@NotNull OfflinePlayer otherPlayer, @Nullable String reason) {
        blockUser(otherPlayer.getUniqueId(), reason);
    }

    public void blockUser(@NotNull UUID otherUUID, @Nullable String reason) {
        // Add the user to the list of blocked users.
        blockedUsers.put(otherUUID, new BlockInfo(uuid, otherUUID, reason, System.currentTimeMillis()));
        // Add the entry to the SQL database.
        BlocklistManager.get().blockUser(uuid.toString(), otherUUID.toString(), reason);
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

    public void moveHome(String name, Location newLocation) {
        homes.get(name).setLocation(newLocation);
        HomeSQLManager.get().moveHome(newLocation, uuid, name);
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

}
