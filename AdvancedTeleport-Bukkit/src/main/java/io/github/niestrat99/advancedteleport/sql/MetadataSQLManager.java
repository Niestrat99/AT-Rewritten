package io.github.niestrat99.advancedteleport.sql;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.AdvancedTeleportAPI;
import io.github.niestrat99.advancedteleport.api.Spawn;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MetadataSQLManager extends SQLManager {

    private static MetadataSQLManager instance;

    public MetadataSQLManager() {
        super();
        instance = this;
    }

    public static MetadataSQLManager get() {
        return instance;
    }

    @Override
    public void createTable() {
        Bukkit.getScheduler()
                .runTaskAsynchronously(
                        CoreClass.getInstance(),
                        () -> {
                            CoreClass.debug(
                                    "Creating table data for the metadata manager if it is not already set up.");

                            try (Connection connection = implementConnection()) {
                                PreparedStatement createTable =
                                        prepareStatement(
                                                connection,
                                                "CREATE TABLE IF NOT EXISTS "
                                                        + tablePrefix
                                                        + "_metadata "
                                                        + "(data_id VARCHAR(256) NOT NULL, "
                                                        + "type VARCHAR(256) NOT NULL,"
                                                        + "key VARCHAR(256) NOT NULL, "
                                                        + "value TEXT NOT NULL)");
                                executeUpdate(createTable);
                            } catch (SQLException exception) {
                                CoreClass.getInstance()
                                        .getLogger()
                                        .severe("Failed to create the metadata table.");
                                exception.printStackTrace();
                            }
                            transferOldData();
                        });
    }

    @Override
    public void transferOldData() {}

    public HashMap<String, String> getAllValues(Connection connection, String type, String key)
            throws SQLException {
        HashMap<String, String> results = new HashMap<>();
        PreparedStatement statement =
                prepareStatement(
                        connection,
                        "SELECT data_id, value FROM "
                                + tablePrefix
                                + "_metadata WHERE type = ? AND key = ?;");
        statement.setString(1, type);
        statement.setString(2, key);
        ResultSet set = executeQuery(statement);
        while (set.next()) {
            results.put(set.getString("data_id"), set.getString("value"));
        }
        return results;
    }

    public CompletableFuture<Boolean> addWarpMetadata(String warpName, String key, String value) {
        return addWarpMetadata(warpName, key, value, true);
    }

    public CompletableFuture<Boolean> addWarpMetadata(
            String warpName, String key, String value, boolean single) {
        return WarpSQLManager.get()
                .getWarpId(warpName)
                .thenApplyAsync(
                        id -> {
                            try (Connection connection = implementConnection()) {
                                if (id == -1) return false;
                                return addMetadata(
                                        connection, String.valueOf(id), "WARP", key, value, single);
                            } catch (SQLException throwables) {
                                throw new RuntimeException(throwables);
                            }
                        },
                        CoreClass.async);
    }

    public boolean addMetadata(
            Connection connection,
            String dataId,
            String type,
            String key,
            String value,
            boolean single)
            throws SQLException {

        if (single) deleteMetadata(connection, dataId, type, key);

        PreparedStatement statement =
                prepareStatement(
                        connection,
                        "INSERT INTO "
                                + tablePrefix
                                + "_metadata (data_id, type, key, value) VALUES (?, ?, ?, ?);");
        statement.setString(1, dataId);
        statement.setString(2, type);
        statement.setString(3, key);
        statement.setString(4, value);
        executeUpdate(statement);
        return true;
    }

    public CompletableFuture<Boolean> addHomeMetadata(
            String homeName, UUID owner, String key, String value) {
        return addHomeMetadata(homeName, owner, key, value, true);
    }

    public CompletableFuture<Boolean> addHomeMetadata(
            String homeName, UUID owner, String key, String value, boolean single) {
        return HomeSQLManager.get()
                .getHomeId(homeName, owner)
                .thenApplyAsync(
                        id -> {
                            try (Connection connection = implementConnection()) {
                                if (id == -1) return false;
                                return addMetadata(
                                        connection, String.valueOf(id), "HOME", key, value, single);
                            } catch (SQLException throwables) {
                                throw new RuntimeException(throwables);
                            }
                        });
    }

    public CompletableFuture<Boolean> addSpawnMetadata(String spawnName, String key, String value) {
        return addSpawnMetadata(spawnName, key, value, true);
    }

    public CompletableFuture<Boolean> addSpawnMetadata(
            String spawnName, String key, String value, boolean single) {
        return SpawnSQLManager.get()
                .getSpawnId(spawnName)
                .thenApplyAsync(
                        id -> {
                            try (Connection connection = implementConnection()) {
                                return addMetadata(
                                        connection,
                                        String.valueOf(id),
                                        "SPAWN",
                                        key,
                                        value,
                                        single);
                            } catch (SQLException throwables) {
                                throw new RuntimeException(throwables);
                            }
                        });
    }

    public CompletableFuture<String> getWarpMetadata(String warpName, String key) {
        return WarpSQLManager.get()
                .getWarpId(warpName)
                .thenApplyAsync(
                        id -> {
                            try (Connection connection = implementConnection()) {
                                if (id == -1) return null;
                                return getValue(connection, String.valueOf(id), "WARP", key);
                            } catch (SQLException throwables) {
                                throw new RuntimeException(throwables);
                            }
                        });
    }

    public @Nullable String getValue(Connection connection, String dataId, String type, String key)
            throws SQLException {
        PreparedStatement statement =
                prepareStatement(
                        connection,
                        "SELECT value FROM "
                                + tablePrefix
                                + "_metadata WHERE data_id = ? AND type = ? AND key = ?;");
        statement.setString(1, dataId);
        statement.setString(2, type);
        statement.setString(3, key);
        ResultSet set = executeQuery(statement);
        if (set.next()) {
            return set.getString("value");
        }
        return null;
    }

    public CompletableFuture<String> getHomeMetadata(String homeName, UUID owner, String key) {
        return HomeSQLManager.get()
                .getHomeId(homeName, owner)
                .thenApplyAsync(
                        id -> {
                            try (Connection connection = implementConnection()) {
                                if (id == -1) return null;
                                return getValue(connection, String.valueOf(id), "HOME", key);
                            } catch (SQLException throwables) {
                                throw new RuntimeException(throwables);
                            }
                        });
    }

    public CompletableFuture<String> getSpawnMetadata(String spawnName, String key) {
        return SpawnSQLManager.get()
                .getSpawnId(spawnName)
                .thenApplyAsync(
                        id -> {
                            try (Connection connection = implementConnection()) {
                                if (id == null) return null;
                                return getValue(connection, String.valueOf(id), "SPAWN", key);
                            } catch (SQLException throwables) {
                                throw new RuntimeException(throwables);
                            }
                        },
                        CoreClass.async);
    }

    public CompletableFuture<Boolean> deleteWarpMetadata(String warpName, String key) {
        return WarpSQLManager.get()
                .getWarpId(warpName)
                .thenApplyAsync(
                        id -> {
                            try (Connection connection = implementConnection()) {
                                if (id == -1) return false;
                                return deleteMetadata(connection, String.valueOf(id), "WARP", key);
                            } catch (SQLException throwables) {
                                throw new RuntimeException(throwables);
                            }
                        },
                        CoreClass.async);
    }

    public boolean deleteMetadata(Connection connection, String dataId, String type, String key)
            throws SQLException {
        PreparedStatement statement =
                prepareStatement(
                        connection,
                        "DELETE FROM "
                                + tablePrefix
                                + "_metadata WHERE data_id = ? AND type = ? AND key = ?;");
        statement.setString(1, dataId);
        statement.setString(2, type);
        statement.setString(3, key);
        executeUpdate(statement);
        return true;
    }

    public CompletableFuture<Boolean> deleteHomeMetadata(String homeName, UUID owner, String key) {
        return HomeSQLManager.get()
                .getHomeId(homeName, owner)
                .thenApplyAsync(
                        id -> {
                            try (Connection connection = implementConnection()) {
                                if (id == -1) return false;
                                return deleteMetadata(connection, String.valueOf(id), "HOME", key);
                            } catch (SQLException throwables) {
                                throw new RuntimeException(throwables);
                            }
                        });
    }

    public CompletableFuture<Boolean> deleteMainSpawn() {
        return CompletableFuture.supplyAsync(
                () -> {
                    try (Connection connection = implementConnection()) {

                        PreparedStatement statement =
                                prepareStatement(
                                        connection,
                                        "DELETE FROM "
                                                + tablePrefix
                                                + "_metadata "
                                                + "WHERE type = 'SPAWN' AND key = 'main_spawn'");
                        statement.executeUpdate();
                        return true;
                    } catch (SQLException exception) {
                        throw new RuntimeException(exception);
                    }
                },
                CoreClass.async);
    }

    public CompletableFuture<Boolean> mirrorSpawn(@NotNull String source, @Nullable String mirror) {
        return SpawnSQLManager.get()
                .getSpawnId(source)
                .thenApplyAsync(
                        id -> {
                            try (Connection connection = implementConnection()) {

                                // Get the raw spawn
                                String idRaw = String.valueOf(id);
                                if (idRaw.equals("-1")) idRaw = source;

                                // Remove any existing mirrors
                                PreparedStatement deleteStatement =
                                        prepareStatement(
                                                connection,
                                                "DELETE FROM "
                                                        + tablePrefix
                                                        + "_metadata "
                                                        + "WHERE type = 'SPAWN' AND key = 'mirror' AND data_id = ?");
                                deleteStatement.setString(1, idRaw);
                                executeUpdate(deleteStatement);

                                // If the mirror is null, stop there
                                if (mirror == null) return false;

                                // Get the mirror ID
                                int mirrorId =
                                        SpawnSQLManager.get().getSpawnIdSync(connection, mirror);
                                String rawMirrorId = String.valueOf(mirrorId);
                                if (rawMirrorId.equals("-1")) rawMirrorId = mirror;

                                // Set up the statement
                                return addMetadata(
                                        connection,
                                        idRaw,
                                        "SPAWN",
                                        "mirror_spawn",
                                        rawMirrorId,
                                        true);
                            } catch (SQLException exception) {
                                throw new RuntimeException(exception);
                            }
                        },
                        CoreClass.async);
    }

    private Spawn getSpawnFromId(Connection connection, String id) throws SQLException {

        // If it's not a numeric ID, just get the destination ID
        if (!id.matches("^[0-9]+$")) {
            World world = Bukkit.getWorld(id);
            if (world == null) return null;
            return AdvancedTeleportAPI.getDestinationSpawn(world, null);
        }

        // Otherwise, fetch the spawn by its ID
        PreparedStatement statement =
                prepareStatement(
                        connection, "SELECT spawn FROM " + tablePrefix + "_spawns WHERE id = ?;");
        statement.setInt(1, Integer.parseInt(id));
        ResultSet set = statement.executeQuery();

        // If there's a result, grab it
        if (set.next()) return AdvancedTeleportAPI.getSpawn(set.getString("spawn"));

        // Return nothing
        return null;
    }

    public CompletableFuture<Void> loadMirroredSpawns() {
        return CompletableFuture.runAsync(
                () -> {
                    try (Connection connection = implementConnection()) {

                        // Get all spawn IDs and mirror IDs
                        HashMap<String, String> ids =
                                getAllValues(connection, "SPAWN", "mirror_spawn");

                        for (String dataId : ids.keySet()) {
                            String mirrorId = ids.get(dataId);

                            // Get the spawns
                            Spawn main = getSpawnFromId(connection, dataId);
                            Spawn mirror = getSpawnFromId(connection, mirrorId);

                            // If the main is not null, then mirror it
                            if (main != null) {
                                Bukkit.getScheduler()
                                        .runTask(
                                                CoreClass.getInstance(),
                                                () -> main.setMirroringSpawn(mirror, null));
                            }
                        }

                    } catch (SQLException exception) {
                        throw new RuntimeException(exception);
                    }
                },
                CoreClass.async);
    }

    public CompletableFuture<@Nullable Spawn> loadMainSpawn() {
        return CompletableFuture.supplyAsync(
                () -> {
                    try (Connection connection = implementConnection()) {

                        PreparedStatement statement =
                                prepareStatement(
                                        connection,
                                        "SELECT spawn FROM "
                                                + tablePrefix
                                                + "_spawns "
                                                + "JOIN "
                                                + tablePrefix
                                                + "_metadata ON "
                                                + tablePrefix
                                                + "_spawns.id = "
                                                + tablePrefix
                                                + "_metadata.data_id "
                                                + "AND "
                                                + tablePrefix
                                                + "_metadata.key = 'main_spawn' AND "
                                                + tablePrefix
                                                + "_metadata.value = 'true';");

                        ResultSet set = statement.executeQuery();

                        if (set.next()) return AdvancedTeleportAPI.getSpawn(set.getString("spawn"));

                        return null;
                    } catch (SQLException exception) {
                        throw new RuntimeException(exception);
                    }
                },
                CoreClass.async);
    }
}
