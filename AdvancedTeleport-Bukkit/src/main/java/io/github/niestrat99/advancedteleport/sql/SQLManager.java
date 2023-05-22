package io.github.niestrat99.advancedteleport.sql;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.data.UnloadedWorldException;
import io.github.niestrat99.advancedteleport.config.MainConfig;
import io.github.niestrat99.advancedteleport.folia.RunnableManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class SQLManager {

    protected static String tablePrefix;
    protected static volatile boolean usingSqlite;

    public SQLManager() {
        tablePrefix = MainConfig.get().TABLE_PREFIX.get();
        if (!tablePrefix.matches("^[_A-Za-z0-9]+$")) {
            CoreClass.getInstance().getLogger().warning("Table prefix " + tablePrefix + " is not alphanumeric. Using advancedtp...");
            tablePrefix = "advancedtp";
        }
        try (Connection ignored = implementConnection()) {
            CoreClass.debug("Connection to SQL data source successful.");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        createTable();
    }

    public Connection implementConnection() {
        Connection connection;
        if (MainConfig.get().USE_MYSQL.get()) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                String url = String.format(
                        "jdbc:mysql://%s:%d/%s?useSSL=%b&autoReconnect=%b&allowPublicKeyRetrieval=%b",
                        MainConfig.get().MYSQL_HOST.get(),
                        MainConfig.get().MYSQL_PORT.get(),
                        MainConfig.get().MYSQL_DATABASE.get(),
                        MainConfig.get().USE_SSL.get(),
                        MainConfig.get().AUTO_RECONNECT.get(),
                        MainConfig.get().ALLOW_PUBLIC_KEY_RETRIEVAL.get()
                );
                connection = DriverManager.getConnection(url, MainConfig.get().USERNAME.get(), MainConfig.get().PASSWORD.get());
                usingSqlite = false;
                return connection;
            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
                connection = loadSqlite();
            }
        } else {
            connection = loadSqlite();
        }
        return connection;
    }

    public abstract void createTable();

    private Connection loadSqlite() {
        // Load JDBC
        try {
            Class.forName("org.sqlite.JDBC");
            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + CoreClass.getInstance().getDataFolder() + "/data.db");
            usingSqlite = true;
            return connection;
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void runAsync(Runnable runnable) {
        RunnableManager.setupRunnerAsync(runnable);
    }

    public static String getTablePrefix() {
        return tablePrefix;
    }

    public abstract void transferOldData();

    public String getStupidAutoIncrementThing() {
        return usingSqlite ? "AUTOINCREMENT" : "AUTO_INCREMENT";
    }

    protected synchronized ResultSet executeQuery(PreparedStatement statement) throws SQLException {
        return statement.executeQuery();
    }

    protected synchronized void executeUpdate(PreparedStatement statement) throws SQLException {
        statement.executeUpdate();
    }

    protected synchronized PreparedStatement prepareStatement(
        Connection connection,
        String sql
    ) throws SQLException {
        return connection.prepareStatement(sql);
    }

    protected void prepareLocation(@NotNull Location location, int startingIndex, PreparedStatement statement) throws SQLException {
        statement.setDouble(startingIndex++, location.getX());
        statement.setDouble(startingIndex++, location.getY());
        statement.setDouble(startingIndex++, location.getZ());
        statement.setFloat(startingIndex++, location.getYaw());
        statement.setFloat(startingIndex++, location.getPitch());
        statement.setString(startingIndex, location.getWorld().getName());
    }

    protected @NotNull Location getLocation(ResultSet set) throws SQLException, UnloadedWorldException {

        // Get base coordinates
        double x = set.getDouble("x");
        double y = set.getDouble("y");
        double z = set.getDouble("z");

        // Get rotation
        float yaw = set.getFloat("yaw");
        float pitch = set.getFloat("pitch");

        // Get the world name
        String worldName = set.getString("world");
        World world = Bukkit.getWorld(worldName);
        if (world == null)
            throw new UnloadedWorldException(worldName, "Error getting location: world " + worldName + " is unloaded.");

        return new Location(world, x, y, z, yaw, pitch);
    }
}
