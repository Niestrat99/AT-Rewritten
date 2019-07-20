package io.github.at.config;

import io.github.at.main.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class Spawn {

    public static File Spawn = new File(Main.getInstance().getDataFolder(), "spawn.yml");
    public static FileConfiguration SpawnPoint = YamlConfiguration.loadConfiguration(Spawn);

    public static void setSpawn(Location location) throws IOException {
        SpawnPoint.set("spawnpoint.x", location.getX());
        SpawnPoint.set("spawnpoint.y", location.getY());
        SpawnPoint.set("spawnpoint.z", location.getZ());
        SpawnPoint.set("spawnpoint.world", location.getWorld().getName());
        SpawnPoint.set("spawnpoint.yaw", location.getYaw());
        SpawnPoint.set("spawnpoint.pitch", location.getPitch());
        save();
    }

    public static void save() throws IOException {
        SpawnPoint.save(Spawn);
    }

    public static Location getSpawn() {
        try {
            return new Location(Bukkit.getWorld(SpawnPoint.getString( "spawnpoint.world")), SpawnPoint.getDouble(  "spawnpoint.x"), SpawnPoint.getDouble("spawnpoint.y"), SpawnPoint.getDouble("spawnpoint.z"), Float.valueOf(String.valueOf(SpawnPoint.getDouble("spawnpoint.yaw"))), Float.valueOf(String.valueOf(SpawnPoint.getDouble("spawnpoint.pitch"))));
        } catch (NullPointerException | IllegalArgumentException ex) {
            return null;
        }
    }
}
