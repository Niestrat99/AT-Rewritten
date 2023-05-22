package io.github.niestrat99.advancedteleport.sql;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.AdvancedTeleportAPI;
import io.github.niestrat99.advancedteleport.api.Warp;
import io.github.niestrat99.advancedteleport.folia.RunnableManager;
import io.github.niestrat99.advancedteleport.managers.NamedLocationManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class WarpSQLManager extends SQLManager {

    private static WarpSQLManager instance;

    public WarpSQLManager() {
        super();
        instance = this;
    }

    public static WarpSQLManager get() {
        return instance;
    }

    @Override
    public void createTable() {
        RunnableManager.setupRunnerAsync(() -> {
            try (Connection connection = implementConnection()) {

                CoreClass.debug("Creating table data for the warps manager if it is not already set up.");

                PreparedStatement createTable = prepareStatement(
                        connection,
                        "CREATE TABLE IF NOT EXISTS " + tablePrefix + "_warps " +
                            "(id INTEGER PRIMARY KEY " + getStupidAutoIncrementThing() + ", " +
                            "warp VARCHAR(256) NOT NULL," +
                            "uuid_creator VARCHAR(256), " +
                            "x DOUBLE NOT NULL," +
                            "y DOUBLE NOT NULL," +
                            "z DOUBLE NOT NULL," +
                            "yaw FLOAT NOT NULL," +
                            "pitch FLOAT NOT NULL," +
                            "world VARCHAR(256) NOT NULL," +
                            "price VARCHAR(256)," +
                            "timestamp_created BIGINT NOT NULL," +
                            "timestamp_updated BIGINT NOT NULL)"
                );
                executeUpdate(createTable);
            } catch (SQLException exception) {
                CoreClass.getInstance().getLogger().severe("Failed to create the warps table.");
                exception.printStackTrace();
            }
            transferOldData();
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

            // For each warp that appears...
            String world = warpSection.getString("world");
            if (world == null) continue;
            if (Bukkit.getWorld(world) == null) continue;
            Location location = new Location(
                Bukkit.getWorld(world),
                warpSection.getDouble("x"),
                warpSection.getDouble("y"),
                warpSection.getDouble("z"),
                (float) warpSection.getDouble("yaw"),
                (float) warpSection.getDouble("pitch")
            );
            Warp warpObj = new Warp(null, warp, location, -1, -1);
            addWarp(warpObj);
            NamedLocationManager.get().registerWarp(warpObj);
        }

        file.renameTo(new File(CoreClass.getInstance().getDataFolder(), "warps-backup.yml"));

    }

    private void addWarps() {
        try (Connection connection = implementConnection()) {
            PreparedStatement statement = prepareStatement(connection, "SELECT * FROM " + tablePrefix + "_warps");
            ResultSet results = executeQuery(statement);
            // For each warp...
            while (results.next()) {
                // Get the world.
                World world = Bukkit.getWorld(results.getString("world"));
                if (world == null) continue;
                // Create the warp object and it'll register itself.
                String creator = results.getString("uuid_creator");
                NamedLocationManager.get().registerWarp(new Warp(
                    creator == null ? null : UUID.fromString(creator),
                    results.getString("warp"),
                    new Location(
                        world,
                        results.getDouble("x"),
                        results.getDouble("y"),
                        results.getDouble("z"),
                        results.getFloat("yaw"),
                        results.getFloat("pitch")
                    ),
                    results.getLong("timestamp_created"),
                    results.getLong("timestamp_updated")
                ));
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public void addWarp(Warp warp) {
        Location location = warp.getLocation();
        UUID creator = warp.getCreator();
        String name = warp.getName();
        long created = warp.getCreatedTime();
        long updated = warp.getUpdatedTime();
        try (Connection connection = implementConnection()) {
            PreparedStatement statement = prepareStatement(
                connection,
                "INSERT INTO " + tablePrefix + "_warps (warp, uuid_creator, x, y, z, yaw, pitch, world, " +
                    "timestamp_created, timestamp_updated) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
            );

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
            executeUpdate(statement);

        } catch (SQLException exception) {
            DataFailManager.get().addFailure(
                DataFailManager.Operation.ADD_WARP,
                location.getWorld().getName(),
                String.valueOf(location.getX()),
                String.valueOf(location.getY()),
                String.valueOf(location.getZ()),
                String.valueOf(location.getYaw()),
                String.valueOf(location.getPitch()),
                name,
                creator == null ? null : creator.toString(),
                String.valueOf(created),
                String.valueOf(updated)
            );
            exception.printStackTrace();
        }
    }

    public void removeWarp(String name) {
        try (Connection connection = implementConnection()) {
            PreparedStatement statement = prepareStatement(
                connection,
                "DELETE FROM " + tablePrefix + "_warps WHERE warp = ?"
            );
            statement.setString(1, name);
            executeUpdate(statement);
        } catch (SQLException exception) {
            DataFailManager.get().addFailure(DataFailManager.Operation.DELETE_WARP, name);
            exception.printStackTrace();
        }
    }

    public void moveWarp(
        Location newLocation,
        String name
    ) {
        try (Connection connection = implementConnection()) {
            PreparedStatement statement = prepareStatement(
                connection,
                "UPDATE " + tablePrefix + "_warps SET x = ?, y = ?, z = ?, yaw = ?, pitch = ?, world = ?, " +
                    "timestamp_updated = ? WHERE warp = ?"
            );

            prepareLocation(newLocation, 1, statement);
            statement.setLong(7, System.currentTimeMillis());
            statement.setString(8, name);
            executeUpdate(statement);
        } catch (SQLException exception) {
            DataFailManager.get().addFailure(
                DataFailManager.Operation.MOVE_WARP,
                newLocation.getWorld().getName(),
                String.valueOf(newLocation.getX()),
                String.valueOf(newLocation.getY()),
                String.valueOf(newLocation.getZ()),
                String.valueOf(newLocation.getYaw()),
                String.valueOf(newLocation.getPitch()),
                name
            );
            exception.printStackTrace();
        }
    }

    public CompletableFuture<Integer> getWarpId(String name) {
        return CompletableFuture.supplyAsync(() -> getWarpIdSync(name), CoreClass.async);
    }

    public int getWarpIdSync(String name) {
        try (Connection connection = implementConnection()) {
            PreparedStatement statement = prepareStatement(
                connection,
                "SELECT id FROM " + tablePrefix + "_warps WHERE warp = ?;"
            );
            statement.setString(1, name);
            ResultSet set = executeQuery(statement);
            if (set.next()) {
                return set.getInt("id");
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return -1;
    }

    public void purgeWarps(String worldName) {
        try (Connection connection = implementConnection()) {
            PreparedStatement statement = prepareStatement(connection, "SELECT warp FROM " + tablePrefix + "_warps WHERE world = ?");
            statement.setString(1, worldName);

            ResultSet set = statement.executeQuery();
            while (set.next()) {
                Warp warp = AdvancedTeleportAPI.getWarp(set.getString("warp"));
                if (warp != null) NamedLocationManager.get().removeWarp(warp);
            }
            set.close();

            statement = prepareStatement(connection, "DELETE FROM " + tablePrefix + "_warps WHERE world = ?");
            statement.setString(1, worldName);

            executeUpdate(statement);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void purgeWarps(UUID creatorID) {
        try (Connection connection = implementConnection()) {
            PreparedStatement statement = prepareStatement(connection, "SELECT warp FROM " + tablePrefix + "_warps WHERE uuid_creator = ?");
            statement.setString(1, creatorID.toString());

            ResultSet set = statement.executeQuery();
            while (set.next()) {
                Warp warp = AdvancedTeleportAPI.getWarp(set.getString("warp"));
                if (warp != null) NamedLocationManager.get().removeWarp(warp);
            }
            set.close();

            statement = prepareStatement(connection, "DELETE FROM " + tablePrefix + "_warps WHERE uuid_creator = ?");
            statement.setString(1, creatorID.toString());

            executeUpdate(statement);
        } catch (SQLException throwables) {
            throw new RuntimeException(throwables);
        }
    }

    public CompletableFuture<List<Warp>> getWarpsBulk() {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = implementConnection()) {
                PreparedStatement statement = prepareStatement(connection, "SELECT * FROM " + tablePrefix + "_warps");
                ResultSet results = executeQuery(statement);
                List<Warp> warps = new ArrayList<>();
                // For each warp...
                while (results.next()) {
                    // Get the world.
                    World world = Bukkit.getWorld(results.getString("world"));
                    if (world == null) continue;
                    // Create the warp object and it'll register itself.
                    String creator = results.getString("uuid_creator");
                    warps.add(new Warp(
                        creator == null ? null : UUID.fromString(creator),
                        results.getString("warp"),
                        new Location(
                            world,
                            results.getDouble("x"),
                            results.getDouble("y"),
                            results.getDouble("z"),
                            results.getFloat("yaw"),
                            results.getFloat("pitch")
                        ),
                        results.getLong("timestamp_created"),
                        results.getLong("timestamp_updated")
                    ));
                }
                return warps;
            } catch (SQLException exception) {
                throw new RuntimeException(exception);
            }
        }, CoreClass.async);
    }
}
