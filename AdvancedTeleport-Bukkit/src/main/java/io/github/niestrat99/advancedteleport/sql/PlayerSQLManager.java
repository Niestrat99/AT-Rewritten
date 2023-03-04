package io.github.niestrat99.advancedteleport.sql;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.ATPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlayerSQLManager extends SQLManager {

    private static PlayerSQLManager instance;
    private static HashMap<UUID, Location> previousLocationData;
    private static YamlConfiguration lastLocations;
    private static File lastLocFile;

    public PlayerSQLManager() {
        super();
        instance = this;
    }

    @Override
    public void createTable() {
        Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), () -> {
            try (Connection connection = implementConnection()) {
                PreparedStatement createTable = prepareStatement(connection,
                        "CREATE TABLE IF NOT EXISTS " + tablePrefix + "_players " +
                                "(id INTEGER PRIMARY KEY " + getStupidAutoIncrementThing() + ", " +
                                "uuid VARCHAR(256) NOT NULL, " +
                                "name VARCHAR(256) NOT NULL," +
                                "timestamp_last_joined BIGINT NOT NULL," +
                                "main_home VARCHAR(256)," +
                                "teleportation_on BIT DEFAULT 1 NOT NULL, " +
                                "x DOUBLE, " +
                                "y DOUBLE, " +
                                "z DOUBLE, " +
                                "yaw FLOAT, " +
                                "pitch FLOAT, " +
                                "world VARCHAR(256))"
                );

                executeUpdate(createTable);
            } catch (SQLException exception) {
                CoreClass.getInstance().getLogger().severe("Failed to create the players table.");
                exception.printStackTrace();
            }
            transferOldData();
        });
    }

    @Override
    public void transferOldData() {
        previousLocationData = new HashMap<>();
        // Get the file itself.
        lastLocFile = new File(CoreClass.getInstance().getDataFolder(), "last-locations.yml");
        if (!lastLocFile.exists()) return;
        // Load the config file.
        lastLocations = YamlConfiguration.loadConfiguration(lastLocFile);
        //
        for (String uuid : lastLocations.getKeys(false)) {
            if (uuid.equals("death")) continue;
            if (lastLocations.getString("death." + uuid) != null) continue;
            String locStr = lastLocations.getString(uuid);
            if (locStr == null) continue;
            String[] locParts = locStr.split(":");
            Location loc = new Location(Bukkit.getWorld(locParts[5]),
                    Double.parseDouble(locParts[0]),
                    Double.parseDouble(locParts[1]),
                    Double.parseDouble(locParts[2]),
                    Float.parseFloat(locParts[3]),
                    Float.parseFloat(locParts[4]));

            previousLocationData.put(UUID.fromString(uuid), loc);
        }
        ConfigurationSection section = lastLocations.getConfigurationSection("death");
        if (section == null) return;

        for (String uuid : section.getKeys(false)) {
            String locStr = section.getString(uuid);
            if (locStr == null) continue;
            String[] locParts = locStr.split(":");
            Location loc = new Location(Bukkit.getWorld(locParts[5]),
                    Double.parseDouble(locParts[0]),
                    Double.parseDouble(locParts[1]),
                    Double.parseDouble(locParts[2]),
                    Float.parseFloat(locParts[3]),
                    Float.parseFloat(locParts[4]));

            previousLocationData.put(UUID.fromString(uuid), loc);
        }

    }

    public void updatePlayerData(OfflinePlayer player) {
        isPlayerInDatabase(player).thenAcceptAsync(result -> {
            if (result) {
                updatePlayerInformation(player);
            } else {
                addPlayer(player);
            }
        }, CoreClass.async);
    }

    public void updatePlayerInformation(OfflinePlayer player) {
        try (Connection connection = implementConnection()) {
            if (player.getName() == null) return;
            PreparedStatement statement = prepareStatement(connection, "UPDATE " + tablePrefix + "_players SET name =" +
                    " ?, timestamp_last_joined = ? WHERE uuid = ?");
            statement.setString(1, player.getName().toLowerCase());
            statement.setLong(2, System.currentTimeMillis());
            statement.setString(3, player.getUniqueId().toString());
            executeUpdate(statement);
            if (previousLocationData.containsKey(player.getUniqueId())) {
                ATPlayer.getPlayer(player).setPreviousLocation(previousLocationData.get(player.getUniqueId()));

                removeLastLocation(player.getUniqueId());
            }
        } catch (SQLException exception) {
            DataFailManager.get().addFailure(DataFailManager.Operation.UPDATE_PLAYER, player.getUniqueId().toString());
            exception.printStackTrace();
        }
    }

    public CompletableFuture<Boolean> isPlayerInDatabase(OfflinePlayer player) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = implementConnection()) {
                PreparedStatement statement = prepareStatement(connection, "SELECT name FROM " + tablePrefix +
                        "_players WHERE uuid = ?");
                statement.setString(1, player.getUniqueId().toString());
                ResultSet results = executeQuery(statement);
                return results.next();
            } catch (SQLException exception) {
                throw new RuntimeException(exception);
            }
        }, CoreClass.async);
    }

    public void addPlayer(OfflinePlayer player) {
        try (Connection connection = implementConnection()) {
            if (player.getName() == null) {
                return;
            }

            PreparedStatement statement = prepareStatement(connection,
                    "INSERT INTO " + tablePrefix + "_players (uuid, name, timestamp_last_joined) VALUES (?, ?, ?)");

            statement.setString(1, player.getUniqueId().toString());
            statement.setString(2, player.getName().toLowerCase());
            statement.setLong(3, System.currentTimeMillis());
            executeUpdate(statement);

            if (previousLocationData.containsKey(player.getUniqueId())) {
                ATPlayer.getPlayer(player).setPreviousLocation(previousLocationData.get(player.getUniqueId()));

                removeLastLocation(player.getUniqueId());
            }

        } catch (SQLException exception) {
            DataFailManager.get().addFailure(
                    DataFailManager.Operation.ADD_PLAYER,
                    player.getUniqueId().toString()
            );

            exception.printStackTrace();
        }
    }

    public boolean isTeleportationOn(UUID uuid) {
        try (Connection connection = implementConnection()) {
            PreparedStatement statement = prepareStatement(connection,
                    "SELECT teleportation_on FROM " + tablePrefix + "_players WHERE uuid = ?");
            statement.setString(1, uuid.toString());
            ResultSet results = executeQuery(statement);
            if (results.next()) return results.getBoolean("teleportation_on");
        } catch (SQLException exception) {
            throw new RuntimeException(exception);
        }
        return false;
    }

    public void setTeleportationOn(UUID uuid, boolean enabled) {
        try (Connection connection = implementConnection()) {
            PreparedStatement statement = prepareStatement(connection, "UPDATE " + tablePrefix + "_players SET " +
                    "teleportation_on = ? WHERE uuid = ?");
            statement.setBoolean(1, enabled);
            statement.setString(2, uuid.toString());
            executeUpdate(statement);
        } catch (SQLException exception) {
            DataFailManager.get().addFailure(DataFailManager.Operation.CHANGE_TELEPORTATION, uuid.toString(),
                    String.valueOf(enabled));
            exception.printStackTrace();
        }
    }

    public @Nullable Location getPreviousLocation(String name) {
        try (Connection connection = implementConnection()) {
            PreparedStatement statement = prepareStatement(connection,
                    "SELECT x, y, z, yaw, pitch, world FROM " + tablePrefix + "_players WHERE name = ?");
            statement.setString(1, name.toLowerCase());
            ResultSet results = executeQuery(statement);
            if (results.next()) {
                try {
                    World world = Bukkit.getWorld(results.getString("world"));
                    return new Location(world, results.getDouble("x"),
                            results.getDouble("y"),
                            results.getDouble("z"),
                            results.getFloat("yaw"),
                            results.getFloat("pitch"));
                } catch (NullPointerException | IllegalArgumentException ex) {
                    throw new RuntimeException(ex);
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException(exception);
        }

        return null;
    }

    public void setPreviousLocation(@NotNull String name, @Nullable Location location) {

        try (Connection connection = implementConnection()) {
            PreparedStatement statement = prepareStatement(connection, "UPDATE " + tablePrefix + "_players SET x " +
                    "= ?, y = ?, z = ?, yaw = ?, pitch = ?, world = ? WHERE name = ?");
            statement.setObject(1, location == null ? null : location.getX());
            statement.setObject(2, location == null ? null : location.getY());
            statement.setObject(3, location == null ? null : location.getZ());
            statement.setObject(4, location == null ? null : location.getYaw());
            statement.setObject(5, location == null ? null : location.getPitch());
            statement.setObject(6, location == null ? null : location.getWorld().getName());
            statement.setString(7, name.toLowerCase());

            executeUpdate(statement);
        } catch (SQLException exception) {
            DataFailManager.get().addFailure(DataFailManager.Operation.UPDATE_LOCATION,
                    location.getWorld().getName(),
                    String.valueOf(location.getX()),
                    String.valueOf(location.getY()),
                    String.valueOf(location.getZ()),
                    String.valueOf(location.getYaw()),
                    String.valueOf(location.getPitch()),
                    name);
            exception.printStackTrace();
        }
    }

    public @Nullable String getMainHome(String name) {
        try (Connection connection = implementConnection()) {
            PreparedStatement statement = prepareStatement(connection, "SELECT main_home FROM " + tablePrefix +
                    "_players WHERE name = ?");
            statement.setString(1, name.toLowerCase());
            ResultSet results = executeQuery(statement);
            if (results.next()) return results.getString("main_home");

        } catch (SQLException exception) {
            throw new RuntimeException(exception);
        }

        return null;
    }

    public void setMainHome(UUID uuid, String home) {
        try (Connection connection = implementConnection()) {
            PreparedStatement statement = prepareStatement(connection, "UPDATE " + tablePrefix + "_players SET " +
                    "main_home = ? WHERE uuid = ?");
            statement.setString(1, home);
            statement.setString(2, uuid.toString());
            executeUpdate(statement);
        } catch (SQLException exception) {
            DataFailManager.get().addFailure(DataFailManager.Operation.SET_MAIN_HOME, home, uuid.toString());
            exception.printStackTrace();
        }
    }

    private static void removeLastLocation(UUID uuid) {
        previousLocationData.remove(uuid);
        lastLocations.set(uuid.toString(), null);
        lastLocations.set("death." + uuid, null);

        if (previousLocationData.isEmpty()) {
            lastLocFile.delete();
        } else {
            try {
                lastLocations.save(lastLocFile);
            } catch (IOException e) {
                CoreClass.getInstance().getLogger().severe("Failed to remove the last location of " + uuid + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

    }

    public static PlayerSQLManager get() {
        return instance;
    }
}
