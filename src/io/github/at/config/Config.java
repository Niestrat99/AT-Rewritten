package io.github.at.config;

import io.github.at.main.CoreClass;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Config {

    public static File configFile = new File(CoreClass.getInstance().getDataFolder(),"config.yml");
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
        config.addDefault("timers.teleportTimers.tpa", "default");
        config.addDefault("timers.teleportTimers.tpahere", "default");
        config.addDefault("timers.teleportTimers.tpr", "default");
        config.addDefault("timers.teleportTimers.warp", "default");
        config.addDefault("timers.teleportTimers.spawn", "default");
        config.addDefault("timers.teleportTimers.home", "default");
        config.addDefault("timers.teleportTimers.back", "default");
      
        config.addDefault("timers.requestLifetime",60);
        config.addDefault("timers.cancel-on-rotate", false);
        config.addDefault("timers.cancel-on-movement", true);
        // Booleans
        config.addDefault("booleans.useVault" , false);
        config.addDefault("booleans.EXPPayment" , false);
        //Sounds
        config.addDefault("sounds.tpa.requestSent", "none");
        config.addDefault("sounds.tpa.requestReceived", "none");
        // Payments
        config.addDefault("payments.vault.teleportPrice" , 100.00);
        config.addDefault("payments.exp.teleportPrice" , 2);

        /* What I've done here is I've made it so that admins can modify the amount/exp paid per command, although by default
        *  it will get the values from the normal options.
        *  For example, ./home <Home name> may cost $200 whereas /tpa <Player name> costs 2 EXP levels.
        */

        // TPA
        config.addDefault("payments.vault.tpa.price", "default");
        config.addDefault("payments.vault.tpa.enabled", "default");
        config.addDefault("payments.exp.tpa.price", "default");
        config.addDefault("payments.exp.tpa.enabled", "default");

        // TPAHere
        config.addDefault("payments.vault.tpahere.price", "default");
        config.addDefault("payments.vault.tpahere.enabled", "default");
        config.addDefault("payments.exp.tpahere.price", "default");
        config.addDefault("payments.exp.tpahere.enabled", "default");

        // TPR
        config.addDefault("payments.vault.tpr.price", "default");
        config.addDefault("payments.vault.tpr.enabled", "default");
        config.addDefault("payments.exp.tpr.price", "default");
        config.addDefault("payments.exp.tpr.enabled", "default");

        // Warp
        config.addDefault("payments.vault.warp.price", "default");
        config.addDefault("payments.vault.warp.enabled", "default");
        config.addDefault("payments.exp.warp.price", "default");
        config.addDefault("payments.exp.warp.enabled", "default");

        // Spawn
        config.addDefault("payments.vault.spawn.price", "default");
        config.addDefault("payments.vault.spawn.enabled", "default");
        config.addDefault("payments.exp.spawn.price", "default");
        config.addDefault("payments.exp.spawn.enabled", "default");

        // Home
        config.addDefault("payments.vault.home.price", "default");
        config.addDefault("payments.vault.home.enabled", "default");
        config.addDefault("payments.exp.home.price", "default");
        config.addDefault("payments.exp.home.enabled", "default");

        // Back
        config.addDefault("payments.vault.back.price", "default");
        config.addDefault("payments.vault.back.enabled", "default");
        config.addDefault("payments.exp.back.price", "default");
        config.addDefault("payments.exp.back.enabled", "default");

        // TPR options
        config.addDefault("tpr.maximum-x", 10000);
        config.addDefault("tpr.minimum-x", -10000);
        config.addDefault("tpr.maximum-z", 10000);
        config.addDefault("tpr.minimum-z", -10000);
        config.addDefault("tpr.useWorldBorder", true);
        config.addDefault("tpr.avoidBlocks", new ArrayList<>(Arrays.asList("WATER","LAVA", "STATIONARY_WATER", "STATIONARY_LAVA")));
        config.addDefault("tpr.blacklist-worlds", new ArrayList<>());

        config.addDefault("distance-limiter.enabled", false);
        config.addDefault("distance-limiter.distance-limit", 1000);
        config.addDefault("distance-limiter.monitor-all-teleports", false);
        config.addDefault("distance-limiter.per-command.tpa", true);
        config.addDefault("distance-limiter.per-command.tpahere", true);
        config.addDefault("distance-limiter.per-command.tpr", false);
        config.addDefault("distance-limiter.per-command.warp", true);
        config.addDefault("distance-limiter.per-command.spawn", true);
        config.addDefault("distance-limiter.per-command.back", true);

        config.addDefault("teleport-limit.blacklisted-worlds", new ArrayList<>());
        config.addDefault("teleport-limit.enabled", false);
        config.addDefault("teleport-limit.monitor-all-teleports", false);
        config.addDefault("teleport-limit.per-command.tpa", true);
        config.addDefault("teleport-limit.per-command.tpahere", true);
        config.addDefault("teleport-limit.per-command.warp", true);
        config.addDefault("teleport-limit.per-command.spawn", true);
        config.addDefault("teleport-limit.per-command.back", true);

        config.addDefault("back.teleport-causes", new ArrayList<>(Arrays.asList("COMMAND", "PLUGIN", "SPECTATE")));

        config.options().copyDefaults(true);
        save();
    }
    public static int commandCooldown(){
        return config.getInt("timers.commandCooldown");
    }

    public static int getTeleportTimer(String command) {
        if (config.get("timers.teleportTimers." + command) instanceof String) {
            return config.getInt("timers.teleportTimer");
        } else {
            return config.getInt("timers.teleportTimers." + command);
        }
    }

    public static int requestLifetime(){
        return config.getInt("timers.requestLifetime");
    }

    /* Used to check if a specific command is using vault for payments.
     * e.g: Config.isUsingVault("home") - returns true if the home command is using payments through Vault.
     */
    public static boolean isUsingVault(String command) {
        if (config.get("payments.vault." + command + ".enabled") instanceof String) {
            return config.getBoolean("booleans.useVault");
        } else {
            return config.getBoolean("payments.vault." + command + ".enabled");
        }
    }

    /* Used to get the sound name that will be played for specific event.
     * e.g: Config.getSound("tpa.requestSent") - returns a string (e.g none, BLOCK_ANVIL_LAND) of the sound name that will be played to a player that sent a tpa request
     */
    public static String getSound(String event){ return config.getString("sounds." + event).toUpperCase(); }

    /* Used to get the amount that is paid for the specific command.
     * e.g: Config.getTeleportPrice("home") - returns a price (e.g $10) for how much the home command costs.
     */
    public static double getTeleportPrice(String command) {
        if (config.get("payments.vault." + command + ".price") instanceof String) {
            return config.getDouble("payments.vault.teleportPrice");
        } else {
            return config.getDouble("payments.vault." + command + ".price");
        }
    }

    /* Used to check if a specific command is using EXP for payments.
     * e.g: Config.isUsingEXPPayment("home") - returns true if the home command is using payments through experience.
     */
    public static boolean isUsingEXPPayment(String command) {
        if (config.get("payments.exp." + command + ".enabled") instanceof String) {
            return config.getBoolean("booleans.EXPPayment");
        } else {
            return config.getBoolean("payments.exp." + command + ".enabled");
        }
    }

    /* Used to get the levels that are paid for the specific command.
     * e.g: Config.getEXPTeleportPrice("home") - returns the level that is required to use the home command.
     */
    public static int getEXPTeleportPrice(String command) {
        if (config.get("payments.exp." + command + ".price") instanceof String) {
            return config.getInt("payments.exp.teleportPrice");
        } else {
            return config.getInt("payments.exp." + command + ".price");
        }
    }

    public static boolean useWorldBorder() {return config.getBoolean("tpr.useWorldBorder");}
    public static int maxX() {return config.getInt("tpr.maximum-x");}
    public static int minX() {return config.getInt("tpr.minimum-x");}
    public static int maxZ() {return config.getInt("tpr.maximum-z");}
    public static int minZ() {return config.getInt("tpr.minimum-z");}
    public static List<String> avoidBlocks() {return config.getStringList("tpr.avoidBlocks");}

    /* This method is used as a replacement for featTP, featWarps, etc. to make checking if the feature is enabled easier.
     * For example:
     * Config.isFeatureEnabled("teleport") - checks if teleports are enabled.
     * Config.isFeatureEnabled("warps") - checks if warps are enabled.
     * Config.isFeatureEnabled("randomTP") - checks if RTP is enabled.
     * Config.isFeatureEnabled("homes") - checks if homes are enabled.
     * Config.isFeatureEnabled("spawn") - checks if the spawn feature is enabled.
     */
    public static boolean isFeatureEnabled(String feature) { return config.getBoolean("features." + feature); }

    public static boolean cancelOnRotate() {return config.getBoolean("timers.cancel-on-rotate");}

    public static boolean cancelOnMovement() {return config.getBoolean("timers.cancel-on-movement");}

    public static void reloadConfig() throws IOException {
        if (configFile == null) {
            configFile = new File(CoreClass.getInstance().getDataFolder(), "config.yml");
        }
        config = YamlConfiguration.loadConfiguration(configFile);
        setDefaults();
        save();
    }

    public static boolean isDistanceLimiterEnabled() {
        return config.getBoolean("distance-limiter.enabled");
    }

    public static boolean isDistanceLimiterEnabledForCmd(String command) {
        return config.getBoolean("distance-limiter.per-command." + command);
    }

    public static double getDistanceLimit() {
        return config.getDouble("distance-limiter.distance-limit");
    }

    public static boolean isTeleportLimiterEnabled() {
        return config.getBoolean("teleport-limit.enabled");
    }

    public static boolean isTeleportLimiterEnabledForCmd(String command) {
        return config.getBoolean("teleport-limit.per-command." + command);
    }

    public static boolean hasStrictTeleportLimiter() {
        return config.getBoolean("teleport-limit.monitor-all-teleports");
    }

    public static boolean hasStrictDistanceMonitor() {
        return config.getBoolean("distance-limiter.monitor-all-teleports");
    }

    public static boolean isCauseAllowed(PlayerTeleportEvent.TeleportCause cause) {
        return config.getStringList("back.teleport-causes").contains(cause.name());
    }

    public static List<String> getBlacklistedWorlds() {
        return config.getStringList("tpr.blacklist-worlds");
    }
}
