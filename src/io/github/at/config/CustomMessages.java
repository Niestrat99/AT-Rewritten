package io.github.at.config;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class CustomMessages {

    public static File ConfigFile = new File("plugins/AdvancedTeleport","CustomMessages.yml");
    public static FileConfiguration Config = YamlConfiguration.loadConfiguration(ConfigFile);

    public static void save() throws IOException {
        Config.save(ConfigFile);
    }

    public static void setDefaults() throws IOException {
        Config.addDefault("Teleport.eventBeforeTP" , "&aTeleporting in &b{countdown} seconds&a, please do not move!");
        Config.addDefault("Teleport.eventTeleport" , "&aTeleporting...");
        Config.addDefault("Teleport.eventMovement" , "&cTeleport has been cancelled due to movement.");
        Config.addDefault("Error.noPermission", "&c&lERROR: &cYou do not have permissions to use this command!");
        Config.options().copyDefaults(true);
        save();
    }
    public static String getString(String path) {
        String str = Config.getString(path);
        if (str == null) return "";
        str = str.replaceAll("''", "'");
        str = str.replaceAll("^'", "");
        str = str.replaceAll("'$", "");
        str = ChatColor.translateAlternateColorCodes('&', str);
        return str;
    }
}
