package io.github.niestrat99.advancedteleport.config;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.fanciful.FancyMessage;
import io.github.thatsmusic99.configurationmaster.CMFile;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.*;

public class CustomMessages extends CMFile {

    public static File ConfigFile = new File(CoreClass.getInstance().getDataFolder(),"custom-messages.yml");
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
        addDefault("Error.noSuchWorld", "&b↑ &8» &7That world doesn't exist!");
        addDefault("Error.noLocation", "&b↑ &8» &7You don't have any location to teleport back to!");
        addDefault("Error.notAPlayer", "&b↑ &8» &7You must be a player to run this command!");
        config.addDefault("Error.noHomes", "&b↑ &8» &7You haven't got any homes!");
        config.addDefault("Error.noHomesOtherPlayer", "&b↑ &8» &b{player} &7hasn't got any homes!");
        config.addDefault("Error.tooFarAway", "&b↑ &8» &7The teleport destination is too far away so you can not teleport there!");
        config.addDefault("Error.noRequestsSent", "&b↑ &8» &7Couldn't send a request to anyone :(");
        config.addDefault("Error.onCountdown","&b↑ &8» &7You can't use this command whilst waiting to teleport!");
        config.addDefault("Error.noPermissionWarp", "&b↑ &8» &7You can't warp to &b{warp}&7!");
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

        config.addDefault("Info.tpOff", "&b↑ &8» &7Successfully disabled teleport requests!");
        config.addDefault("Info.tpOn", "&b↑ &8» &7Successfully enabled teleport requests!");
        config.addDefault("Info.tpAdminOff", "&b↑ &8» &7Successfully disabled teleport requests for &b{player}&7!");
        config.addDefault("Info.tpAdminOn", "&b↑ &8» &7Successfully enabled teleport requests for &b{player}&7!");
        config.addDefault("Info.requestSent", "&b↑ &8» &7Successfully sent request to &b{player}&7!" +
                "\n&b↑ &8» &7They've got &b{lifetime} &7to respond!" +
                "\n&7To cancel the request use &b/tpcancel &7to cancel it." +
                "\n" +
                "\n                                [&7&l[CANCEL]](/tpcancel {player})" +
                "\n&7");
        config.addDefault("Info.tpaRequestReceived", "&b↑ &8» &7The player &b{player} &7wants to teleport to you!" +
                "\n&b↑ &8» &7If you want to accept it, use &b/tpayes&7, but if not, use &b/tpano&7." +
                "\n&b↑ &8» &7You've got &b{lifetime} &7to respond to it!" +
                "\n" +
                "\n                   [&a&l[ACCEPT]](/tpayes {player}|&aClick here to accept the request.)              [&c&l[DENY]](/tpano {player}|&cClick here to deny the request.)" +
                "\n&7");
        config.addDefault("Info.tpaRequestHere", "&b↑ &8» &7The player &b{player} &7wants to teleport you to them!" +
                "\n&b↑ &8» &7If you want to accept it, use &b/tpayes&7, but if not, use &b/tpano&7." +
                "\n&b↑ &8» &7You've got &b{lifetime} seconds &7to respond to it!" +
                "\n" +
                "\n                   [&a&l[ACCEPT]](/tpayes {player}|&aClick here to accept the request.)              [&c&l[DENY]](/tpano {player}|&cClick here to deny the request.)" +
                "\n&7");
        config.addDefault("Info.blockPlayer", "&b↑ &8» &b{player} &7has been blocked.");
        config.addDefault("Info.tpCancel", "&b↑ &8» &7You have cancelled your teleport request.");
        config.addDefault("Info.tpCancelResponder", "&b↑ &8» &b{player} &7has cancelled their teleport request.");
        config.addDefault("Info.multipleRequestsCancel", "&b↑ &8» &7You have multiple teleport requests pending! Click one of the following to cancel:");
        config.addDefault("Info.multipleRequestsIndex", "&b> {player}");
        config.addDefault("Info.multipleRequestsList", "&b↑ &8» &7Do /tpalist <Page Number> To check other requests.");
        config.addDefault("Info.multipleRequestAccept", "&b↑ &8» &7You have multiple teleport requests pending! Click one of the following to accept:");
        config.addDefault("Info.multipleRequestDeny", "&b↑ &8» &7You have multiple teleport requests pending! Click one of the following to deny:");
        config.addDefault("Info.requestDeclined", "&b↑ &8» &7You've declined the teleport request!");
        config.addDefault("Info.requestDeclinedResponder", "&b↑ &8» &b{player} &7has declined your teleport request!");
        config.addDefault("Info.requestDisplaced", "&b↑ &8» &7Your request has been cancelled because &b{player} &7got another request!");

