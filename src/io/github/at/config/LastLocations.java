package io.github.at.config;

import io.github.at.events.TeleportTrackingManager;
import io.github.at.main.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

public class LastLocations {
    public static File configFile = new File(Main.getInstance().getDataFolder(),"last-locations.yml");
    public static FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

    public static void save() throws IOException {
        config.save(configFile);
    }

    public static void saveLocations() {
        for (Player player : TeleportTrackingManager.getLastLocations().keySet()) {
            Location loc = TeleportTrackingManager.getLastLocation(player);
            // Format: player-uuid: x.y.z.yaw.pitch.world
            config.addDefault(player.getUniqueId().toString(),
                    + loc.getX() + ":"
                    + loc.getY() + ":"
                    + loc.getZ() + ":"
                    + loc.getYaw() + ":"
                    + loc.getPitch() + ":"
                    + loc.getWorld().getName());
        }
        config.options().copyDefaults(true);
        try {
            save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Location getLocation(Player player) {
        try {
            String[] loc = config.getString(player.getUniqueId().toString()).split(":");
            return new Location(Bukkit.getWorld(loc[5]), Double.valueOf(loc[0]), Double.valueOf(loc[1]), Double.valueOf(loc[2]), Float.valueOf(loc[3]), Float.valueOf(loc[4]));
        } catch (Exception e) {
            return null;
        }

    }
}
