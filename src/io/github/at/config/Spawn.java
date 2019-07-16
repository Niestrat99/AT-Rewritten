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
        SpawnPoint.set("spawnpoint.x", location.getBlockX());
        SpawnPoint.set("spawnpoint.y", location.getBlockY());
        SpawnPoint.set("spawnpoint.z", location.getBlockZ());
        SpawnPoint.set("spawnpoint.world", location.getWorld().getName());
        save();
    }

    private static void save() throws IOException {
        SpawnPoint.save(Spawn);
    }

    public static Location getSpawn() {
        try {
            Location location = new Location(Bukkit.getWorld(SpawnPoint.getString( "spawnpoint.world")), SpawnPoint.getInt(  "spawnpoint.x"), SpawnPoint.getInt("spawnpoint.y"), SpawnPoint.getInt("spawnpoint.z"));
            return location;
        } catch (NullPointerException | IllegalArgumentException ex) {
            return null;
        }

    }
}