        config.addDefault("Info.deletedHome", "&b↑ &8» &7Successfully deleted the home &b{home}&7!");
        config.addDefault("Info.deletedHomeOther", "&b↑ &8» &7Successfully deleted the home &b{home} &7for &b{player}&7!");
        config.addDefault("Info.setHome", "&b↑ &8» &7Successfully set the home &b{home}&7!");
        config.addDefault("Info.setHomeOther", "&b↑ &8» &7Successfully set the home &b{home} &7for &b{player}&7!");
        config.addDefault("Info.setSpawn", "&b↑ &8» &7Successfully set the spawnpoint!");
        config.addDefault("Info.setWarp", "&b↑ &8» &7Successfully set the warp &b{warp}&7!");
        config.addDefault("Info.deletedWarp", "&b↑ &8» &7Successfully deleted the warp &b{warp}&7!");
        config.addDefault("Info.searching", "&b↑ &8» &7Searching for a location...");
        config.addDefault("Info.unblockPlayer", "&b↑ &8» &7Successfully unblocked &b{player}&7!");
        config.addDefault("Info.reloadingConfig", "&b↑ &8» &7Reloading &bAdvancedTeleport&7's config...");
        config.addDefault("Info.reloadedConfig", "&b↑ &8» &7Finished reloading the config!");
        config.addDefault("Info.warps", "&b&lWarps &8» &r");
        config.addDefault("Info.homes", "&b&lHomes &8» &r");
        config.addDefault("Info.homesOther", "&b&l{player}'s homes &8» &r");
        config.addDefault("Info.requestAccepted", "&b↑ &8» &7You've accepted the teleport request!");
        config.addDefault("Info.requestAcceptedResponder", "&b↑ &8» &b{player} &7has accepted the teleport request!");
        config.addDefault("Info.paymentVault", "&b↑ &8» &7You have paid &b${amount} &7and now have &b${balance}&7!");
        config.addDefault("Info.paymentEXP", "&b↑ &8» &7You have paid &b{amount} EXP Levels &7and now have &b{levels} &7levels!");
        config.addDefault("Info.paymentPoints", "&b↑ &8» &7You have paid &b{amount} EXP Points &7and now have &b{points} &7points!");
        config.addDefault("Info.createdWarpSign", "&b↑ &8» &7Successfully created the warp sign!");
        config.addDefault("Info.createdRTPSign", "&b↑ &8» &7Successfully created the RandomTP sign!");
        config.addDefault("Info.createdSpawnSign", "&b↑ &8» &7Successfully created the spawn sign!");
        config.addDefault("Info.tpallRequestSent", "&b↑ &8» &7Successfully sent a teleport request to &b{amount} &7player(s)!");
        config.addDefault("Info.teleportedToLoc", "&b↑ &8» &7Successfully teleported you to &b{x}&7, &b{y}&7, &b{z}&7! (Yaw: &b{yaw}&7, Pitch: &b{pitch}&7, World: &b{world}&7)");
        config.addDefault("Info.teleportedToLocOther", "&b↑ &8» &7Successfully teleported &b{player} &7to &b{x}&7, &b{y}&7, &b{z}&7! (Yaw: &b{yaw}&7, Pitch: &b{pitch}&7, World: &b{world}&7)");
        config.addDefault("Info.movedWarp", "&b↑ &8» &7Moved &b{warp} &7to your current location!");
        config.addDefault("Info.movedHome", "&b↑ &8» &7Moved home &b{home} &7to your current location!");
        config.addDefault("Info.movedHomeOther", "&b↑ &8» &7Moved &b{player}'s &7home &b{home} &7to your location!");
        config.addDefault("Info.setMainHome", "&b↑ &8» &7Made &b{home} &7your main home!");
        config.addDefault("Info.setAndMadeMainHome", "&b↑ &8» &7Set &b{home} &7at your current location and made it your main home!");
        config.addDefault("Info.setMainHomeOther", "&b↑ &8» &7Made &b{home} {player}'s &7main home!");
        config.addDefault("Info.setAndMadeMainHomeOther", "&b↑ &8» &7Set &b{home} &7for &b{player} &7at your current location and made it their main home!");
        config.addDefault("Tooltip.homes", "&b↑ &8» &7Teleports you to your home: &b{home}");
        config.addDefault("Tooltip.warps", "&b↑ &8» &7Teleports you to warp: &b{warp}");
        config.addDefault("Tooltip.location", "" +
                "\n&bX &8» &7{x}" +
                "\n&bY &8» &7{y}" +
                "\n&bZ &8» &7{z}" +
                "\n&bWorld &8» &7{world}");
        config.addDefault("Help.mainHelp", new ArrayList<>(Arrays.asList("&b&lAdvancedTeleport Help",
                "&6Please type &b/athelp <category> &6to get a list of commands about this category.",
                "&6--[ &bCategories &6]--",
                "&6- Teleport",
                "&6- Warps",
                "&6- Spawn",
                "&6- RandomTP",
                "&6- HomesCommand")));
        config.addDefault("Help.mainHelpAdmin", new ArrayList<>(Collections.singletonList("&6- Admin")));
        config.addDefault("Help.teleport", new ArrayList<>(Arrays.asList("&b&lTeleport help",
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
        config.addDefault("Help.teleportAdmin", new ArrayList<>(Arrays.asList("&6- /tpo <player> - Instantly teleports you to the player.",
                "&6- /tpohere <player> - Instantly teleports the player to you.",
                "&6- /tpall - Sends a teleport request to every online player to teleport to you.",
                "&6- /tploc <x|~> <y|~> <z|~> [Yaw|~] [Pitch|~] [World|~] [Player] [precise|noflight] - Teleports you or another player to a specified location.",
                "&6- /tpoffline <player> - Teleports to an offline player.",
                "&6- /tpofflinehere <player> - Teleports an offline player to you.")));
        config.addDefault("Help.warps", new ArrayList<>(Arrays.asList("&b&lWarps help",
                "&6- /warp <Warp> - Teleports you to an existing warp point.",
                "&6- /warps - Gives you a list of warps.")));
        config.addDefault("Help.warpsAdmin", new ArrayList<>(Arrays.asList(
                "&6- /setwarp <Name> - Sets a warp at a given location.",
                "&6- /delwarp <Warp> - Deletes a warp.",
                "&6- /movewarp <Warp> - Moves a warp to your location.")));
        config.addDefault("Help.spawn", new ArrayList<>(Arrays.asList("&b&lSpawn help",
                "- /spawn - Teleports you to the spawn point.")));
        config.addDefault("Help.spawnAdmin", new ArrayList<>(Collections.singletonList("&6- /setspawn - Sets a spawn point at your location.")));
        config.addDefault("Help.randomTP", new ArrayList<>(Arrays.asList("&b&lRandomTP help",
                "&6- /rtp - Teleports you to a random location.")));
        addDefault("Help.homes", new ArrayList<>(Arrays.asList("&b&lHomes help",
                "&6- /sethome <Home> - Sets a home point at your location.",
                "&6- /delhome <Home> - Deletes a home point you've set.",
                "&6- /home <Home> - Teleports you to your home.",
                "&6- /homes - Gives you a list of homes you've set.",
                "&6- /movehome <Home> - Moves one of your homes to your current location.",
                "&6- /setmainhome <Home> - Sets a home at your location or makes an existing one your main home.")));
        addDefault("Help.homesAdmin", new ArrayList<>(Arrays.asList("&6- /sethome <player> <home name> - Sets a home point at your location for the player.",
                "&6- /delhome <player> <home name> - Deletes a home point of a player.",
                "&6- /home <player> <home name> - Teleports you to a home point a player has set.",
                "&6- /homes <player> - Gives you a list of homes of a player.")));
        addDefault("Help.admin", new ArrayList<>(Arrays.asList("&b&lAdmin help",
                "&6- /atinfo - Shows informations about this plugin.",
                "&6- /atreload - Reloads all configuration files of this plugin.")));

    }

    public static String getString(String path, String... placeholders) {
        return translateString(config.getConfig().getString(path), placeholders);
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
