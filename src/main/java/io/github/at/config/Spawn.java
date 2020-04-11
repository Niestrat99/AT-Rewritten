package io.github.at.config;

import io.github.at.main.CoreClass;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class Spawn {

    public static File spawnFile = new File(CoreClass.getInstance().getDataFolder(), "spawn.yml");
    public static FileConfiguration spawn = YamlConfiguration.loadConfiguration(spawnFile);

    public static void setSpawn(Location location) throws IOException {
        spawn.set("spawnpoint.x", location.getX());
        spawn.set("spawnpoint.y", location.getY());
        spawn.set("spawnpoint.z", location.getZ());
        spawn.set("spawnpoint.world", location.getWorld().getName());
        spawn.set("spawnpoint.yaw", location.getYaw());
        spawn.set("spawnpoint.pitch", location.getPitch());
        save();
    }

    public static void save() throws IOException {
        spawn.save(spawnFile);
    }

    public static Location getSpawnFile() {
        try {
            return new Location(Bukkit.getWorld(spawn.getString( "spawnpoint.world")), spawn.getDouble(  "spawnpoint.x"), spawn.getDouble("spawnpoint.y"), spawn.getDouble("spawnpoint.z"), Float.valueOf(String.valueOf(spawn.getDouble("spawnpoint.yaw"))), Float.valueOf(String.valueOf(spawn.getDouble("spawnpoint.pitch"))));
        } catch (NullPointerException | IllegalArgumentException ex) {
            return null;
        }
    }

    public static void reloadSpawn() throws IOException {
        if (spawnFile == null) {
            spawnFile = new File(CoreClass.getInstance().getDataFolder(), "last-locations.yml");
        }
        spawn = YamlConfiguration.loadConfiguration(spawnFile);
        save();
    }
}
