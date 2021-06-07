package io.github.niestrat99.advancedteleport.config;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.limitations.LimitationsManager;
import io.github.niestrat99.advancedteleport.payments.PaymentManager;
import io.github.thatsmusic99.configurationmaster.CMFile;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NewConfig extends CMFile {

    public ConfigOption<Boolean> USE_BASIC_TELEPORT_FEATURES;
    public ConfigOption<Boolean> USE_WARPS;
    public ConfigOption<Boolean> USE_RANDOMTP;
    public ConfigOption<Boolean> USE_SPAWN;
    public ConfigOption<Boolean> USE_HOMES;
    public ConfigOption<List<String>> DISABLED_COMMANDS;

    public ConfigOption<Integer> REQUEST_LIFETIME;
    public ConfigOption<Boolean> USE_MULTIPLE_REQUESTS;
    public ConfigOption<Boolean> NOTIFY_ON_EXPIRE;

    public ConfigOption<Integer> WARM_UP_TIMER_DURATION;
    public ConfigOption<Boolean> CANCEL_WARM_UP_ON_ROTATION;
    public ConfigOption<Boolean> CANCEL_WARM_UP_ON_MOVEMENT;
    public PerCommandOption<Integer> WARM_UPS;

    public ConfigOption<Integer> COOLDOWN_TIMER_DURATION;
    public ConfigOption<Boolean> ADD_COOLDOWN_DURATION_TO_WARM_UP;
    public ConfigOption<Boolean> APPLY_COOLDOWN_TO_ALL_COMMANDS;
    public ConfigOption<String> APPLY_COOLDOWN_AFTER;
    public PerCommandOption<Integer> COOLDOWNS;

    public ConfigOption<Object> COST_AMOUNT;
    public PerCommandOption<Object> COSTS;

    public ConfigOption<Boolean> USE_MYSQL;
    public ConfigOption<String> MYSQL_HOST;
    public ConfigOption<Integer> MYSQL_PORT;
    public ConfigOption<String> MYSQL_DATABASE;
    public ConfigOption<String> USERNAME;
    public ConfigOption<String> PASSWORD;
    public ConfigOption<String> TABLE_PREFIX;

    public ConfigOption<Boolean> ENABLE_DISTANCE_LIMITATIONS;
    public ConfigOption<Integer> MAXIMUM_TELEPORT_DISTANCE;
    public ConfigOption<Boolean> MONITOR_ALL_TELEPORTS;
    public PerCommandOption<Integer> DISTANCE_LIMITS;

    public ConfigOption<Boolean> ENABLE_TELEPORT_LIMITATIONS;
    public ConfigOption<Boolean> MONITOR_ALL_TELEPORTS_LIMITS;
    public ConfigOption<ConfigurationSection> WORLD_RULES;
    public PerCommandOption<String> COMMAND_RULES;

    public ConfigOption<Integer> MAXIMUM_X;
    public ConfigOption<Integer> MAXIMUM_Z;
    public ConfigOption<Integer> MINIMUM_X;
    public ConfigOption<Integer> MINIMUM_Z;
    public ConfigOption<Boolean> USE_WORLD_BORDER;
    public ConfigOption<Boolean> RAPID_RESPONSE;
    public ConfigOption<Integer> PREPARED_LOCATIONS_LIMIT;
    public ConfigOption<List<String>> IGNORE_WORLD_GENS;
    public ConfigOption<List<String>> AVOID_BLOCKS;
    public ConfigOption<List<String>> AVOID_BIOMES;
    public ConfigOption<Boolean> WHITELIST_WORLD;
    public ConfigOption<Boolean> REDIRECT_TO_WORLD;
    public ConfigOption<List<String>> ALLOWED_WORLDS;

    public ConfigOption<Integer> DEFAULT_HOMES_LIMIT;
    public ConfigOption<Boolean> ADD_BED_TO_HOMES;
    public ConfigOption<Boolean> DENY_HOMES_IF_OVER_LIMIT;
    public ConfigOption<Boolean> HIDE_HOMES_IF_DENIED;

    public ConfigOption<String> TPA_REQUEST_RECEIVED;
    public ConfigOption<String> TPA_REQUEST_SENT;
    public ConfigOption<String> TPAHERE_REQUEST_RECEIVED;
    public ConfigOption<String> TPAHERE_REQUEST_SENT;

    public ConfigOption<List<String>> BACK_TELEPORT_CAUSES;

    public ConfigOption<Boolean> TELEPORT_TO_SPAWN_FIRST;
    public ConfigOption<Boolean> TELEPORT_TO_SPAWN_EVERY;

    public ConfigOption<ConfigurationSection> DEATH_MANAGEMENT;

    public ConfigOption<List<String>> DEFAULT_PERMISSIONS;
    public ConfigOption<Boolean> ALLOW_ADMIN_PERMS;

    private static NewConfig instance;
    private static List<String> defaults;
    /**
     *
     */
    public NewConfig() {
        super(CoreClass.getInstance(), "config");
        addLink("SpigotMC", "https://www.spigotmc.org/resources/advanced-teleport.64139/");
        addLink("Wiki", "https://github.com/Niestrat99/AT-Rewritten/wiki");
        addLink("Discord", "https://discord.gg/mgWbbN4");
        load();
    }

    @Override
    public void loadDefaults() {
        instance = this;

        addComment("Another comment at the very top for all you lads :)");
        addDefault("use-basic-teleport-features", true, "Features", "Whether basic teleportation features should be enabled or not." +
                "\nThis includes /tpa, /tpahere, /tpblock, /tpunblock and /back." +
                "\nThis does not disable the command for other plugins - if you want other plugins to use the provided commands, use Bukkit's commands.yml file." +
                "\nPlease refer to https://bukkit.gamepedia.com/Commands.yml for this!");

        addDefault("use-warps", true, "Whether warps should be enabled in the plugin.");
        addDefault("use-spawn", true, "Whether the plugin should modify spawn/spawn properties.");
        addDefault("use-randomtp", true, "Whether the plugin should allow random teleportation.");
        addDefault("use-homes", true, "Whether homes should be enabled in the plugin.");
        addDefault("disabled-commands", new ArrayList<>(), "The commands that AT should not register upon starting up.\n" +
                "In other words, this gives up the command for other plugins to use.\n" +
                "NOTE: If you are using Essentials with AT and want AT to give up its commands to Essentials, Essentials does NOT go down without a fight. Jesus Christ. You'll need to restart the server for anything to change.");

        addSection("Teleport Requesting");
        addDefault("request-lifetime", 60, "How long tpa and tpahere requests last before expiring.");
        addDefault("allow-multiple-requests", true, "Whether or not the plugin should enable the use of multiple requests.\n" +
                "When enabled, user 1 may get TPA requests from user 2 and 3, but user 1 is prompted to select a specific request.\n" +
                "When this is disabled and user 1 receives requests from user 2 and then 3, they will only have user 3's request to respond to.");
        addDefault("notify-on-expire", true, "Let the player know when their request has timed out or been displaced by another user's request.\n" +
                "Displacement only occurs when allow-multiple-requests is disabled.");
        addDefault("tpa-restrict-movement-on", "requester");
        addDefault("tpahere-restrict-movement-on", "requester");

        addDefault("warm-up-timer-duration", 3, "Warm-Up Timers", "The number of seconds it takes for the teleportation to take place following confirmation.\n" +
                "(i.e. \"You will teleport in 3 seconds!\")\n" +
                "This acts as the default option for the per-command warm-ups.");
        addDefault("cancel-warm-up-on-rotation", true, "Whether or not teleportation should be cancelled if the player rotates or moves.");
        addDefault("cancel-warm-up-on-movement", true, "Whether or not teleportation should be cancelled upon movement only.");

        addComment("per-command-warm-ups", "Command-specific warm-ups.");
        addDefault("per-command-warm-ups.tpa", "default", "Warm-up timer for /tpa.");
        addDefault("per-command-warm-ups.tpahere", "default", "Warm-up timer for /tpahere");
        addDefault("per-command-warm-ups.tpr", "default", "Warm-up timer for /tpr, or /rtp.");
        addDefault("per-command-warm-ups.warp", "default", "Warm-up timer for /warp");
        addDefault("per-command-warm-ups.spawn", "default", "Warm-up timer for /spawn");
        addDefault("per-command-warm-ups.home", "default", "Warm-up timer for /home");
        addDefault("per-command-warm-ups.back", "default", "Warm-up timer for /back");

        addDefault("cooldown-duration", 5, "Cooldowns", "How long before the user can use a command again.\n" +
                "This stops users spamming commands repeatedly.\n" +
                "This is also the default cooldown period for all commands.");
        addDefault("add-cooldown-duration-to-warm-up", true, "Adds the warm-up duration to the cooldown duration.\n" +
                "For example, if the cooldown duration was 5 seconds but the warm-up was 3, the cooldown becomes 8 seconds long.");
        addDefault("apply-cooldown-to-all-commands", false, "Whether or not the cooldown of one command will stop a user from using all commands.\n" +
                "For example, if a player used /tpa with a cooldown of 10 seconds but then used /tpahere with a cooldown of 5, the 10-second cooldown would still apply.\n" +
                "On the other hand, if a player used /tpahere, the cooldown of 5 seconds would apply to /tpa and other commands.");
        addDefault("apply-cooldown-after", "request", "When to apply the cooldown\n" +
                        "Options include:\n" +
                        "- request - Cooldown starts as soon as any teleport command is made and still applies even if no teleport takes place (i.e. cancelled by movement or not accepted).\n" +
                        "- accept - Cooldown starts only when the teleport request is accepted (with /tpyes) and still applies even if no teleport takes place (i.e. cancelled by movement).\n" +
                        "- teleport - Cooldown starts only when the teleport actually happens.\n" +
                        "Note:\n" +
                        "'request' and 'accept' behave the same for /rtp, /back, /spawn, /warp, and /home\n" +
                        "cooldown for /tpall always starts when the command is ran, regardless if any player accepts or teleports");

        addComment("per-command-cooldowns", "Command-specific cooldowns.");
        addDefault("per-command-cooldowns.tpa", "default", "Cooldown for /tpa.");
        addDefault("per-command-cooldowns.tpahere", "default", "Cooldown for /tpahere");
        addDefault("per-command-cooldowns.tpr", "default", "Cooldown for /tpr, or /rtp.");
        addDefault("per-command-cooldowns.warp", "default", "Cooldown for /warp");
        addDefault("per-command-cooldowns.spawn", "default", "Cooldown for /spawn");
        addDefault("per-command-cooldowns.home", "default", "Cooldown for /home");
        addDefault("per-command-cooldowns.back", "default", "Cooldown for /back");
        //addDefault("per-command-cooldowns.sethome", "default", "Cooldown for /sethome");
        //addDefault("per-command-cooldowns.setwarp", "default", "Cooldown for /setwarp");

        addDefault("cost-amount", 100.0, "Teleportation Costs", "The amount it costs to teleport somewhere." +
                "\nIf you want to use Vault Economy, use 100.0 to charge $100." +
                "\nIf you want to use Minecraft EXP points, use 10EXP for 10 EXP Points." +
                "\nIf you want to use Minecraft EXP levels, use 5LVL for 5 levels." +
                "\nIf you want to use items, use the format MATERIAL:AMOUNT or MATERIAL:AMOUNT:BYTE." +
                "\nFor example, on 1.13+, ORANGE_WOOL:3 for 3 orange wool, but on versions before 1.13, WOOL:1:3." +
                "\nIf you're on a legacy version and unsure on what byte to use, see https://minecraftitemids.com/types" +
                "\nTo use multiple methods of charging, use a ; - e.g. '100.0;10LVL' for $100 and 10 EXP levels." +
                "\nTo disable, just put an empty string, i.e. ''");

        addComment("per-command-cost", "Command-specific costs.");
        addDefault("per-command-cost.tpa", "default", "Cost for /tpa.");
        addDefault("per-command-cost.tpahere", "default", "Cost for /tpahere.");
        addDefault("per-command-cost.tpr", "default", "Cost for /tpr, or /rtp.");
        addDefault("per-command-cost.warp", "default", "Cost for /warp");
        addDefault("per-command-cost.spawn", "default", "Cost for /spawn");
        addDefault("per-command-cost.home", "default", "Cost for /home");
        addDefault("per-command-cost.back", "default", "Cost for /back");
        //addDefault("per-command-cost.sethome", "default", "Cost for /sethome");
        //addDefault("pet-command-cost.setwarp", "default", "Cost for /setwarp");

        addSection("SQL Storage");

        addDefault("use-mysql", false, "Whether the plugin should use SQL storage or not.\n" +
                "By default, AT uses SQLite storage, which stores data in a .db file locally.");
        addDefault("mysql-host", "127.0.0.1", "The MySQL host to connect to.");
        addDefault("mysql-port", 3306, "The port to connect to.");
        addDefault("mysql-database", "database", "The database to connect to.");
        addDefault("mysql-username", "username", "The username to use when connecting.");
        addDefault("mysql-password", "password", "The password to use when connecting.");
        addDefault("mysql-table-prefix", "advancedtp", "The prefix of all AT tables. \n" +
                "If you're on Bungee, you may want to add your server's name to the end.");

        addDefault("enable-distance-limitations", false, "Distance Limitations",
                "Enables the distance limiter to stop players teleporting over a large distance.\n" +
                        "This is only applied when people are teleporting in the same world.");
        addDefault("maximum-teleport-distance", 1000, "The maximum distance that a player can teleport.\n" +
                "This is the default distance applied to all commands when specified.");
        addDefault("monitor-all-teleports-distance", false, "Whether or not all teleportations - not just AT's - should be checked for distance.");

        addComment("per-command-distance-limitations", "Determines the distance limit for each command.");
        addDefault("per-command-distance-limitations.tpa", "default", "Distance limit for /tpa");
        addDefault("per-command-distance-limitations.tpahere", "default", "Distance limit for /tpahere");
        addDefault("per-command-distance-limitations.tpr", "default", "Distance limit for /tpr");
        addDefault("per-command-distance-limitations.warp", "default", "Distance limit for /warp");
        addDefault("per-command-distance-limitations.spawn", "default", "Distance limit for /spawn");
        addDefault("per-command-distance-limitations.home", "default", "Distance limit for /home");
        addDefault("per-command-distance-limitations.back", "default", "Distance limit for /back");

        addSection("Teleportation Limitations");

        addComment("WARNING: A lot of the options below are considered advanced and use special syntax that is not often accepted in YAML.\n" +
                "When using such options, wrap them in quotes: ''\n" +
                "As an example, 'stop-teleportation-out:world,world_nether'");

        addDefault("enable-teleport-limitations", false,
                "Enables teleport limitations. This means cross-world or even world teleportation can be limited within specific worlds.");
        addDefault("monitor-all-teleports-limitations", false, "Whether or not all teleportation - not just AT's - should be checked to see if teleportation is allowed.");

        addComment("world-rules", "The teleportation rules defined for each world.\n" +
                "Rules include:\n" +
                "- stop-teleportation-out - Stops players teleporting to another world when they are in this world.\n" +
                "- stop-teleportation-within - Stops players teleporting within the world.\n" +
                "- stop-teleportation-into - Stops players teleporting into this world.\n" +
                "To combine multiple rules, use a ; - e.g. stop-teleportation-out;stop-teleportation-within\n" +
                "For out and into rules, you can make it so that rules only initiate when in or going to a specific world using :, e.g. stop-teleportation-out:world stops players teleporting to \"world\" in the world they're currently in.\n" +
                "To do the opposite (i.e. initiates the rule when users are not in the specified world), use !, e.g. stop-teleportation-into!world stops teleportation into a specific world if they are not in \"world\". If ! and : are used in the same rule, then : is given top priority." +
                "To make this rule work with multiple worlds, use a comma (,), e.g. stop-teleportation-into:world,world_nether");

        addLenientSection("world-rules");
        addDefault("world-rules.default", "stop-teleportation-within");
        addExample("world-rules.world", "default");
        addExample("world-rules.world_nether", "stop-teleportation-into!world", "Stops people teleporting into the Nether if they're not coming from \"world\"");

        addComment("command-rules", "The teleportation rules defined for each AT command.\n" +
                "Rules include:\n" +
                "- override - The command will override world rules and run regardless.\n" +
                "- ignore - The command will refuse to run regardless of world rules.\n" +
                "To combine multiple rules, use a ;.\n" +
                "To make rules behave differently in different worlds, use : to initiate the rule in a specific world (e.g. override:world to make the command override \"world\"'s rules.)\n" +
                "To initiate rules outside of a specific world, use ! (e.g. override!world to make the command override world rules everywhere but in world)\n" +
                "To use multiple worlds, use a comma (,).\n" +
                "By default, all commands will comply with the world rules. If no rules are specified, they will comply.\n" +
                "All worlds specified will be considered the world in which the player is currently in. For worlds being teleported to, add > to the start of the world name.\n" +
                "For example, ignore:world,>world_nether will not run if the player is in \"world\" or if the player is going into the Nether.");
        addDefault("command-rules.tpa", "");
        addDefault("command-rules.tpahere", "");
        addDefault("command-rules.tpr", "");
        addDefault("command-rules.warp", "");
        addDefault("command-rules.spawn", "");
        addDefault("command-rules.home", "");
        addDefault("command-rules.back", "");

        addDefault("maximum-x", 5000, "RandomTP", "The maximum X coordinate to go up to when selecting a random location.");
        addDefault("maximum-z", 5000, "The maximum Z coordinate to go up to when selecting a random location.");
        addDefault("minimum-x", -5000, "The minimum X coordinate to go down to when selecting a random location.");
        addDefault("minimum-z", -5000, "The minimum Z coordinate to go down to when selecting a random location.");
        addDefault("use-world-border", true, "When WorldBorder is installed, AT will check the border of each world instead rather than using the minimum and maximum coordinates.");
        addDefault("use-rapid-response", true, "Use the new rapid response system for RTP.\n" +
                "This means valid locations are prepared before a user chooses to use /tpr or interact with a sign, meaning they are ready for use and can instantly TP a player.\n" +
                "This feature allows you to use the \"tpr\" death option in the death management section further down.\n" +
                "IMPORTANT NOTE - this feature only works on the Paper server type and any of its forks. It is not considered safe to use on Spigot or Bukkit.");
        addDefault("prepared-locations-limit", 3, "How many locations can be prepared per world when using AT's Rapid Response system.\n" +
                "These are immediately prepared upon startup and when a world is loaded.");
        addDefault("ignore-world-generators", new ArrayList<>(Arrays.asList(
                "us.talabrek.ultimateskyblock.world.SkyBlockChunkGenerator",
                "us.talabrek.ultimateskyblock.world.SkyBlockNetherChunkGenerator",
                "world.bentobox.bskyblock.generators.ChunkGeneratorWorld",
                "world.bentobox.acidisland.world.ChunkGeneratorWorld",
                "world.bentobox.oneblock.generators.ChunkGeneratorWorld",
                "com.wasteofplastic.askyblock.generators.ChunkGeneratorWorld",
                "com.wasteofplastic.acidisland.generators.ChunkGeneratorWorld", 
                "b.a",
                "com.chaseoes.voidworld.VoidWorld.VoidWorldGenerator",
                "club.bastonbolado.voidgenerator.EmptyChunkGenerator")), "AT's Rapid Response system automatically loads locations for each world, but can be problematic on some worlds, mostly SkyBlock worlds.\n" +
                "In response, this list acts as pro-active protection and ignores worlds generated using the following generators.\n" +
                "This is provided as an option so you can have control over which worlds have locations load.");
        addDefault("avoid-blocks", new ArrayList<>(Arrays.asList("WATER", "LAVA", "STATIONARY_WATER", "STATIONARY_LAVA")),
                "Blocks that people must not be able to land in when using /tpr.");
        addDefault("avoid-biomes", new ArrayList<>(Arrays.asList("OCEAN", "DEEP_OCEAN")), "Biomes that the plugin should avoid when searching for a location.");
        addDefault("whitelist-worlds", false, "Whether or not /tpr should only be used in the worlds listed below.");
        addDefault("redirect-to-whitelisted-worlds", true, "Whether or not players should be directed to a whitelisted world when using /tpr.\n" +
                "When this option is disabled and the player tries to use /tpr in a non-whitelisted world, the command simply won't work.");
        addDefault("allowed-worlds", new ArrayList<>(Arrays.asList("world", "world_nether")), "Worlds you can use /tpr in.\n" +
                "If a player uses /tpr in a world that doesn't allow it, they will be teleported in the first world on the list instead.\n" +
                "To make this feature effective, turn on \"whitelist-worlds\" above.");


        addDefault("default-homes-limit", -1, "Homes", "The default maximum of homes people can have.\n" +
                "This can be overridden by giving people permissions such as at.member.homes.10.\n" +
                "To disable this, use -1 as provided by default.");
        addDefault("add-bed-to-homes", true, "Whether or not the bed home should be added to /homes.");
        addDefault("deny-homes-if-over-limit", false, "Whether or not players should be denied access to some of their homes if they exceed their homes limit.\n" +
                "The homes denied access to will end up being their most recently set homes.\n" +
                "For example, having homes A, B, C, D and E with a limit of 3 will deny access to D and E.");
        addDefault("hide-homes-if-denied", false, "If homes should be hidden from /homes should they be denied access.\n" +
                "If this is false, they will be greyed out in the /homes list.");

        addDefault("tpa-request-received", "none", "Notifications/Sounds",
                "The sound played when a player receives a teleportation (tpa) request.\n" +
                        "For 1.16+, check https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Sound.html for a list of sounds you can use\n" +
                        "For 1.15 and below, check https://www.spigotmc.org/threads/sounds-spigot-1-7-1-14-4-sound-enums.340452/ for a list of sounds down to 1.7.\n" +
                        "(Friendly reminder that 1.7.x is not supported though!)\n" +
                        "Set to \"none\" if you want no sound playing.");
        addDefault("tpa-request-sent", "none", "The sound played when a player sends a teleportation (tpa) request.");
        addDefault("tpahere-request-received", "none", "The sound played when a player receives a teleportation (tpahere) request.");
        addDefault("tpahere-request-sent", "none", "The sound played when a player sends a teleportation (tpahere) request.");

        addDefault("used-teleport-causes", new ArrayList<>(Arrays.asList("COMMAND", "PLUGIN", "SPECTATE")), "Back",
                "The teleport causes that the plugin must listen to allow players to teleport back to the previous location.\n" +
                        "You can see a full list of these causes at https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/player/PlayerTeleportEvent.TeleportCause.html");

        addDefault("teleport-to-spawn-on-first-join", true, "Spawn Management",
                "Whether the player should be teleported to the spawnpoint when they join for the first time.");
        addDefault("teleport-to-spawn-on-every-join", false,
                "Whether the player should be teleported to the spawnpoint every time they join.");

        addComment("death-management", "Determines how and where players teleport when they die.\n" +
                "Options include:\n" +
                "- spawn - Teleports the player to the spawnpoint of either the world or specified by the plugin.\n" +
                "- bed - Teleports to the player's bed.\n" +
                "- anchor - 1.16+ only, teleports to the player's respawn anchor. However, due to limitations with Spigot's API, it may or may not always work. (add Player#getRespawnAnchor pls)\n" +
                "- warp:Warp Name - Teleports the player to a specified warp. For example, if you want to teleport to Hub, you'd type warp:Hub\n" +
                "- tpr - Teleports the player to a random location. Can only be used when the rapid response system is enabled." +
                "- {default} - Uses the default respawn option, which is spawn unless set differently.\n" +
                "If you're using EssentialsX Spawn and want AT to take over respawn mechanics, set respawn-listener-priority in EssX's config.yml file to lowest.");

        addLenientSection("death-management");
        addDefault("death-management.default", "spawn");
        addExample("death-management.world", "{default}");
        addExample("death-management.special-world", "warp:Special");
        addExample("death-management.another-world", "bed");

        addDefault("default-permissions", new ArrayList<>(Arrays.asList("at.member.*", "at.member.warp.*")), "Permissions",
                "The default permissions given to users without OP.\n" +
                        "By default, Advanced Teleport allows users without OP to use all member features.\n" +
                        "This allows for permission management without a permissions plugin, especially if a user doesn't understand how such plugins work.\n" +
                        "However, if you have a permissions plugin and Vault installed, you cannot make admin permissions work by default.");
        addDefault("allow-admin-permissions-as-default-perms", false, "Allows admin permissions to be allowed as default permissions by default.\n" +
                "If you want to use admin permissions, it's often recommended to use a permissions plugin such as LuckPerms.\n" +
                "Do not enable this if you are unsure of the risks this option proposes.");

    }

    public static NewConfig get() {
        return instance;
    }

    @Override
    public void moveToNew() {
        moveTo("features.teleport", "use-basic-teleport-features");
        moveTo("features.warps", "use-warps");
        moveTo("features.spawn", "use-spawn");
        moveTo("features.randomTP", "use-randomtp");
        moveTo("features.homes", "use-homes");

        moveTo("timers.requestLifetime", "request-lifetime");
        moveTo("timers.teleportTimer", "warm-up-timer-duration");
        moveTo("timers.cancel-on-rotate", "cancel-warm-up-on-rotation");
        moveTo("timers.cancel-on-movement", "cancel-warm-up-on-movement");
        for (String command : Arrays.asList("tpa", "tpahere", "tpr", "warp", "spawn", "home", "back")) {
            moveTo("timers.teleportTimers." + command, "per-command-warm-ups." + command);
        }

        moveTo("cooldowns.default", "cooldown-duration");
        moveTo("cooldowns.apply-globally", "apply-cooldown-to-all-commands");
        moveTo("cooldowns.add-to-timer", "add-cooldown-duration-to-warm-up");
        for (String command : Arrays.asList("tpa", "tpahere", "tpr", "warp", "spawn", "home", "back")) {
            moveTo("cooldowns." + command, "per-command-cooldowns." + command);
        }

        moveTo("distance-limiter.enabled", "enable-distance-limitations");
        moveTo("distance-limiter.distance-limit", "maximum-teleport-distance");
        moveTo("distance-limiter.monitor-all-teleports", "monitor-all-teleports-distance");

        boolean defaultVault = getConfig().getBoolean("booleans.useVault");
        boolean defaultEXP = getConfig().getBoolean("booleans.EXPPayment");
        int defaultEXPAmount = getConfig().getInt("payments.exp.teleportPrice");
        double defaultPrice = getConfig().getDouble("payments.vault.teleportPrice");

        StringBuilder builder = new StringBuilder();
        if (defaultVault) {
            builder.append(defaultPrice);
        }

        if (defaultEXP) {
            if (builder.length() > 0) {
                builder.append(";");
            }
            builder.append(defaultEXPAmount).append("LVL");
        }
        if (builder.length() > 0) {
            set("cost-amount", builder.toString());
        }
        for (String command : Arrays.asList("tpa", "tpahere", "tpr", "warp", "spawn", "home", "back")) {
            try {
                Object vault = getConfig().get("payments.vault." + command + ".price");
                Object exp = getConfig().get("payments.exp." + command + ".price");
                boolean vaultOn = getConfig().get("payments.vault." + command + ".enabled").equals("default")
                        ? defaultVault : getConfig().getBoolean("payments.vault." + command + ".enabled");
                boolean expOn = getConfig().get("payments.exp." + command + ".enabled").equals("default")
                        ? defaultEXP : getConfig().getBoolean("payments.exp." + command + ".enabled");
                StringBuilder paymentCombination = new StringBuilder();
                if (vaultOn) {
                    if (vault.equals("default")) {
                        paymentCombination.append(defaultPrice);
                    } else {
                        paymentCombination.append(vault);
                    }
                    if (expOn) paymentCombination.append(";");
                }

                if (expOn) {
                    if (exp.equals("default")) {
                        paymentCombination.append(defaultEXPAmount).append("LVL");
                    } else {
                        paymentCombination.append(exp).append("LVL");
                    }
                }

                if (paymentCombination.length() == 0) {
                    paymentCombination.append("default");
                }
                set("per-command-cost." + command, paymentCombination.toString());

            } catch (Exception ignored) {

            }

        }

        moveTo("sounds.tpa.requestSent", "tpa-request-sent");
        moveTo("sounds.tpa.requestReceived", "tpa-request-received");
        moveTo("sounds.tpahere.requestSent", "tpahere-request-sent");
        moveTo("sounds.tpahere.requestReceived", "tpahere-request-received");

        moveTo("tpr.maximum-x", "maximum-x");
        moveTo("tpr.maximum-z", "maximum-z");
        moveTo("tpr.minimum-x", "minimum-x");
        moveTo("tpr.minimum-z", "minimum-z");
        moveTo("tpr.useWorldBorder", "use-world-border");
        moveTo("tpr.avoidBlocks", "avoid-blocks");

        moveTo("back.teleport-causes", "used-teleport-causes");

        moveTo("homes.default-limit", "default-homes-limit");
        moveTo("homes.add-bed-to-homes", "add-bed-to-homes");

        moveTo("spawn.death.teleport", "death-management");
        moveTo("spawn.join.teleport-on-first-join", "teleport-to-spawn-on-first-join");
        moveTo("spawn.join.teleport-on-every-join", "teleport-to-spawn-on-every-join");

        moveTo("permissions.default-permissions", "default-permissions");
        moveTo("permissions.allow-admin-perms-as-defaults", "allow-admin-permissions-as-default-perms");

    }

    @Override
    public void postSave() {

        USE_BASIC_TELEPORT_FEATURES = new ConfigOption<>("use-basic-teleport-features");
        USE_WARPS = new ConfigOption<>("use-warps");
        USE_RANDOMTP = new ConfigOption<>("use-randomtp");
        USE_SPAWN = new ConfigOption<>("use-spawn");
        USE_HOMES = new ConfigOption<>("use-homes");
        DISABLED_COMMANDS = new ConfigOption<>("disabled-commands");

        REQUEST_LIFETIME = new ConfigOption<>("request-lifetime");
        USE_MULTIPLE_REQUESTS = new ConfigOption<>("allow-multiple-requests");
        NOTIFY_ON_EXPIRE = new ConfigOption<>("notify-on-expire");

        WARM_UP_TIMER_DURATION = new ConfigOption<>("warm-up-timer-duration");
        CANCEL_WARM_UP_ON_ROTATION = new ConfigOption<>("cancel-warm-up-on-rotation");
        CANCEL_WARM_UP_ON_MOVEMENT = new ConfigOption<>("cancel-warm-up-on-movement");
        WARM_UPS = new PerCommandOption<>("per-command-warm-ups", "warm-up-timer-duration");

        COOLDOWN_TIMER_DURATION = new ConfigOption<>("cooldown-duration");
        ADD_COOLDOWN_DURATION_TO_WARM_UP = new ConfigOption<>("add-cooldown-duration-to-warm-up");
        APPLY_COOLDOWN_TO_ALL_COMMANDS = new ConfigOption<>("apply-cooldown-to-all-commands");

        APPLY_COOLDOWN_AFTER = new ConfigOption<>("apply-cooldown-after");
        switch (APPLY_COOLDOWN_AFTER.get().toLowerCase()) {
            case "accept":
            case "request":
            case "teleport":
                break;
            default:
                CoreClass.getInstance().getLogger().warning("Bad input for apply-cooldown-after option! Using \"request\" as the default option...");
                set("apply-cooldown-after", "request");
        }
        COOLDOWNS = new PerCommandOption<>("per-command-cooldowns", "cooldown-duration");

        COST_AMOUNT = new ConfigOption<>("cost-amount");
        COSTS = new PerCommandOption<>("per-command-cost", "cost-amount");

        USE_MYSQL = new ConfigOption<>("use-mysql");
        MYSQL_HOST = new ConfigOption<>("mysql-host");
        MYSQL_PORT = new ConfigOption<>("mysql-port");
        MYSQL_DATABASE = new ConfigOption<>("mysql-database");
        USERNAME = new ConfigOption<>("mysql-username");
        PASSWORD = new ConfigOption<>("mysql-password");
        TABLE_PREFIX = new ConfigOption<>("mysql-table-prefix");

        ENABLE_DISTANCE_LIMITATIONS = new ConfigOption<>("enable-distance-limitations");
        MAXIMUM_TELEPORT_DISTANCE = new ConfigOption<>("maximum-teleport-distance");
        MONITOR_ALL_TELEPORTS = new ConfigOption<>("monitor-all-teleports-distance");
        DISTANCE_LIMITS = new PerCommandOption<>("per-command-distance-limitations", "maximum-teleport-distance");

        ENABLE_TELEPORT_LIMITATIONS = new ConfigOption<>("enable-teleport-limitations");
        MONITOR_ALL_TELEPORTS_LIMITS = new ConfigOption<>("monitor-all-teleports-limitations");
        WORLD_RULES = new ConfigOption<>("world-rules");
        COMMAND_RULES = new PerCommandOption<>("command-rules", "");

        MAXIMUM_X = new ConfigOption<>("maximum-x");
        MAXIMUM_Z = new ConfigOption<>("maximum-z");
        MINIMUM_X = new ConfigOption<>("minimum-x");
        MINIMUM_Z = new ConfigOption<>("minimum-z");
        USE_WORLD_BORDER = new ConfigOption<>("use-world-border");
        RAPID_RESPONSE = new ConfigOption<>("use-rapid-response");
        PREPARED_LOCATIONS_LIMIT = new ConfigOption<>("prepared-locations-limit");
        IGNORE_WORLD_GENS = new ConfigOption<>("ignore-world-generators");
        AVOID_BLOCKS = new ConfigOption<>("avoid-blocks");
        AVOID_BIOMES = new ConfigOption<>("avoid-biomes");
        WHITELIST_WORLD = new ConfigOption<>("whitelist-worlds");
        REDIRECT_TO_WORLD = new ConfigOption<>("redirect-to-whitelisted-worlds");
        ALLOWED_WORLDS = new ConfigOption<>("allowed-worlds");

        DEFAULT_HOMES_LIMIT = new ConfigOption<>("default-homes-limit");
        ADD_BED_TO_HOMES = new ConfigOption<>("add-bed-to-homes");
        DENY_HOMES_IF_OVER_LIMIT = new ConfigOption<>("deny-homes-if-over-limit");
        HIDE_HOMES_IF_DENIED = new ConfigOption<>("hide-homes-if-denied");

        TPA_REQUEST_RECEIVED = new ConfigOption<>("tpa-request-received");
        TPA_REQUEST_SENT = new ConfigOption<>("tpa-request-sent");
        TPAHERE_REQUEST_RECEIVED = new ConfigOption<>("tpahere-request-received");
        TPAHERE_REQUEST_SENT = new ConfigOption<>("tpahere-request-sent");

        BACK_TELEPORT_CAUSES = new ConfigOption<>("used-teleport-causes");

        TELEPORT_TO_SPAWN_FIRST = new ConfigOption<>("teleport-to-spawn-on-first-join");
        TELEPORT_TO_SPAWN_EVERY = new ConfigOption<>("teleport-to-spawn-on-every-join");

        DEATH_MANAGEMENT = new ConfigOption<>("death-management");

        DEFAULT_PERMISSIONS = new ConfigOption<>("default-permissions");
        ALLOW_ADMIN_PERMS = new ConfigOption<>("allow-admin-permissions-as-default-perms");

        new PaymentManager();
        LimitationsManager.init();

        // HANDLING DEFAULT PERMISSIONS

        List<String> permissions = DEFAULT_PERMISSIONS.get();
        if (defaults == null) {
            defaults = new ArrayList<>();
        } else {
            for (String permission : defaults) {
                Permission permObject = Bukkit.getPluginManager().getPermission(permission);
                permObject.setDefault(PermissionDefault.OP);
            }
        }


        Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), () -> {
            boolean warned = false;
            for (String permission : permissions) {
                if (!permission.startsWith("at")) continue;
                if (permission.startsWith("at.admin")) {
                    if (!warned) {
                        CoreClass.getInstance().getLogger().warning("WARNING: You've given an admin permission by default to all users.");
                        if (!ALLOW_ADMIN_PERMS.get() || CoreClass.getPerms() != null) {
                            CoreClass.getInstance().getLogger().warning("This can potentially be destructive, so we're not adding it right now.");
                            CoreClass.getInstance().getLogger().warning("To allow people to use admin permissions such as the ones specified, please disable the check in the configuration.");
                            CoreClass.getInstance().getLogger().warning("If you have a permissions plugin hooked into Vault too, you cannot make admin permissions default permissions.");
                        } else {
                            CoreClass.getInstance().getLogger().warning("This can potentially be destructive, so if this is not your doing, please check your configuration.");
                            CoreClass.getInstance().getLogger().warning("To stop people to use admin permissions such as the ones specified, please enable the check in the configuration.");
                        }
                        warned = true;
                    }
                    if (ALLOW_ADMIN_PERMS.get() && CoreClass.getPerms() == null) {
                        CoreClass.getInstance().getLogger().info("Allowed default access to " + permission);
                    } else {
                        CoreClass.getInstance().getLogger().info("Denied default access to " + permission);
                        continue;
                    }
                }
                Permission permObject = Bukkit.getPluginManager().getPermission(permission);
                if (permObject == null) {
                    permObject = new Permission(permission);
                    Bukkit.getPluginManager().addPermission(permObject);
                }
                permObject.setDefault(PermissionDefault.TRUE);
                defaults.add(permission);
            }
        });

    }

    public static class ConfigOption<T> {

        private String path;
        private String defaultPath;

        public ConfigOption(String path) {
            this.path = path;
        }

        public ConfigOption(String path, String defaultPath) {
            this.path = path;
            this.defaultPath = defaultPath;
        }

        public T get() {
            if (defaultPath != null && !defaultPath.isEmpty()) {
                if (instance.getConfig().get(path).equals("default")) {
                    return (T) instance.getConfig().get(defaultPath);
                } else {
                    return (T) instance.getConfig().get(path);
                }
            } else {
                return (T) instance.getConfig().get(path);
            }

        }
    }

    public static class PerCommandOption<T> {

        public ConfigOption<T> TPA;
        public ConfigOption<T> TPAHERE;
        public ConfigOption<T> TPR;
        public ConfigOption<T> WARP;
        public ConfigOption<T> SPAWN;
        public ConfigOption<T> HOME;
        public ConfigOption<T> BACK;

        public PerCommandOption(String path, String defaultPath) {
            TPA = new ConfigOption<>(path + ".tpa", defaultPath);
            TPAHERE = new ConfigOption<>(path + ".tpahere", defaultPath);
            TPR = new ConfigOption<>(path + ".tpr", defaultPath);
            WARP = new ConfigOption<>(path + ".warp", defaultPath);
            SPAWN = new ConfigOption<>(path + ".spawn", defaultPath);
            HOME = new ConfigOption<>(path + ".home", defaultPath);
            BACK = new ConfigOption<>(path + ".back", defaultPath);
        }

        public ConfigOption<T> valueOf(String command) {
            switch (command) {
                case "tpa":
                    return TPA;
                case "tpahere":
                    return TPAHERE;
                case "tpr":
                    return TPR;
                case "warp":
                    return WARP;
                case "spawn":
                    return SPAWN;
                case "home":
                    return HOME;
                case "back":
                    return BACK;
            }
            return null;
        }

        public ConfigOption<T>[] values() {
            return (ConfigOption<T>[]) new ConfigOption[]{TPA, TPAHERE, TPR, WARP, SPAWN, HOME, BACK};
        }
    }
}
