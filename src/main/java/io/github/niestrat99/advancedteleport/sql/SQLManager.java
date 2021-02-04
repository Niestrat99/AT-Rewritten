package io.github.niestrat99.advancedteleport.sql;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public abstract class SQLManager {

    protected Connection connection;

    public SQLManager() {

        Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), () -> {

            if (NewConfig.getInstance().USE_MYSQL.get()) {

            } else {
                // Load JDBC
                try {
                    Class.forName("org.sqlite.JDBC");
                    connection = DriverManager.getConnection("jdbc:sqlite:" + CoreClass.getInstance().getDataFolder() + "/data.db");
                } catch (ClassNotFoundException | SQLException e) {
                    e.printStackTrace();
                }
            }

            createTable();
            transferOldData();
        });


    }


    public abstract void createTable();

    public abstract void transferOldData();

    public interface SQLCallback<D> {
        void onSuccess(D data);

        default void onSuccess() {}

        default void onFail() {}
    }


}
