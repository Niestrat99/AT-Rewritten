package io.github.niestrat99.advancedteleport.sql;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.Warp;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class WarpSQLManager extends SQLManager {

    private static WarpSQLManager instance;

    public WarpSQLManager() {
        super();
        instance = this;
    }

    @Override
    public void createTable() {
        Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), () -> {
            try {
                PreparedStatement createTable = connection.prepareStatement(
                        "CREATE TABLE IF NOT EXISTS advancedtp_warps " +
                        "(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "warp VARCHAR(256) NOT NULL," +
                        "uuid_creator VARCHAR(256), " +
                        "x DOUBLE NOT NULL," +
                        "y DOUBLE NOT NULL," +
                        "z DOUBLE NOT NULL," +
                        "yaw FLOAT NOT NULL," +
                        "pitch FLOAT NOT NULL," +
                        "world VARCHAR(256) NOT NULL," +
                        "timestamp_created BIGINT NOT NULL," +
                        "timestamp_updated BIGINT NOT NULL)");
                createTable.executeUpdate();
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        });
    }

    @Override
    public void transferOldData() {
        // Create the existing warps first because we're savage
        addWarps();
        // Get the file itself.
        File file = new File(CoreClass.getInstance().getDataFolder(), "warps.yml");
        if (!file.exists()) return;
        // Load the config file.
        YamlConfiguration warps = YamlConfiguration.loadConfiguration(file);
        // For each player found...
        for (String warp : warps.getKeys(false)) {
            // Get the config section representing their homes.
            ConfigurationSection warpSection = warps.getConfigurationSection(warp);
            if (warpSection == null) continue;
            // For each home that appears...
            String world = warpSection.getString("world");
            if (world == null) continue;
            if (Bukkit.getWorld(world) == null) continue;
            Location location = new Location(Bukkit.getWorld(world),
                    warpSection.getDouble("x"),
                    warpSection.getDouble("y"),
                    warpSection.getDouble("z"),
                    (float) warpSection.getDouble("yaw"),
                    (float) warpSection.getDouble("pitch"));
            System.out.println(location);
            addWarp(new Warp(null, warp, location, -1, -1), null);
        }

        file.delete();

    }

    public void addWarp(Warp warp, SQLCallback<Boolean> callback) {
        Location location = warp.getLocation();
        UUID creator = warp.getCreator();
        String name = warp.getName();
        long created = warp.getCreatedTime();
        long updated = warp.getUpdatedTime();
        Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), () -> {
            try {
                PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO advancedtp_warps (warp, uuid_creator, x, y, z, yaw, pitch, world, timestamp_created, timestamp_updated) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

                statement.setString(1, name);
                statement.setString(2, creator == null ? null : creator.toString());
                statement.setDouble(3, location.getX());
                statement.setDouble(4, location.getY());
                statement.setDouble(5, location.getZ());
                statement.setDouble(6, location.getYaw());
                statement.setDouble(7, location.getPitch());
                statement.setString(8, location.getWorld().getName());
                statement.setLong(9, created);
                statement.setLong(10, updated);
                statement.executeUpdate();

                if (callback != null) {
                    callback.onSuccess(true);
                }
            } catch (SQLException exception) {
                DataFailManager.get().addFailure(DataFailManager.Operation.ADD_WARP,
                        location.getWorld().getName(),
                        String.valueOf(location.getX()),
                        String.valueOf(location.getY()),
                        String.valueOf(location.getZ()),
                        String.valueOf(location.getYaw()),
                        String.valueOf(location.getPitch()),
                        name,
                        creator == null ? null : creator.toString(),
                        String.valueOf(created),
                        String.valueOf(updated));
                exception.printStackTrace();
                if (callback != null) {
                    callback.onFail();
                }
            }
        });
    }

    public void removeWarp(String name, SQLCallback<Boolean> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), () -> {
            try {
                PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM advancedtp_warps WHERE warp = ?");
                statement.setString(1, name);
                statement.executeUpdate();
                if (callback != null) {
                    callback.onSuccess(true);
                }
            } catch (SQLException exception) {
                DataFailManager.get().addFailure(DataFailManager.Operation.DELETE_WARP, name);
                exception.printStackTrace();
                if (callback != null) {
                    callback.onFail();
                }
            }
        });
    }

    public void moveWarp(Location newLocation, String name, SQLCallback<Boolean> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), () -> {
            try {
                PreparedStatement statement = connection.prepareStatement(
                        "UPDATE advancedtp_warps SET x = ?, y = ?, z = ?, yaw = ?, pitch = ?, world = ?, timestamp_updated = ? WHERE warp = ?");

                statement.setDouble(1, newLocation.getX());
                statement.setDouble(2, newLocation.getY());
                statement.setDouble(3, newLocation.getZ());
                statement.setDouble(4, newLocation.getYaw());
                statement.setDouble(5, newLocation.getPitch());
                statement.setString(6, newLocation.getWorld().getName());
                statement.setLong(7, System.currentTimeMillis());
                statement.setString(8, name);
                statement.executeUpdate();
                callback.onSuccess(true);
            } catch (SQLException exception) {
                DataFailManager.get().addFailure(DataFailManager.Operation.MOVE_WARP,
                        newLocation.getWorld().getName(),
                        String.valueOf(newLocation.getX()),
                        String.valueOf(newLocation.getY()),
                        String.valueOf(newLocation.getZ()),
                        String.valueOf(newLocation.getYaw()),
                        String.valueOf(newLocation.getPitch()),
                        name);
                exception.printStackTrace();
                callback.onFail();
            }
        });
    }

    private void addWarps() {
        Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), () -> {
            try {
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM advancedtp_warps");
                ResultSet results = statement.executeQuery();
                // For each warp...
                while (results.next()) {
                    // Get the world.
                    World world = Bukkit.getWorld(results.getString("world"));
                    if (world == null) continue;
                    // Create the warp object and it'll register itself.
                    String creator = results.getString("uuid_creator");
                    new Warp(creator == null ? null : UUID.fromString(creator),
                            results.getString("warp"),
                            new Location(world,
                                    results.getDouble("x"),
                                    results.getDouble("y"),
                                    results.getDouble("z"),
                                    results.getFloat("yaw"),
                                    results.getFloat("pitch")),
                            results.getLong("timestamp_created"),
                            results.getLong("timestamp_updated"));
                }
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        });
    }

    public static WarpSQLManager get() {
        return instance;
    }
}
