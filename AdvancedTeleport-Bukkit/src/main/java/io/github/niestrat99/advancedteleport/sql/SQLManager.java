package io.github.niestrat99.advancedteleport.sql;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class SQLManager {

    protected static String tablePrefix;
    protected static volatile boolean usingSqlite;

    public SQLManager() {
        tablePrefix = NewConfig.get().TABLE_PREFIX.get();
        if (!tablePrefix.matches("^[_A-Za-z0-9]+$")) {
            CoreClass.getInstance().getLogger().warning("Table prefix " + tablePrefix + " is not alphanumeric. Using advancedtp...");
            tablePrefix = "advancedtp";
        }
        implementConnection();
        createTable();
    }

    public Connection implementConnection() {
        Connection connection;
        if (NewConfig.get().USE_MYSQL.get()) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                String url = String.format(
                    "jdbc:mysql://%s:%d/%s?useSSL=%b&autoReconnect=%b&allowPublicKeyRetrieval=%b",
                    NewConfig.get().MYSQL_HOST.get(),
                    NewConfig.get().MYSQL_PORT.get(),
                    NewConfig.get().MYSQL_DATABASE.get(),
                    NewConfig.get().USE_SSL.get(),
                    NewConfig.get().AUTO_RECONNECT.get(),
                    NewConfig.get().ALLOW_PUBLIC_KEY_RETRIEVAL.get()
                );
                connection = DriverManager.getConnection(url, NewConfig.get().USERNAME.get(), NewConfig.get().PASSWORD.get());
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
        Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), runnable);
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

    public interface SQLCallback<D> {
        static SQLCallback<Boolean> getDefaultCallback(
            CommandSender sender,
            String success,
            String fail,
            String... placeholders
        ) {
            return new SQLCallback<>() {
                @Override
                public void onSuccess(Boolean data) {
                    CustomMessages.sendMessage(sender, success, (Object[]) placeholders);
                }

                @Override
                public void onFail() {
                    CustomMessages.sendMessage(sender, fail, (Object[]) placeholders);
                }
            };
        }

        void onSuccess(D data);

        default void onSuccess() { }

        default void onFail() { }
    }

}
