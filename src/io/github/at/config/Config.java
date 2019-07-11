package io.github.at.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Config {

    public static File configFile = new File("plugins/AdvancedTeleport","Config.yml");
    public static FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

    public static void save() throws IOException {
        config.save(configFile);
    }

    public static void setDefaults() throws IOException {
        // Features
        config.addDefault("features.teleport", true);
        config.addDefault("features.warps", true);
        config.addDefault("features.spawn", true);
        config.addDefault("features.randomTP", true);
        config.addDefault("features.homes",true);
        // Timers
        config.addDefault("timers.commandCooldown",5);
        config.addDefault("timers.teleportTimer",3);
        config.addDefault("timers.requestLifetime",60);
        config.addDefault("timers.cancel-on-rotate", false);
        // Booleans
        config.addDefault("booleans.useVault" , false);
        config.addDefault("booleans.EXPPayment" , false);
        // Payments
        config.addDefault("payments.vault.teleportPrice" , 100.00);
        config.addDefault("payments.vault.vaultTPRCost" , 200);
        config.addDefault("payments.exp.EXPTeleportPrice" , 2);
        config.addDefault("payments.exp.EXPTPRCost" , 4);

        /* What I've done here is I've made it so that admins can modify the amount/exp paid per command, although by default
        *  it will get the values from the normal options.
        *  For example, ./home <Home name> may cost $200 whereas /tpa <Player name> costs 2 EXP levels.
        */

        config.addDefault("payments.");
        // TPR options
        config.addDefault("tpr.maximum-x", 10000);
        config.addDefault("tpr.minimum-x", -10000);
        config.addDefault("tpr.maximum-z", 10000);
        config.addDefault("tpr.minimum-z", -10000);
        config.addDefault("tpr.useWorldBorder", true);
        config.addDefault("tpr.avoidBlocks", new ArrayList<>(Arrays.asList("WATER","LAVA", "STATIONARY_WATER", "STATIONARY_LAVA")));
        config.options().copyDefaults(true);
        save();
    }
    public static int commandCooldown(){
        return config.getInt("timers.commandCooldown");
    }
    public static int teleportTimer(){
        return config.getInt("timers.teleportTimer");
    }
    public static int requestLifetime(){
        return config.getInt("timers.requestLifetime");
    }
    public static boolean useVault() {return config.getBoolean("booleans.useVault");}
    public static double teleportPrice() {return config.getDouble("payments.vault.teleportPrice");}
    public static boolean EXPPayment() {return config.getBoolean("booleans.EXPPayment");}
    public static int EXPTeleportPrice() {return config.getInt("payments.exp.EXPTeleportPrice");}
    public static int vaultTPRCost() {return config.getInt("payments.vault.vaultTPRCost");}
    public static int EXPTPRCost() {return config.getInt("payments.exp.EXPTPRCost");}
    public static boolean useWorldBorder() {return config.getBoolean("tpr.useWorldBorder");}
    public static int maxX() {return config.getInt("tpr.maximum-x");}
    public static int minX() {return config.getInt("tpr.minimum-x");}
    public static int maxZ() {return config.getInt("tpr.maximum-z");}
    public static int minZ() {return config.getInt("tpr.minimum-z");}
    public static List<String> avoidBlocks() {return config.getStringList("tpr.avoidBlocks");}
    public static boolean featTP() {return config.getBoolean("features.teleport");}
    public static boolean featWarps() {return config.getBoolean("features.warps");}
    public static boolean featSpawn() {return config.getBoolean("features.spawn");}
    public static boolean featRTP() {return config.getBoolean("features.randomTP");}
    public static boolean featHomes() {return config.getBoolean("features.homes");}
    public static boolean cancelOnRotate() {return config.getBoolean("timers.cancel-on-rotate");}

    public static void reloadConfig() throws IOException {
        config = YamlConfiguration.loadConfiguration(configFile);
        save();
    }
}
