package io.github.at.config;

import io.github.at.main.CoreClass;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class Homes {

    public static File homesFile = new File(CoreClass.getInstance().getDataFolder(),"homes.yml");
    public static FileConfiguration homes = YamlConfiguration.loadConfiguration(homesFile);

    public static void save() throws IOException {
        homes.save(homesFile);
    }

    public static void setHome(String uuid, String homename, Location location) throws IOException {
        if (homename.contains(".")) {
            String s = homename;
            homename = homename.replaceAll("\\.", "_");
            homes.set(uuid + "." + homename + ".name", s);

        }
        homes.set(uuid + "." + homename + ".x", location.getX());
        homes.set(uuid + "." + homename + ".y", location.getY());
        homes.set(uuid + "." + homename + ".z", location.getZ());
        homes.set(uuid + "." + homename + ".world", location.getWorld().getName());
        homes.set(uuid + "." + homename + ".yaw", location.getYaw());
        homes.set(uuid + "." + homename + ".pitch", location.getPitch());
        save();
    }

    public static HashMap<String,Location> getHomes(String uuid){
        HashMap<String,Location> homes = new HashMap<>();
        try {
            for (String home: Homes.homes.getConfigurationSection(uuid).getKeys(false)) {
                Location location = new Location(Bukkit.getWorld(Homes.homes.getString(uuid + "." + home + ".world")), // Gets world from name
                        Homes.homes.getDouble(uuid + "." + home + ".x"), // Gets X value
                        Homes.homes.getDouble(uuid + "." + home + ".y"), // Gets Y value
                        Homes.homes.getDouble(uuid + "." + home + ".z"), // Gets Z value
                        Float.parseFloat(String.valueOf(Homes.homes.getDouble(uuid + "." + home + ".yaw"))),
                        Float.parseFloat(String.valueOf(Homes.homes.getDouble(uuid + "." + home + ".pitch"))));
                home = Homes.homes.getString(uuid + "." + home + ".name") != null ? Homes.homes.getString(uuid + "." + home + ".name") : home;
                homes.put(home,location);
            }
        } catch (NullPointerException ex) {
            Homes.homes.createSection(uuid);
        }

        return homes;
    }

    public static void delHome(OfflinePlayer player, String homename) throws IOException {
        homes.set(player.getUniqueId().toString()+"."+homename,null);
        save();
    }

    public static void reloadHomes() throws IOException {
        if (homesFile == null) {
            homesFile = new File(CoreClass.getInstance().getDataFolder(), "homes.yml");
        }
        homes = YamlConfiguration.loadConfiguration(homesFile);
        save();
    }
}
