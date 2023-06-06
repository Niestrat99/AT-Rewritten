package io.github.niestrat99.advancedteleport.config;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.limitations.LimitationsManager;
import io.github.niestrat99.advancedteleport.payments.PaymentManager;
import io.github.thatsmusic99.configurationmaster.api.ConfigSection;
import io.github.thatsmusic99.configurationmaster.api.Title;
import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class MainConfig extends ATConfig {

    private static MainConfig instance;
    private static List<String> defaults;
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
    public ConfigOption<ConfigSection> CUSTOM_WARM_UPS;
    public ConfigOption<Boolean> BLINDNESS_ON_WARMUP;
    public ConfigOption<Integer> COOLDOWN_TIMER_DURATION;
    public ConfigOption<Boolean> ADD_COOLDOWN_DURATION_TO_WARM_UP;
    public ConfigOption<Boolean> APPLY_COOLDOWN_TO_ALL_COMMANDS;
    public ConfigOption<String> APPLY_COOLDOWN_AFTER;
    public PerCommandOption<Integer> COOLDOWNS;
    public ConfigOption<ConfigSection> CUSTOM_COOLDOWNS;
    public ConfigOption<Object> COST_AMOUNT;
    public PerCommandOption<Object> COSTS;
    public ConfigOption<ConfigSection> CUSTOM_COSTS;
    public ConfigOption<Boolean> USE_PARTICLES;
    public PerCommandOption<String> TELEPORT_PARTICLES;
    public PerCommandOption<String> WAITING_PARTICLES;
    public ConfigOption<Boolean> USE_MYSQL;
    public ConfigOption<String> MYSQL_HOST;
    public ConfigOption<Integer> MYSQL_PORT;
    public ConfigOption<String> MYSQL_DATABASE;
    public ConfigOption<String> USERNAME;
    public ConfigOption<String> PASSWORD;
    public ConfigOption<String> TABLE_PREFIX;
    public ConfigOption<Boolean> USE_SSL;
    public ConfigOption<Boolean> AUTO_RECONNECT;
    public ConfigOption<Boolean> ALLOW_PUBLIC_KEY_RETRIEVAL;
    public ConfigOption<Boolean> ENABLE_DISTANCE_LIMITATIONS;
    public ConfigOption<Integer> MAXIMUM_TELEPORT_DISTANCE;
    public ConfigOption<Boolean> MONITOR_ALL_TELEPORTS;
    public PerCommandOption<Integer> DISTANCE_LIMITS;
    public ConfigOption<ConfigSection> CUSTOM_DISTANCE_LIMITS;
    public ConfigOption<Boolean> ENABLE_TELEPORT_LIMITATIONS;
    public ConfigOption<Boolean> MONITOR_ALL_TELEPORTS_LIMITS;
    public ConfigOption<ConfigSection> WORLD_RULES;
    public PerCommandOption<String> COMMAND_RULES;
    public ConfigOption<ConfigSection> X;
    public ConfigOption<ConfigSection> Z;
    public ConfigOption<Boolean> RAPID_RESPONSE;
    public ConfigOption<Boolean> USE_VANILLA_BORDER;
    public ConfigOption<Boolean> USE_PLUGIN_BORDERS;
    public ConfigOption<Boolean> PROTECT_CLAIM_LOCATIONS;
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
    public ConfigOption<Boolean> OVERWRITE_SETHOME;
    public ConfigOption<Boolean> SHOW_HOMES_WITH_NO_INPUT;
    public ConfigOption<Boolean> PRIORITISE_MAIN_HOME;

    public ConfigOption<String> TPA_REQUEST_RECEIVED;
    public ConfigOption<String> TPA_REQUEST_SENT;
    public ConfigOption<String> TPAHERE_REQUEST_RECEIVED;
    public ConfigOption<String> TPAHERE_REQUEST_SENT;
    public ConfigOption<List<String>> BACK_TELEPORT_CAUSES;
    public ConfigOption<Integer> BACK_SEARCH_RADIUS;
    public MapOptions MAP_HOMES;
    public MapOptions MAP_WARPS;
    public MapOptions MAP_SPAWNS;
    public ConfigOption<Boolean> TELEPORT_TO_SPAWN_FIRST;
    public ConfigOption<String> FIRST_SPAWN_POINT;
    public ConfigOption<Boolean> TELEPORT_TO_SPAWN_EVERY;
    public ConfigOption<Boolean> TELEPORT_TO_NEAREST_SPAWN;
    public ConfigOption<Boolean> USE_OVERWORLD;

    public ConfigOption<ConfigSection> DEATH_MANAGEMENT;
    public ConfigOption<List<String>> DEFAULT_PERMISSIONS;
    public ConfigOption<Boolean> ALLOW_ADMIN_PERMS;
    public ConfigOption<Boolean> CHECK_FOR_UPDATES;
    public ConfigOption<Boolean> NOTIFY_ADMINS;
    public ConfigOption<Boolean> DEBUG;
    public ConfigOption<Boolean> USE_FLOODGATE_FORMS;
    public ConfigOption<Boolean> SEND_ACTIONBAR_TO_CONSOLE;

    /**
     *
     */
    public MainConfig() throws IOException {
        super("config.yml");
        setTitle(new Title().withWidth(100).addSolidLine()
            .addLine("-<( AdvancedTeleport )>-", Title.Pos.CENTER)
            .addLine("Made by Niestrat99 and Thatsmusic99", Title.Pos.CENTER)
            .addLine("")
            .addSolidLine('-')
            .addLine("A rapidly growing teleportation plugin looking to break the boundaries of traditional " +
                "teleport plugins.")
            .addLine("")
            .addLine("SpigotMC - https://www.spigotmc.org/resources/advanced-teleport.64139/")
            .addLine("Wiki - https://github.com/Niestrat99/AT-Rewritten/wiki")
            .addLine("Discord - https://discord.gg/mgWbbN4")
            .addSolidLine());
    }

    @Override
    public void loadDefaults() {
        instance = this;

        addComment("Another comment at the very top for all you lads :)");
        addDefault("use-basic-teleport-features", true, "Features", """
                Whether basic teleportation features should be enabled or not.
                This includes /tpa, /tpahere, /tpblock, /tpunblock and /back.
                This does not disable the command for other plugins - if you want other plugins to use the provided commands, use Bukkit's commands.yml file.
                Please refer to https://bukkit.gamepedia.com/Commands.yml for this!""");

        addDefault("use-warps", true, "Whether warps should be enabled in the plugin.");
        addDefault("use-spawn", true, "Whether the plugin should modify spawn/spawn properties.");
        addDefault("use-randomtp", true, "Whether the plugin should allow random teleportation.");
        addDefault("use-homes", true, "Whether homes should be enabled in the plugin.");
        addDefault("disabled-commands", new ArrayList<>(), """
                The commands that AT should not register upon starting up.
                In other words, this gives up the command for other plugins to use.
                NOTE: If you are using Essentials with AT and want AT to give up its commands to Essentials, Essentials does NOT go down without a fight. Jesus Christ. You'll need to restart the server for anything to change.
                To use this section, use the following format:
                disabled-commands:
                - back""");

        addSection("Teleport Requesting");
        addDefault("request-lifetime", 60, "How long tpa and tpahere requests last before expiring.");
        addDefault("allow-multiple-requests", true, """
                Whether or not the plugin should enable the use of multiple requests.
                When enabled, user 1 may get TPA requests from user 2 and 3, but user 1 is prompted to select a specific request.
                When this is disabled and user 1 receives requests from user 2 and then 3, they will only have user 3's request to respond to.""");
        addDefault("notify-on-expire", true, "Let the player know when their request has timed out or been displaced " +
                "by another user's request.\n" +
                "Displacement only occurs when allow-multiple-requests is disabled.");
        // addDefault("tpa-restrict-movement-on", "requester");
        // addDefault("tpahere-restrict-movement-on", "requester");

        addDefault("warm-up-timer-duration", 3, "Warm-Up Timers", """
                The number of seconds it takes for the teleportation to take place following confirmation.
                (i.e. "You will teleport in 3 seconds!")
                This acts as the default option for the per-command warm-ups.""");
        addDefault("cancel-warm-up-on-rotation", true, "Whether or not teleportation should be cancelled if the " +
                "player rotates or moves.");
        addDefault("cancel-warm-up-on-movement", true, "Whether or not teleportation should be cancelled upon " +
                "movement only.");

        addComment("per-command-warm-ups", "Command-specific warm-ups.");
        addDefault("per-command-warm-ups.tpa", "default", "Warm-up timer for /tpa.");
        addDefault("per-command-warm-ups.tpahere", "default", "Warm-up timer for /tpahere");
        addDefault("per-command-warm-ups.tpr", "default", "Warm-up timer for /tpr, or /rtp.");
        addDefault("per-command-warm-ups.warp", "default", "Warm-up timer for /warp");
        addDefault("per-command-warm-ups.spawn", "default", "Warm-up timer for /spawn");
        addDefault("per-command-warm-ups.home", "default", "Warm-up timer for /home");
        addDefault("per-command-warm-ups.back", "default", "Warm-up timer for /back");
        addComment("""
                Use this section to create custom warm-ups per-group.
                Use the following format:
                custom-warm-ups:
                  vip-warm-up: 3
                Giving a group, such as VIP, the permission at.member.timer.vip-warm-up will have a warm-up of 3.
                The key (vip-warm-up) and group name (VIP) do not have to be different, this is just an example.
                You can also add at.member.timer.3, but this is more efficient if you find permissions lag.To make it per-command, use at.member.timer.<command>.vip-warm-up. To make it per-world, use at.member.timer.<world>.vip-warm-up.
                To combine the two, you can use at.member.timer.<command>.<world>.vip-warm-up.""");
        makeSectionLenient("custom-warm-ups");

        addDefault("blindness-on-warmup", false, "Gives the teleporting player a blindness effect whilst waiting to " +
            "teleport.");

        addDefault("cooldown-duration", 5, "Cooldowns", """
                How long before the user can use a command again.
                This stops users spamming commands repeatedly.
                This is also the default cooldown period for all commands.""");
        addDefault("add-cooldown-duration-to-warm-up", true, "Adds the warm-up duration to the cooldown duration.\n" +
            "For example, if the cooldown duration was 5 seconds but the warm-up was 3, the cooldown becomes 8 " +
            "seconds long.");
        addDefault("apply-cooldown-to-all-commands", false, """
                Whether or not the cooldown of one command will stop a user from using all commands.
                For example, if a player used /tpa with a cooldown of 10 seconds but then used /tpahere with a cooldown of 5, the 10-second cooldown would still apply.
                On the other hand, if a player used /tpahere, the cooldown of 5 seconds would apply to /tpa and other commands.""");
        addDefault("apply-cooldown-after", "request", """
                When to apply the cooldown
                Options include:
                - request - Cooldown starts as soon as any teleport command is made and still applies even if no teleport takes place (i.e. cancelled by movement or not accepted).
                - accept - Cooldown starts only when the teleport request is accepted (with /tpyes) and still applies even if no teleport takes place (i.e. cancelled by movement).
                - teleport - Cooldown starts only when the teleport actually happens.
                Note:
                'request' and 'accept' behave the same for /rtp, /back, /spawn, /warp, and /home
                cooldown for /tpall always starts when the command is ran, regardless if any player accepts or teleports""");

        addComment("per-command-cooldowns", "Command-specific cooldowns.");
        addDefault("per-command-cooldowns.tpa", "default", "Cooldown for /tpa.");
        addDefault("per-command-cooldowns.tpahere", "default", "Cooldown for /tpahere");
        addDefault("per-command-cooldowns.tpr", "default", "Cooldown for /tpr, or /rtp.");
        addDefault("per-command-cooldowns.warp", "default", "Cooldown for /warp");
        addDefault("per-command-cooldowns.spawn", "default", "Cooldown for /spawn");
        addDefault("per-command-cooldowns.home", "default", "Cooldown for /home");
        addDefault("per-command-cooldowns.back", "default", "Cooldown for /back");
        // addDefault("per-command-cooldowns.sethome", "default", "Cooldown for /sethome");
        // addDefault("per-command-cooldowns.setwarp", "default", "Cooldown for /setwarp");
        makeSectionLenient("custom-cooldowns");
        addComment("custom-cooldowns", """
                Use this section to create custom cooldowns per-group.
                Use the following format:
                custom-cooldowns:
                  vip-cooldown: 3
                Giving a group, such as VIP, the permission at.member.cooldown.vip-cooldown will have a cooldown of 3.
                The key (vip-cooldown) and group name (VIP) do not have to be different, this is just an example.
                You can also add at.member.cooldown.3, but this is more efficient if you find permissions lag.To make it per-command, use at.member.cooldown.<command>.vip-cooldown. To make it per-world, use at.member.cooldown.<world>.vip-cooldown.
                To combine the two, you can use at.member.cooldown.<command>.<world>.vip-cooldown.""");

        addDefault("cost-amount", 100.0, "Teleportation Costs", """
                The amount it costs to teleport somewhere.
                If you want to use Vault Economy, use 100.0 to charge $100.
                If you have multiple plugins hooking into Vault, enter the plugin name in front separated by a colon, e.g. Essentials:100.50
                Do note some plugins require Vault support to be toggled on manually.
                If you want to use Minecraft EXP points, use 10EXP for 10 EXP Points.
                If you want to use Minecraft EXP levels, use 5LVL for 5 levels.
                If you want to use items, use the format MATERIAL:AMOUNT or MATERIAL:AMOUNT:BYTE.
                For example, on 1.13+, ORANGE_WOOL:3 for 3 orange wool, but on versions before 1.13, WOOL:3:1.
                If you're on a legacy version and unsure on what byte to use, see https://minecraftitemids.com/types
                To use multiple methods of charging, use a ; - e.g. '100.0;10LVL' for $100 and 10 EXP levels.
                To disable, just put an empty string, i.e. ''""");

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
        makeSectionLenient("custom-costs");
        addComment("custom-costs", """
                Use this section to create custom costs per-group.
                Use the following format:
                custom-costs:
                  vip-cost: Essentials:100
                Giving a group, such as VIP, the permission at.member.cost.vip-cost will have a cost of $100.
                To make it per-command, add the permission at.member.cost.tpa.vip-cost (for tpa) instead.""");

        addDefault("use-particles", true, "Particles", "Whether particles should be used in the plugin.\n" +
                "Some standalone implementation is used, but otherwise, PlayerParticles is used.");
        addDefault("default-waiting-particles", "", "The default waiting particles during the warm-up period.");
        addComment("waiting-particles", "Command-specific waiting particles.");
        addDefault("waiting-particles.tpa", "default");
        addDefault("waiting-particles.tpahere", "default");
        addDefault("waiting-particles.tpr", "default");
        addDefault("waiting-particles.warp", "default");
        addDefault("waiting-particles.spawn", "default");
        addDefault("waiting-particles.home", "default");
        addDefault("waiting-particles.back", "default");

        addDefault("default-teleporting-particles", "spark", "The default particles used as soon as the player teleports. \n" +
                "At this time, only spark is supported. However, other recommendations are welcome with that.");
        addComment("teleporting-particles", "Command-specific teleporting particles.");
        addDefault("teleporting-particles.tpa", "default");
        addDefault("teleporting-particles.tpahere", "default");
        addDefault("teleporting-particles.tpr", "default");
        addDefault("teleporting-particles.warp", "default");
        addDefault("teleporting-particles.spawn", "default");
        addDefault("teleporting-particles.home", "default");
        addDefault("teleporting-particles.back", "default");

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
        addDefault("use-ssl", false, "Whether or not to connect to the MySQL server using SSL.");
        addDefault("auto-reconnect", true, "Whether or not the plugin should reconnect to the MySQL server when a " +
                "connection is closed.");
        addDefault("allow-public-key-retrieval", false, "Whether or not to enable public key retrieval. \n" +
                "Please do not enable it without being explicitly told by one of the developers.");

        addDefault("enable-distance-limitations", false, "Distance Limitations",
                "Enables the distance limiter to stop players teleporting over a large distance.\n" +
                        "This is only applied when people are teleporting in the same world.");
        addDefault("maximum-teleport-distance", 1000, "The maximum distance that a player can teleport.\n" +
                "This is the default distance applied to all commands when specified.");
        addDefault("monitor-all-teleports-distance", false, "Whether or not all teleports - not just AT's - " +
                "should be checked for distance.");

        addComment("per-command-distance-limitations", "Determines the distance limit for each command.");
        addDefault("per-command-distance-limitations.tpa", "default", "Distance limit for /tpa");
        addDefault("per-command-distance-limitations.tpahere", "default", "Distance limit for /tpahere");
        addDefault("per-command-distance-limitations.tpr", "default", "Distance limit for /tpr");
        addDefault("per-command-distance-limitations.warp", "default", "Distance limit for /warp");
        addDefault("per-command-distance-limitations.spawn", "default", "Distance limit for /spawn");
        addDefault("per-command-distance-limitations.home", "default", "Distance limit for /home");
        addDefault("per-command-distance-limitations.back", "default", "Distance limit for /back");

        makeSectionLenient("custom-distance-limitations");
        addComment("custom-distance-limitations", """
                Use this section to create custom distance limitations per-group.
                Use the following format:
                custom-distance-limitations:
                  vip-distance: 300000
                Giving a group, such as VIP, the permission at.member.distance.vip-distance will have a maximum distance of 300000.
                The key (vip-distance) and group name (VIP) do not have to be different, this is just an example.
                You can also add at.member.distance.300000, but this is more efficient if you find permissions lag. To make it per-command, use at.member.distance.<command>.vip-distance. To make it per-world, use at.member.distance.<world>.vip-distance.
                To combine the two, you can use at.member.distance.<command>.<world>.vip-distance.""");

        addSection("Teleportation Limitations");

        addComment("""
                WARNING: A lot of the options below are considered advanced and use special syntax that is not often accepted in YAML.
                When using such options, wrap them in quotes: ''
                As an example, 'stop-teleportation-out:world,world_nether'""");

        addDefault("enable-teleport-limitations", false,
                "Enables teleport limitations. This means cross-world or even world teleportation can be limited " +
                        "within specific worlds.");
        addDefault("monitor-all-teleports-limitations", false, "Whether or not all teleportation - not just AT's - " +
                "should be checked to see if teleportation is allowed.");

        addComment("world-rules", """
                The teleportation rules defined for each world.
                Rules include:
                - stop-teleportation-out - Stops players teleporting to another world when they are in this world.
                - stop-teleportation-within - Stops players teleporting within the world.
                - stop-teleportation-into - Stops players teleporting into this world.
                To combine multiple rules, use a ; - e.g. stop-teleportation-out;stop-teleportation-within
                For out and into rules, you can make it so that rules only initiate when in or going to a specific world using :, e.g. stop-teleportation-out:world stops players teleporting to "world" in the world they're currently in.
                To do the opposite (i.e. initiates the rule when users are not in the specified world), use !, e.g. stop-teleportation-into!world stops teleportation into a specific world if they are not in "world". If ! and : are used in the same rule, then : is given top priority.To make this rule work with multiple worlds, use a comma (,), e.g. stop-teleportation-into:world,world_nether""");

        makeSectionLenient("world-rules");
        addDefault("world-rules.default", "stop-teleportation-within");
        addExample("world-rules.world", "default");
        addExample("world-rules.world_nether", "stop-teleportation-into!world" /*, "Stops people teleporting into the
         Nether if they're not coming from \"world\"" */);

        addComment("command-rules", """
                The teleportation rules defined for each AT command.
                Rules include:
                - override - The command will override world rules and run regardless.
                - ignore - The command will refuse to run regardless of world rules.
                To combine multiple rules, use a ;.
                To make rules behave differently in different worlds, use : to initiate the rule in a specific world (e.g. override:world to make the command override "world"'s rules.)
                To initiate rules outside of a specific world, use ! (e.g. override!world to make the command override world rules everywhere but in world)
                To use multiple worlds, use a comma (,).
                By default, all commands will comply with the world rules. If no rules are specified, they will comply.
                All worlds specified will be considered the world in which the player is currently in. For worlds being teleported to, add > to the start of the world name.
                For example, ignore:world,>world_nether will not run if the player is in "world" or if the player is going into the Nether.""");
        addDefault("command-rules.tpa", "");
        addDefault("command-rules.tpahere", "");
        addDefault("command-rules.tpr", "");
        addDefault("command-rules.warp", "");
        addDefault("command-rules.spawn", "");
        addDefault("command-rules.home", "");
        addDefault("command-rules.back", "");

        addSection("RandomTP");
        makeSectionLenient("x");
        addDefault("x.default", "5000;-5000");
        addExample("x.world_the_end", "10000;-10000");
        addComment("x",
                """
                        Defines the range of X coordinates that players can teleport to.
                        Using a value for example 5000 would automatically set the minimum to -5000.
                        These are able to be defined for each world by name.
                        Split the values with a semicolon (;).
                        If a world is defined here but not in the z section, the x values will be reused for the z coords.
                        """
        );
        addComment("z",
                """
                        Defines the range of z coordinates that players can teleport to.
                        Using a value for example 5000 would automatically set the minimum to -5000.
                        These are able to be defined for each world by name.
                        Split the values with a semicolon (;).
                        If a world is defined here but not in the x section, the z values will be reused for the x coords.
                        """
        );
        makeSectionLenient("z");
        addDefault("z.default", "5000;-5000");
        addExample("z.world_the_end", "10000;-10000");
        addDefault("maximum-x", 5000, "Deprecated\n # The maximum X coordinate to go up to when selecting a random location.");
        addDefault("maximum-z", 5000, "Deprecated\n # The maximum Z coordinate to go up to when selecting a random location.");
        addDefault("minimum-x", -5000, "Deprecated\n # The minimum X coordinate to go down to when selecting a random location.");
        addDefault("minimum-z", -5000, "Deprecated\n # The minimum Z coordinate to go down to when selecting a random location.");
        addDefault("use-rapid-response", true, """
                Use the new rapid response system for RTP.
                This means valid locations are prepared before a user chooses to use /tpr or interact with a sign, meaning they are ready for use and can instantly TP a player.
                This feature allows you to use the "tpr" death option in the death management section further down.
                IMPORTANT NOTE - this feature only works on the Paper server type and any of its forks. It is not considered safe to use on Spigot or Bukkit.""");
        addDefault("use-vanilla-border", false, "Whether the plugin should use the Vanilla world border as a viable " +
                "option for managing /tpr boundaries.");
        addDefault("use-plugin-borders", true, "Whether the plugin should use plugin world borders for managing /tpr " +
                "boundaries.\n" +
                "Currently supported plugins are WorldBorder and ChunkyBorder.");
        addDefault("protect-claim-locations", true,
                "If enabled checks if the player is in either an unclaimed area or that they have build permission in the area.\n" +
                        "Supported plugins are Lands, WorldGuard, and GriefPrevention."
        );
        addDefault("prepared-locations-limit", 3, "How many locations can be prepared per world when using AT's Rapid" +
                " Response system.\n" +
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
                "club.bastonbolado.voidgenerator.EmptyChunkGenerator",
                "de.xtkq.voidgen.generator.interfaces.ChunkGen")), """
                AT's Rapid Response system automatically loads locations for each world, but can be problematic on some worlds, mostly SkyBlock worlds.
                In response, this list acts as pro-active protection and ignores worlds generated using the following generators.
                This is provided as an option so you can have control over which worlds have locations load.""");
        addDefault("avoid-blocks", new ArrayList<>(Arrays.asList("WATER", "LAVA", "STATIONARY_WATER",
                        "STATIONARY_LAVA")),
                "Blocks that people must not be able to land in when using /tpr.");
        addDefault("avoid-biomes", new ArrayList<>(Arrays.asList("OCEAN", "DEEP_OCEAN")), "Biomes that the plugin " +
                "should avoid when searching for a location.");
        addDefault("whitelist-worlds", false, "Whether or not /tpr should only be used in the worlds listed below.");
        addDefault("redirect-to-whitelisted-worlds", true, "Whether or not players should be directed to a " +
                "whitelisted world when using /tpr.\n" +
                "When this option is disabled and the player tries to use /tpr in a non-whitelisted world, the " +
                "command simply won't work.");
        addDefault("allowed-worlds", new ArrayList<>(Arrays.asList("world", "world_nether")), """
                Worlds you can use /tpr in.
                If a player uses /tpr in a world that doesn't allow it, they will be teleported in the first world on the list instead.
                To make this feature effective, turn on "whitelist-worlds" above.""");


        addDefault("default-homes-limit", -1, "Homes", """
                The default maximum of homes people can have.
                This can be overridden by giving people permissions such as at.member.homes.10.
                To disable this, use -1 as provided by default.""");
        addDefault("add-bed-to-homes", true, "Whether or not the bed home should be added to /homes.");
        addDefault("deny-homes-if-over-limit", false, """
                Whether or not players should be denied access to some of their homes if they exceed their homes limit.
                The homes denied access to will end up being their most recently set homes.
                For example, having homes A, B, C, D and E with a limit of 3 will deny access to D and E.""");
        addDefault("hide-homes-if-denied", false, "If homes should be hidden from /homes should they be denied access" +
                ".\n" +
                "If this is false, they will be greyed out in the /homes list.");
        addDefault("overwrite-sethome", false, "When enabled, setting homes with a name that already exists in your " +
                "list gets overwritten.");
        addDefault("show-homes-with-no-input", false, "Shows a list of homes the player has when doing /home and nothing else.\n" +
                "This overwrites /home when attempting to teleport to their main home, but if you're more used to what Essentials does, set this to true.");
        addDefault("prioritise-main-home", true, "If the player has a main home set, then the option above is ignored. I gotta be flexible.");

        addDefault("tpa-request-received", "none", "Notifications/Sounds",
                """
                        The sound played when a player receives a teleportation (tpa) request.
                        For 1.16+, check https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Sound.html for a list of sounds you can use
                        For 1.15 and below, check https://www.spigotmc.org/threads/sounds-spigot-1-7-1-14-4-sound-enums.340452/ for a list of sounds down to 1.7.
                        (Friendly reminder that 1.7.x is not supported though!)
                        Set to "none" if you want no sound playing.""");
        addDefault("tpa-request-sent", "none", "The sound played when a player sends a teleportation (tpa) request.");
        addDefault("tpahere-request-received", "none", "The sound played when a player receives a teleportation " +
                "(tpahere) request.");
        addDefault("tpahere-request-sent", "none", "The sound played when a player sends a teleportation (tpahere) " +
                "request.");

        addDefault("used-teleport-causes", new ArrayList<>(Arrays.asList("COMMAND", "PLUGIN", "SPECTATE")), "Back",
                "The teleport causes that the plugin must listen to allow players to teleport back to the previous " +
                        "location.\n" +
                        "You can see a full list of these causes at https://hub.spigotmc" +
                        ".org/javadocs/spigot/org/bukkit/event/player/PlayerTeleportEvent.TeleportCause.html");
        addDefault("back-search-radius", 5, """
                The cubic radius to search for a safe block when using /back.
                If a player teleports from an unsafe location and uses /back to return to it, the plugin will search all blocks within this radius to see if it is a safe place for the player to be moved to.
                It is recommend to avoid setting this option too high as this can have a worst case execution time of O(n^3) (e.g. run 27 times, 64, 125, 216 and so on).
                To disable, either set to 0 or -1.""");

        addSection("Map Plugin Integration");
        addComment("At this time, AdvancedTeleport supports dynmap and squaremap.\n" +
                "If you are using dynmap, the plugin has extra icons you can use as placeholders.");
        // Map options
        for (String type : Arrays.asList("homes", "warps", "spawns")) {
            // home, warp, spawn
            String singular = type.substring(0, type.length() - 1);
            // Homes, Warps, Spawns
            String capitalised = type.toUpperCase().charAt(0) + type.toLowerCase().substring(1);
            addComment(type, "Covers map options for " + type + ".");
            addDefault(type + ".enabled", !type.equals("homes"), "Whether the icons for " + type + " will be added at all.");
            addDefault(type + ".default-icon", singular + "-default", "The default icon for " + type + " in the map.");
            addDefault(type + ".shown-by-default", true, "Whether the player viewing the map has to explicitly enable the layer to view " + type + " on the map.");
            addDefault(type + ".hover-tooltip", "name", "The tooltip that will appear when someone hovers over the icon in the map." +
                    "\nFor Dynmap, this supports HTML formatting.");
            addDefault(type + ".click-tooltip", "name", "Squaremap only - the tooltip that will appear when someone clicks on the icon.");
            addDefault(type + ".icon-size", "32", "The scale of the icon on the map.\n" +
                    "With Dynmap, only 8, 16 and 32 are supported. With Squaremap, 2147483647 is your limit. But don't try it.");
            addDefault(type + ".layer-name", capitalised, "The layer display name that appears on the map.");
        }
        addDefault("add-spawns", true, "Whether to make spawnpoints visible for everyone on the map.");
        addDefault("add-warps", true, "Whether to make warps visible for everyone on the map.");
        addDefault("add-homes", false, "Whether to make all homes visible for everyone on the map.");
        addDefault("default-icon-size", 40, "The default icon size for AT's icons on the map.");

        addDefault("teleport-to-spawn-on-first-join", true, "Spawn Management",
                "Whether the player should be teleported to the spawnpoint when they join for the first time.");
        addDefault("first-spawn-point", "", "The name of the spawnpoint players will be first teleported to if they joined for the first time.\n" +
                "If it is blank, then it will take the main spawnpoint.");
        addDefault("teleport-to-spawn-on-every-join", false,
                "Whether the player should be teleported to the spawnpoint every time they join.");
        addDefault("teleport-to-nearest-spawnpoint", false, "Whether using /spawn, joining or respawning should send the user to the closest spawnpoint they have access to.\n" +
                "If the user doesn't have permission to the specified spawnpoint, then they are not sent to it." +
                "Only spawns in the same dimension/world are considered. If no spawnpoint is set in the same dimension, then the normal main spawn is used.");
        addDefault("use-overworld", true, "If no main spawn has been set and the world being checked is in the Nether or End, use the Overworld spawn instead (if applicable).");

        addComment("death-management", """
                Determines how and where players teleport when they die.
                Options include:
                - spawn - Teleports the player to the spawnpoint of either the world or specified by the plugin.
                - bed - Teleports to the player's bed.
                - anchor - 1.16+ only, teleports to the player's respawn anchor. However, due to limitations with Spigot's API, it may or may not always work. (add Player#getRespawnAnchor pls)
                - warp:Warp Name - Teleports the player to a specified warp. For example, if you want to teleport to Hub, you'd type warp:Hub
                - tpr - Teleports the player to a random location. Can only be used when the rapid response system is enabled.- default - Uses the default respawn option, which is spawn unless set differently.
                If you're using EssentialsX Spawn and want AT to take over respawn mechanics, set respawn-listener-priority in EssX's config.yml file to lowest.""");

        makeSectionLenient("death-management");
        addDefault("death-management.default", "bed;spawn");
        addExample("death-management.world", "default");
        addExample("death-management.special-world", "warp:Special");
        addExample("death-management.another-world", "bed");

        addDefault("default-permissions", new ArrayList<>(Arrays.asList("at.member.*", "at.member.warp.*", "at.member.warp.sign.*", "at.member.core.help", "at.member.core.info")),
                "Permissions",
                """
                        The default permissions given to users without OP.
                        By default, Advanced Teleport allows users without OP to use all member features.
                        This allows for permission management without a permissions plugin, especially if a user doesn't understand how such plugins work.
                        However, if you have a permissions plugin and Vault installed, you cannot make admin permissions work by default.""");
        addDefault("allow-admin-permissions-as-default-perms", false, """
                Allows admin permissions to be allowed as default permissions by default.
                If you want to use admin permissions, it's often recommended to use a permissions plugin such as LuckPerms.
                Do not enable this if you are unsure of the risks this option proposes.""");

        addSection("Updates");
        addDefault("check-for-updates", true, "Whether or not the plugin should check for updates.");
        addDefault("notify-admins-on-update", true, "Whether or not to notify admins when an update is available.\n" +
                "Anyone with the permission at.admin.notify will receive this notification.");
        addDefault("debug", false, "Used for debugging purposes.");
        addDefault("use-floodgate-forms", true, """
                Whether to use Cumulus forms for Bedrock players.
                These work by having a Bedrock player type in the command itself (such as /warp, /tpa, /setwarp), then fill in the rest of the commands through a form.
                This only works when Geyser and Floodgate are used on the server. This improves accessibility for mobile or console players.""");
        addDefault("send-actionbar-to-console", true, "If you are just using action bars for messages and have empty base messages, the console will not receive them." +
                "\nIf you have this option set to true, then the console will receive the message that the action bar uses.");

    }

    public static MainConfig get() {
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

        boolean defaultVault = getBoolean("booleans.useVault");
        boolean defaultEXP = getBoolean("booleans.EXPPayment");
        int defaultEXPAmount = getInteger("payments.exp.teleportPrice");
        double defaultPrice = getDouble("payments.vault.teleportPrice");

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
                Object vault = get("payments.vault." + command + ".price");
                Object exp = get("payments.exp." + command + ".price");
                boolean vaultOn = get("payments.vault." + command + ".enabled").equals("default")
                                  ? defaultVault : getBoolean("payments.vault." + command + ".enabled");
                boolean expOn = get("payments.exp." + command + ".enabled").equals("default")
                                ? defaultEXP : getBoolean("payments.exp." + command + ".enabled");
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
        CUSTOM_WARM_UPS = new ConfigOption<>("custom-warm-ups");

        BLINDNESS_ON_WARMUP = new ConfigOption<>("blindness-on-warmup");

        COOLDOWN_TIMER_DURATION = new ConfigOption<>("cooldown-duration");
        ADD_COOLDOWN_DURATION_TO_WARM_UP = new ConfigOption<>("add-cooldown-duration-to-warm-up");
        APPLY_COOLDOWN_TO_ALL_COMMANDS = new ConfigOption<>("apply-cooldown-to-all-commands");
        CUSTOM_COOLDOWNS = new ConfigOption<>("custom-cooldowns");

        APPLY_COOLDOWN_AFTER = new ConfigOption<>("apply-cooldown-after");
        switch (APPLY_COOLDOWN_AFTER.get().toLowerCase()) {
            case "accept":
            case "request":
            case "teleport":
                break;
            default:
                CoreClass.getInstance().getLogger().warning("Bad input for apply-cooldown-after option! Using " +
                        "\"request\" as the default option...");
                set("apply-cooldown-after", "request");
        }
        COOLDOWNS = new PerCommandOption<>("per-command-cooldowns", "cooldown-duration");

        COST_AMOUNT = new ConfigOption<>("cost-amount");
        COSTS = new PerCommandOption<>("per-command-cost", "cost-amount");
        CUSTOM_COSTS = new ConfigOption<>("custom-costs");

        USE_PARTICLES = new ConfigOption<>("use-particles");
        WAITING_PARTICLES = new PerCommandOption<>("waiting-particles", "default-waiting-particles");
        TELEPORT_PARTICLES = new PerCommandOption<>("teleporting-particles", "default-teleporting-particles");

        USE_MYSQL = new ConfigOption<>("use-mysql");
        MYSQL_HOST = new ConfigOption<>("mysql-host");
        MYSQL_PORT = new ConfigOption<>("mysql-port");
        MYSQL_DATABASE = new ConfigOption<>("mysql-database");
        USERNAME = new ConfigOption<>("mysql-username");
        PASSWORD = new ConfigOption<>("mysql-password");
        TABLE_PREFIX = new ConfigOption<>("mysql-table-prefix");
        USE_SSL = new ConfigOption<>("use-ssl");
        AUTO_RECONNECT = new ConfigOption<>("auto-reconnect");
        ALLOW_PUBLIC_KEY_RETRIEVAL = new ConfigOption<>("allow-public-key-retrieval");

        ENABLE_DISTANCE_LIMITATIONS = new ConfigOption<>("enable-distance-limitations");
        MAXIMUM_TELEPORT_DISTANCE = new ConfigOption<>("maximum-teleport-distance");
        MONITOR_ALL_TELEPORTS = new ConfigOption<>("monitor-all-teleports-distance");
        DISTANCE_LIMITS = new PerCommandOption<>("per-command-distance-limitations", "maximum-teleport-distance");
        CUSTOM_DISTANCE_LIMITS = new ConfigOption<>("custom-distance-limitations");

        ENABLE_TELEPORT_LIMITATIONS = new ConfigOption<>("enable-teleport-limitations");
        MONITOR_ALL_TELEPORTS_LIMITS = new ConfigOption<>("monitor-all-teleports-limitations");
        WORLD_RULES = new ConfigOption<>("world-rules");
        COMMAND_RULES = new PerCommandOption<>("command-rules", "");

        X = new ConfigOption<>("x");
        Z = new ConfigOption<>("z");
        RAPID_RESPONSE = new ConfigOption<>("use-rapid-response");
        USE_VANILLA_BORDER = new ConfigOption<>("use-vanilla-border");
        USE_PLUGIN_BORDERS = new ConfigOption<>("use-plugin-borders");
        PROTECT_CLAIM_LOCATIONS = new ConfigOption<>("protect-claim-locations");
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
        OVERWRITE_SETHOME = new ConfigOption<>("overwrite-sethome");
        SHOW_HOMES_WITH_NO_INPUT = new ConfigOption<>("show-homes-with-no-input");
        PRIORITISE_MAIN_HOME = new ConfigOption<>("prioritise-main-home");

        TPA_REQUEST_RECEIVED = new ConfigOption<>("tpa-request-received");
        TPA_REQUEST_SENT = new ConfigOption<>("tpa-request-sent");
        TPAHERE_REQUEST_RECEIVED = new ConfigOption<>("tpahere-request-received");
        TPAHERE_REQUEST_SENT = new ConfigOption<>("tpahere-request-sent");

        BACK_TELEPORT_CAUSES = new ConfigOption<>("used-teleport-causes");
        BACK_SEARCH_RADIUS = new ConfigOption<>("back-search-radius");

        MAP_HOMES = new MapOptions("homes");
        MAP_SPAWNS = new MapOptions("spawns");
        MAP_WARPS = new MapOptions("warps");

        TELEPORT_TO_SPAWN_FIRST = new ConfigOption<>("teleport-to-spawn-on-first-join");
        FIRST_SPAWN_POINT = new ConfigOption<>("first-spawn-point");
        TELEPORT_TO_SPAWN_EVERY = new ConfigOption<>("teleport-to-spawn-on-every-join");
        TELEPORT_TO_NEAREST_SPAWN = new ConfigOption<>("teleport-to-nearest-spawnpoint");
        USE_OVERWORLD = new ConfigOption<>("use-overworld");

        DEATH_MANAGEMENT = new ConfigOption<>("death-management");

        DEFAULT_PERMISSIONS = new ConfigOption<>("default-permissions");
        ALLOW_ADMIN_PERMS = new ConfigOption<>("allow-admin-permissions-as-default-perms");

        CHECK_FOR_UPDATES = new ConfigOption<>("check-for-updates");
        NOTIFY_ADMINS = new ConfigOption<>("notify-admins-on-update");
        DEBUG = new ConfigOption<>("debug");
        USE_FLOODGATE_FORMS = new ConfigOption<>("use-floodgate-forms");
        SEND_ACTIONBAR_TO_CONSOLE = new ConfigOption<>("send-actionbar-to-console");

        new PaymentManager();
        LimitationsManager.init();

        // HANDLING DEFAULT PERMISSIONS

        List<String> permissions = DEFAULT_PERMISSIONS.get() == null ? new ArrayList<>() : DEFAULT_PERMISSIONS.get();
        if (defaults == null) {
            defaults = new ArrayList<>();
        } else {
            for (String permission : defaults) {
                Permission permObject = Bukkit.getPluginManager().getPermission(permission);
                if (permObject == null) continue;
                permObject.setDefault(PermissionDefault.OP);
            }
        }

        Bukkit.getScheduler().runTaskLater(CoreClass.getInstance(), () -> {
            boolean warned = false;
            for (String permission : permissions) {
                if (!permission.startsWith("at")) continue;
                if (permission.startsWith("at.admin")) {
                    if (!warned) {
                        CoreClass.getInstance().getLogger().warning("WARNING: You've given an admin permission by default" +
                            " to all users.");
                        if (!ALLOW_ADMIN_PERMS.get() || CoreClass.getPerms() != null) {
                            CoreClass.getInstance().getLogger().warning("This can potentially be destructive, so we're " +
                                "not adding it right now.");
                            CoreClass.getInstance().getLogger().warning("To allow people to use admin permissions such as" +
                                " the ones specified, please disable the check in the configuration.");
                            CoreClass.getInstance().getLogger().warning("If you have a permissions plugin hooked into " +
                                "Vault too, you cannot make admin permissions default permissions.");
                        } else {
                            CoreClass.getInstance().getLogger().warning("This can potentially be destructive, so if this " +
                                "is not your doing, please check your configuration.");
                            CoreClass.getInstance().getLogger().warning("To stop people to use admin permissions such as " +
                                "the ones specified, please enable the check in the configuration.");
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
        }, 200);
    }

    public static class ConfigOption<T> {

        private final String path;
        private String defaultPath;

        public ConfigOption(String path) {
            this.path = path;
        }

        public ConfigOption(
            String path,
            String defaultPath
        ) {
            this.path = path;
            this.defaultPath = defaultPath;
        }

        public T get() {
            if (defaultPath != null && !defaultPath.isEmpty()) {
                if (instance.get(path).equals("default")) {
                    return (T) instance.get(defaultPath);
                } else {
                    return (T) instance.get(path);
                }
            } else {
                return (T) instance.get(path);
            }
        }
    }

    public static class PerCommandOption<T> {

        public final ConfigOption<T> TPA;
        public final ConfigOption<T> TPAHERE;
        public final ConfigOption<T> TPR;
        public final ConfigOption<T> WARP;
        public final ConfigOption<T> SPAWN;
        public final ConfigOption<T> HOME;
        public final ConfigOption<T> BACK;

        public PerCommandOption(
            String path,
            String defaultPath
        ) {
            TPA = new ConfigOption<>(path + ".tpa", defaultPath);
            TPAHERE = new ConfigOption<>(path + ".tpahere", defaultPath);
            TPR = new ConfigOption<>(path + ".tpr", defaultPath);
            WARP = new ConfigOption<>(path + ".warp", defaultPath);
            SPAWN = new ConfigOption<>(path + ".spawn", defaultPath);
            HOME = new ConfigOption<>(path + ".home", defaultPath);
            BACK = new ConfigOption<>(path + ".back", defaultPath);
        }

        public ConfigOption<T> valueOf(String command) {
            return switch (command) {
                case "tpa" -> TPA;
                case "tpahere" -> TPAHERE;
                case "tpr" -> TPR;
                case "warp" -> WARP;
                case "spawn" -> SPAWN;
                case "home" -> HOME;
                case "back" -> BACK;
                default -> null;
            };
        }

        public ConfigOption<T>[] values() {
            return (ConfigOption<T>[]) new ConfigOption[]{TPA, TPAHERE, TPR, WARP, SPAWN, HOME, BACK};
        }
    }

    public static class MapOptions {
        private final String section;
        private final boolean enabled;
        private final String defaultIcon;
        private final boolean shownByDefault;
        private final String hoverTooltip;
        private final String clickTooltip;
        private final int iconSize;
        private final String layerName;

        public MapOptions(String section) {
            this.section = section;
            this.enabled = get().getBoolean(section + ".enabled");
            this.defaultIcon = get().getString(section + ".default-icon");
            this.shownByDefault = get().getBoolean(section + ".shown-by-default");
            this.hoverTooltip = get().getString(section + ".hover-tooltip");
            this.clickTooltip = get().getString(section + ".click-tooltip");
            this.iconSize = get().getInteger(section + ".icon-size");
            this.layerName = get().getString(section + ".layer-name");
        }

        public boolean isEnabled() {
            return enabled;
        }

        public boolean isShownByDefault() {
            return shownByDefault;
        }

        public int getIconSize() {
            return iconSize;
        }

        public String getClickTooltip() {
            return clickTooltip;
        }

        public String getDefaultIcon() {
            return defaultIcon;
        }

        public String getHoverTooltip() {
            return hoverTooltip;
        }

        public String getLayerName() {
            return layerName;
        }

        public String getSection() {
            return section;
        }
    }
}
