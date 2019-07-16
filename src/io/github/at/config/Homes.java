package io.github.at.config;

import io.github.at.main.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class Homes {

    public static File HomesFile = new File(Main.getInstance().getDataFolder(),"homes.yml");
    public static FileConfiguration homes = YamlConfiguration.loadConfiguration(HomesFile);

    public static void save() throws IOException {
        homes.save(HomesFile);
    }

    public static void setHome(Player player, String homename, Location location) throws IOException {
        homes.set(player.getUniqueId().toString() + "." + homename + ".x", location.getBlockX());
        homes.set(player.getUniqueId().toString() + "." + homename + ".y", location.getBlockY());
        homes.set(player.getUniqueId().toString() + "." + homename + ".z", location.getBlockZ());
        homes.set(player.getUniqueId().toString() + "." + homename + ".world", location.getWorld().getName());
        save();
    }

    public static HashMap<String,Location> getHomes(Player player){
        HashMap<String,Location> homes = new HashMap<>();
        try {
            for (String home: Homes.homes.getConfigurationSection(player.getUniqueId().toString()).getKeys(false)) {
                Location location = new Location(Bukkit.getWorld(Homes.homes.getString(player.getUniqueId().toString() + "." + home + ".world")), // Gets world from name
                        Homes.homes.getInt(player.getUniqueId().toString() + "." + home + ".x"), // Gets X value
                        Homes.homes.getInt(player.getUniqueId().toString() + "." + home + ".y"), // Gets Y value
                        Homes.homes.getInt(player.getUniqueId().toString() + "." + home + ".z")); // Gets Z value
                homes.put(home,location);
            }
        } catch (NullPointerException ex) {
            Homes.homes.createSection(player.getUniqueId().toString());
        }

        return homes;
    }

    public static void delHome(Player player, String homename) throws IOException {
        homes.set(player.getUniqueId().toString()+"."+homename,null);
        save();
    }


}
