package io.github.at.config;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Config {

    public static File ConfigFile = new File("plugins/AdvancedTeleport","Config.yml");
    public static FileConfiguration Config = YamlConfiguration.loadConfiguration(ConfigFile);

    public static void save() throws IOException {
        Config.save(ConfigFile);
    }

    public static void setDefaults() throws IOException {
        Config.addDefault("features.teleport", true);
        Config.addDefault("features.warps", true);
        Config.addDefault("features.spawn", true);
        Config.addDefault("features.randomTP", true);
        Config.addDefault("features.homes",true);
        Config.addDefault("timers.commandCooldown",5);
        Config.addDefault("timers.teleportTimer",3);
        Config.addDefault("timers.requestLifetime",60);
        Config.addDefault("timers.cancel-on-rotate", false);
        Config.addDefault("booleans.useVault" , false);
        Config.addDefault("booleans.EXPPayment" , false);
        Config.addDefault("payments.vault.teleportPrice" , 100.00);
        Config.addDefault("payments.vault.vaultTPRCost" , 200);
        Config.addDefault("payments.exp.EXPTeleportPrice" , 2);
        Config.addDefault("payments.exp.EXPTPRCost" , 4);
        Config.addDefault("tpr.maximum-x", 10000);
        Config.addDefault("tpr.minimum-x", -10000);
        Config.addDefault("tpr.maximum-z", 10000);
        Config.addDefault("tpr.minimum-z", -10000);
        Config.addDefault("tpr.useWorldBorder", true);
        Config.addDefault("tpr.avoidBlocks", new ArrayList<>(Arrays.asList("WATER","LAVA", "STATIONARY_WATER", "STATIONARY_LAVA")));
        Config.options().copyDefaults(true);
        save();
    }
    public static int commandCooldown(){
        return Config.getInt("timers.commandCooldown");
    }
    public static int teleportTimer(){
        return Config.getInt("timers.teleportTimer");
    }
    public static int requestLifetime(){
        return Config.getInt("timers.requestLifetime");
    }
    public static boolean useVault() {return Config.getBoolean("booleans.useVault");}
    public static double teleportPrice() {return Config.getDouble("payments.vault.teleportPrice");}
    public static boolean EXPPayment() {return Config.getBoolean("booleans.EXPPayment");}
    public static int EXPTeleportPrice() {return Config.getInt("payments.exp.EXPTeleportPrice");}
    public static int vaultTPRCost() {return Config.getInt("payments.vault.vaultTPRCost");}
    public static int EXPTPRCost() {return Config.getInt("payments.exp.EXPTPRCost");}
    public static boolean useWorldBorder() {return Config.getBoolean("tpr.useWorldBorder");}
    public static int maxX() {return Config.getInt("tpr.maximum-x");}
    public static int minX() {return Config.getInt("tpr.minimum-x");}
    public static int maxZ() {return Config.getInt("tpr.maximum-z");}
    public static int minZ() {return Config.getInt("tpr.minimum-z");}
    public static List<String> avoidBlocks() {return Config.getStringList("tpr.avoidBlocks");}
    public static boolean featTP() {return Config.getBoolean("features.teleport");}
    public static boolean featWarps() {return Config.getBoolean("features.warps");}
    public static boolean featSpawn() {return Config.getBoolean("features.spawn");}
    public static boolean featRTP() {return Config.getBoolean("features.randomTP");}
    public static boolean featHomes() {return Config.getBoolean("features.homes");}
    public static boolean cancelOnRotate() {return Config.getBoolean("timers.cancel-on-rotate");}

    public static void reloadConfig() throws IOException {
        Config = YamlConfiguration.loadConfiguration(ConfigFile);
        save();
    }
}
