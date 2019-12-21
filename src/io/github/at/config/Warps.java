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

    public static File warp = new File(Main.getInstance().getDataFolder(), "warps.yml");
    public static FileConfiguration warps = YamlConfiguration.loadConfiguration(warp);

    public static void save() throws IOException {
        warps.save(warp);
    }

    public static void setWarp(String warpName, Location location) throws IOException {
        if (warpName.contains(".")) {
            String s = warpName;
            warpName = warpName.replaceAll("\\.", "_");
            warps.set(warpName + ".name", s);

        }
        warps.set(warpName + ".x", location.getX());
        warps.set(warpName + ".y", location.getY());
        warps.set(warpName + ".z", location.getZ());
        warps.set(warpName + ".yaw", location.getYaw());
        warps.set(warpName + ".pitch", location.getPitch());
        warps.set(warpName + ".world", location.getWorld().getName());
        save();
    }

    public static HashMap<String, Location> getWarps() {
        HashMap<String, Location> warps = new HashMap<>();
        for (String Warp : Warps.warps.getKeys(false)) {
            Location location = new Location(Bukkit.getWorld(Warps.warps.getString(Warp + ".world")), Warps.warps.getDouble(Warp + ".x"), Warps.warps.getDouble(Warp + ".y"), Warps.warps.getDouble(Warp + ".z"), Float.valueOf(String.valueOf(Warps.warps.getDouble(Warp + ".yaw"))), Float.valueOf(String.valueOf(Warps.warps.getDouble(Warp + ".pitch"))));
            Warp = Warps.warps.getString(Warp + ".name") != null ? Warps.warps.getString(Warp + ".name") : Warp;
            warps.put(Warp, location);
        }
        return warps;

    }
    public static void delWarp(String warpName) throws IOException {
        warps.set(warpName,null);
        save();
    }

    public static void reloadWarps() throws IOException {
        if (warp == null) {
            warp = new File(Main.getInstance().getDataFolder(), "warps.yml");
        }
        warps = YamlConfiguration.loadConfiguration(warp);
        save();
    }
}
