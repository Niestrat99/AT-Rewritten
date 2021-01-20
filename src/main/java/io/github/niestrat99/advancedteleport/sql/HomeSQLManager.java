package io.github.niestrat99.advancedteleport.sql;

import io.github.niestrat99.advancedteleport.CoreClass;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class HomeSQLManager extends SQLManager {

    @Override
    public void createTable() {
        Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), () -> {
            try {
                PreparedStatement createTable = connection.prepareStatement("CREATE TABLE IF NOT EXISTS advancedtp_homes " +
                        "(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "uuid_owner VARCHAR(256) NOT NULL, " +
                        "home VARCHAR(256) NOT NULL," +
                        "x DOUBLE NOT NULL," +
                        "y DOUBLE NOT NULL," +
                        "z DOUBLE NOT NULL," +
                        "yaw FLOAT NOT NULL," +
                        "pitch FLOAT NOT NULL," +
                        "world VARCHAR(256) NOT NULL," +
                        "timestamp BIGINT NOT NULL)");
                createTable.executeUpdate();
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        });
    }

    @Override
    public void transferOldData() {
        // Load the config file.
        YamlConfiguration homes = YamlConfiguration.loadConfiguration(
                new File(CoreClass.getInstance().getDataFolder(), "homes.yml"));
        // For each player found...
        for (String player : homes.getKeys(false)) {
            // Get the config section representing their homes.
            ConfigurationSection homeSection = homes.getConfigurationSection(player);
            // For each home that appears...

        }
    }

    public void addHome(Location location, UUID owner, String name) {
        Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), () -> {
            try {
                PreparedStatement statement = connection.prepareStatement(
                            "INSERT INTO advancedtp_homes (uuid_owner, home, x, y, z, yaw, pitch, world, timestamp) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");

                statement.setString(1, owner.toString());
                statement.setString(2, name);
                statement.setDouble(3, location.getX());
                statement.setDouble(4, location.getY());
                statement.setDouble(5, location.getZ());
                statement.setDouble(6, location.getYaw());
                statement.setDouble(7, location.getPitch());
                statement.setString(8, location.getWorld().getName());
                statement.setLong(9, System.currentTimeMillis());
                statement.executeUpdate();
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        });
    }

    public void removeHome(UUID owner, String name) {
        
    }
}
