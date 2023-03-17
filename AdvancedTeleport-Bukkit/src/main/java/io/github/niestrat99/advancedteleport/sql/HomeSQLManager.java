package io.github.niestrat99.advancedteleport.sql;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.api.Home;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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

            CoreClass.debug("Creating table data for the home manager if it is not already set up.");

            try (Connection connection = implementConnection()) {
                PreparedStatement createTable = prepareStatement(connection,
                        "CREATE TABLE IF NOT EXISTS " + tablePrefix + "_homes " +
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
                CoreClass.getInstance().getLogger().severe("Failed to create the homes table.");
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

                // Get the raw config section
                ConfigurationSection homeRaw = homes.getConfigurationSection(player + "." + home);
                if (homeRaw == null) continue;

                // Get the world the home is in - but if it doesn't exist, ignore it
                String world = homeRaw.getString("world");
                if (world == null) continue;
                if (Bukkit.getWorld(world) == null) continue;

                // Add the home to the database
                addHome(new Location(Bukkit.getWorld(world),
                        homeRaw.getDouble("x"),
                        homeRaw.getDouble("y"),
                        homeRaw.getDouble("z"),
                        (float) homeRaw.getDouble("yaw"),
                        (float) homeRaw.getDouble("pitch")), UUID.fromString(player), home, false);
            }
        }

        // Create a backup file
        file.renameTo(new File(CoreClass.getInstance().getDataFolder(), "homes-backup.yml"));
    }

    public void addHome(Location location, UUID owner, String name) {
        addHome(location, owner, name, true);
    }

    public void addHome(Location location, UUID owner, String name, boolean async) {
        if (async) {
            Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), () -> addHomePrivate(location, owner
                    , name));
        } else {
            addHomePrivate(location, owner, name);
        }
    }
    
    public CompletableFuture<Integer> getHomeId(String name, UUID owner) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = implementConnection()) {
                PreparedStatement statement = prepareStatement(connection,
                        "SELECT id FROM " + tablePrefix + "_homes WHERE home = ? AND uuid_owner = ?;");
                statement.setString(1, name);
                statement.setString(2, owner.toString());
                ResultSet set = executeQuery(statement);
                if (set.next()) {
                    return set.getInt("id");
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            return -1;
        }, CoreClass.async);
    }

    private void addHomePrivate(Location location, UUID owner, String name) {
        try (Connection connection = implementConnection()) {
            PreparedStatement statement = prepareStatement(connection,
                    "INSERT INTO " + tablePrefix + "_homes (uuid_owner, home, x, y, z, yaw, pitch, world, " +
                            "timestamp_created, timestamp_updated) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            statement.setString(1, owner.toString());
            statement.setString(2, name);
            prepareLocation(location, 3, statement);
            statement.setLong(9, System.currentTimeMillis());
            statement.setLong(10, System.currentTimeMillis());
            executeUpdate(statement);

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
        }
    }

    public void removeHome(UUID owner, String name) {
        // Create the connection
        try (Connection connection = implementConnection()) {

            // Set up the SQL statement to remove the home
            PreparedStatement statement = prepareStatement(connection,
                    "DELETE FROM " + tablePrefix + "_homes WHERE uuid_owner = ? AND home = ?");

            statement.setString(1, owner.toString());
            statement.setString(2, name);
            executeUpdate(statement);

        } catch (SQLException exception) {
            // If something went wrong through, add the failure to the data fail manager
            DataFailManager.get().addFailure(DataFailManager.Operation.DELETE_HOME,
                    owner.toString(),
                    name);

            // Throw an extra exception
            throw new RuntimeException(exception);
        }
    }

    public void moveHome(Location newLocation, UUID owner, String name) {
        moveHome(newLocation, owner, name, true);
    }

    public void moveHome(Location newLocation, UUID owner, String name, boolean async) {
        if (async) {
            Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), () -> moveHomePrivate(newLocation,
                    owner, name));
        } else {
            moveHomePrivate(newLocation, owner, name);
        }
    }

    public void moveHomePrivate(Location newLocation, UUID owner, String name) {
        try (Connection connection = implementConnection()) {
            PreparedStatement statement = prepareStatement(connection,
                    "UPDATE " + tablePrefix + "_homes SET x = ?, y = ?, z = ?, yaw = ?, pitch = ?, world = ?, " +
                            "timestamp_updated = ? WHERE uuid_owner = ? AND home = ? ");

            prepareLocation(newLocation, 1, statement);
            statement.setLong(7, System.currentTimeMillis());
            statement.setString(8, owner.toString());
            statement.setString(9, name);
            executeUpdate(statement);
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
        }
    }

    public LinkedHashMap<String, Home> getHomes(String ownerUUID) {
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

            return homes;
        } catch (SQLException exception) {
            throw new RuntimeException(exception);
        }
    }

    public void purgeHomes(String worldName) {
        try (Connection connection = implementConnection()) {
            PreparedStatement statement = prepareStatement(connection,
                    "SELECT uuid_owner, home FROM " + tablePrefix + "_homes WHERE world = ?");
            statement.setString(1, worldName);

            ResultSet set = statement.executeQuery();

            while (set.next()) {
                OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(set.getString("uuid_owner")));
                if (player.getName() == null || !ATPlayer.isPlayerCached(player.getName())) continue;
                ATPlayer.getPlayer(player).removeHome(set.getString("home"));
            }
            set.close();

            statement = prepareStatement(connection, "DELETE FROM " + tablePrefix + "_homes WHERE world = ?");
            statement.setString(1, worldName);

            executeUpdate(statement);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void purgeHomes(UUID owner) {
        try (Connection connection = implementConnection()) {

            OfflinePlayer player = Bukkit.getOfflinePlayer(owner);
            if (player.getName() != null && ATPlayer.isPlayerCached(player.getName())) {
                ATPlayer atPlayer = ATPlayer.getPlayer(player);
                PreparedStatement statement = prepareStatement(connection, "SELECT home FROM " + tablePrefix +
                        "_homes WHERE uuid_owner = ?");
                statement.setString(1, owner.toString());

                ResultSet set = statement.executeQuery();

                while (set.next()) {
                    atPlayer.removeHome(set.getString("home"));
                }
                set.close();
            }

            PreparedStatement statement = prepareStatement(connection, "DELETE FROM " + tablePrefix + "_homes " +
                    "WHERE uuid_owner = ?");
            statement.setString(1, owner.toString());

            executeUpdate(statement);
        } catch (SQLException throwables) {
            throw new RuntimeException(throwables);
        }
    }

    public static HomeSQLManager get() {
        return instance;
    }

    public CompletableFuture<List<Home>> getHomesBulk() {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = implementConnection()) {

                // GOTTA CATCH 'EM ALL
                PreparedStatement statement = prepareStatement(connection, "SELECT * FROM " + tablePrefix + "_homes");
                ResultSet set = executeQuery(statement);

                // Get the list of homes
                List<Home> homes = new ArrayList<>();
                while (set.next()) {

                    // UUID of the owner
                    UUID owner = UUID.fromString(set.getString("uuid_owner"));

                    // Get the name of the home
                    String name = set.getString("home");

                    // Coordinates
                    double x = set.getDouble("x");
                    double y = set.getDouble("y");
                    double z = set.getDouble("z");
                    double yaw = set.getDouble("yaw");
                    double pitch = set.getDouble("pitch");

                    // The world name
                    String worldStr = set.getString("world");

                    // Timestamps
                    long createdTimestamp = set.getLong("timestamp_created");
                    long updatedTimestamp = set.getLong("timestamp_updated");

                    // Make sure the world is there
                    World world = Bukkit.getWorld(worldStr);
                    if (world == null) continue;

                    // Add the world
                    homes.add(new Home(owner, name, new Location(world, x, y, z, (float) yaw, (float) pitch),
                            createdTimestamp, updatedTimestamp));
                }
                return homes;
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }, CoreClass.async);
    }
}
