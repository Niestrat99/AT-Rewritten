package io.github.niestrat99.advancedteleport.sql;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.api.Home;
import io.github.niestrat99.advancedteleport.api.Warp;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class HomeSQLManager extends SQLManager {

    private static HomeSQLManager instance;

    public HomeSQLManager() {
        super();
        instance = this;
    }

    @Override
    public void createTable() {
        Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), () -> {
            try (Connection connection = implementConnection()) {
                PreparedStatement createTable = prepareStatement(connection, "CREATE TABLE IF NOT EXISTS " + tablePrefix + "_homes " +
                        "(id INTEGER PRIMARY KEY " + getStupidAutoIncrementThing() + ", " +
                        "uuid_owner VARCHAR(256) NOT NULL, " +
                        "home VARCHAR(256) NOT NULL," +
                        "x DOUBLE NOT NULL," +
                        "y DOUBLE NOT NULL," +
                        "z DOUBLE NOT NULL," +
                        "yaw FLOAT NOT NULL," +
                        "pitch FLOAT NOT NULL," +
                        "world VARCHAR(256) NOT NULL," +
                        "icon VARCHAR(256) DEFAULT 'GRASS_BLOCK' NOT NULL," +
                        "timestamp_created BIGINT NOT NULL," +
                        "timestamp_updated BIGINT NOT NULL)");
                executeUpdate(createTable);
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
            transferOldData();
        });
    }

    @Override
    public void transferOldData() {
        // Get the file itself.
        File file = new File(CoreClass.getInstance().getDataFolder(), "homes.yml");
        if (!file.exists()) return;
        // Load the config file.
        YamlConfiguration homes = YamlConfiguration.loadConfiguration(file);
        // For each player found...
        for (String player : homes.getKeys(false)) {
            // Get the config section representing their homes.
            ConfigurationSection homeSection = homes.getConfigurationSection(player);
            if (homeSection == null) continue;
            // For each home that appears...
            for (String home : homeSection.getKeys(false)) {
                ConfigurationSection homeRaw = homes.getConfigurationSection(player + "." + home);
                if (homeRaw == null) continue;
                String world = homeRaw.getString("world");
                if (world == null) continue;
                if (Bukkit.getWorld(world) == null) continue;
                addHome(new Location(Bukkit.getWorld(world),
                        homeRaw.getDouble("x"),
                        homeRaw.getDouble("y"),
                        homeRaw.getDouble("z"),
                        (float) homeRaw.getDouble("yaw"),
                        (float) homeRaw.getDouble("pitch")), UUID.fromString(player), home, null);
            }
        }

        file.renameTo(new File(CoreClass.getInstance().getDataFolder(), "homes-backup.yml"));
    }

    public void addHome(Location location, UUID owner, String name, SQLCallback<Boolean> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), () -> {
            try (Connection connection = implementConnection()) {
                PreparedStatement statement = prepareStatement(connection,
                        "INSERT INTO " + tablePrefix + "_homes (uuid_owner, home, x, y, z, yaw, pitch, world, timestamp_created, timestamp_updated) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

                statement.setString(1, owner.toString());
                statement.setString(2, name);
                statement.setDouble(3, location.getX());
                statement.setDouble(4, location.getY());
                statement.setDouble(5, location.getZ());
                statement.setDouble(6, location.getYaw());
                statement.setDouble(7, location.getPitch());
                statement.setString(8, location.getWorld().getName());
                statement.setLong(9, System.currentTimeMillis());
                statement.setLong(10, System.currentTimeMillis());
                executeUpdate(statement);

                if (callback != null) {
                    callback.onSuccess(true);
                }
            } catch (SQLException exception) {
                DataFailManager.get().addFailure(
                        DataFailManager.Operation.ADD_HOME,
                        location.getWorld().getName(),
                        String.valueOf(location.getX()),
                        String.valueOf(location.getY()),
                        String.valueOf(location.getZ()),
                        String.valueOf(location.getYaw()),
                        String.valueOf(location.getPitch()),
                        name,
                        owner.toString()

                );
                exception.printStackTrace();
                if (callback != null) {
                    callback.onFail();
                }
            }
        });
    }

    public void removeHome(UUID owner, String name, SQLCallback<Boolean> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), () -> {
            try (Connection connection = implementConnection()) {
                PreparedStatement statement = prepareStatement(connection,
                        "DELETE FROM " + tablePrefix + "_homes WHERE uuid_owner = ? AND home = ?");

                statement.setString(1, owner.toString());
                statement.setString(2, name);
                executeUpdate(statement);
                if (callback != null) {
                    callback.onSuccess(true);
                }
            } catch (SQLException exception) {
                DataFailManager.get().addFailure(DataFailManager.Operation.DELETE_HOME,
                        owner.toString(),
                        name);
                exception.printStackTrace();
                if (callback != null) {
                    callback.onFail();
                }
            }
        });
    }

    public void moveHome(Location newLocation, UUID owner, String name, SQLCallback<Boolean> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), () -> {
            try (Connection connection = implementConnection()) {
                PreparedStatement statement = prepareStatement(connection,
                        "UPDATE " + tablePrefix + "_homes SET x = ?, y = ?, z = ?, yaw = ?, pitch = ?, world = ?, timestamp_updated = ? WHERE uuid_owner = ? AND home = ? ");

                statement.setDouble(1, newLocation.getX());
                statement.setDouble(2, newLocation.getY());
                statement.setDouble(3, newLocation.getZ());
                statement.setDouble(4, newLocation.getYaw());
                statement.setDouble(5, newLocation.getPitch());
                statement.setString(6, newLocation.getWorld().getName());
                statement.setLong(7, System.currentTimeMillis());
                statement.setString(8, owner.toString());
                statement.setString(9, name);
                executeUpdate(statement);
                if (callback != null) {
                    callback.onSuccess(true);
                }
            } catch (SQLException exception) {
                DataFailManager.get().addFailure(
                        DataFailManager.Operation.MOVE_HOME,
                        newLocation.getWorld().getName(),
                        String.valueOf(newLocation.getX()),
                        String.valueOf(newLocation.getY()),
                        String.valueOf(newLocation.getZ()),
                        String.valueOf(newLocation.getYaw()),
                        String.valueOf(newLocation.getPitch()),
                        name,
                        owner.toString());
                exception.printStackTrace();
                if (callback != null) {
                    callback.onFail();
                }
            }
        });
    }

    public void getHomes(String ownerUUID, SQLCallback<LinkedHashMap<String, Home>> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), () -> {
            try (Connection connection = implementConnection()) {
                PreparedStatement statement = prepareStatement(connection,
                        "SELECT * FROM " + tablePrefix + "_homes WHERE uuid_owner = ?");
                statement.setString(1, ownerUUID);
                ResultSet results = executeQuery(statement);
                // Create a list for all homes.
                LinkedHashMap<String, Home> homes = new LinkedHashMap<>();
                // For each home...
                while (results.next()) {
                    // Get the world.
                    World world = Bukkit.getWorld(results.getString("world"));
                    if (world == null) continue;
                    // Create the home object
                    Home home = new Home(UUID.fromString(ownerUUID),
                            results.getString("home"),
                            new Location(world,
                                    results.getDouble("x"),
                                    results.getDouble("y"),
                                    results.getDouble("z"),
                                    results.getFloat("yaw"),
                                    results.getFloat("pitch")),
                            results.getLong("timestamp_created"),
                            results.getLong("timestamp_updated"));
                    // Add it to the list.
                    homes.put(results.getString("home"), home);
                }
                // Go back to the main thread and return the list.
                Bukkit.getScheduler().runTask(CoreClass.getInstance(), () -> callback.onSuccess(homes));
            } catch (SQLException exception) {
                exception.printStackTrace();
                callback.onFail();
            }
        });
    }

    public void purgeHomes(String worldName, SQLCallback<Void> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), () -> {
            try (Connection connection = implementConnection()) {
                PreparedStatement statement = prepareStatement(connection, "SELECT uuid_owner, home FROM " + tablePrefix + "_homes WHERE world = ?");
                statement.setString(1, worldName);

                ResultSet set = statement.executeQuery();

                while (set.next()) {
                    OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(set.getString("uuid_owner")));
                    if (player.getName() != null && !ATPlayer.isPlayerCached(player.getName())) continue;
                    ATPlayer.getPlayer(player).removeHome(set.getString("home"), null);
                }
                set.close();

                statement = prepareStatement(connection, "DELETE FROM " + tablePrefix + "_homes WHERE world = ?");
                statement.setString(1, worldName);

                executeUpdate(statement);
                if (callback != null) callback.onSuccess(null);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
                if (callback != null) callback.onFail();
            }
        });
    }

    public void purgeHomes(UUID owner, SQLCallback<Void> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), () -> {
            try (Connection connection = implementConnection()) {

                OfflinePlayer player = Bukkit.getOfflinePlayer(owner);
                if (player.getName() != null && ATPlayer.isPlayerCached(player.getName())) {
                    ATPlayer atPlayer = ATPlayer.getPlayer(player);
                    PreparedStatement statement = prepareStatement(connection, "SELECT home FROM " + tablePrefix + "_homes WHERE uuid_owner = ?");
                    statement.setString(1, owner.toString());

                    ResultSet set = statement.executeQuery();

                    while (set.next()) {
                        atPlayer.removeHome(set.getString("home"), null);
                    }
                    set.close();
                }

                PreparedStatement statement = prepareStatement(connection, "DELETE FROM " + tablePrefix + "_homes WHERE uuid_owner = ?");
                statement.setString(1, owner.toString());

                executeUpdate(statement);
                if (callback != null) callback.onSuccess(null);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
                if (callback != null) callback.onFail();
            }
        });
    }

    public static HomeSQLManager get() {
        return instance;
    }
}
