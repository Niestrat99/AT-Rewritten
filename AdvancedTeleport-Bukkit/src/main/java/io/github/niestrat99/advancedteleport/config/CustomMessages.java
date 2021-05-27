package io.github.niestrat99.advancedteleport.config;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.fanciful.FancyMessage;
import io.github.thatsmusic99.configurationmaster.CMFile;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class CustomMessages extends CMFile {

    public static CustomMessages config;
    private static HashMap<CommandSender, BukkitRunnable> titleManager;

    public CustomMessages(Plugin plugin) {
        super(plugin, "custom-messages");
        setDescription(null);
        config = this;
        titleManager = new HashMap<>();
    }

    public void loadDefaults() {
        addLenientSection("Teleport");
        addDefault("Teleport.eventBeforeTP" , "&b↑ &8» &7Teleporting in &b{countdown} seconds&7, please do not move!");

        addComment("Teleport.eventBeforeTP_title", "This is an example use for titles and subtitles in the plugin." +
                "\nThis feature is supported only if you're on version 1.8.8 or newer.");
        addExample("Teleport.eventBeforeTP_title.length" , 80, "How many seconds (in ticks) the header should last. This is not including fading.");
        addExample("Teleport.eventBeforeTP_title.fade-in" , 0, "How many seconds (in ticks) the header should take to fade in.");
        addExample("Teleport.eventBeforeTP_title.fade-out" , 10, "How many seconds (in ticks) the header should take to fade out.");
        addExample("Teleport.eventBeforeTP_title.0", "&7&lTeleporting...");
        addExample("Teleport.eventBeforeTP_title.20", "&b> &7&lTeleporting... &b<");
        addExample("Teleport.eventBeforeTP_title.40", "&b>> &7&lTeleporting... &b<<");
        addExample("Teleport.eventBeforeTP_title.60", "&b>>> &e&lTeleported! &b<<<");
        addExample("Teleport.eventBeforeTP_subtitle.0", "&bPlease do not move!");
        addExample("Teleport.eventBeforeTP_subtitle.60", "");

        addDefault("Teleport.eventBeforeTPMovementAllowed" , "&b↑ &8» &7Teleporting in &b{countdown} seconds&7!");
        addDefault("Teleport.eventTeleport" , "&b↑ &8» &7Teleporting...");
        addDefault("Teleport.eventMovement" , "&b↑ &8» &7Teleport has been cancelled due to movement.");
        addDefault("Teleport.eventMovement_title.length", 60);
        addDefault("Teleport.eventMovement_title.fade-in", 0);
        addDefault("Teleport.eventMovement_title.fade-out", 10);
        addDefault("Teleport.eventMovement_title.0", "&e&l! &c&lCancelled &e&l!");
        addDefault("Teleport.teleportingToSpawn", "&b↑ &8» &7Teleporting you to spawn!");
        addDefault("Teleport.teleporting", "&b↑ &8» &7Teleporting to &b{player}&7!");
        addDefault("Teleport.teleportingToHome", "&b↑ &8» &7Teleporting to &b{home}&7!");
        addDefault("Teleport.teleportingToHomeOther", "&b↑ &8» &7Teleporting to &b{player}&7's home, &b{home}&7!");
        addDefault("Teleport.teleportingToWarp", "&b↑ &8» &7Teleporting you to &b{warp}&7!");
        addDefault("Teleport.teleportingPlayerToSelf", "&b↑ &8» &7Teleporting &b{player} &7to you!");
        addDefault("Teleport.teleportingSelfToPlayer", "&b↑ &8» &7Teleporting you to &b{player}&7!");
        addDefault("Teleport.teleportingToRandomPlace", "&b↑ &8» &7Teleporting you to a random place!");
        addDefault("Teleport.teleportingToLastLoc", "&b↑ &8» &7Teleporting to your last location!");
        addDefault("Teleport.teleportedToOfflinePlayer", "&b↑ &8» &7Teleported to offline player &b{player}&7!");
        addDefault("Teleport.teleportedOfflinePlayerHere", "&b↑ &8» &7Teleported offline player &b{player} &7to your location!");

        addLenientSection("Error");
        addDefault("Error.noPermission", "&b↑ &8» &7You do not have permission to use this command!");
        addDefault("Error.noPermissionSign", "&b↑ &8» &7You do not have permission to make this sign!");
        addDefault("Error.featureDisabled", "&b↑ &8» &7This feature has been disabled!");
        addDefault("Error.noRequests", "&b↑ &8» &7You do not have any pending requests!");
    //    Config.addDefault("Error.requestSendFail", "&cCould not send request to &e{player}!"); - NOT USED!!!
        addDefault("Error.tpOff", "&b↑ &8» &b{player} &7has their teleportation disabled!");
        addDefault("Error.tpBlock", "&b↑ &8» &b{player} &7has blocked you from sending requests to them!");
        addDefault("Error.alreadyOn", "&b↑ &8» &7Your teleport requests are already enabled!");
        addDefault("Error.alreadyOff", "&b↑ &8» &7Your teleport requests are already disabled!");
        addDefault("Error.alreadyBlocked", "&b↑ &8» &7This player is already blocked!");
        addDefault("Error.neverBlocked", "&b↑ &8» &7This player was never blocked!");
        addDefault("Error.onCooldown", "&b↑ &8» &7Please wait another &b{time} &7seconds to use this command!");
        addDefault("Error.requestSentToSelf", "&b↑ &8» &7You can't send a request to yourself!");
        addDefault("Error.noSuchPlayer", "&b↑ &8» &7The player is either currently offline or doesn't exist!");
        addDefault("Error.alreadySentRequest", "&b↑ &8» &7You've already sent a request to &7{player}&b!");
        addDefault("Error.notEnoughEXP", "&b↑ &8» &7You do not have enough EXP Levels to teleport there!" +
                "\n&b↑ &8» &7You need at least &b{levels} &7EXP levels!");
        addDefault("Error.notEnoughEXPPoints", "&b↑ &8» &7You do not have enough EXP Points to teleport there!" +
                "\n&b↑ &8» &7You need at least &b{points} &7EXP points!");
        addDefault("Error.notEnoughMoney", "&b↑ &8» &7You do not have enough money to teleport there!" +
                "\n&b↑ &8» &7You need at least &b${amount}&7!");
        addDefault("Error.requestExpired", "&b↑ &8» &7Your teleport request to &b{player} &7has expired!");
        addDefault("Error.noPlayerInput", "&b↑ &8» &7You must include a player name!");
        addDefault("Error.blockSelf", "&b↑ &8» &7You can't block yourself!");
        addDefault("Error.noRequestsFromPlayer", "&b↑ &8» &7You don't have any pending requests from &b{player}&7!");
        addDefault("Error.noRequests", "&b↑ &8» &7You don't have any pending requests!");
        addDefault("Error.invalidPageNo", "&b↑ &8» &7You've inserted an invalid page number!");
        addDefault("Error.noHomeInput", "&b↑ &8» &7You have to include a home name!");
        addDefault("Error.noSuchHome", "&b↑ &8» &7This home doesn't exist!");
        addDefault("Error.noBedHome", "&b↑ &8» &7You don't have any bed spawn set!");
        addDefault("Error.noBedHomeOther", "&b↑ &8» &b{player} &7doesn't have a bed spawn set!");
        addDefault("Error.reachedHomeLimit", "&b↑ &8» &7You can't set any more homes!");
        addDefault("Error.homeAlreadySet", "&b↑ &8» &7You already have a home called &b{home}&7!");
        addDefault("Error.noWarpInput", "&b↑ &8» &7You have to include the warp's name!");
        addDefault("Error.noSuchWarp", "&b↑ &8» &7That warp doesn't exist!");
        addDefault("Error.warpAlreadySet", "&b↑ &8» &7There is already a warp called &b{warp}&7!");
        addDefault("Error.noSuchWorld", "&b↑ &8» &7That world doesn't exist!");
        addDefault("Error.noLocation", "&b↑ &8» &7You don't have any location to teleport back to!");
        addDefault("Error.notAPlayer", "&b↑ &8» &7You must be a player to run this command!");
        addDefault("Error.noHomes", "&b↑ &8» &7You haven't got any homes!");
        addDefault("Error.noHomesOtherPlayer", "&b↑ &8» &b{player} &7hasn't got any homes!");
        addDefault("Error.tooFarAway", "&b↑ &8» &7The teleport destination is too far away so you can not teleport there!");
        addDefault("Error.noRequestsSent", "&b↑ &8» &7Couldn't send a request to anyone :(");
        addDefault("Error.onCountdown","&b↑ &8» &7You can't use this command whilst waiting to teleport!");
        addDefault("Error.noPermissionWarp", "&b↑ &8» &7You can't warp to &b{warp}&7!");
        addDefault("Error.cantTPToWorld", "&b↑ &8» &7You can't randomly teleport in that world!");
       // config.addDefault("Error.invalidName", "&cHomes and warps may only have letters and numbers in the names!");
        addDefault("Error.cantTPToWorldLim", "&b↑ &8» &7You can't teleport to &b{world}&7!");
        addDefault("Error.tooFewArguments", "&b↑ &8» &7Too few arguments!");
        addDefault("Error.invalidArgs", "&b↑ &8» &7Invalid arguments!");
        addDefault("Error.cantTPToPlayer", "&b↑ &8» &7You can't request a teleportation to &b{player}&7!");
        addDefault("Error.noWarps", "&b↑ &8» &7There are no warps as of currently!");
        addDefault("Error.noAccessHome", "&b↑ &8» &7You cannot access &b{home}&7 as of currently!");
        addDefault("Error.moveHomeFail", "&b↑ &8» &7The home has been moved but the data has not been stored successfully. The plugin will try to fix this itself.");
        addDefault("Error.setMainHomeFail", "&b↑ &8» &7The main home has been set but the data has not been stored successfully. The plugin will try to fix this itself.");
        addDefault("Error.deleteHomeFail", "&b↑ &8» &7The home has been deleted but the data has not been stored successfully. The plugin will try to fix this itself.");
        addDefault("Error.setHomeFail", "&b↑ &8» &7The home has been set but the data has not been stored successfully. The plugin will try to fix this itself.");
        addDefault("Error.deleteWarpFail", "&b↑ &8» &7The warp has been set but the data has not been stored successfully. The plugin will try to fix this itself.");
        addDefault("Error.homesNotLoaded", "&b↑ &8» &7Homes for this player haven't loaded yet, please wait a little bit (even just a second) before trying this again!");
        addDefault("Error.noOfflineLocation", "&b↑ &8» &7No offline location was found for &b{player}&7!");
        addDefault("Error.failedOfflineTeleport", "&b↑ &8» &7Failed to teleport to offline player &b{player}&7!");
        addDefault("Error.failedOfflineTeleportHere", "&b↑ &8» &7Failed to teleport offline player &b{player} &7 to your location!");
        addDefault("Error.alreadySearching", "&b↑ &8» &7Already searching for a location to teleport to!");
        addDefault("Error.mirrorSpawnNoArguments", "&b↑ &8» &7No worlds/spawn points have been specified!");
        addDefault("Error.mirrorSpawnLackOfArguments", "&b↑ &8» &7You must be a player to only specify one world - please specify a world and a spawnpoint to mirror players to!");
        addDefault("Error.noSuchSpawn", "&b↑ &8» &7There is no such spawn called &b{spawn}&7!");
        addDefault("Error.cannotSetMainSpawn", "&b↑ &8» &7You can only make existing spawnpoints into the main spawnpoint rather than create new ones!");
        addDefault("Error.cannotSetMainSpawnConsole", "&b↑ &8» &7You can only make existing spawnpoints into the main spawnpoint rather than create new ones since you are not a player!");
        addDefault("Error.nonAlphanumericSpawn", "&b↑ &8» &7Spawnpoints need to be alphanumeric!");
        addDefault("Error.removeSpawnNoArgs", "&b↑ &8» &7You have to specify a spawnpoint to remove!");

        addDefault("Info.tpOff", "&b↑ &8» &7Successfully disabled teleport requests!");
        addDefault("Info.tpOn", "&b↑ &8» &7Successfully enabled teleport requests!");
        addDefault("Info.tpAdminOff", "&b↑ &8» &7Successfully disabled teleport requests for &b{player}&7!");
        addDefault("Info.tpAdminOn", "&b↑ &8» &7Successfully enabled teleport requests for &b{player}&7!");
        addDefault("Info.requestSent", "&b↑ &8» &7Successfully sent request to &b{player}&7!" +
                "\n&b↑ &8» &7They've got &b{lifetime} &7to respond!" +
                "\n&7To cancel the request use &b/tpcancel &7to cancel it." +
                "\n" +
                "\n                                [&7&l[CANCEL]](/tpcancel {player})" +
                "\n&7");
        addDefault("Info.tpaRequestReceived", "&b↑ &8» &7The player &b{player} &7wants to teleport to you!" +
                "\n&b↑ &8» &7If you want to accept it, use &b/tpayes&7, but if not, use &b/tpano&7." +
                "\n&b↑ &8» &7You've got &b{lifetime} &7to respond to it!" +
                "\n" +
                "\n                   [&a&l[ACCEPT]](/tpayes {player}|&aClick here to accept the request.)              [&c&l[DENY]](/tpano {player}|&cClick here to deny the request.)" +
                "\n&7");
        addDefault("Info.tpaRequestHere", "&b↑ &8» &7The player &b{player} &7wants to teleport you to them!" +
                "\n&b↑ &8» &7If you want to accept it, use &b/tpayes&7, but if not, use &b/tpano&7." +
                "\n&b↑ &8» &7You've got &b{lifetime} seconds &7to respond to it!" +
                "\n" +
                "\n                   [&a&l[ACCEPT]](/tpayes {player}|&aClick here to accept the request.)              [&c&l[DENY]](/tpano {player}|&cClick here to deny the request.)" +
                "\n&7");
        addDefault("Info.blockPlayer", "&b↑ &8» &b{player} &7has been blocked.");
        addDefault("Info.tpCancel", "&b↑ &8» &7You have cancelled your teleport request.");
        addDefault("Info.tpCancelResponder", "&b↑ &8» &b{player} &7has cancelled their teleport request.");
        addDefault("Info.multipleRequestsCancel", "&b↑ &8» &7You have multiple teleport requests pending! Click one of the following to cancel:");
        addDefault("Info.multipleRequestsIndex", "&b> {player}");
        addDefault("Info.multipleRequestsList", "&b↑ &8» &7Do /tpalist <Page Number> To check other requests.");
        addDefault("Info.multipleRequestAccept", "&b↑ &8» &7You have multiple teleport requests pending! Click one of the following to accept:");
        addDefault("Info.multipleRequestDeny", "&b↑ &8» &7You have multiple teleport requests pending! Click one of the following to deny:");
        addDefault("Info.requestDeclined", "&b↑ &8» &7You've declined the teleport request!");
        addDefault("Info.requestDeclinedResponder", "&b↑ &8» &b{player} &7has declined your teleport request!");
        addDefault("Info.requestDisplaced", "&b↑ &8» &7Your request has been cancelled because &b{player} &7got another request!");

        addDefault("Info.deletedHome", "&b↑ &8» &7Successfully deleted the home &b{home}&7!");
        addDefault("Info.deletedHomeOther", "&b↑ &8» &7Successfully deleted the home &b{home} &7for &b{player}&7!");
        addDefault("Info.setHome", "&b↑ &8» &7Successfully set the home &b{home}&7!");
        addDefault("Info.setHomeOther", "&b↑ &8» &7Successfully set the home &b{home} &7for &b{player}&7!");
        addDefault("Info.setSpawn", "&b↑ &8» &7Successfully set the spawnpoint!");
        addDefault("Info.setWarp", "&b↑ &8» &7Successfully set the warp &b{warp}&7!");
        addDefault("Info.deletedWarp", "&b↑ &8» &7Successfully deleted the warp &b{warp}&7!");
        addDefault("Info.searching", "&b↑ &8» &7Searching for a location...");
        addDefault("Info.unblockPlayer", "&b↑ &8» &7Successfully unblocked &b{player}&7!");
        addDefault("Info.reloadingConfig", "&b↑ &8» &7Reloading &bAdvancedTeleport&7's config...");
        addDefault("Info.reloadedConfig", "&b↑ &8» &7Finished reloading the config!");
        addDefault("Info.warps", "&b&lWarps &8» &r");
        addDefault("Info.homes", "&b&lHomes &8» &r");
        addDefault("Info.homesOther", "&b&l{player}'s homes &8» &r");
        addDefault("Info.requestAccepted", "&b↑ &8» &7You've accepted the teleport request!");
        addDefault("Info.requestAcceptedResponder", "&b↑ &8» &b{player} &7has accepted the teleport request!");
        addDefault("Info.paymentVault", "&b↑ &8» &7You have paid &b${amount} &7and now have &b${balance}&7!");
        addDefault("Info.paymentEXP", "&b↑ &8» &7You have paid &b{amount} EXP Levels &7and now have &b{levels} &7levels!");
        addDefault("Info.paymentPoints", "&b↑ &8» &7You have paid &b{amount} EXP Points &7and now have &b{points} &7points!");
        addDefault("Info.createdWarpSign", "&b↑ &8» &7Successfully created the warp sign!");
        addDefault("Info.createdRTPSign", "&b↑ &8» &7Successfully created the RandomTP sign!");
        addDefault("Info.createdSpawnSign", "&b↑ &8» &7Successfully created the spawn sign!");
        addDefault("Info.tpallRequestSent", "&b↑ &8» &7Successfully sent a teleport request to &b{amount} &7player(s)!");
        addDefault("Info.teleportedToLoc", "&b↑ &8» &7Successfully teleported you to &b{x}&7, &b{y}&7, &b{z}&7! (Yaw: &b{yaw}&7, Pitch: &b{pitch}&7, World: &b{world}&7)");
        addDefault("Info.teleportedToLocOther", "&b↑ &8» &7Successfully teleported &b{player} &7to &b{x}&7, &b{y}&7, &b{z}&7! (Yaw: &b{yaw}&7, Pitch: &b{pitch}&7, World: &b{world}&7)");
        addDefault("Info.movedWarp", "&b↑ &8» &7Moved &b{warp} &7to your current location!");
        addDefault("Info.movedHome", "&b↑ &8» &7Moved home &b{home} &7to your current location!");
        addDefault("Info.movedHomeOther", "&b↑ &8» &7Moved &b{player}'s &7home &b{home} &7to your location!");
        addDefault("Info.setMainHome", "&b↑ &8» &7Made &b{home} &7your main home!");
        addDefault("Info.setAndMadeMainHome", "&b↑ &8» &7Set &b{home} &7at your current location and made it your main home!");
        addDefault("Info.setMainHomeOther", "&b↑ &8» &7Made &b{home} {player}'s &7main home!");
        addDefault("Info.setAndMadeMainHomeOther", "&b↑ &8» &7Set &b{home} &7for &b{player} &7at your current location and made it their main home!");
        addDefault("Info.mirroredSpawn", "&b↑ &8» &7Mirrored &b{from}&7's spawnpoint to &b{spawn}&7!");
        addDefault("Info.setMainSpawn", "&b↑ &8» &7Set the main spawnpoint to &b{spawn}&7! All players will teleport there if there are no overriding spawns/permissions.");
        addDefault("Info.removedSpawn", "&b↑ &8» &7Removed the spawnpoint &b{spawn}&7!");
        addDefault("Info.setSpawnSpecial", "&b↑ &8» &7Set spawnpoint &b{spawn}&7!");

        addDefault("Tooltip.homes", "&b↑ &8» &7Teleports you to your home: &b{home}");
        addDefault("Tooltip.warps", "&b↑ &8» &7Teleports you to warp: &b{warp}");
        addDefault("Tooltip.location", "" +
                "\n&bX &8» &7{x}" +
                "\n&bY &8» &7{y}" +
                "\n&bZ &8» &7{z}" +
                "\n&bWorld &8» &7{world}");

        addDefault("Descriptions.Subcommands.help", "Sends the help menu, providing a full list of commands.");
        addDefault("Descriptions.Subcommands.info", "Sends information regarding the plugin.");
        addDefault("Descriptions.Subcommands.import", "Imports data from another plugin so that it can be used within AT.");
        addDefault("Descriptions.Subcommands.export", "Exports data within AT to another plugin.");
        addDefault("Descriptions.Subcommands.reload", "Reloads the plugin's configuration.");
        addDefault("Descriptions.at", "The core command for AT.");
        addDefault("Descriptions.tpa", "Sends a request to teleport to the player.");
        addDefault("Descriptions.tpahere", "Sends a request to the player to teleport to you.");
        addDefault("Descriptions.tpyes", "Accepts a player's teleport request.");
        addDefault("Descriptions.tpno", "Declines a player's teleport request.");
        addDefault("Descriptions.tpcancel", "Cancels your teleport request to a player.");
        addDefault("Descriptions.toggletp", "Either stops or allows players to send teleport requests to you.");
        addDefault("Descriptions.tpon", "Allows players to send teleport requests to you.");
        addDefault("Descriptions.tpoff", "Stops players from sending teleport requests to you.");
        addDefault("Descriptions.tpblock", "Stops a specific player from sending teleport requests to you.");
        addDefault("Descriptions.tpunblock", "Allows a blocked player to send you teleport requests again.");
        addDefault("Descriptions.back", "Teleports you to your previous location.");
        addDefault("Descriptions.tpalist", "Lists all of your current teleport requests.");
        addDefault("Descriptions.tpo", "Instantly teleports you to a player.");
        addDefault("Descriptions.tpohere", "Instantly teleports a player to you.");
        addDefault("Descriptions.tpall", "Sends a teleport request to everyone in the server to you.");
        addDefault("Descriptions.tploc", "Teleports you to a specific location.");
        addDefault("Descriptions.tpoffline", "Teleports you to an offline player.");
        addDefault("Descriptions.tpofflinehere", "Teleports an offline player to you.");
        addDefault("Descriptions.tpr", "Teleports you to a random location.");
        addDefault("Descriptions.warp", "Teleports you to a given warp point.");
        addDefault("Descriptions.warps", "Gives you a list of warps you can teleport to.");
        addDefault("Descriptions.setwarp", "Sets a warp at your location.");
        addDefault("Descriptions.delwarp", "Deletes a warp.");
        addDefault("Descriptions.movewarp", "Moves a warp to a new location.");
        addDefault("Descriptions.spawn", "Teleports you to the spawnpoint.");
        addDefault("Descriptions.setspawn", "Sets a spawn with a name when specified.");
        addDefault("Descriptions.mirrorspawn", "Redirects people using /spawn in one world to another spawn point.");
        addDefault("Descriptions.setmainspawn", "Sets a specified spawnpoint to become the main spawnpoint. If it does not exist, it will be created if you have /setspawn permissions.");
        addDefault("Descriptions.removespawn", "Removes a specified spawnpoint. If none is specified, the one in your current world is removed.");
        addDefault("Descriptions.home", "Teleports you to your home.");
        addDefault("Descriptions.homes", "Gives you a list of homes you've set.");
        addDefault("Descriptions.sethome", "Sets a home at your current location.");
        addDefault("Descriptions.delhome", "Deletes a home.");
        addDefault("Descriptions.movehome", "Moves a home to a new location.");
        addDefault("Descriptions.setmainhome", "Sets a home at your location or makes an existing one your main home.");

        addDefault("Usages.Subcommands.help", "/at help [Category|Page]");
        addDefault("Usages.Subcommands.info", "/at info");
        addDefault("Usages.Subcommands.import", "/at import <Plugin> [All|Homes|LastLocs|Warps|Spawns|Players]");
        addDefault("Usages.Subcommands.export", "/at export <Plugin> [All|Homes|LastLocs|Warps|Spawns|Players]");
        addDefault("Usages.Subcommands.reload", "/at reload");
        addDefault("Usages.at", "/at <Command>");
        addDefault("Usages.tpa", "/tpa <Player>");
        addDefault("Usages.tpahere", "/tpahere <Player>");
        addDefault("Usages.tpyes", "/tpyes [Player]");
        addDefault("Usages.tpno", "/tpno [Player]");
        addDefault("Usages.tpcancel", "/tpcancel [Player]");
        addDefault("Usages.toggletp", "/toggletp");
        addDefault("Usages.tpon", "/tpon");
        addDefault("Usages.tpoff", "/tpoff");
        addDefault("Usages.tpblock", "/tpblock <Player> [Reason]");
        addDefault("Usages.tpunblock", "/tpunblock <Player>");
        addDefault("Usages.back", "/back");
        addDefault("Usages.tpalist", "/tpalist");
        addDefault("Usages.tpo", "/tpo <Player>");
        addDefault("Usages.tpohere", "/tpohere <Player>");
        addDefault("Usages.tpall", "/tpall");
        addDefault("Usages.tploc", "/tploc <x|~> <y|~> <z|~> [Yaw|~] [Pitch|~] [World|~] [Player] [precise|noflight]");
        addDefault("Usages.tpoffline", "/tpoffline <Player>");
        addDefault("Usages.tpofflinehere", "/tpofflinehere <Player>");
        addDefault("Usages.tpr", "/tpr [World]");
        addDefault("Usages.warp", "/warp <Warp>");
        addDefault("Usages.warps", "/warps");
        addDefault("Usages.setwarp", "/setwarp <Name>");
        addDefault("Usages.delwarp", "/delwarp <Name>");
        addDefault("Usages.movewarp", "/movewarp <Name>");
        addDefault("Usages.spawn", "/spawn");
        addDefault("Usages.setspawn", "/setspawn [ID]");
        addDefault("Usages.mirrorspawn", "/mirrorspawn <To Point>|[From World] [To Point]");
        addDefault("Usages.setmainspawn", "/setmainspawn [Point]");
        addDefault("Usages.removespawn", "/removespawn [Point]");
        addDefault("Usages.home", "/home [Home]");
        addDefault("Usages.homes", "/homes");
        addDefault("Usages.sethome", "/sethome <Name>");
        addDefault("Usages.delhome", "/delhome <Home>");
        addDefault("Usages.movehome", "/movehome <Home>");
        addDefault("Usages.setmainhome", "/setmainhome <Home>");

        addDefault("Usages-Admin.tpr", "/tpr [World] [Player]");
        addDefault("Usages-Admin.home", "/home [Home]|<Player> <Home>");
        addDefault("Usages-Admin.homes", "/homes [Player]");
        addDefault("Usages-Admin.delhome", "/delhome <Home>|<Player> <Home>");
        addDefault("Usages-Admin.sethome", "/sethome <Name>|<Player> <Name>");
        addDefault("Usages-Admin.movehome", "/movehome <Home>|<Player> <Home>");
        addDefault("Usages-Admin.setmainhome", "/setmainhome <Home>|<Player> <Home>");
        addDefault("Usages-Admin.spawn", "/spawn <ID>");

    }

    public static String getStringA(String path) {
        return translateString(config.getString(path));
    }

    public static String getString(String path, String... placeholders) {
        return translateString(config.getString(path), placeholders);
    }

    public static String translateString(String str, String... placeholders) {
        if (str == null) return "";
        str = str.replaceAll("''", "'");
        str = str.replaceAll("^'", "");
        str = str.replaceAll("'$", "");
        str = ChatColor.translateAlternateColorCodes('&', str);

        for (int i = 0; i < placeholders.length; i += 2) {
            try {
                str = str.replace(placeholders[i], placeholders[i + 1]);
            } catch (ArrayIndexOutOfBoundsException ignored) {

            }
        }

        return str;
    }

    public static void sendMessage(CommandSender sender, String path, String... placeholders) {
        if (config.getConfig() == null) return;
        if (supportsTitles() && sender instanceof Player) {
            Player player = (Player) sender;

            ConfigurationSection titles = config.getConfig().getConfigurationSection(path + "_title");
            ConfigurationSection subtitles = config.getConfig().getConfigurationSection(path + "_subtitle");
            if (titles != null || subtitles != null) {

                // Fade in, stay, out
                int[] titleInfo = new int[]{0, 0, 0};

                if (titles != null) {
                    titleInfo[0] = titles.getInt("fade-in");
                    titleInfo[1] = titles.getInt("length");
                    titleInfo[2] = titles.getInt("fade-out");
                }

                BukkitRunnable runnable = new BukkitRunnable() {

                    private int current = 0;
                    private String previousTitle = null;
                    private String previousSubtitle = null;

                    @Override
                    public void run() {
                        if (current == titleInfo[1] || titleManager.get(player) != this) {
                            cancel();
                            return;
                        }
                        String title = null;
                        String subtitle = null;
                        if (titles != null) {
                            title = titles.getString(String.valueOf(current));
                        }
                        if (subtitles != null) {
                            subtitle = subtitles.getString(String.valueOf(current));
                        }

                        player.sendTitle(title == null ? previousTitle : (previousTitle = translateString(title, placeholders)),
                                subtitle == null ? previousSubtitle : (previousSubtitle = translateString(subtitle, placeholders)),
                                titleInfo[0],
                                titleInfo[1] - current,
                                titleInfo[2]);

                        current++;
                    }
                };
                titleManager.put(player, runnable);
                runnable.runTaskTimer(CoreClass.getInstance(), 1, 1);
            }
        }
        if (config.getConfig().get(path) instanceof List) {
            List<String> messages = config.getConfig().getStringList(path);
            for (int i = 0; i < messages.size(); i++) {
                getFancyMessage(translateString(messages.get(i), placeholders)).sendProposal(sender, i);
            }
        } else {
            String[] messages = translateString(config.getConfig().getString(path), placeholders).split("\n");
            for (int i = 0; i < messages.length; i++) {
                getFancyMessage(messages[i]).sendProposal(sender, i);
            }
        }
        FancyMessage.send(sender);
    }

    private static boolean supportsTitles() {
        try {
            Player.class.getDeclaredMethod("sendTitle", String.class, String.class, int.class, int.class, int.class);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    // Doing it like this because Regex is not co-operating
    private static FancyMessage getFancyMessage(String str) {
        int startTextPointer = -1;
        int endTextPointer = -1;

        int startCommandPointer = -1;
        int endCommandPointer;

        int lastMarkdownPointer = 0;

        FancyMessage builder = new FancyMessage();
        boolean buildingComponent = false;

        for (int i = 0; i < str.length(); i++) {
            if (!buildingComponent && str.charAt(i) == '[' && (i == 0 || !(str.charAt(i - 1) == '\\'))) {
                startTextPointer = i + 1;
                buildingComponent = true;
            } else if (buildingComponent && str.charAt(i) == ']' && !(str.charAt(i - 1) == '\\')) {
                endTextPointer = i;
            } else if (buildingComponent && str.charAt(i) == '(') {
                if (str.charAt(i - 1) == ']') {
                    startCommandPointer = i + 1;
                } else if (startCommandPointer == -1) {
                    buildingComponent = false;
                    startTextPointer = -1;
                    endTextPointer = -1;
                }
            } else if (buildingComponent && str.charAt(i) == ')' && !(str.charAt(i - 1) == '\\')) {
                if (startCommandPointer != -1) {
                    endCommandPointer = i;
                    // Get all the
                    builder.text(str.substring(lastMarkdownPointer, startTextPointer - 1));

                    String command = "";
                    List<String> tooltip = new ArrayList<>();

                    String fullCommand = str.substring(startCommandPointer, endCommandPointer);

                    int dividerIndex = fullCommand.indexOf('|');
                    if (dividerIndex != -1 && fullCommand.charAt(dividerIndex - 1) != '\\') {
                        String[] parts = fullCommand.split("\\|");
                        for (String part : parts) {
                            if (part.startsWith("/") && command.isEmpty()) {
                                command = part;
                            } else if (!part.isEmpty()) {
                                tooltip.add(part);
                            }
                        }
                    } else {
                        if (fullCommand.startsWith("/")) {
                            command = fullCommand;
                        } else if (!fullCommand.isEmpty()) {
                            tooltip.add(fullCommand);
                        }
                    }

                    builder.then(str.substring(startTextPointer, endTextPointer)).tooltip(tooltip);

                    if (!command.isEmpty()) {
                        builder.command(command);
                    }

                    builder.then();

                    lastMarkdownPointer = endCommandPointer + 1;

                    startTextPointer = -1;
                    endTextPointer = -1;
                    startCommandPointer = -1;

                    buildingComponent = false;

                }
            }
        }

        builder.text(str.substring(lastMarkdownPointer));

        return builder;
    }
}
