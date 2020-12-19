package io.github.niestrat99.advancedteleport.config;

import io.github.niestrat99.advancedteleport.CoreClass;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class CustomMessages {

    public static File ConfigFile = new File(CoreClass.getInstance().getDataFolder(),"custom-messages.yml");
    public static FileConfiguration Config = YamlConfiguration.loadConfiguration(ConfigFile);

    public static void save() throws IOException {
        Config.save(ConfigFile);
    }

    public static void setDefaults() throws IOException {
        Config.addDefault("Teleport.eventBeforeTP" , "&aTeleporting in &b{countdown} seconds&a, please do not move!");
        Config.addDefault("Teleport.eventBeforeTPMovementAllowed" , "&aTeleporting in &b{countdown} seconds&a!");
        Config.addDefault("Teleport.eventTeleport" , "&aTeleporting...");
        Config.addDefault("Teleport.eventMovement" , "&cTeleport has been cancelled due to movement.");
        Config.addDefault("Teleport.teleportingToSpawn", "&aTeleporting you to spawn!");
        Config.addDefault("Teleport.teleporting", "&aTeleporting to &e{player}&a!");
        Config.addDefault("Teleport.teleportingToHome", "&aTeleporting to &e{home}&a!");
        Config.addDefault("Teleport.teleportingToHomeOther", "&aTeleporting to &e{player}&a's home, &e{home}&a!");
        Config.addDefault("Teleport.teleportingToWarp", "&aTeleporting you to &e{warp}&a!");
        Config.addDefault("Teleport.teleportingPlayerToSelf", "&aTeleporting &e{player} &ato you!");
        Config.addDefault("Teleport.teleportingSelfToPlayer", "&aTeleporting you to &e{player}&a!");
        Config.addDefault("Teleport.teleportingToRandomPlace", "&aTeleporting you to a random place!");
        Config.addDefault("Teleport.teleportingToLastLoc", "&aTeleporting to your last location!");
        Config.addDefault("Error.noPermission", "&cYou do not have permission to use this command!");
        Config.addDefault("Error.noPermissionSign", "&cYou do not have permission to make this sign!");
        Config.addDefault("Error.featureDisabled", "&cThis feature has been disabled!");
        Config.addDefault("Error.noRequests", "&cYou do not have any pending requests!");
    //    Config.addDefault("Error.requestSendFail", "&cCould not send request to &e{player}!"); - NOT USED!!!
        Config.addDefault("Error.tpOff", "&e{player} &chas their teleportation disabled!");
        Config.addDefault("Error.tpBlock", "&c{player} has blocked you from sending requests to them!");
        Config.addDefault("Error.alreadyOn", "&cYour teleport requests are already enabled!");
        Config.addDefault("Error.alreadyOff", "&cYour teleport requests are already disabled!");
        Config.addDefault("Error.alreadyBlocked", "&cThis player is already blocked!");
        Config.addDefault("Error.neverBlocked", "&cThis player was never blocked!");
        Config.addDefault("Error.onCooldown", "&cPlease wait another {time} seconds to use this command!");
        Config.addDefault("Error.requestSentToSelf", "&cYou can't send a request to yourself!");
        Config.addDefault("Error.noSuchPlayer", "&cThe player is either currently offline or doesn't exist!");
        Config.addDefault("Error.alreadySentRequest", "&cYou've already sent a request to &e{player}&c!");
        Config.addDefault("Error.notEnoughEXP", "&cYou do not have enough EXP Levels to teleport there!" +
                "\n&cYou need at least &e{levels} &cEXP levels!");
        Config.addDefault("Error.notEnoughMoney", "&cYou do not have enough money to teleport there!" +
                "\n&cYou need at least &e${amount}&c!");
        Config.addDefault("Error.requestExpired", "&cYour teleport request to &e{player} &chas expired!");
        Config.addDefault("Error.noPlayerInput", "&cYou must include a player name!");
        Config.addDefault("Error.blockSelf", "&cYou can't block yourself!");
        Config.addDefault("Error.noRequestsFromPlayer", "&cYou don't have any pending requests from &a{player}&c!");
        Config.addDefault("Error.noRequests", "&cYou don't have any pending requests!");
        Config.addDefault("Error.invalidPageNo", "&cYou've inserted an invalid page number!");
        Config.addDefault("Error.noHomeInput", "&cYou have to include a home name!");
        Config.addDefault("Error.noSuchHome", "&cThis home doesn't exist!");
        Config.addDefault("Error.noBedHome", "&cYou don't have any bed spawn set!");
        Config.addDefault("Error.noBedHomeOther", "&a{player} &cdoesn't have a bed spawn set!");
        Config.addDefault("Error.reachedHomeLimit", "&cYou can't set any more homes!");
        Config.addDefault("Error.homeAlreadySet", "&cYou already have a home called &e{home}&c!");
        Config.addDefault("Error.noWarpInput", "&cYou have to include the warp's name!");
        Config.addDefault("Error.noSuchWarp", "&cThat warp doesn't exist!");
        Config.addDefault("Error.noSuchWorld", "&cThat world doesn't exist!");
        Config.addDefault("Error.noLocation", "&cYou don't have any location to teleport back to!");
        Config.addDefault("Error.notAPlayer", "&cYou must be a player to run this command!");
        Config.addDefault("Error.noHomes", "&cYou haven't got any homes!");
        Config.addDefault("Error.noHomesOtherPlayer", "&e{player} &chasn't got any homes!");
        Config.addDefault("Error.tooFarAway", "&cThe teleport destination is too far away so you can not teleport there!");
        Config.addDefault("Error.noRequestsSent", "&cCouldn't send a request to anyone :(");
        Config.addDefault("Error.onCountdown","&c&lERROR: &cCan't use command whilst waiting to teleport!");
        Config.addDefault("Error.noPermissionWarp", "&cYou can't warp to &e{warp}&c!");
        Config.addDefault("Error.cantTPToWorld", "&cYou can't use the RandomTP in that world!");
        Config.addDefault("Error.invalidName", "&cHomes and warps may only have letters and numbers in the names!");
        Config.addDefault("Error.cantTPToWorldLim", "&cYou can't teleport to &e{world}&c!");
        Config.addDefault("Error.tooFewArguments", "&cToo few arguments!");
        Config.addDefault("Error.invalidArgs", "&cInvalid arguments!");
        Config.addDefault("Error.cantTPToPlayer", "&cYou can't request a teleportation to &e{player}&c!");
        Config.addDefault("Error.noWarps", "&cThere are no warps as of currently!");

        Config.addDefault("Info.tpOff", "&aSuccessfully disabled teleport requests!");
        Config.addDefault("Info.tpOn", "&aSuccessfully enabled teleport requests!");
        Config.addDefault("Info.tpAdminOff", "&aSuccessfully disabled teleport requests for &e{player}&a!");
        Config.addDefault("Info.tpAdminOn", "&aSuccessfully enabled teleport requests for &e{player}&a!");
        Config.addDefault("Info.requestSent", "&aSuccessfully sent request to &e{player}&a!" +
                "\n&aThey've got &e{lifetime} &ato respond!" +
                "\n&aTo cancel the request use &e/tpcancel &ato cancel it.");
        Config.addDefault("Info.tpaRequestReceived", "&aThe player &e{player} &awants to teleport to you!" +
                "\n&aIf you want to accept it, use &e/tpayes&a, but if not, use &e/tpano&a." +
                "\n&aYou've got &e{lifetime} &ato respond to it!");
        Config.addDefault("Info.tpaRequestHere", "&aThe player &e{player} &awants to teleport you to them!" +
                "\n&aIf you want to accept it, use &e/tpayes&a, but if not, use &e/tpano&a." +
                "\n&aYou've got &e{lifetime} &ato respond to it!");
        Config.addDefault("Info.blockPlayer", "&e{player} &ahas been blocked.");
        Config.addDefault("Info.tpCancel", "&aYou have cancelled your teleport request.");
        Config.addDefault("Info.tpCancelResponder", "&e{player} &ahas cancelled their teleport request.");
        Config.addDefault("Info.multipleRequestsCancel", "&aYou have multiple teleport requests pending! Click one of the following to cancel:");
        Config.addDefault("Info.multipleRequestsIndex", "&b> {player}");
        Config.addDefault("Info.multipleRequestsList", "&aDo /tpalist <Page Number> To check other requests.");
        Config.addDefault("Info.multipleRequestAccept", "&aYou have multiple teleport requests pending! Click one of the following to accept:");
        Config.addDefault("Info.multipleRequestDeny", "&aYou have multiple teleport requests pending! Click one of the following to deny:");
        Config.addDefault("Info.requestDeclined", "&aYou've declined the teleport request!");
        Config.addDefault("Info.requestDeclinedResponder", "&e{player} &ahas declined your teleport request!");

        Config.addDefault("Info.deletedHome", "&aSuccessfully deleted the home &e{home}&a!");
        Config.addDefault("Info.deletedHomeOther", "&aSuccessfully deleted the home &e{home} &afor &e{player}&a!");
        Config.addDefault("Info.setHome", "&aSuccessfully set the home &e{home}&a!");
        Config.addDefault("Info.setHomeOther", "&aSuccessfully set the home &e{home} &afor &e{player}&a!");
        Config.addDefault("Info.setSpawn", "&aSuccessfully set the spawnpoint!");
        Config.addDefault("Info.setWarp", "&aSuccessfully set the warp &e{warp}&a!");
        Config.addDefault("Info.deletedWarp", "&aSuccessfully deleted the warp &e{warp}&a!");
        Config.addDefault("Info.searching", "&aSearching for a location...");
        Config.addDefault("Info.unblockPlayer", "&aSuccessfully unblocked &e{player}&a!");
        Config.addDefault("Info.reloadingConfig", "&aReloading &bAdvancedTeleport&a's config...");
        Config.addDefault("Info.reloadedConfig", "&aFinished reloading the config!");
        Config.addDefault("Info.warps", "&b&lWarps: &r");
        Config.addDefault("Info.homes", "&b&lHomes: &r");
        Config.addDefault("Info.homesOther", "&b&l{player}'s homes: &r");
        Config.addDefault("Info.requestAccepted", "&aYou've accepted the teleport request!");
        Config.addDefault("Info.requestAcceptedResponder", "&e{player} &ahas accepted the teleport request!");
        Config.addDefault("Info.paymentVault", "&aYou have paid &e${amount} &aand now have &e${balance}&a!");
        Config.addDefault("Info.paymentEXP", "&aYou have paid &e{amount} EXP Levels &aand now have &e{levels}&a!");
        Config.addDefault("Info.paymentPoints", "&aYou have paid &e{amount} EXP Points &aand now have &e{points}&a!");
        Config.addDefault("Info.createdWarpSign", "&aSuccessfully created the warp sign!");
        Config.addDefault("Info.createdRTPSign", "&aSuccessfully created the RandomTP sign!");
        Config.addDefault("Info.createdSpawnSign", "&aSuccessfully created the spawn sign!");
        Config.addDefault("Info.tpallRequestSent", "&aSuccessfully sent a teleport request to &e{amount} &aplayer(s)!");
        Config.addDefault("Info.teleportedToLoc", "&aSuccessfully teleported you to &e{x}&a, &e{y}&a, &e{z}&a! (Yaw: &e{yaw}&a, Pitch: &e{pitch}&a, World: &e{world}&a)");
        Config.addDefault("Info.teleportedToLocOther", "&aSuccessfully teleported &e{player} &ato &e{x}&a, &e{y}&a, &e{z}&a! (Yaw: &e{yaw}&a, Pitch: &e{pitch}&a, World: &e{world}&a)");
        Config.addDefault("Tooltip.homes", "&aTeleports you to your home: &e{home}");
        Config.addDefault("Tooltip.warps", "&aTeleports you to warp: &e{warp}");
        Config.addDefault("Tooltip.location", "" +
                "\n&bX &8» &7{x}" +
                "\n&bY &8» &7{y}" +
                "\n&bZ &8» &7{z}" +
                "\n&bWorld &8» &7{world}");
        Config.addDefault("Help.mainHelp", new ArrayList<>(Arrays.asList("&b&lAdvancedTeleport Help",
                "&6Please type &b/athelp <category> &6to get a list of commands about this category.",
                "&6--[ &bCategories &6]--",
                "&6- Teleport",
                "&6- Warps",
                "&6- Spawn",
                "&6- RandomTP",
                "&6- HomesCommand")));
        Config.addDefault("Help.mainHelpAdmin", new ArrayList<>(Collections.singletonList("&6- Admin")));
        Config.addDefault("Help.teleport", new ArrayList<>(Arrays.asList("&b&lTeleport help",
                "&6- /tpa <player> - Sends a request to teleport to the player.",
                "&6- /tpahere <player> - Sends a request to the player to teleport to you",
                "&6- /tpaccept - Accepts a player's teleport request.",
                "&6- /tpdeny - Declines a player's teleport request.",
                "&6- /tpcancel - Lets you cancel the request you have sent to a player.",
                "&6- /toggletp - Lets you switch between /tpon and /tpoff.",
                "&6- /tpon - Enables teleport requests to you.",
                "&6- /tpoff - Disables teleport requests to you.",
                "&6- /tpblock <player> - Blocks the player so that they cannot send you teleport requests anymore.",
                "&6- /tpunblock <player> - Unblocks the player so that they can send you teleport requests.",
                "&6- /back - Teleports you to your last location.",
                "&6- /tpalist - Lists your teleport requests.")));
        Config.addDefault("Help.teleportAdmin", new ArrayList<>(Arrays.asList("&6- /tpo <player> - Instantly teleports you to the player.",
                "&6- /tpohere <player> - Instantly teleports the player to you.",
                "&6- /tpall - Sends a teleport request to every online player to teleport to you.",
                "&6- /tploc <x|~> <y|~> <z|~> [Yaw|~] [Pitch|~] [World|~] [Player] [precise|noflight] - Teleports you or another player to a specified location.")));
        Config.addDefault("Help.warps", new ArrayList<>(Arrays.asList("&b&lWarps help",
                "&6- /warp <warp name> - Teleports you to an existing warp point.",
                "&6- /warps - Gives you a list of warps.")));
        Config.addDefault("Help.warpsAdmin", new ArrayList<>(Arrays.asList("&6- /warp set <warp name> - Sets a warp point at your location.",
                "&6- /warp delete <warp name> - Deletes an existing warp point.")));
        Config.addDefault("Help.spawn", new ArrayList<>(Arrays.asList("&b&lSpawn help",
                "- /spawn - Teleports you to the spawn point.")));
        Config.addDefault("Help.spawnAdmin", new ArrayList<>(Collections.singletonList("&6- /setspawn - Sets a spawn point at your location.")));
        Config.addDefault("Help.randomTP", new ArrayList<>(Arrays.asList("&b&lRandomTP help",
                "&6- /rtp - Teleports you to a random location.")));
        Config.addDefault("Help.homes", new ArrayList<>(Arrays.asList("&b&lHomes help",
                "&6- /sethome <home name> - Sets a home point at your location.",
                "&6- /delhome <home name> - Deletes a home point you've set.",
                "&6- /home <home name> - Teleports you to your home.",
                "&6- /homes - Gives you a list of homes you've set.")));
        Config.addDefault("Help.homesAdmin", new ArrayList<>(Arrays.asList("&6- /sethome <player> <home name> - Sets a home point at your location for the player.",
                "&6- /delhome <player> <home name> - Deletes a home point of a player.",
                "&6- /home <player> <home name> - Teleports you to a home point a player has set.",
                "&6- /homes <player> - Gives you a list of homes of a player.")));
        Config.addDefault("Help.admin", new ArrayList<>(Arrays.asList("&b&lAdmin help",
                "&6- /atinfo - Shows informations about this plugin.",
                "&6- /atreload - Reloads all configuration files of this plugin.")));
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

    public static void reloadConfig() throws IOException {
        if (ConfigFile == null) {
            ConfigFile = new File(CoreClass.getInstance().getDataFolder(), "custom-messages.yml");
        }
        Config = YamlConfiguration.loadConfiguration(ConfigFile);
        setDefaults();
        save();
    }

    public static String getEventBeforeTPMessage() {
        if(NewConfig.getInstance().CANCEL_WARM_UP_ON_MOVEMENT.get() || NewConfig.getInstance().CANCEL_WARM_UP_ON_ROTATION.get()) {
            return getString("Teleport.eventBeforeTP");
        } else {
            return getString("Teleport.eventBeforeTPMovementAllowed");
        }
    }
}
