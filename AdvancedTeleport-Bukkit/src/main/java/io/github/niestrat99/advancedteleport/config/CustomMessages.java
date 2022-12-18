package io.github.niestrat99.advancedteleport.config;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.data.PartialComponent;
import io.github.niestrat99.advancedteleport.extensions.ExPermission;
import io.github.niestrat99.advancedteleport.managers.PluginHookManager;
import io.github.niestrat99.advancedteleport.utilities.PagedLists;
import io.github.thatsmusic99.configurationmaster.api.ConfigSection;
import io.papermc.lib.PaperLib;
import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import kotlin.Pair;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.HoverEventSource;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.geysermc.floodgate.api.FloodgateApi;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CustomMessages extends ATConfig {

    public static CustomMessages config;
    private static HashMap<CommandSender, BukkitRunnable> titleManager;

    @NotNull private static HashMap<String, PartialComponent> messageCache = new HashMap<>();

    @Nullable private static BukkitAudiences audience;

    public CustomMessages() throws IOException {
        super("custom-messages.yml");
        config = this;
        titleManager = new HashMap<>();
        messageCache = new HashMap<>();

        if (!PaperLib.isPaper()) {
            audience = BukkitAudiences.create(CoreClass.getInstance());
        }
    }

    public void loadDefaults() {
        makeSectionLenient("Teleport");
        addDefault("Common.prefix", "<aqua>↑</aqua> <dark_gray>»</dark_gray>");
        addDefault("Teleport.eventBeforeTP" , "<prefix> <gray>Teleporting in <aqua><countdown> seconds</aqua>, please do not move!");

        addComment("Teleport.eventBeforeTP_title", "This is an example use for titles and subtitles in the plugin." +
                "\nThis feature is supported only if you're on version 1.8.8 or newer.");
        addExample("Teleport.eventBeforeTP_title.length" , 80 , "How many seconds (in ticks) the header should last. This is not including fading.");
        addExample("Teleport.eventBeforeTP_title.fade-in" , 0 , "How many seconds (in ticks) the header should take to fade in.");
        addExample("Teleport.eventBeforeTP_title.fade-out" , 10 , "How many seconds (in ticks) the header should take to fade out.");
        addExample("Teleport.eventBeforeTP_title.0", "<gray><bold>Teleporting...");
        addExample("Teleport.eventBeforeTP_title.20", "<aqua>></aqua>&lTeleporting... &b<");
        addExample("Teleport.eventBeforeTP_title.40", "<aqua>>></aqua>&lTeleporting... &b<<");
        addExample("Teleport.eventBeforeTP_title.60", "&b>>> &e&lTeleported! &b<<<");
        addExample("Teleport.eventBeforeTP_subtitle.0", "&bPlease do not move!");
        addExample("Teleport.eventBeforeTP_subtitle.60", "");

        addDefault("Teleport.eventBeforeTPMovementAllowed", "<prefix> <gray>Teleporting in <aqua><countdown></aqua> seconds!");
        addDefault("Teleport.eventTeleport", "<prefix> <gray>Teleporting...");
        addDefault("Teleport.eventMovement", "<prefix> <gray>Teleport has been cancelled due to movement.");
        addDefault("Teleport.eventMovement_title.length", 60);
        addDefault("Teleport.eventMovement_title.fade-in", 0);
        addDefault("Teleport.eventMovement_title.fade-out", 10);
        addDefault("Teleport.eventMovement_title.0", "&e&l! &c&lCancelled &e&l!");
        addDefault("Teleport.teleportingToSpawn", "<prefix> <gray>Teleporting you to spawn!");
        addDefault("Teleport.teleporting", "<prefix> <gray>Teleporting to <aqua>player</aqua>!");
        addDefault("Teleport.teleportingToHome", "<prefix> <gray>Teleporting to <aqua>home</aqua>!");
        addDefault("Teleport.teleportingToHomeOther", "<prefix> <gray>Teleporting to <aqua>player</aqua>'s home, <aqua>home</aqua>!");
        addDefault("Teleport.teleportingToWarp", "<prefix> <gray>Teleporting you to <aqua>warp</aqua>!");
        addDefault("Teleport.teleportingPlayerToSelf", "<prefix> <gray>Teleporting <aqua><player></aqua> to you!");
        addDefault("Teleport.teleportingSelfToPlayer", "<prefix> <gray>Teleporting you to <aqua>player</aqua>!");
        addDefault("Teleport.teleportingToRandomPlace", "<prefix> <gray>Teleporting you to a random place!");
        addDefault("Teleport.teleportingToLastLoc", "<prefix> <gray>Teleporting to your last location!");
        addDefault("Teleport.teleportedToOfflinePlayer", "<prefix> <gray>Teleported to offline player <aqua>player</aqua>!");
        addDefault("Teleport.teleportedOfflinePlayerHere", "<prefix> <gray>Teleported offline player <aqua><player></aqua> to your location!");

        makeSectionLenient("Error");
        addDefault("Error.noPermission", "<prefix> <gray>You do not have permission to use this command!");
        addDefault("Error.noPermissionSign", "<prefix> <gray>You do not have permission to make this sign!");
        addDefault("Error.featureDisabled", "<prefix> <gray>This feature has been disabled!");
        addDefault("Error.noRequests", "<prefix> <gray>You do not have any pending requests!");
    //    Config.addDefault("Error.requestSendFail", "&cCould not send request to &e<player>!"); - NOT USED!!!
        addDefault("Error.tpOff", "<prefix> <aqua><player> <gray>has their teleportation disabled!");
        addDefault("Error.tpBlock", "<prefix> <aqua><player> <gray>has blocked you from sending requests to them!");
        addDefault("Error.alreadyOn", "<prefix> <gray>Your teleport requests are already enabled!");
        addDefault("Error.alreadyOff", "<prefix> <gray>Your teleport requests are already disabled!");
        addDefault("Error.alreadyBlocked", "<prefix> <gray>This player is already blocked!");
        addDefault("Error.neverBlocked", "<prefix> <gray>This player was never blocked!");
        addDefault("Error.onCooldown", "<prefix> <gray>Please wait another <aqua><time></aqua> seconds to use this command!");
        addDefault("Error.requestSentToSelf", "<prefix> <gray>You can't send a request to yourself!");
        addDefault("Error.noSuchPlayer", "<prefix> <gray>The player is either currently offline or doesn't exist!");
        addDefault("Error.alreadySentRequest", "<prefix> <gray>You've already sent a request to <aqua><player></aqua>!");
        addDefault("Error.notEnoughEXP", """
            <prefix> <gray>You do not have enough EXP Levels to teleport there!
            <prefix> <gray>You need at least <aqua><levels></aqua>EXP levels!
        """.trim());
        addDefault("Error.notEnoughEXPPoints", """
            <prefix> <gray>You do not have enough EXP Points to teleport there!
            <prefix> <gray>You need at least <aqua><points></aqua>EXP points!
        """.trim());
        addDefault("Error.notEnoughMoney", """
            <prefix> <gray>You do not have enough money to teleport there!
            <prefix> <gray>You need at least <aqua>amount</aqua>!
        """.trim());
        addDefault("Error.requestExpired", "<prefix> <gray>Your teleport request to <aqua><player></aqua> has expired!");
        addDefault("Error.noPlayerInput", "<prefix> <gray>You must include a player name!");
        addDefault("Error.blockSelf", "<prefix> <gray>You can't block yourself!");
        addDefault("Error.noRequestsFromPlayer", "<prefix> <gray>You don't have any pending requests from <aqua>player</aqua>!");
        addDefault("Error.noRequests", "<prefix> <gray>You don't have any pending requests!");
        addDefault("Error.invalidPageNo", "<prefix> <gray>You've inserted an invalid page number!");
        addDefault("Error.noHomeInput", "<prefix> <gray>You have to include a home name!");
        addDefault("Error.noSuchHome", "<prefix> <gray>This home doesn't exist!");
        addDefault("Error.noBedHome", "<prefix> <gray>You don't have any bed spawn set!");
        addDefault("Error.noBedHomeOther", "<prefix> <aqua><player></aqua> <gray>doesn't have a bed spawn set!");
        addDefault("Error.reachedHomeLimit", "<prefix> <gray>You can't set any more homes!");
        addDefault("Error.homeAlreadySet", "<prefix> <gray>You already have a home called <aqua>home</aqua>!");
        addDefault("Error.noWarpInput", "<prefix> <gray>You have to include the warp's name!");
        addDefault("Error.noSuchWarp", "<prefix> <gray>That warp doesn't exist!");
        addDefault("Error.warpAlreadySet", "<prefix> <gray>There is already a warp called <aqua>warp</aqua>!");
        addDefault("Error.noSuchWorld", "<prefix> <gray>That world doesn't exist!");
        addDefault("Error.noLocation", "<prefix> <gray>You don't have any location to teleport back to!");
        addDefault("Error.notAPlayer", "<prefix> <gray>You must be a player to run this command!");
        addDefault("Error.noHomes", "<prefix> <gray>You haven't got any homes!");
        addDefault("Error.noHomesOther", "<prefix> <aqua><player></aqua> <gray>hasn't got any homes!"); // TODO: Note this changed from noHomesOtherPlayer
        addDefault("Error.tooFarAway", "<prefix> <gray>The teleport destination is too far away so you can not teleport there!");
        addDefault("Error.noRequestsSent", "<prefix> <gray>Couldn't send a request to anyone :(");
        addDefault("Error.onCountdown","<prefix> <gray>You can't use this command whilst waiting to teleport!");
        addDefault("Error.noPermissionWarp", "<prefix> <gray>You can't warp to <aqua>warp</aqua>!");
        addDefault("Error.cantTPToWorld", "<prefix> <gray>You can't randomly teleport in that world!");
       // config.addDefault("Error.invalidName", "&cHomes and warps may only have letters and numbers in the names!");
        addDefault("Error.cantTPToWorldLim", "<prefix> <gray>You can't teleport to <aqua>world</aqua>!");
        addDefault("Error.tooFewArguments", "<prefix> <gray>Too few arguments!");
        addDefault("Error.invalidArgs", "<prefix> <gray>Invalid arguments!");
        addDefault("Error.cantTPToPlayer", "<prefix> <gray>You can't request a teleportation to <aqua>player</aqua>!");
        addDefault("Error.noWarps", "<prefix> <gray>There are no warps as of currently!");
        addDefault("Error.noAccessHome", "<prefix> <gray>You cannot access <aqua>home</aqua> as of currently!");
        addDefault("Error.moveHomeFail", "<prefix> <gray>The home has been moved but the data has not been stored successfully. The plugin will try to fix this itself.");
        addDefault("Error.setMainHomeFail", "<prefix> <gray>The main home has been set but the data has not been stored successfully. The plugin will try to fix this itself.");
        addDefault("Error.deleteHomeFail", "<prefix> <gray>The home has been deleted but the data has not been stored successfully. The plugin will try to fix this itself.");
        addDefault("Error.setHomeFail", "<prefix> <gray>The home has been set but the data has not been stored successfully. The plugin will try to fix this itself.");
        addDefault("Error.deleteWarpFail", "<prefix> <gray>The warp has been set but the data has not been stored successfully. The plugin will try to fix this itself.");
        addDefault("Error.purgeWarpsFail", "<prefix> <gray>Could not purge warps. Please check the console for more information.");
        addDefault("Error.purgeHomesFail", "<prefix> <gray>Could not purge homes. Please check the console for more information.");
        addDefault("Error.homesNotLoaded", "<prefix> <gray>Homes for this player haven't loaded yet, please wait a little bit (even just a second) before trying this again!");
        addDefault("Error.noOfflineLocation", "<prefix> <gray>No offline location was found for <aqua>player</aqua>!");
        addDefault("Error.failedOfflineTeleport", "<prefix> <gray>Failed to teleport to offline player <aqua>player</aqua>!");
        addDefault("Error.failedOfflineTeleportHere", "<prefix> <gray>Failed to teleport offline player <aqua><player></aqua> to your location!");
        addDefault("Error.alreadySearching", "<prefix> <gray>Already searching for a location to teleport to!");
        addDefault("Error.mirrorSpawnNoArguments", "<prefix> <gray>No worlds/spawn points have been specified!");
        addDefault("Error.mirrorSpawnLackOfArguments", "<prefix> <gray>You must be a player to only specify one world - please specify a world and a spawnpoint to mirror players to!");
        addDefault("Error.noSuchSpawn", "<prefix> <gray>There is no such spawn called <aqua>spawn</aqua>!");
        addDefault("Error.cannotSetMainSpawn", "<prefix> <gray>You can only make existing spawnpoints into the main spawnpoint rather than create new ones!");
        addDefault("Error.cannotSetMainSpawnConsole", "<prefix> <gray>You can only make existing spawnpoints into the main spawnpoint rather than create new ones since you are not a player!");
        addDefault("Error.nonAlphanumericSpawn", "<prefix> <gray>Spawnpoints need to be alphanumeric!");
        addDefault("Error.removeSpawnNoArgs", "<prefix> <gray>You have to specify a spawnpoint to remove!");
        addDefault("Error.noSuchPlugin", "<prefix> <gray>This plugin is not supported for importing/exporting yet!");
        addDefault("Error.cantImport", "<prefix> <gray>Can't import plugin data from <aqua><plugin></aqua> (make sure it's enabled and by the correct authors)!");
        addDefault("Error.cantExport", "<prefix> <gray>Can't export plugin data from <aqua><plugin></aqua> (make sure it's enabled and by the correct authors)!");
        addDefault("Error.notEnoughItems", """
            <prefix> <gray>You do not have enough items to teleport there!
            <prefix> <gray>You need at least &b<amount> <type>(s)!
        """.trim());
        addDefault("Error.mirrorSpawnFail", "<prefix> <gray>Failed to mirror <aqua>from</aqua>'s spawnpoint to <aqua>spawn</aqua>!");
        addDefault("Error.removeSpawnFail", "<prefix> <gray>Failed to remove the spawnpoint <aqua>spawn</aqua>!");
        addDefault("Error.setMainSpawnFail", "<prefix> <gray>Failed to set the main spawnpoint <aqua>spawn</aqua>!");
        addDefault("Error.blockFail", "<prefix> <gray>Failed to save the block against <aqua>player</aqua>!");
        addDefault("Error.unblockFail", "<prefix> <gray>Failed to save the block removal against <aqua>player</aqua>!");
        addDefault("Error.noParticlePlugins", "<prefix> <gray>There are no particle plugins on this server! You need at least one (PlayerParticles) to use this command.");
        addDefault("Error.setWarpFail", "&b↑ &8» &7Failed to set the warp {warp}!");

        addDefault("Info.tpOff", "<prefix> <gray>Successfully disabled teleport requests!");
        addDefault("Info.tpOn", "<prefix> <gray>Successfully enabled teleport requests!");
        addDefault("Info.tpAdminOff", "<prefix> <gray>Successfully disabled teleport requests for <aqua>player</aqua>!");
        addDefault("Info.tpAdminOn", "<prefix> <gray>Successfully enabled teleport requests for <aqua>player</aqua>!");
        // TODO: Possible "Common" components for the hover and click events?
        addDefault("Info.requestSent", """
            <prefix> <gray>Successfully sent request to <aqua>player</aqua>!
            <prefix> <gray>They've got <aqua><lifetime></aqua> to respond!
            <prefix> <gray>To cancel the request use <aqua>/tpcancel</aqua> to cancel it.

                                <click:run_command:tpcancel <player>><hover:show_text:'<red>Click here to cancel the request.'><gray><bold>[CANCEL]</hover></click>
        """.stripIndent());
        addDefault("Info.tpaRequestReceived", """
            <prefix> <gray>The player <aqua><player></aqua> wants to teleport to you!
            <prefix> <gray>If you want to accept it, use <aqua>/tpayes</aqua>, but if not, use <aqua>/tpano</aqua>.
            <prefix> <gray>You've got <aqua><lifetime></aqua> to respond to it!

                                <click:run_command:tpayes <player>><hover:show_text:'<green>Click here to accept the request.'><green><bold>[ACCEPT]</hover></click>             <click:run_command:/tpano <player>><hover:show_text:'<red>Click here to deny the request.>&c&l[DENY]</hover></click>
        &7""".stripIndent());
        addDefault("Info.tpaRequestHere", """
            <prefix> <gray>The player <aqua><player></aqua> wants to teleport you to them!
            <prefix> <gray>If you want to accept it, use <aqua>/tpayes</aqua>, but if not, use <aqua>/tpano</aqua>.
            <prefix> <gray>You've got <aqua><lifetime> seconds</aqua> to respond to it!
        addDefault("Error.setWarpFail", "&b↑ &8» &7Failed to set the warp {warp}!");

                              <click:run_command:/tpayes <player>><hover:show_text:'<green>Click here to accept the request.><green>&l[ACCEPT]</hover></click>             <click:run_command:/tpano <player>><hover:show_text:'<red>Click here to deny the request.>&c&l[DENY]</hover></click>
        &7""".stripIndent());
        addDefault("Info.blockPlayer", "<prefix> <aqua><player> <gray>has been blocked.");
        addDefault("Info.tpCancel", "<prefix> <gray>You have cancelled your teleport request.");
        addDefault("Info.tpCancelResponder", "<prefix> <aqua><player> <gray>has cancelled their teleport request.");
        addDefault("Info.multipleRequestsCancel", "<prefix> <gray>You have multiple teleport requests pending! Click one of the following to cancel:");
        addDefault("Info.multipleRequestsIndex", "<prefix> <click:run_command:<command> <player>><player></click>");
        addDefault("Info.multipleRequestsList", "<prefix> <gray>Do /tpalist <Page Number> To check other requests.");
        addDefault("Info.multipleRequestAccept", "<prefix> <gray>You have multiple teleport requests pending! Click one of the following to accept:");
        addDefault("Info.multipleRequestDeny", "<prefix> <gray>You have multiple teleport requests pending! Click one of the following to deny:");
        addDefault("Info.requestDeclined", "<prefix> <gray>You've declined the teleport request!");
        addDefault("Info.requestDeclinedResponder", "<prefix> <aqua><player></aqua> has declined your teleport request!");
        addDefault("Info.requestDisplaced", "<prefix> <gray>Your request has been cancelled because <aqua><player></aqua> got another request!");

        addDefault("Info.deletedHome", "<prefix> <gray>Successfully deleted the home <aqua><home></aqua>!");
        addDefault("Info.deletedHomeOther", "<prefix> <gray>Successfully deleted the home <aqua><home></aqua> for <aqua><player></aqua>!");
        addDefault("Info.setHome", "<prefix> <gray>Successfully set the home <aqua><home></aqua>!");
        addDefault("Info.setHomeOther", "<prefix> <gray>Successfully set the home <aqua><home></aqua> for <aqua>player</aqua>!");
        addDefault("Info.setSpawn", "<prefix> <gray>Successfully set the spawnpoint!");
        addDefault("Info.setWarp", "<prefix> <gray>Successfully set the warp <aqua>warp</aqua>!");
        addDefault("Info.deletedWarp", "<prefix> <gray>Successfully deleted the warp <aqua>warp</aqua>!");
        addDefault("Info.purgeWarpsWorld", "<prefix> <gray>Successfully purged warps in <aqua>world</aqua>!");
        addDefault("Info.purgeWarpsCreator", "<prefix> <gray>Successfully purged warps created by <aqua>player</aqua>!");
        addDefault("Info.purgeHomesWorld", "<prefix> <gray>Successfully purged homes in <aqua>world</aqua>!");
        addDefault("Info.purgeHomesCreator", "<prefix> <gray>Successfully purged homes created for <aqua>player</aqua>!");
        addDefault("Info.searching", "<prefix> <gray>Searching for a location...");
        addDefault("Info.unblockPlayer", "<prefix> <gray>Successfully unblocked <aqua>player</aqua>!");
        addDefault("Info.reloadingConfig", "<prefix> <gray>Reloading <aqua>AdvancedTeleport</aqua>'s config...");
        addDefault("Info.reloadedConfig", "<prefix> <gray>Finished reloading the config!");
        addDefault("Info.warps", "<aqua><bold>Warps <dark_gray>» <reset>");
        addDefault("Info.homes", "<aqua><bold>Homes <dark_gray>» <reset>");
        addDefault("Info.homesOther", "<aqua><bold><player>'s homes <dark_gray>» <reset>");
        addDefault("Info.requestAccepted", "<prefix> <gray>You've accepted the teleport request!");
        addDefault("Info.requestAcceptedResponder", "<prefix> <aqua><player></aqua> has accepted the teleport request!");
        addDefault("Info.paymentVault", "<prefix> <gray>You have paid <aqua><amount></aqua> and now have <aqua>balance</aqua>!");
        addDefault("Info.paymentEXP", "<prefix> <gray>You have paid <aqua><amount> EXP Levels</aqua>and now have <aqua><levels></aqua>levels!");
        addDefault("Info.paymentPoints", "<prefix> <gray>You have paid <aqua><amount> EXP Points</aqua>and now have <aqua><points></aqua>points!");
        addDefault("Info.createdWarpSign", "<prefix> <gray>Successfully created the warp sign!");
        addDefault("Info.createdRTPSign", "<prefix> <gray>Successfully created the RandomTP sign!");
        addDefault("Info.createdSpawnSign", "<prefix> <gray>Successfully created the spawn sign!");
        addDefault("Info.tpallRequestSent", "<prefix> <gray>Successfully sent a teleport request to <aqua><amount></aqua>player(s)!");
        addDefault("Info.teleportedToLoc", "<prefix> <gray>Successfully teleported you to <aqua>x</aqua>, <aqua>y</aqua>, <aqua>z</aqua>! (Yaw: <aqua>yaw</aqua>, Pitch: <aqua>pitch</aqua>, World: <aqua>world</aqua>)");
        addDefault("Info.teleportedToLocOther", "<prefix> <gray>Successfully teleported <aqua><player></aqua>to <aqua>x</aqua>, <aqua>y</aqua>, <aqua>z</aqua>! (Yaw: <aqua>yaw</aqua>, Pitch: <aqua>pitch</aqua>, World: <aqua>world</aqua>)");
        addDefault("Info.movedWarp", "<prefix> <gray>Moved <aqua><warp></aqua>to your current location!");
        addDefault("Info.movedHome", "<prefix> <gray>Moved home <aqua><home></aqua>to your current location!");
        addDefault("Info.movedHomeOther", "<prefix> <gray>Moved <aqua><player>'s </aqua> home <aqua><home></aqua>to your location!");
        addDefault("Info.setMainHome", "<prefix> <gray>Made <aqua><home></aqua>your main home!");
        addDefault("Info.setAndMadeMainHome", "<prefix> <gray>Set <aqua><home></aqua>at your current location and made it your main home!");
        addDefault("Info.setMainHomeOther", "<prefix> <gray>Made <aqua><home> <player></aqua>'s main home!");
        addDefault("Info.setAndMadeMainHomeOther", "<prefix> <gray>Set <aqua><home></aqua>for <aqua><player></aqua>at your current location and made it their main home!");
        addDefault("Info.mirroredSpawn", "<prefix> <gray>Mirrored <aqua>from</aqua>'s spawnpoint to <aqua>spawn</aqua>!");
        addDefault("Info.setMainSpawn", "<prefix> <gray>Set the main spawnpoint to <aqua>spawn</aqua>! All players will teleport there if there are no overriding spawns/permissions.");
        addDefault("Info.removedSpawn", "<prefix> <gray>Removed the spawnpoint <aqua>spawn</aqua>!");
        addDefault("Info.setSpawnSpecial", "<prefix> <gray>Set spawnpoint <aqua>spawn</aqua>!");
        addDefault("Info.importStarted", "<prefix> <gray>Starting import from <aqua>plugin</aqua>...");
        addDefault("Info.importFinished", "<prefix> <gray>Finished import from <aqua>plugin</aqua>!");
        addDefault("Info.exportStarted", "<prefix> <gray>Starting export to <aqua>plugin</aqua>...");
        addDefault("Info.exportFinished", "<prefix> <gray>Finished export to <aqua>plugin</aqua>!");
        addDefault("Info.paymentItems", "<prefix> <gray>You have paid <aqua><amount> <type>(s)</aqua> for that teleport!");
        addDefault("Info.updateInfo", """
            <prefix> [&7AdvancedTeleport has an update available!
            Click/hover over this text for more information.]
            (&bCurrent Version » &7<version>|&bNew Version <dark_gray> &7{new-version}|&bTitle <dark_gray> &7<title>
            |https://www.spigotmc.org/resources/advancedteleport.64139/)
        """.trim());
        addDefault("Info.defaultParticlesUpdated", "<prefix> <gray>The default waiting particles have been set to your current particle setup!");
        addDefault("Info.specificParticlesUpdated", "<prefix> <gray>The waiting particles settings for <aqua><type></aqua>have been set to your current particle setup!");

        addDefault("Tooltip.homes", "<prefix> <gray>Teleports you to your home: &b<home>");
        addDefault("Tooltip.warps", "<prefix> <gray>Teleports you to warp: &b<warp>");
        addDefault("Tooltip.location", """

            <aqua>X <dark_gray>» <gray><x>
            <aqua>Y <dark_gray>» <gray><y>
            <aqua>Z <dark_gray>» <gray><z>
            <aqua>World <dark_gray>» <gray><world>""");

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
        addDefault("Descriptions.purge", "Removes all warps or homes for the specified player or world.");
        addDefault("Descriptions.particles", "Ports your current particle selection to the default waiting particles configuration, or a command one.");

        addDefault("Usages.Subcommands.help", "/at help [Category|Page]");
        addDefault("Usages.Subcommands.info", "/at info");
        addDefault("Usages.Subcommands.import", "/at import <Plugin> [All|Homes|LastLocs|Warps|Spawns|Players]");
        addDefault("Usages.Subcommands.export", "/at export <Plugin> [All|Homes|LastLocs|Warps|Spawns|Players]");
        addDefault("Usages.Subcommands.purge", "/at purge <Homes|Warps> <Player|World> <Player Name|World Name>");
        addDefault("Usages.Subcommands.reload", "/at reload");
        addDefault("Usages.Subcommands.particles", "/at particles [Tpa|Tpahere|Home|Tpr|Warp|Spawn|Back]");
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

        addFormsDefault("tpahere", "TPAHere Request", "Select a player to send a TPAHere request to.");
        addFormsDefault("tpa", "TPA Request", "Select a player to send a TPA request to.");
        addFormsDefault("tpa-received", "TPA Request", "The player <player> wants to teleport to you!");
        addDefault("Forms.tpa-received-accept", "Accept");
        addDefault("Forms.tpa-received-deny", "Deny");
        addFormsDefault("tpahere-received", "TPAHere Request", "The player <player> wants you to teleport to them!");
        addDefault("Forms.tpahere-received-accept", "Accept");
        addDefault("Forms.tpahere-received-deny", "Deny");
        addFormsDefault("home", "Homes", "Select a home to teleport to.");
        addFormsDefault("sethome", "Set Home", "Enter a home name.");
        addFormsDefault("delhome", "Delete Home", "Select the home to delete.");
        addFormsDefault("setmainhome", "Set Main Home", "Enter an existing home name or a new one.");
        addFormsDefault("movehome", "Move Home", "Choose the home to be moved.");
        addFormsDefault("warp", "Warps", "Select a warp to teleport to.");
        addFormsDefault("delwarp", "Delete Warp", "Select a warp to delete.");
        addFormsDefault("setwarp", "Set Warp", "Enter a warp name.");
        addFormsDefault("movewarp", "Move Warp", "Select a warp to move.");
        addFormsDefault("tpblock", "Block Player", "Select a player to block.");
        addFormsDefault("tpunblock", "Unblock Player", "Select a player to unblock.");
        addFormsDefault("tpcancel", "Cancel TP Request", "Select a request to cancel.");
        addFormsDefault("tpo", "Teleport", "Select a player to teleport to.");
        addFormsDefault("tpohere", "Teleport Here", "Select a player to teleport to your location.");
    }

    /**
     * <a href="https://github.com/DaRacci/Minix/blob/72bdcd377a66808c6cf79ac647fbd3b886fd909f/Minix-API/src/main/kotlin/dev/racci/minix/api/data/LangConfig.kt#L29">Based on</a>
     *
     * @param path The path to the message
     * @param placeholders An array of placeholders, which are composed of a String (key) followed by a Supplier<String|Component> (value)
     */
    public static @NotNull Component get(
        @NotNull final String path,
        @Nullable final Object... placeholders
    ) throws IllegalArgumentException {
        final var partial = messageCache.get(path);
        if (partial == null) {
            return Component.text("Invalid path: " + path);
        }

        if (placeholders == null || placeholders.length == 0) {
            return partial.getValue();
        }

        if (placeholders.length % 2 != 0) {
            throw new IllegalArgumentException("Placeholders must be in pairs");
        }

        final Pair<String, Object>[] array = new Pair[placeholders.length / 2];
        for (int i = 0; i < placeholders.length / 2; i++) {
            final var key = placeholders[i * 2];
            final var value = placeholders[i * 2 + 1];

            if (key == null) throw new IllegalArgumentException("Placeholder key cannot be null");
            if (value == null) throw new IllegalArgumentException("Placeholder value cannot be null");

            array[i] = new Pair<>(key.toString(), value);
        }

        return partial.get(array);
    }

    // Can't be named “get” because it conflicts with the non-static method.
    public static @NotNull Component getComponent(@NotNull final String path) {
        return get(path, (Object[]) null);
    }

    public static @NotNull String asString(
        @NotNull final String path,
        @Nullable final Object... placeholders
    ) { return PlainTextComponentSerializer.plainText().serialize(get(path, placeholders)); }

    public static @NotNull String asString(@NotNull final String path) {
        return asString(path, (Object[]) null);
    }

    public static void sendMessage(
        @NotNull final CommandSender sender,
        @NotNull final String path,
        @Nullable final Object... placeholders
    ) {
        if (config == null) return;

        if (supportsTitles() && sender instanceof Player player) {
            ConfigSection titles = config.getConfigSection(path + "_title");
            ConfigSection subtitles = config.getConfigSection(path + "_subtitle");
            if (titles != null || subtitles != null) {

                // Fade in, stay, out
                int[] titleInfo = new int[]{0, 0, 0};

                if (titles != null) {
                    titleInfo[0] = titles.getInteger("fade-in");
                    titleInfo[1] = titles.getInteger("length");
                    titleInfo[2] = titles.getInteger("fade-out");
                }

                BukkitRunnable runnable = new BukkitRunnable() {

                    private int current = 0;
                    @Nullable private Component previousTitle = null;
                    @Nullable private Component previousSubtitle = null;

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

                        asAudience(player).showTitle(
                            Title.title(
                                title == null ? previousTitle : (previousTitle = get(title, placeholders)),
                                subtitle == null ? previousSubtitle : (previousSubtitle = get(subtitle, placeholders)),
                                Title.Times.times(
                                    Duration.ofMillis(titleInfo[0] * 50L),
                                    Duration.ofMillis((titleInfo[1] - current) * 50L),
                                    Duration.ofMillis(titleInfo[2] * 50L)
                                )
                            )
                        );

                        current++;
                    }
                };
                titleManager.put(player, runnable);
                runnable.runTaskTimer(CoreClass.getInstance(), 1, 1);
            }
        }

        final var component = Component.text();
        if (config.get(path) instanceof List) {
            config.getStringList(path).forEach(line -> component.append(get(line, placeholders)));
        } else component.append(get(path, placeholders));

        asAudience(sender).sendMessage(component);
    }

    @Contract(pure = true)
    public static @NotNull String contextualPath(
        @NotNull final CommandSender sender,
        @NotNull final UUID target,
        @NotNull final String path
    ) { return sender instanceof OfflinePlayer player && player.getUniqueId() == target ? path : (path + "Other"); }

    @Contract(pure = true)
    public static @NotNull String contextualPath(
        @NotNull final CommandSender sender,
        @NotNull final OfflinePlayer target,
        @NotNull final String path
    ) { return contextualPath(sender, target.getUniqueId(), path); }

    @Contract(pure = true)
    public static void failableContextualPath(
        @NotNull final CommandSender sender,
        @NotNull final UUID target,
        @NotNull final String path,
        @NotNull final String errorPath,
        @NotNull final BooleanSupplier isError,
        final Object... placeholders
    ) {
        final var truePath = isError.getAsBoolean() ? errorPath : contextualPath(sender, target, path);
        sendMessage(sender, truePath, placeholders);
    }

    @Contract(pure = true)
    public static void failableContextualPath(
        @NotNull final CommandSender sender,
        @NotNull final OfflinePlayer target,
        @NotNull final String path,
        @NotNull final String errorPath,
        @NotNull final BooleanSupplier isError,
        final Object... placeholders
    ) { failableContextualPath(sender, target.getUniqueId(), path, errorPath, isError, placeholders); }

    @Contract(pure = true)
    public static void failableContextualPath(
        @NotNull final CommandSender sender,
        @NotNull final ATPlayer target,
        @NotNull final String path,
        @NotNull final String errorPath,
        @NotNull final BooleanSupplier isError,
        final Object... placeholders
    ) { failableContextualPath(sender, target.uuid(), path, errorPath, isError, placeholders); }

    @Contract(pure = true)
    public static void failable(
        @NotNull final CommandSender sender,
        @NotNull final String path,
        @NotNull final String errorPath,
        @NotNull final BooleanSupplier isError,
        final Object... placeholders
    ) {
        final var truePath = isError.getAsBoolean() ? errorPath : path;
        sendMessage(sender, truePath, placeholders);
    }

    @ApiStatus.Internal // TODO: maybe cache this?
    @Contract(pure = true)
    public static @NotNull Audience asAudience(@NotNull final CommandSender sender) {
        if (!PaperLib.isPaper()) {
            if (sender instanceof Player player) {
                return audience.player(player);
            } else return audience.sender(sender);
        }

        return sender; // Paper already implements Audience
    }

    @ApiStatus.Internal // TODO: I think this works, need to double check
    @Contract(pure = true)
    public static @NotNull HoverEventSource<Component> locationBasedTooltip(
        @NotNull final CommandSender sender,
        @NotNull final Location location,
        @NotNull final String path
    ) {
        final var tooltipBuilder = Component.text().append(CustomMessages.getComponent("Tooltip." + path));

        if (ExPermission.hasPermissionOrStar(sender, "at.member." + path + ".location")) {
            tooltipBuilder.append(CustomMessages.get(
                "Tooltip.location",
                "x", location.getBlock(),
                "y", location.getBlockY(),
                "z", location.getBlockZ(),
                "world", location.getWorld().getName()
            ));
        }

        return tooltipBuilder.build().asHoverEvent();
    }

    @ApiStatus.Internal
    @Contract(pure = true)
    public static <T> @NotNull Component getPagesComponent(
        final int page,
        @NotNull final PagedLists<T> pages,
        @NotNull final Function<T, Component> componentSupplier
    ) {
        return Component.join(
            JoinConfiguration.newlines(),
            pages.getContentsInPage(page).stream().map(componentSupplier).toList() // TODO: Ensure order is correct
        );
    }


    private static boolean supportsTitles() {
        try {
            Player.class.getDeclaredMethod("sendTitle", String.class, String.class, int.class, int.class, int.class);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private void addFormsDefault(String command, String title, String description) {
        addDefault("Forms." + command + "-title", title);
        addDefault("Forms." + command + "-description", description);
    }

    /**
     * This function tests if sender is a floodgateplayer
     * @param sender the CommandSender
     * @return true if sender is a floodgateplayer
     */
    @Contract(pure = true)
    private static boolean isFloodgate(@NotNull final CommandSender sender){
        /*
         * if floodgate is installed, we test if it is a floodgate player. This solves the problem of different prefixes.
         * We note, that this is more relyable than the previous method and solves any problem, beside the fact that the
         * sysadmin has to install floodgate on the backendservers in a bungeecord network. But this should be considered the easiest way.
         */
        if (!PluginHookManager.get().floodgateEnabled()) return false;

        try {
            FloodgateApi instance = FloodgateApi.getInstance();
            if (instance.isFloodgateId(((Player) sender).getUniqueId())) return true;
        } catch (final Exception ignored) {}

        return false;
    }
}
