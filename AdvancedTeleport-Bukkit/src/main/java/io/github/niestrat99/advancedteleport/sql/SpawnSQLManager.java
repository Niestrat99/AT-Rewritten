package io.github.niestrat99.advancedteleport.sql;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.spawn.Spawn;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SpawnSQLManager extends SQLManager {

    private static SpawnSQLManager instance;

    public SpawnSQLManager() {
        super();
        instance = this;

        transferOldData();
    }

    public static SpawnSQLManager get() {
        return instance;
    }

    @Override
    public void createTable() {
        Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), () -> {
            try (Connection connection = implementConnection()) {
                PreparedStatement createTable = prepareStatement(connection,
                        "CREATE TABLE IF NOT EXISTS " + tablePrefix + "_spawns " +
                                "(id INTEGER PRIMARY KEY " + getStupidAutoIncrementThing() + ", " +
                                "spawn VARCHAR(256) NOT NULL, " +
                                "x DOUBLE NOT NULL," +
                                "y DOUBLE NOT NULL," +
                                "z DOUBLE NOT NULL," +
                                "yaw FLOAT NOT NULL," +
                                "pitch FLOAT NOT NULL," +
                                "world VARCHAR(256) NOT NULL," +
                                "timestamp_created BIGINT NOT NULL," +
                                "timestamp_updated BIGINT NOT NULL)");
                executeUpdate(createTable);
            } catch (SQLException exception) {
                CoreClass.getInstance().getLogger().severe("Failed to create the spawns table.");
                exception.printStackTrace();
            }
            transferOldData();
        });
    }

    @Override
    public void transferOldData() {

        // Get the file itself.
        File file = new File(CoreClass.getInstance().getDataFolder(), "spawn.yml");
        if (!file.exists()) return;

        // Load the config file.
        YamlConfiguration spawnsFile = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection spawns = spawnsFile.getConfigurationSection("spawns");
        if (spawns == null) return;

        // Get the main spawn
        String mainSpawn = spawnsFile.getString("main-spawn");

        // For each player found...
        for (String spawnName : spawns.getKeys(false)) {

            // Get the config section representing their homes.
            ConfigurationSection spawnSection = spawns.getConfigurationSection(spawnName);
            if (spawnSection == null) continue;

            // Get the world the home is in - but if it doesn't exist, ignore it
            String world = spawnSection.getString("world");
            if (world == null) continue;

            // Add the home to the database
            addSpawn(spawnName,
                    world,
                    spawnSection.getDouble("x"),
                    spawnSection.getDouble("y"),
                    spawnSection.getDouble("z"),
                    (float) spawnSection.getDouble("yaw"),
                    (float) spawnSection.getDouble("pitch"));

            // If the spawn name matches, add the metadata
            if (spawnName.equals(mainSpawn)) {
                MetadataSQLManager.get().addSpawnMetadata(spawnName, "main_spawn", "true");
            }
        }

        // Create a backup file
        file.renameTo(new File(CoreClass.getInstance().getDataFolder(), "spawn-backup.yml"));
    }

    public CompletableFuture<Void> addSpawn(@NotNull Spawn spawn) {
        return addSpawn(spawn.getName(),
                spawn.getLocation().getWorld().getName(),
                spawn.getLocation().x(),
                spawn.getLocation().y(),
                spawn.getLocation().z(),
                spawn.getLocation().getYaw(),
                spawn.getLocation().getPitch());
    }

    public CompletableFuture<Void> addSpawn(
            @NotNull String name,
            @NotNull String worldName,
            double x,
            double y,
            double z,
            float yaw,
            float pitch) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = implementConnection()) {

                PreparedStatement statement = prepareStatement(connection, "INSERT INTO " + tablePrefix + "_spawns " +
                        "(spawn, x, y, z, yaw, pitch, world, timestamp_created, timestamp_updated) VALUES (?,?,?,?,?,?,?,?)");
                statement.setString(1, name);
                statement.setDouble(2, x);
                statement.setDouble(3, y);
                statement.setDouble(4, z);
                statement.setFloat(5, yaw);
                statement.setFloat(6, pitch);
                statement.setString(7, worldName);
                statement.setDouble(8, System.currentTimeMillis());
                statement.setDouble(9, System.currentTimeMillis());

                executeUpdate(statement);

            } catch (SQLException exception) {
                throw new RuntimeException(exception);
            }
        }, CoreClass.async);
    }

    public CompletableFuture<Void> removeSpawn(String name) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = implementConnection()) {

                PreparedStatement statement = prepareStatement(connection, "DELETE FROM " + tablePrefix + "_spawns " +
                        "WHERE spawn = ?");
                statement.setString(1, name);

                executeUpdate(statement);

            } catch (SQLException exception) {
                throw new RuntimeException(exception);
            }
        });
    }

    public CompletableFuture<List<Spawn>> getSpawns() {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = implementConnection()) {

                PreparedStatement statement = prepareStatement(connection, "SELECT * FROM " + tablePrefix + "_spawns");

                // Get the result
                ResultSet result = executeQuery(statement);

                //

            } catch (SQLException exception) {
                throw new RuntimeException(exception);
            }
        });
    }

    public CompletableFuture<Integer> getSpawnId(String name) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = implementConnection()) {
                PreparedStatement statement = prepareStatement(connection,
                        "SELECT id FROM " + tablePrefix + "_spawns WHERE spawn = ?;");
                statement.setString(1, name);
                ResultSet set = executeQuery(statement);
                if (set.next()) {
                    connection.close();
                    return set.getInt("id");
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            return -1;
        }, CoreClass.async);
    }
}
