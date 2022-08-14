package io.github.niestrat99.advancedteleport.sql;

import io.github.niestrat99.advancedteleport.CoreClass;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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
        Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), () -> {
            try (Connection connection = implementConnection()) {
                PreparedStatement createTable = prepareStatement(connection,
                        "CREATE TABLE IF NOT EXISTS " + tablePrefix + "_metadata " +
                                "(data_id INTEGER, " +
                                "type VARCHAR(256) NOT NULL," +
                                "key VARCHAR(256) NOT NULL, " +
                                "value TEXT NOT NULL)");
                executeUpdate(createTable);
            } catch (SQLException exception) {
                CoreClass.getInstance().getLogger().severe("Failed to create the metadata table.");
                exception.printStackTrace();
            }
            transferOldData();
        });
    }

    @Override
    public void transferOldData() {
    }

    public String getValue(Connection connection, int dataId, String type, String key) throws SQLException {
        PreparedStatement statement = prepareStatement(connection,
                "SELECT value FROM " + tablePrefix + "_metadata WHERE data_id = ? AND type = ? AND key = ?;");
        statement.setInt(1, dataId);
        statement.setString(2, type);
        statement.setString(3, key);
        ResultSet set = executeQuery(statement);
        if (set.next()) {
            return set.getString("value");
        }
        return null;
    }

    public List<String> getAllValues(Connection connection, int dataId, String type, String key) throws SQLException {
        List<String> results = new ArrayList<>();
        PreparedStatement statement = prepareStatement(connection,
                "SELECT value FROM " + tablePrefix + "_metadata WHERE data_id = ? AND type = ? AND key = ?;");
        statement.setInt(1, dataId);
        statement.setString(2, type);
        statement.setString(3, key);
        ResultSet set = executeQuery(statement);
        while (set.next()) {
            results.add(set.getString("value"));
        }
        return results;
    }

    public boolean addMetadata(Connection connection, int dataId, String type, String key, String value) throws SQLException {
        PreparedStatement statement = prepareStatement(connection,
                "INSERT INTO " + tablePrefix + "_metadata (data_id, type, key, value) VALUES (?, ?, ?, ?);");
        statement.setInt(1, dataId);
        statement.setString(2, type);
        statement.setString(3, key);
        statement.setString(4, value);
        executeUpdate(statement);
        return true;
    }

    public boolean deleteMetadata(Connection connection, int dataId, String type, String key) throws SQLException {
        PreparedStatement statement = prepareStatement(connection,
                "DELETE FROM " + tablePrefix + "_metadata WHERE data_id = ? AND type = ? AND key = ?;");
        statement.setInt(1, dataId);
        statement.setString(2, type);
        statement.setString(3, key);
        executeUpdate(statement);
        return true;
    }

    public CompletableFuture<Boolean> addWarpMetadata(String warpName, String key, String value) {
        return WarpSQLManager.get().getWarpId(warpName).thenApplyAsync(id -> {
            try (Connection connection = implementConnection()) {
                if (id == -1) return false;
                return addMetadata(connection, id, "WARP", key, value);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            return false;
        }, CoreClass.async);
    }

    public CompletableFuture<Boolean> addHomeMetadata(String homeName, UUID owner, String key, String value) {
        return HomeSQLManager.get().getHomeId(homeName, owner).thenApplyAsync(id -> {
            try (Connection connection = implementConnection()) {
                if (id == -1) return false;
                return addMetadata(connection, id, "HOME", key, value);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            return false;
        });
    }

    public CompletableFuture<String> getWarpMetadata(String warpName, String key) {
        return WarpSQLManager.get().getWarpId(warpName).thenApplyAsync(id -> {
            try (Connection connection = implementConnection()) {
                if (id == -1) return null;
                return getValue(connection, id, "WARP", key);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            return null;
        });
    }

    public CompletableFuture<String> getHomeMetadata(String homeName, UUID owner, String key) {
        return HomeSQLManager.get().getHomeId(homeName, owner).thenApplyAsync(id -> {
            try (Connection connection = implementConnection()) {
                if (id == -1) return null;
                return getValue(connection, id, "HOME", key);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            return null;
        });
    }

    public CompletableFuture<Boolean> deleteWarpMetadata(String warpName, String key) {
        return WarpSQLManager.get().getWarpId(warpName).thenApplyAsync(id -> {
            try (Connection connection = implementConnection()) {
                if (id == -1) return false;
                return deleteMetadata(connection, id, "WARP", key);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            return false;
        }, CoreClass.async);
    }

    public CompletableFuture<Boolean> deleteHomeMetadata(String homeName, UUID owner, String key) {
        return HomeSQLManager.get().getHomeId(homeName, owner).thenApplyAsync(id -> {
            try (Connection connection = implementConnection()) {
                if (id == -1) return false;
                return deleteMetadata(connection, id, "HOME", key);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            return false;
        });
    }
}
