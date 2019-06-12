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
        Config.addDefault("Error.noPermission", "&cYou do not have permissions to use this command!");
        Config.addDefault("Error.featureDisabled", "&cThis feature has been disabled!");
        Config.addDefault("Error.noRequests", "&cYou do not have any pending requests!");
        Config.addDefault("Error.requestSendFail", "&cCould not send Request to &e{player}!");
        Config.addDefault("Error.tpOff", "&e{player} &chas their Teleportations disabled!");
        Config.addDefault("Error.tpBlock", "&cThis player has blocked you from sending requests to them!");
        Config.addDefault("Error.alreadyOn", "&cYour teleport requests are already enabled!");
        Config.addDefault("Error.alreadyOff", "&cYour teleport requests are already disabled!");
        Config.addDefault("Error.alreadyBlocked", "&cThis player is already blocked!");
        Config.addDefault("Error.neverBlocked", "&cThis player was never blocked!");
        Config.addDefault("Info.tpOff", "&aSuccessfully disabled teleport requests!");
        Config.addDefault("Info.tpOn", "&aSuccessfully enabled teleport requests!");

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
