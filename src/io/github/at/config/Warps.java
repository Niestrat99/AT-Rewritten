package io.github.at.config;

import io.github.at.main.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class Warps {

    public static File Warp = new File(Main.getInstance().getDataFolder(), "Warps.yml");
    public static FileConfiguration Warps = YamlConfiguration.loadConfiguration(Warp);

    public static void save() throws IOException {
        Warps.save(Warp);
    }

    public static void setWarp(String warpName, Location location) throws IOException {
        if (warpName.contains(".")) {
            String s = warpName;
            warpName = warpName.replaceAll("\\.", "_");
            Warps.set(warpName + ".name", s);

        }
        Warps.set(warpName + ".x", location.getX());
        Warps.set(warpName + ".y", location.getY());
        Warps.set(warpName + ".z", location.getZ());
        Warps.set(warpName + ".yaw", location.getYaw());
        Warps.set(warpName + ".pitch", location.getPitch());
        Warps.set(warpName + ".world", location.getWorld().getName());
        save();
    }

    public static HashMap<String, Location> getWarps() {
        HashMap<String, Location> warps = new HashMap<>();
        for (String Warp : Warps.getKeys(false)) {
            Location location = new Location(Bukkit.getWorld(Warps.getString(Warp + ".world")), Warps.getDouble(Warp + ".x"), Warps.getDouble(Warp + ".y"), Warps.getDouble(Warp + ".z"), Float.valueOf(String.valueOf(Warps.getDouble(Warp + ".yaw"))), Float.valueOf(String.valueOf(Warps.getDouble(Warp + ".pitch"))));
            Warp = Warps.getString(Warp + ".name") != null ? Warps.getString(Warp + ".name") : Warp;
            warps.put(Warp, location);
        }
        return warps;

    }
    public static void delWarp(String warpName) throws IOException {
        Warps.set(warpName,null);
        save();
    }
}
