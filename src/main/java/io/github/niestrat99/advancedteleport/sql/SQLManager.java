package io.github.niestrat99.advancedteleport.sql;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.config.NewConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public abstract class SQLManager {

    protected static Connection connection;

    public SQLManager() {
        if (connection == null) {
            if (NewConfig.get().USE_MYSQL.get()) {
                try {
                    Class.forName("com.mysql.jdbc.Driver");
                    connection = DriverManager.getConnection("jdbc:mysql://"
                            + NewConfig.get().MYSQL_HOST.get() + ":"
                            + NewConfig.get().MYSQL_PORT.get() + "/"
                            + NewConfig.get().MYSQL_DATABASE.get() + "?useSSL=false&autoReconnect=true",
                            NewConfig.get().USERNAME.get(),
                            NewConfig.get().PASSWORD.get());

                } catch (ClassNotFoundException | SQLException e) {
                    e.printStackTrace();
                    loadSqlite();
                }


            } else {
                loadSqlite();
            }
        }
        createTable();
    }

    private void loadSqlite() {
        // Load JDBC
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + CoreClass.getInstance().getDataFolder() + "/data.db");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public abstract void createTable();

    public abstract void transferOldData();

    public interface SQLCallback<D> {
        void onSuccess(D data);

        default void onSuccess() {}

        default void onFail() {}
    }


}
