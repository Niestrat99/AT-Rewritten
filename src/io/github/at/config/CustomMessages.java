package io.github.at.config;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class CustomMessages {

    public static File ConfigFile = new File("plugins/AdvancedTeleport","custom-messages.yml");
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
    //    Config.addDefault("Error.requestSendFail", "&cCould not send request to &e{player}!");
        Config.addDefault("Error.tpOff", "&e{player} &chas their teleportation disabled!");
        Config.addDefault("Error.tpBlock", "&c{player} has blocked you from sending requests to them!");
        Config.addDefault("Error.alreadyOn", "&cYour teleport requests are already enabled!");
        Config.addDefault("Error.alreadyOff", "&cYour teleport requests are already disabled!");
        Config.addDefault("Error.alreadyBlocked", "&cThis player is already blocked!");
        Config.addDefault("Error.neverBlocked", "&cThis player was never blocked!");
        Config.addDefault("Error.onCooldown", "&cThis command has a cooldown of {time} seconds each use - Please wait!");
        Config.addDefault("Error.requestSentToSelf", "&cYou can't send a request to yourself!");
        Config.addDefault("Error.noSuchPlayer", "&cThe player is either currently offline or doesn't exist!");
        Config.addDefault("Error.alreadySentRequest", "&cYou've already sent a request to {player}!");
        Config.addDefault("Error.notEnoughEXP", "&cYou do not have enough EXP Levels to send a teleport request to someone else!" +
                "\n&cYou need at least &e{levels} &cEXP levels!");
        Config.addDefault("Error.notEnoughMoney", "&cYou do not have enough money to send a teleport request to someone else!" +
                "\n&cYou need at least &e${amount}&c!");
        Config.addDefault("Error.requestExpired", "&cYour teleport request to &e{player} &chas expired!");
        Config.addDefault("Error.noPlayerInput", "&cYou must include a player name!");
        Config.addDefault("Error.blockSelf", "&cYou can't block yourself!");
        Config.addDefault("Error.noRequestsFromPlayer", "&cYou don't have any pending requests from &a{player}&e!");
        Config.addDefault("Error.noRequests", "&cYou don't have any pending requests!");
        Config.addDefault("Info.tpOff", "&aSuccessfully disabled teleport requests!");
        Config.addDefault("Info.tpOn", "&aSuccessfully enabled teleport requests!");
        Config.addDefault("Info.requestSent", "&aSuccessfully sent request to &e{player}&a!" +
                "\n&aThey've got &e{lifetime} &ato respond!" +
                "\n&aTo cancel the request use &e/tpcancel &ato cancel it.");
        Config.addDefault("Info.tpaRequestReceived", "&aThe player &e{player} &awants to teleport to you!" +
                "\n&aIf you want to accept it, use &e/tpayes&a, but if not, use &e/tpano&a." +
                "\n&aYou've got &e{lifetime} &ato respond to it!");
        Config.addDefault("Info.blockPlayer", "&e{player} &ahas been blocked.");
        Config.addDefault("Info.tpCancel", "&aYou have cancelled your teleport request.");
        Config.addDefault("Info.tpCancelResponder", "&e{player} &ahas cancelled their teleport request.");

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
