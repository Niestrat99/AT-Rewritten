package io.github.niestrat99.advancedteleport.sql;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import org.bukkit.command.CommandSender;

import java.sql.*;

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

    public Connection implementConnection() {
        Connection connection;
        if (NewConfig.get().USE_MYSQL.get()) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                connection = DriverManager.getConnection("jdbc:mysql://"
                                + NewConfig.get().MYSQL_HOST.get() + ":"
                                + NewConfig.get().MYSQL_PORT.get() + "/"
                                + NewConfig.get().MYSQL_DATABASE.get() + "?useSSL=false&autoReconnect=true",
                        NewConfig.get().USERNAME.get(),
                        NewConfig.get().PASSWORD.get());
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

    public abstract void transferOldData();

    public static String getTablePrefix() {
        return tablePrefix;
    }

    public String getStupidAutoIncrementThing() {
        return usingSqlite ? "AUTOINCREMENT" : "AUTO_INCREMENT";
    }

    protected synchronized ResultSet executeQuery(PreparedStatement statement) throws SQLException {
        return statement.executeQuery();
    }

    protected synchronized void executeUpdate(PreparedStatement statement) throws SQLException {
        statement.executeUpdate();
    }

    protected synchronized PreparedStatement prepareStatement(Connection connection, String sql) throws SQLException {
        return connection.prepareStatement(sql);
    }

    public interface SQLCallback<D> {
        void onSuccess(D data);

        default void onSuccess() {}

        default void onFail() {}

        static SQLCallback<Boolean> getDefaultCallback(CommandSender sender, String success, String fail, String... placeholders) {
            return new SQLCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean data) {
                    CustomMessages.sendMessage(sender, success, placeholders);
                }

                @Override
                public void onFail() {
                    CustomMessages.sendMessage(sender, fail, placeholders);
                }
            };
        }
    }


}
