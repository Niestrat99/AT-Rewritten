package io.github.niestrat99.advancedteleport.config;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.api.NamedLocation;
import io.github.niestrat99.advancedteleport.api.data.ATException;
import io.github.niestrat99.advancedteleport.data.PartialComponent;
import io.github.niestrat99.advancedteleport.extensions.ExPermission;
import io.github.niestrat99.advancedteleport.managers.PluginHookManager;
import io.github.niestrat99.advancedteleport.utilities.PagedLists;
import io.github.thatsmusic99.configurationmaster.api.ConfigSection;
import io.github.thatsmusic99.configurationmaster.impl.CMConfigSection;
import io.papermc.lib.PaperLib;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEventSource;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.OfflinePlayer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.geysermc.floodgate.api.FloodgateApi;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO: Load all components on initialization and reload and formatRaw at that point.
public final class CustomMessages extends ATConfig {

    public static CustomMessages config;
    private static HashMap<CommandSender, BukkitRunnable> titleManager;
    private static HashMap<CommandSender, BukkitRunnable> actionBarManager;
    private static HashMap<CommandSender, BukkitRunnable> soundManager;
    @NotNull private static ImmutableMap<String, PartialComponent> messageCache = ImmutableMap.of();
    @NotNull private static ImmutableSortedSet<String> prefixes = ImmutableSortedSet.of();
    @Nullable private static BukkitAudiences audience;

    public CustomMessages() throws Exception {
        super("custom-messages.yml");
        config = this;
        titleManager = new HashMap<>();
        actionBarManager = new HashMap<>();
        soundManager = new HashMap<>();

        populate();

        if (!PaperLib.isPaper()) {
            audience = BukkitAudiences.create(CoreClass.getInstance());
        }
    }

    @Override
    public void reload() throws Exception {
        super.reload();
        populate();
    }

