package io.github.niestrat99.advancedteleport.sql;


import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.Home;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class PlayerSQLManager extends SQLManager {

    private static PlayerSQLManager instance;

    public PlayerSQLManager() {
        super();
        instance = this;
    }

    @Override
    public void createTable() {
        Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), () -> {
            try {
                PreparedStatement createTable = connection.prepareStatement(
                        "CREATE TABLE IF NOT EXISTS advancedtp_players " +
                        "(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "uuid VARCHAR(256) NOT NULL, " +
                        "name VARCHAR(256) NOT NULL," +
                        "timestamp_last_joined BIGINT NOT NULL," +
                        "main_home VARCHAR(256)," +
                        "teleportation_on BIT DEFAULT 1 NOT NULL, " +
                        "x DOUBLE NOT NULL, " +
                        "y DOUBLE NOT NULL, " +
                        "z DOUBLE NOT NULL, " +
                        "yaw FLOAT NOT NULL, " +
                        "pitch FLOAT NOT NULL, " +
                        "world VARCHAR(256) NOT NULL)"
                );

                createTable.executeUpdate();
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
            transferOldData();
        });
    }

    @Override
    public void transferOldData() {

    }

    public void updatePlayerData(Player player) {
        isPlayerInDatabase(player, result -> {
            if (result) {
                updatePlayerInformation(player, null);
            } else {
                addPlayer(player, null);
            }
        });
    }

    public void updatePlayerInformation(OfflinePlayer player, SQLCallback<Boolean> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), () -> {
            try {
                PreparedStatement statement = connection.prepareStatement("UPDATE advancedtp_players SET name = ?, timestamp_last_joined = ? WHERE uuid = ?");
                statement.setString(1, player.getName().toLowerCase());
                statement.setLong(2, System.currentTimeMillis());
                statement.setString(3, player.getUniqueId().toString());
                statement.executeUpdate();
                if (callback != null) {
                    callback.onSuccess(true);
                }
            } catch (SQLException exception) {
                DataFailManager.get().addFailure(DataFailManager.Operation.UPDATE_PLAYER, player.getUniqueId().toString());
                exception.printStackTrace();
            }
        });
    }

    public void isPlayerInDatabase(Player player, SQLCallback<Boolean> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), () -> {
            try {
                PreparedStatement statement = connection.prepareStatement("SELECT name FROM advancedtp_players WHERE uuid = ?");
                statement.setString(1, player.getUniqueId().toString());
                ResultSet results = statement.executeQuery();
                callback.onSuccess(results.next());
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        });
    }

    public void addPlayer(Player player, SQLCallback<Boolean> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), () -> {
            try {
                if (player == null) {
                    if (callback != null) {
                        callback.onFail();
                    }

                    return;
                }
                PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO advancedtp_players (uuid, name, timestamp_last_joined, x, y, z, yaw, pitch, world) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");

                statement.setString(1, player.getUniqueId().toString());
                statement.setString(2, player.getName().toLowerCase());
                statement.setLong(3, System.currentTimeMillis());
                statement.setDouble(4, player.getLocation().getX());
                statement.setDouble(5, player.getLocation().getY());
                statement.setDouble(6, player.getLocation().getZ());
                statement.setDouble(7, player.getLocation().getYaw());
                statement.setDouble(8, player.getLocation().getPitch());
                statement.setString(9, player.getLocation().getWorld().getName());
                statement.executeUpdate();
                if (callback != null) {
                    callback.onSuccess(true);
                }

            } catch (SQLException exception) {
                DataFailManager.get().addFailure(
                        DataFailManager.Operation.ADD_PLAYER,
                        player.getUniqueId().toString()
                );
                if (callback != null) {
                    callback.onFail();
                }

                exception.printStackTrace();
            }
        });
    }

    public void isTeleportationOn(UUID uuid, SQLCallback<Boolean> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), () -> {
            try {
                PreparedStatement statement = connection.prepareStatement("SELECT teleportation_on FROM advancedtp_players WHERE uuid = ?");
                statement.setString(1, uuid.toString());
                ResultSet results = statement.executeQuery();
                if (results.next()) {
                    callback.onSuccess(results.getBoolean("teleportation_on"));
                    return;
                }
                callback.onSuccess(false);
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        });
    }

    public void setTeleportationOn(UUID uuid, boolean enabled, SQLCallback<Boolean> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), () -> {
            try {
                PreparedStatement statement = connection.prepareStatement("UPDATE advancedtp_players SET teleportation_on = ? WHERE uuid = ?");
                statement.setString(1, uuid.toString());
                statement.setBoolean(2, enabled);
                statement.executeUpdate();
                if (callback != null) {
                    callback.onSuccess(true);
                }
            } catch (SQLException exception) {
                DataFailManager.get().addFailure(DataFailManager.Operation.CHANGE_TELEPORTATION, uuid.toString(), String.valueOf(enabled));
                callback.onFail();
                exception.printStackTrace();
            }
        });
    }

    public void getLocation(String name, SQLCallback<Location> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), () -> {
            try {
                PreparedStatement statement = connection.prepareStatement("SELECT x, y, z, yaw, pitch, world FROM advancedtp_players WHERE name = ?");
                statement.setString(1, name.toLowerCase());
                ResultSet results = statement.executeQuery();
                if (results.next()) {
                    World world = Bukkit.getWorld(results.getString("world"));
                    callback.onSuccess(new Location(world, results.getDouble("x"),
                            results.getDouble("y"),
                            results.getDouble("z"),
                            results.getFloat("yaw"),
                            results.getFloat("pitch")));
                    return;
                }
                callback.onFail();
            } catch (SQLException exception) {
                exception.printStackTrace();
                callback.onFail();
            }
        });
    }

    public void getMainHome(String name, SQLCallback<String> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), () -> {
            try {
                PreparedStatement statement = connection.prepareStatement("SELECT main_home FROM advancedtp_players WHERE name = ?");
                statement.setString(1, name.toLowerCase());
                ResultSet results = statement.executeQuery();
                if (results.next()) {
                    callback.onSuccess(results.getString("main_home"));
                    return;
                }
                callback.onSuccess(null);
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        });
    }

    public void setMainHome(UUID uuid, String home, SQLCallback<Boolean> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), () -> {
            try {
                PreparedStatement statement = connection.prepareStatement("UPDATE advancedtp_players SET main_home = ? WHERE uuid = ?");
                statement.setString(1, home);
                statement.setString(2, uuid.toString());
                statement.executeUpdate();
                if (callback != null) {
                    callback.onSuccess(true);
                }
            } catch (SQLException exception) {
                DataFailManager.get().addFailure(DataFailManager.Operation.CHANGE_TELEPORTATION, home, uuid.toString());
                if (callback != null) {
                    callback.onFail();
                }
                exception.printStackTrace();
            }
        });
    }

    public static PlayerSQLManager get() {
        return instance;
    }
}