    @Override
    public void addDefaults() {

        // Add notice
        addComment("""
                This messages file uses MiniMessage formatting, a new form of message formatting for newer versions of MC.
                More information about this formatting can be found here: https://docs.advntr.dev/minimessage/format.html#\s
                If you prefer to use the Legacy Code format (i.e. &a, &b, etc.) then you can still use that format.
                
                It is important to note though that this format may be subject to removal in a future version of AT, however nothing is set in stone yet.""");

        makeSectionLenient("Common");
        addDefault(
            "Common.prefixes",
            List.of("<aqua>↑</aqua> <dark_gray>»</dark_gray>"),
            """
            The prefixes for messages, the first element of this list will be usable as <prefix>,
            with each element after that being usable as <prefix:index> with index being the items index in the list.
            """.trim()
        );

        makeSectionLenient("Teleport");
        addDefault("Teleport.eventBeforeTP" , "<prefix> <gray>Teleporting in <aqua><countdown> seconds</aqua>, please do not move!");

        addComment("Teleport.eventBeforeTP_title", "This is an example use for titles and subtitles in the plugin." +
                "\nThis feature is supported only if you're on version 1.8.8 or newer.");
        addExample("Teleport.eventBeforeTP_title.length" , 80 , "How many seconds (in ticks) the header should last. This is not including fading.");
        addExample("Teleport.eventBeforeTP_title.fade-in" , 0 , "How many seconds (in ticks) the header should take to fade in.");
        addExample("Teleport.eventBeforeTP_title.fade-out" , 10 , "How many seconds (in ticks) the header should take to fade out.");
        addExample("Teleport.eventBeforeTP_title.0", "<gray><b>Teleporting...");
        addExample("Teleport.eventBeforeTP_title.20", "<aqua>></aqua> <gray><b>Teleporting...</b></gray> <aqua><");
        addExample("Teleport.eventBeforeTP_title.40", "<aqua>>></aqua> <gray><b>Teleporting...</b></gray> <aqua><<");
        addExample("Teleport.eventBeforeTP_title.60", "<aqua>>>> <b><yellow>Teleported!</yellow></b> <aqua><<<");
        addExample("Teleport.eventBeforeTP_subtitle.0", "<aqua>Please do not move!");
        addExample("Teleport.eventBeforeTP_subtitle.60", "");

        addDefault("Teleport.eventBeforeTPMovementAllowed", "<prefix> <gray>Teleporting in <aqua><countdown></aqua> seconds!");
        addDefault("Teleport.eventTeleport", "<prefix> <gray>Teleporting...");
        addDefault("Teleport.eventMovement", "<prefix> <gray>Teleport has been cancelled due to movement.");
        addDefault("Teleport.eventMovement_title.length", 60);
        addDefault("Teleport.eventMovement_title.fade-in", 0);
        addDefault("Teleport.eventMovement_title.fade-out", 10);
        addDefault("Teleport.eventMovement_title.0", "<yellow><b>! <red>Cancelled</red> !");
        addDefault("Teleport.teleportingToSpawn", "<prefix> <gray>Teleporting you to spawn!");
        addDefault("Teleport.teleporting", "<prefix> <gray>Teleporting to <aqua><player></aqua>!");
        addDefault("Teleport.teleportingToHome", "<prefix> <gray>Teleporting to <aqua><home></aqua>!");
        addDefault("Teleport.teleportingToHomeOther", "<prefix> <gray>Teleporting to <aqua><player></aqua>'s home, <aqua><home></aqua>!");
        addDefault("Teleport.teleportingToWarp", "<prefix> <gray>Teleporting you to <aqua><warp></aqua>!");
        addDefault("Teleport.teleportingPlayerToSelf", "<prefix> <gray>Teleporting <aqua><player></aqua> to you!");
        addDefault("Teleport.teleportingSelfToPlayer", "<prefix> <gray>Teleporting you to <aqua><player></aqua>!");
        addDefault("Teleport.teleportingToRandomPlace", "<prefix> <gray>Teleporting you to a random place!");
        addDefault("Teleport.teleportingToLastLoc", "<prefix> <gray>Teleporting to your last location!");
        addDefault("Teleport.teleportedToOfflinePlayer", "<prefix> <gray>Teleported to offline player <aqua><player></aqua>!");
        addDefault("Teleport.teleportedOfflinePlayerHere", "<prefix> <gray>Teleported offline player <aqua><player></aqua> to your location!");

        makeSectionLenient("Error");
        addDefault("Error.noPermission", "<prefix> <gray>You do not have permission to use this command!");
        addDefault("Error.noPermissionSign", "<prefix> <gray>You do not have permission to make this sign!");
        addDefault("Error.featureDisabled", "<prefix> <gray>This feature has been disabled!");
        addDefault("Error.noRequests", "<prefix> <gray>You do not have any pending requests!");
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
        addDefault("Error.noRequestsFromPlayer", "<prefix> <gray>You don't have any pending requests from <aqua><player></aqua>!");
        addDefault("Error.noRequests", "<prefix> <gray>You don't have any pending requests!");
        addDefault("Error.invalidPageNo", "<prefix> <gray>You've inserted an invalid page number!");
        addDefault("Error.noHomeInput", "<prefix> <gray>You have to include a home name!");
        addDefault("Error.noSuchHome", "<prefix> <gray>This home doesn't exist!");
        addDefault("Error.noBedHome", "<prefix> <gray>You don't have any bed spawn set!");
        addDefault("Error.noBedHomeOther", "<prefix> <aqua><player></aqua> <gray>doesn't have a bed spawn set!");
        addDefault("Error.reachedHomeLimit", "<prefix> <gray>You can't set any more homes!");
        addDefault("Error.homeAlreadySet", "<prefix> <gray>You already have a home called <aqua><home></aqua>!");
        addDefault("Error.noWarpInput", "<prefix> <gray>You have to include the warp's name!");
        addDefault("Error.noSuchWarp", "<prefix> <gray>That warp doesn't exist!");
        addDefault("Error.warpAlreadySet", "<prefix> <gray>There is already a warp called <aqua><warp></aqua>!");
        addDefault("Error.noSuchWorld", "<prefix> <gray>That world doesn't exist!");
        addDefault("Error.noLocation", "<prefix> <gray>You don't have any location to teleport back to!");
        addDefault("Error.notAPlayer", "<prefix> <gray>You must be a player to run this command!");
        addDefault("Error.noHomes", "<prefix> <gray>You haven't got any homes!");
        addDefault("Error.noHomesOther", "<prefix> <aqua><player></aqua> <gray>hasn't got any homes!");
        addDefault("Error.tooFarAway", "<prefix> <gray>The teleport destination is too far away so you can not teleport there!");
        addDefault("Error.noRequestsSent", "<prefix> <gray>Couldn't send a request to anyone :(");
        addDefault("Error.onCountdown","<prefix> <gray>You can't use this command whilst waiting to teleport!");
        addDefault("Error.noPermissionWarp", "<prefix> <gray>You can't warp to <aqua><warp></aqua>!");
        addDefault("Error.cantTPToWorld", "<prefix> <gray>You can't randomly teleport in that world!");
        addDefault("Error.cantTPToWorldLim", "<prefix> <gray>You can't teleport to <aqua><world></aqua>!");
        addDefault("Error.tooFewArguments", "<prefix> <gray>Too few arguments!");
        addDefault("Error.invalidArgs", "<prefix> <gray>Invalid arguments!");
        addDefault("Error.cantTPToPlayer", "<prefix> <gray>You can't request a teleportation to <aqua><player></aqua>!");
        addDefault("Error.noWarps", "<prefix> <gray>There are no warps as of currently!");
        addDefault("Error.noAccessHome", "<prefix> <gray>You cannot access <aqua><home></aqua> as of currently!");
        addDefault("Error.moveHomeFail", "<prefix> <gray>The home has been moved but the data has not been stored successfully. The plugin will try to fix this itself.");
        addDefault("Error.setMainHomeFail", "<prefix> <gray>The main home has been set but the data has not been stored successfully. The plugin will try to fix this itself.");
        addDefault("Error.deleteHomeFail", "<prefix> <gray>The home has been deleted but the data has not been stored successfully. The plugin will try to fix this itself.");
        addDefault("Error.setHomeFail", "<prefix> <gray>The home has been set but the data has not been stored successfully. The plugin will try to fix this itself.");
        addDefault("Error.deleteWarpFail", "<prefix> <gray>The warp has been set but the data has not been stored successfully. The plugin will try to fix this itself.");
        addDefault("Error.purgeWarpsFail", "<prefix> <gray>Could not purge warps. Please check the console for more information.");
        addDefault("Error.purgeHomesFail", "<prefix> <gray>Could not purge homes. Please check the console for more information.");
        addDefault("Error.homesNotLoaded", "<prefix> <gray>Homes for this player haven't loaded yet, please wait a little bit (even just a second) before trying this again!");
        addDefault("Error.noOfflineLocation", "<prefix> <gray>No offline location was found for <aqua><player></aqua>!");
        addDefault("Error.failedOfflineTeleport", "<prefix> <gray>Failed to teleport to offline player <aqua><player></aqua>!");
        addDefault("Error.failedOfflineTeleportHere", "<prefix> <gray>Failed to teleport offline player <aqua><player></aqua> to your location!");
        addDefault("Error.alreadySearching", "<prefix> <gray>Already searching for a location to teleport to!");
        addDefault("Error.mirrorSpawnNoArguments", "<prefix> <gray>No worlds/spawn points have been specified!");
        addDefault("Error.mirrorSpawnLackOfArguments", "<prefix> <gray>You must be a player to only specify one world - please specify a world and a spawnpoint to mirror players to!");
        addDefault("Error.noSuchSpawn", "<prefix> <gray>There is no such spawn called <aqua><spawn></aqua>!");
        addDefault("Error.cannotSetMainSpawn", "<prefix> <gray>You can only make existing spawnpoints into the main spawnpoint rather than create new ones!");
        addDefault("Error.cannotSetMainSpawnConsole", "<prefix> <gray>You can only make existing spawnpoints into the main spawnpoint rather than create new ones since you are not a player!");
        addDefault("Error.nonAlphanumericSpawn", "<prefix> <gray>Spawnpoints need to be alphanumeric!");
        addDefault("Error.removeSpawnNoArgs", "<prefix> <gray>You have to specify a spawnpoint to remove!");
        addDefault("Error.noSuchPlugin", "<prefix> <gray>This plugin is not supported for importing/exporting yet!");
        addDefault("Error.cantImport", "<prefix> <gray>Can't import plugin data from <aqua><plugin></aqua> (make sure it's enabled and by the correct authors)!");
        addDefault("Error.cantExport", "<prefix> <gray>Can't export plugin data from <aqua><plugin></aqua> (make sure it's enabled and by the correct authors)!");
        addDefault("Error.noPluginSpecified", "<prefix> <gray>You need to specify a plugin to import/export from!");
        addDefault("Error.invalidOption", "<prefix> <gray>That is not a valid option to import/export!");
        addDefault("Error.notEnoughItems", """
            <prefix> <gray>You do not have enough items to teleport there!
            <prefix> <gray>You need at least <aqua><amount></aqua> <type>(s)!
        """.trim());
        addDefault("Error.mirrorSpawnFail", "<prefix> <gray>Failed to mirror <aqua><from></aqua>'s spawnpoint to <aqua><spawn></aqua>!");
        addDefault("Error.removeSpawnFail", "<prefix> <gray>Failed to remove the spawnpoint <aqua><spawn></aqua>!");
        addDefault("Error.setMainSpawnFail", "<prefix> <gray>Failed to set the main spawnpoint <aqua><spawn></aqua>!");
        addDefault("Error.blockFail", "<prefix> <gray>Failed to save the block against <aqua><player></aqua>!");
        addDefault("Error.unblockFail", "<prefix> <gray>Failed to save the block removal against <aqua><player></aqua>!");
        addDefault("Error.noParticlePlugins", "<prefix> <gray>There are no particle plugins on this server! You need at least one (PlayerParticles) to use this command.");
        addDefault("Error.notEnoughArgs", "<prefix> <gray>You haven't specified enough arguments to run this command!");
        addDefault("Error.failedMapIconUpdate", "<prefix> <gray>Failed to update the map icon! Please check the console for more information.");

        addDefault("Error.setWarpFail", "<prefix> <gray>Failed to set the warp <warp>!");
        addDefault("Error.teleportFailed", "<prefix> <gray>Sorry, we couldn't teleport you :(");
        addDefault("Error.randomLocFailed", "<prefix> <gray>Sorry, we couldn't find a location to teleport you to :(");

        makeSectionLenient("Info");
        addDefault("Info.tpOff", "<prefix> <gray>Successfully disabled teleport requests!");
        addDefault("Info.tpOn", "<prefix> <gray>Successfully enabled teleport requests!");
        addDefault("Info.tpAdminOff", "<prefix> <gray>Successfully disabled teleport requests for <aqua><player></aqua>!");
        addDefault("Info.tpAdminOn", "<prefix> <gray>Successfully enabled teleport requests for <aqua><player></aqua>!");
        // TODO: Possible "Common" components for the hover and click events?
        addDefault("Info.requestSent", """
            <prefix> <gray>Successfully sent request to <aqua><player></aqua>!
            <prefix> <gray>They've got <aqua><lifetime></aqua> to respond!
            <prefix> <gray>To cancel the request use <aqua>/tpcancel</aqua> to cancel it.

                                <click:run_command:'tpcancel <player>'><hover:show_text:'<red>Click here to cancel the request.'><gray><bold>[CANCEL]</hover></click>
        """.stripIndent());
        addDefault("Info.tpaRequestReceived", """
            <prefix> <gray>The player <aqua><player></aqua> wants to teleport to you!
            <prefix> <gray>If you want to accept it, use <aqua>/tpayes</aqua>, but if not, use <aqua>/tpano</aqua>.
            <prefix> <gray>You've got <aqua><lifetime></aqua> to respond to it!

                                <click:run_command:'tpayes <player>'><hover:show_text:'<green>Click here to accept the request.'><green><bold>[ACCEPT]</hover></click>             <click:run_command:/tpano <player>><hover:show_text:'<red>Click here to deny the request.><red><bold>[DENY]</red></bold></hover></click>
        """.stripIndent());
        addDefault("Info.tpaRequestHere", """
            <prefix> <gray>The player <aqua><player></aqua> wants to teleport you to them!
            <prefix> <gray>If you want to accept it, use <aqua>/tpayes</aqua>, but if not, use <aqua>/tpano</aqua>.
            <prefix> <gray>You've got <aqua><lifetime> seconds</aqua> to respond to it!

                              <click:run_command:'/tpayes <player>'><hover:show_text:'<green>Click here to accept the request.'><green><bold>[ACCEPT]</bold></hover></click>             <click:run_command:/tpano <player>><hover:show_text:'<red>Click here to deny the request.><red><bold>[DENY]</red></bold></hover></click>
        """.stripIndent());
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
        addDefault("Info.setHomeOther", "<prefix> <gray>Successfully set the home <aqua><home></aqua> for <aqua><player></aqua>!");
        addDefault("Info.setSpawn", "<prefix> <gray>Successfully set the spawnpoint!");
        addDefault("Info.setWarp", "<prefix> <gray>Successfully set the warp <aqua><warp></aqua>!");
        addDefault("Info.deletedWarp", "<prefix> <gray>Successfully deleted the warp <aqua><warp></aqua>!");
        addDefault("Info.purgeWarpsWorld", "<prefix> <gray>Successfully purged warps in <aqua><world></aqua>!");
        addDefault("Info.purgeWarpsCreator", "<prefix> <gray>Successfully purged warps created by <aqua><player></aqua>!");
        addDefault("Info.purgeHomesWorld", "<prefix> <gray>Successfully purged homes in <aqua><world></aqua>!");
        addDefault("Info.purgeHomesCreator", "<prefix> <gray>Successfully purged homes created for <aqua><player></aqua>!");
        addDefault("Info.searching", "<prefix> <gray>Searching for a location...");
        addDefault("Info.unblockPlayer", "<prefix> <gray>Successfully unblocked <aqua><player></aqua>!");
        addDefault("Info.reloadingConfig", "<prefix> <gray>Reloading <aqua>AdvancedTeleport</aqua>'s config...");
        addDefault("Info.reloadedConfig", "<prefix> <gray>Finished reloading the config!");
        addDefault("Info.warps", "<aqua><bold>Warps <dark_gray>» <reset>");
        addDefault("Info.homes", "<aqua><bold>Homes <dark_gray>» <reset>");
        addDefault("Info.homesOther", "<aqua><bold><player>'s homes <dark_gray>» <reset>");
        addDefault("Info.requestAccepted", "<prefix> <gray>You've accepted the teleport request!");
        addDefault("Info.requestAcceptedResponder", "<prefix> <aqua><player></aqua> has accepted the teleport request!");
        addDefault("Info.paymentVault", "<prefix> <gray>You have paid <aqua><amount></aqua> and now have <aqua><balance></aqua>!");
        addDefault("Info.paymentEXP", "<prefix> <gray>You have paid <aqua><amount> EXP Levels</aqua>and now have <aqua><levels></aqua>levels!");
        addDefault("Info.paymentPoints", "<prefix> <gray>You have paid <aqua><amount> EXP Points</aqua>and now have <aqua><points></aqua>points!");
        addDefault("Info.createdWarpSign", "<prefix> <gray>Successfully created the warp sign!");
        addDefault("Info.createdRTPSign", "<prefix> <gray>Successfully created the RandomTP sign!");
        addDefault("Info.createdSpawnSign", "<prefix> <gray>Successfully created the spawn sign!");
        addDefault("Info.tpallRequestSent", "<prefix> <gray>Successfully sent a teleport request to <aqua><amount></aqua>player(s)!");
        addDefault("Info.teleportedToLoc", "<prefix> <gray>Successfully teleported you to <aqua><x></aqua>, <aqua><y></aqua>, <aqua><z></aqua>! (Yaw: <aqua><yaw></aqua>, Pitch: <aqua><pitch></aqua>, World: <aqua><world></aqua>)");
        addDefault("Info.teleportedToLocOther", "<prefix> <gray>Successfully teleported <aqua><player></aqua>to <aqua><x></aqua>, <aqua><y></aqua>, <aqua><z></aqua>! (Yaw: <aqua><yaw></aqua>, Pitch: <aqua><pitch></aqua>, World: <aqua><world></aqua>)");
        addDefault("Info.movedWarp", "<prefix> <gray>Moved <aqua><warp></aqua>to your current location!");
        addDefault("Info.movedHome", "<prefix> <gray>Moved home <aqua><home></aqua>to your current location!");
        addDefault("Info.movedHomeOther", "<prefix> <gray>Moved <aqua><player>'s </aqua> home <aqua><home></aqua>to your location!");
        addDefault("Info.setMainHome", "<prefix> <gray>Made <aqua><home></aqua>your main home!");
        addDefault("Info.setAndMadeMainHome", "<prefix> <gray>Set <aqua><home></aqua>at your current location and made it your main home!");
        addDefault("Info.setMainHomeOther", "<prefix> <gray>Made <aqua><home> <player></aqua>'s main home!");
        addDefault("Info.setAndMadeMainHomeOther", "<prefix> <gray>Set <aqua><home></aqua>for <aqua><player></aqua>at your current location and made it their main home!");
        addDefault("Info.mirroredSpawn", "<prefix> <gray>Mirrored <aqua><from></aqua>'s spawnpoint to <aqua><spawn></aqua>!");
        addDefault("Info.setMainSpawn", "<prefix> <gray>Set the main spawnpoint to <aqua><spawn></aqua>! All players will teleport there if there are no overriding spawns/permissions.");
        addDefault("Info.removedSpawn", "<prefix> <gray>Removed the spawnpoint <aqua><spawn></aqua>!");
        addDefault("Info.setSpawnSpecial", "<prefix> <gray>Set spawnpoint <aqua><spawn></aqua>!");
        addDefault("Info.importStarted", "<prefix> <gray>Starting import from <aqua><plugin></aqua>...");
        addDefault("Info.importFinished", "<prefix> <gray>Finished import from <aqua><plugin></aqua>!");
        addDefault("Info.exportStarted", "<prefix> <gray>Starting export to <aqua><plugin></aqua>...");
        addDefault("Info.exportFinished", "<prefix> <gray>Finished export to <aqua><plugin></aqua>!");
        addDefault("Info.paymentItems", "<prefix> <gray>You have paid <aqua><amount> <type>(s)</aqua> for that teleport!");
        addDefault("Info.updateInfo", """
            <prefix> <hover:show_text:'<aqua>Current Version <dark_gray>» <gray><version>
            <aqua>New Version <dark_gray>» <gray><new-version>
            <aqua>Title <dark_gray>» <gray><title>'><click:open_url:'https://www.spigotmc.org/resources/advancedteleport.64139/'><gray>AdvancedTeleport has an update available! Click/hover over this text for more information.</click></hover>""".trim());
        addDefault("Info.defaultParticlesUpdated", "<prefix> <gray>The default waiting particles have been set to your current particle setup!");
        addDefault("Info.specificParticlesUpdated", "<prefix> <gray>The waiting particles settings for <aqua><type></aqua>have been set to your current particle setup!");
        addDefault("Info.mapIconUpdateClickTooltip", "<prefix> <gray>Updated click tooltip for <type> <aqua><name></aqua>! The map should update shortly.");
        addDefault("Info.mapIconUpdateHoverTooltip", "<prefix> <gray>Updated hover tooltip for <type> <aqua><name></aqua>! The map should update shortly.");
        addDefault("Info.mapIconUpdateIcon", "<prefix> <gray>Updated the icon for <type> <aqua><name></aqua>! The map should update shortly.");
        addDefault("Info.mapIconUpdateSize", "<prefix> <gray>Updated the icon size for <type> <aqua><name></aqua>! The map should update shortly.");
        addDefault("Info.mapIconUpdateVisibility", "<prefix> <gray>Updated the icon visibility for <type> <aqua><name></aqua>! The map should update shortly.");
        addDefault("Info.mirrorSpawnSame", "<prefix> <gray>The spawns for <aqua><from></aqua> and <aqua><spawn></aqua> already to go the same place! Don't worry :)");

        addDefault("Tooltip.homes", "<prefix> <gray>Teleports you to your home: <aqua><home>");
        addDefault("Tooltip.warps", "<prefix> <gray>Teleports you to warp: <aqua><warp>");
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
        addDefault("Descriptions.Subcommands.clearcache", "Clears the RTP cache.");
        addDefault("Descriptions.Subcommands.map.setclicktooltip", "Sets the tooltip of an AT icon in a map plugin when it is clicked (excluding Dynmap).");
        addDefault("Descriptions.Subcommands.map.sethovertooltip", "Sets the tooltip of an AT icon in a map plugin when it is hovered over.");
        addDefault("Descriptions.Subcommands.map.seticon", "Sets the image of an AT icon in a map plugin.");
        addDefault("Descriptions.Subcommands.map.setsize", "Sets the size of an AT icon in a map plugin (excluding Dynmap).");
        addDefault("Descriptions.Subcommands.map.setvisible", "Sets the visibility of an AT icon in a map plugin when it is clicked.");
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
        addDefault("Usages.Subcommands.clearcache", "/at clearcache [World]");
        addDefault("Usages.Subcommands.particles", "/at particles [Tpa|Tpahere|Home|Tpr|Warp|Spawn|Back]");
        addDefault("Usages.Subcommands.map.setclicktooltip", "/at map setclicktooltip <Home|Warp|Spawn> [Home Owner] <Tooltip>");
        addDefault("Usages.Subcommands.map.sethovertooltip", "/at map sethovertooltip <Home|Warp|Spawn> [Home Owner] <Tooltip>");
        addDefault("Usages.Subcommands.map.seticon", "/at map seticon <Home|Warp|Spawn> [Home Owner] <Image Name>");
        addDefault("Usages.Subcommands.map.setsize", "/at map setsize <Home|Warp|Spawn> [Home Owner] <Image Size>");
        addDefault("Usages.Subcommands.map.setvisible", "/at map setvisible <Home|Warp|Spawn> [Home Owner] <true|false>");
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

    @Override
    public void moveToNew() {
        moveTo("Error.noHomesOtherPlayer", "Error.noHomesOther");
    }

    /**
     * <a href="https://github.com/DaRacci/Minix/blob/72bdcd377a66808c6cf79ac647fbd3b886fd909f/Minix-API/src/main/kotlin/dev/racci/minix/api/data/LangConfig.kt#L29">Based on</a>
     *
     * @param path The path to the message
     * @param placeholders An array of placeholders, which are composed of a String (key) followed by a Supplier<String|Component> (value)
     * @throws IllegalArgumentException If the given path doesn't exist or if the placeholders aren't in pairs.
     */
    public static @NotNull Component get(
        @NotNull final String path,
        @Nullable final TagResolver... placeholders
    ) throws IllegalArgumentException {
        if (config == null) throw new IllegalStateException("Config not initialized");

        final var partial = messageCache.get(path);
        if (partial == null) return Component.empty();

        if (placeholders == null || placeholders.length == 0) {
            return partial.getValue();
        }

        return partial.get(placeholders);
    }

    // Can't be named “get” because it conflicts with the non-static method.
    public static @NotNull Component getComponent(
            @NotNull final String path,
            @Nullable final TagResolver... placeholders
    ) {
        return get(path, placeholders);
    }

    public static @NotNull Component translate(
            @NotNull final String text,
            @NotNull final TagResolver... placeholders
    ) {
        return MiniMessage.miniMessage().deserialize(translateLegacy(text), placeholders);
    }

    public static @NotNull String asString(
        @NotNull final String path,
        @Nullable final TagResolver... placeholders
    ) { return PlainTextComponentSerializer.plainText().serialize(get(path, placeholders)); }

    public static @NotNull String asString(@NotNull final String path) {
        return asString(path, (TagResolver[]) null);
    }

    public static void sendMessage(
        @NotNull final CommandSender sender,
        @NotNull String path,
        @NotNull final Function<String, String> preProcess,
        @NotNull final TagResolver... placeholders
    ) {
        if (config == null) return;
        if (sender instanceof Player player) {

            final String mainPath = path;

            handleSpecialMessage(player, path + "_actionbar", (content -> asAudience(player).sendActionBar(translate(content, placeholders))), actionBarManager);
            handleSpecialMessage(player, path + "_sound", (sound -> sendSound(player, sound, mainPath)), soundManager);

            @Nullable ConfigSection titles = config.getConfigSection(path + "_title");
            @Nullable ConfigSection subtitles = config.getConfigSection(path + "_subtitle");
            if (titles != null || subtitles != null) {

                // Debug
                CoreClass.debug("Found special message format - titles: " + titles + ", subtitles: " + subtitles);

                // Fade in, stay, out
                int[] titleInfo = new int[]{0, 0, 0};

                if (titles != null) {
                    titleInfo[0] = titles.getInteger("fade-in");
                    titleInfo[1] = titles.getInteger("length");
                    titleInfo[2] = titles.getInteger("fade-out");
                }

                // Handle t
                BukkitRunnable titleRunnable = new BukkitRunnable() {

                    private int current = 0;
                    private @Nullable Component previousTitle = null;
                    private @Nullable Component previousSubtitle = null;

                    @Override
                    public void run() {
                        if (current == titleInfo[1] || titleManager.get(player) != this) {
                            cancel();
                            return;
                        }

                        String title = null;
                        String subtitle = null;

                        if (titles != null) title = titles.getString(String.valueOf(current));
                        if (subtitles != null) subtitle = subtitles.getString(String.valueOf(current));

                        asAudience(player).showTitle(
                                Title.title(
                                        title == null ? (previousTitle == null ? Component.empty() : previousTitle) : (previousTitle = get(title, placeholders)),
                                        subtitle == null ? (previousSubtitle == null ? Component.empty() : previousSubtitle) : (previousSubtitle = get(subtitle, placeholders)),
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

                titleManager.put(player, titleRunnable);
                titleRunnable.runTaskTimer(CoreClass.getInstance(), 1, 1);
            }
        } else {
            String raw = config.getString(path);
            if (MainConfig.get().SEND_ACTIONBAR_TO_CONSOLE.get() && (raw == null || raw.isEmpty())) path += "_actionbar";
        }

        var component = Component.text();
        if (config.get(path) instanceof List) {
            config.getStringList(path).forEach(line -> component.append(translate(preProcess.apply(line), placeholders)));
        } else if (config.getString(path) != null && !config.getString(path).isEmpty()) component.append(get(path, placeholders));

        if (component.content().isEmpty() && component.children().size() == 0) return;
        asAudience(sender).sendMessage(component);
    }

    @Contract(pure = true)
    private static void appendNonEmpty(
            @NotNull final TextComponent.Builder base,
            @NotNull final Function<String, String> preProcess,
            @NotNull final String line,
            @NotNull final TagResolver... placeholders
    ) {
        if (line.isEmpty()) return;
        Component component = translate(preProcess.apply(line), placeholders);
        base.append(component);

        var component2 = Component.text();
        component2.append(component);
    }

    public static void sendMessage(
        @NotNull final CommandSender sender,
        @NotNull final String path,
        @NotNull final TagResolver... placeholders
    ) {
        sendMessage(sender, path, Function.identity(), placeholders);
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
        @Nullable final Throwable error,
        final TagResolver... placeholders
    ) {
        final var truePath = error != null ? errorPath : contextualPath(sender, target, path);
        sendMessage(sender, truePath, placeholders);

        // If there was an error, print it
        if (error != null && !(error instanceof ATException)) error.printStackTrace();
    }

    @Contract(pure = true)
    public static void failableContextualPath(
        @NotNull final CommandSender sender,
        @NotNull final OfflinePlayer target,
        @NotNull final String path,
        @NotNull final String errorPath,
        @Nullable final Throwable error,
        final TagResolver... placeholders
    ) { failableContextualPath(sender, target.getUniqueId(), path, errorPath, error, placeholders); }

    @Contract(pure = true)
    public static void failableContextualPath(
        @NotNull final CommandSender sender,
        @NotNull final ATPlayer target,
        @NotNull final String path,
        @NotNull final String errorPath,
        @Nullable final Throwable error,
        final TagResolver... placeholders
    ) { failableContextualPath(sender, target.uuid(), path, errorPath, error, placeholders); }

    @Contract(pure = true)
    public static void failable(
        @NotNull final CommandSender sender,
        @NotNull final String path,
        @NotNull final String errorPath,
        @Nullable final Throwable error,
        final TagResolver... placeholders
    ) {
        final var truePath = error != null ? errorPath : path;
        sendMessage(sender, truePath, placeholders);

        // If there was an error, print it
        if (error != null && !(error instanceof ATException)) error.printStackTrace();
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
        @NotNull final NamedLocation location,
        @NotNull final String path
    ) {
        final var tooltipBuilder = Component.text().append(
                CustomMessages.get("Tooltip." + path,
                        Placeholder.unparsed("home", location.getName()),
                        Placeholder.unparsed("warp", location.getName())
                ));

        if (ExPermission.hasPermissionOrStar(sender, "at.member." + path + ".location")) {
            tooltipBuilder.append(CustomMessages.get(
                "Tooltip.location",
                    Placeholder.unparsed("x", String.valueOf(location.getLocation().getBlockX())),
                    Placeholder.unparsed("y", String.valueOf(location.getLocation().getBlockY())),
                    Placeholder.unparsed("z", String.valueOf(location.getLocation().getBlockZ())),
                    Placeholder.unparsed("world", location.getLocation().getWorld().getName())
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

    @Contract(pure = true)
    private void populate() {
        prefixes = ImmutableSortedSet.copyOf(this.getStringList("Common.prefixes"));

        final var cacheBuilder = ImmutableMap.<String, PartialComponent>builder();
        final var keys = new ArrayList<String>();

        // Is there a better way to do this?
        // This seems too complicated.
        final var queue = new ArrayDeque<>(getKeys(false));
        while (queue.peek() != null) {
            final var rootKey = queue.pop();
            if (getConfigSection(rootKey) instanceof CMConfigSection configSection) {
                configSection.getKeys(false).stream()
                        .map(key -> rootKey + "." + key)
                        .forEach(queue::addFirst);
            } else keys.add(rootKey);
        }

        keys.forEach(key -> {
            final var rawValue = this.get(key, this.defaults.get(key));
            final var component = PartialComponent.of(translateLegacy(rawValue.toString()));
            component.formatRaw(prefixes);
            cacheBuilder.put(key, component);
        });

        messageCache = cacheBuilder.build();
    }

    @Contract(pure = true)
    private static void handleSpecialMessage(
            @NotNull Player player,
            @NotNull String id,
            @NotNull Consumer<String> consumer,
            @NotNull HashMap<CommandSender, BukkitRunnable> runnableTracker
    ) {

        // If the section does not exist, stop there
        if (!config.contains(id)) return;

        // If it's just a string, then consume that
        if (config.get(id) instanceof String) {
            consumer.accept(config.getString(id));
            return;
        }

        // If it's not a section though, then stop there
        if (config.getConfigSection(id) == null) return;
        ConfigSection section = config.getConfigSection(id);

        // Create the BukkitRunnable
        BukkitRunnable runnable = new BukkitRunnable() {

            private final Queue<String> times = new ArrayDeque<>(section.getKeys(false));
            private int current = 0;

            @Override
            public void run() {

                // If the times queue is empty stop there
                if (times.isEmpty() || runnableTracker.get(player) != this) {
                    cancel();
                    return;
                }

                // If the next element is equal to the current timer, use that
                if (!String.valueOf(current++).equals(times.peek())) return;

                // Get the message to be sent and send it
                String content = section.getString(times.poll());
                if (content == null) return;
                consumer.accept(content);
            }
        };

        // Add it to the hashmap and run it
        runnableTracker.put(player, runnable);
        runnable.runTaskTimer(CoreClass.getInstance(), 1, 1);
    }

    @Contract(pure = true)
    private static void sendSound(
            @NotNull final Player player,
            @NotNull final String sound,
            @NotNull final String id
    ) {

        // Set default variables
        float volume = 1.0f;
        float pitch = 1.0f;

        // Check the formatting of the sound
        String[] rawSound = sound.split(";");
        if (rawSound.length == 0) return;

        // Set the volume and pitch
        if (rawSound.length == 2) volume = Float.parseFloat(rawSound[1]);
        if (rawSound.length >= 3) pitch = Float.parseFloat(rawSound[2]);

        // Get the sound type
        if (rawSound[0].matches("([a-z0-9_\\-.]+:)?[a-z0-9_\\-./]+")) {
            try {
                player.playSound(Sound.sound(Key.key(rawSound[0]), Sound.Source.NEUTRAL, volume, pitch));
            } catch (NoSuchMethodError | NoClassDefFoundError ignored) {
                CoreClass.getInstance().getLogger().warning("Sound for " + id + " (" + rawSound[0] + ") could not be played: namespaces are not supported on your platform.");
            }
        } else {
            try {
                player.playSound(player, org.bukkit.Sound.valueOf(rawSound[0]), volume, pitch);
            } catch (IllegalArgumentException ex) {
                CoreClass.getInstance().getLogger().warning("Sound for " + id + " (" + rawSound[0] + ") could not be played: sound does not exist.");
            }
        }
    }

    /**
     * Used to translate the legacy AT message format into the MiniMessage format.
     *
     * @param format the message to be translated.
     * @return The MM-formatted message.
     */
    @ApiStatus.Internal
    public static @NotNull String translateLegacy(String format) {

        // Replace brackets
        format = format.replace('{', '<').replace('}', '>');

        // Replace legacy codes
        var serializer = LegacyComponentSerializer.legacyAmpersand();
        format = MiniMessage.miniMessage().serialize(serializer.deserialize(format));

        // STOP GETTING RID OF THE BACKSLASHES!!
        format = format.replace("\\<", "<");

        // Replace markdown
        format = translateMarkdown(format);

        return format;
    }

    /**
     * Used to translate the legacy markdown format in pre-v6 versions.
     *
     * @param format the message to be translated.
     * @return the message in MiniMessage format.
     */
    @ApiStatus.Internal
    private static @NotNull String translateMarkdown(String format) {
        int startTextPointer = -1;
        int endTextPointer = -1;

        // Command () pointers
        int startCommandPointer = -1;
        int endCommandPointer;

        // Last markdown pointer
        int lastMarkdownPointer = 0;
        boolean building = false;

        // The built result
        StringBuilder result = new StringBuilder();

        // Go through each character
        for (int i = 0; i < format.length(); i++) {

            // If a component isn't being built but we're gonna start one, let's begin
            if (!building && format.charAt(i) == '[' && (i == 0 || !(format.charAt(i - 1) == '\\'))) {
                startTextPointer = i + 1;
                building = true;
                continue;
            }

            // If the text pointer is ending, note that
            if (building && format.charAt(i) == ']' && !(format.charAt(i - 1) == '\\')) {
                endTextPointer = i;
                continue;
            }

            // If the command pointer is starting, start building that
            if (building && format.charAt(i) == '(') {
                if (format.charAt(i - 1) == ']') {
                    startCommandPointer = i + 1;
                } else if (startCommandPointer == -1) {
                    building = false;
                    startTextPointer = -1;
                    endTextPointer = -1;
                }
                continue;
            }

            // If we're not building a component, add the letter
            if (!building) {
                result.append(format.charAt(i));
                continue;
            }

            // If this isn't a valid character to end on, then continue for now
            if (format.charAt(i) != ')' || (format.charAt(i - 1) == '\\')) continue;
            if (startCommandPointer == -1) continue;

            // Note the ending command pointer
            endCommandPointer = i;

            // Get the text and the command itself
            String text = format.substring(startTextPointer, endTextPointer);
            String[] commands = format.substring(startCommandPointer, endCommandPointer).split("\\|");

            // Note specific elements
            @Nullable String command = null;
            @Nullable String url = null;
            List<String> hoverText = new ArrayList<>();

            // Go through each command part
            for (String cmd : commands) {

                // If it starts with /, note it as a command
                if (cmd.startsWith("/")) {
                    command = cmd;
                    continue;
                }

                // If it's a URL, note it as such
                if (cmd.startsWith("http")) {
                    url = cmd;
                    continue;
                }

                // Otherwise, consider it as hover text
                hoverText.add(stripEndingTags(cmd));
            }

            // Ensure there's no closing tags at the start - they need to be outside
            String component = stripEndingTags(text);


            // Add the command
            if (command != null) {
                component = "<click:run_command:'" + command + "'>" + component + "</click>";
            }

            // Add the URL
            if (url != null) {
                component = "<click:open_url:'" + url + "'>" + component + "</click>";
            }

            // Add the hovertext
            if (!hoverText.isEmpty()) {
                component = "<hover:show_text:'" + String.join("\n", hoverText) + "'>" + component + "</hover>";
            }

            result.append(component);

            // Reset variables
            building = false;
            startTextPointer = -1;
            endTextPointer = -1;
            startCommandPointer = -1;
        }

        // If we're building but at the end of the string, add everything remaining
        if (building) result.append(format.substring(startTextPointer - 1));

        return result.toString();
    }

    @ApiStatus.Internal
    private static String stripEndingTags(String input) {

        // Ensure there's no closing tags at the start - they need to be outside
        Pattern pattern = Pattern.compile("^(</[a-zA-Z_-]+>)");
        Matcher matcher;

        // Whilst it still matches...
        while ((matcher = pattern.matcher(input)).find()) {

            // Get the matched tag and extract it
            String tag = matcher.group(1);
            input = input.replaceFirst(tag, "");
        }

        return input;
    }
}
