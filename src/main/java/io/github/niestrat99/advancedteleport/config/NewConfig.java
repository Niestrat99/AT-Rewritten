package io.github.niestrat99.advancedteleport.config;

import io.github.niestrat99.advancedteleport.payments.PaymentManager;
import org.bukkit.configuration.MemoryConfiguration;

import java.util.ArrayList;
import java.util.Arrays;

public class NewConfig extends ConfigurationMaster {

    public ConfigOption<Boolean> USE_BASIC_TELEPORT_FEATURES = new ConfigOption<>("use-basic-teleport-features");
    public ConfigOption<Boolean> USE_WARPS = new ConfigOption<>("use-warps");
    public ConfigOption<Boolean> USE_RANDOMTP = new ConfigOption<>("use-randomtp");
    public ConfigOption<Boolean> USE_SPAWN = new ConfigOption<>("use-spawn");
    public ConfigOption<Boolean> USE_HOMES = new ConfigOption<>("use-homes");

    public ConfigOption<Integer> WARM_UP_TIMER_DURATION = new ConfigOption<>("warp-up-timer-duration");
    public ConfigOption<Boolean> CANCEL_WARM_UP_ON_ROTATION = new ConfigOption<>("cancel-warm-up-on-rotation");
    public ConfigOption<Boolean> CANCEL_WARM_UP_ON_MOVEMENT = new ConfigOption<>("cancel-warm-up-on-movement");
    public PerCommandOption<Integer> WARM_UPS = new PerCommandOption<>("per-command-warm-ups", "warp-up-timer-duration");

    public ConfigOption<Integer> COOLDOWN_TIMER_DURATION = new ConfigOption<>("warp-up-timer-duration");
    public ConfigOption<Boolean> ADD_COOLDOWN_DURATION_TO_WARM_UP = new ConfigOption<>("add-cooldown-duration-to-warm-up");
    public ConfigOption<Boolean> APPLY_COOLDOWN_TO_ALL_COMMANDS = new ConfigOption<>("apply-cooldown-to-all-commands");
    public PerCommandOption<Integer> COOLDOWNS = new PerCommandOption<>("per-command-cooldowns", "warp-up-timer-duration");

    public ConfigOption<Object> COST_AMOUNT = new ConfigOption<>("cost-amount");
    public PerCommandOption<Object> COSTS = new PerCommandOption<>("per-command-cost", "cost-amount");

    public ConfigOption<Integer> DEFAULT_HOMES_LIMIT = new ConfigOption<>("default-homes-limit");
    public ConfigOption<Boolean> ADD_BED_TO_HOMES = new ConfigOption<>("add-bed-to-homes");

    private static NewConfig instance;
    /**
     *
     */
    public NewConfig() {
        super("config-new");
        instance = this;
    }

    @Override
    public void loadDefaults() {
        addDefault("use-basic-teleport-features", true, "Features", "Whether basic teleportation features should be enabled or not." +
                "\nThis includes /tpa, /tpahere, /tpblock, /tpunblock and /back." +
                "\nThis does not disable the command for other plugins - if you want other plugins to use the provided commands, use Bukkit's commands.yml file." +
                "\nPlease refer to https://bukkit.gamepedia.com/Commands.yml for this!");

        addDefault("use-warps", true, "Whether warps should be enabled in the plugin.");
        addDefault("use-spawn", true, "Whether the plugin should modify spawn/spawn properties.");
        addDefault("use-randomtp", true, "Whether the plugin should allow random teleportation.");
        addDefault("use-homes", true, "Whether homes should be enabled in the plugin.");

        addDefault("warm-up-timer-duration", 3, "Warm-Up Timers", "The number of seconds it takes for the teleportation to take place following confirmation.\n" +
                "(i.e. \"You will teleport in 3 seconds!\")\n" +
                "This acts as the default option for the per-command warm-ups.");
        addDefault("cancel-warm-up-on-rotation", true, "Whether or not teleportation should be cancelled if the player rotates or moves.");
        addDefault("cancel-warm-up-on-movement", true, "Whether or not teleportation should be cancelled upon movement only.");

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
        addDefault("apply-cooldowns-to-all-commands", false, "Whether or not the cooldown of one command will stop a user from using all commands.\n" +
                "For example, if a player used /tpa with a cooldown of 10 seconds but then used /tpahere with a cooldown of 5, the 10-second cooldown would still apply.\n" +
                "On the other hand, if a player used /tpahere, the cooldown of 5 seconds would apply to /tpa and other commands.");

        addComment("per-command-cooldowns", "Command-specific cooldowns.");
        addDefault("per-command-cooldowns.tpa", "default", "Cooldown for /tpa.");
        addDefault("per-command-cooldowns.tpahere", "default", "Cooldown for /tpahere");
        addDefault("per-command-cooldowns.tpr", "default", "Cooldown for /tpr, or /rtp.");
        addDefault("per-command-cooldowns.warp", "default", "Cooldown for /warp");
        addDefault("per-command-cooldowns.spawn", "default", "Cooldown for /spawn");
        addDefault("per-command-cooldowns.home", "default", "Cooldown for /home");
        addDefault("per-command-cooldowns.back", "default", "Cooldown for /back");

        addDefault("cost-amount", 100.0, "Teleportation Costs", "The amount it costs to teleport somewhere." +
                "\nIf you want to use Vault Economy, use 100.0 to charge $100." +
                "\nIf you want to use Minecraft EXP points, use 10EXP for 10 EXP Points." +
                "\nIf you want to use Minecraft EXP levels, use 5LVL for 5 levels." +
                "\nTo use multiple methods of charging, use a ; - e.g. '100.0;10LVL' for $100 and 10 EXP levels.");

        addDefault("per-command-cost.tpa", "default", "Cost for /tpa.");
        addDefault("per-command-cost.tpahere", "default", "Cost for /tpahere.");
        addDefault("per-command-cost.tpr", "default", "Cost for /tpr, or /rtp.");
        addDefault("per-command-cost.warp", "default", "Cost for /warp");
        addDefault("per-command-cost.spawn", "default", "Cost for /spawn");
        addDefault("per-command-cost.home", "default", "Cost for /home");
        addDefault("per-command-cost.back", "default", "Cost for /back");

        addDefault("enable-distance-limitations", true, "Distance Limitations",
                "Enables the distance limiter to stop players teleporting over a large distance.\n" +
                        "This is only applied when people are teleporting in the same world.");
        addDefault("maximum-teleport-distance", 1000, "The maximum distance that a player can teleport.\n" +
                "This is the default distance applied to all commands when specified.");
        addDefault("monitor-all-teleports-distance", false, "Whether or not all teleportations - not just AT's - should be checked for distance.\n" +
                "This can cause some potential conflict ");

        addComment("per-command-distance-limitations", "");
        addDefault("per-command-distance-limitations.tpa", "default");
        addDefault("per-command-distance-limitations.tpahere", "default");
        addDefault("per-command-distance-limitations.tpr", "default");
        addDefault("per-command-distance-limitations.warp", "default");
        addDefault("per-command-distance-limitations.spawn", "default");
        addDefault("per-command-distance-limitations.home", "default");
        addComment("per-command-distance-limitations.back", "default");

        addDefault("default-homes-limit", -1, "Homes", "The default maximum of homes people can have.\n" +
                "This can be overridden by giving people permissions such as at.member.homes.10.\n" +
                "To disable this, use -1 as provided by default.");
        addDefault("add-bed-to-homes", true, "Whether or not the bed home should be added to /homes.");

        addDefault("used-teleport-causes", new ArrayList<>(Arrays.asList("COMMAND", "PLUGIN", "SPECTATE")), "Back",
                "The teleport causes that the plugin must listen to allow players to teleport back to the previous location.\n" +
                        "You can see a full list of these causes at https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/player/PlayerTeleportEvent.TeleportCause.html");

        addDefault("teleport-to-spawn-on-first-join", true, "Spawn Management",
                "Whether the player should be teleported to the spawnpoint when they join for the first time.");
        addDefault("teleport-to-spawn-on-every-join", true,
                "Whether the player should be teleported to the spawnpoint every time they join.");

        addDefault("death-management.default", "spawn", "");
        addExample("death-management.world", "{default}", "");
        addExample("death-management.special-world", "warp:Special");
        addExample("death-management.another-world", "bed");

        addDefault("default-permissions", new ArrayList<>(Arrays.asList("at.member.*", "at.member.warp.*")), "Permissions", "");

    }

    public static NewConfig getInstance() {
        return instance;
    }

    @Override
    public void postSave() {
        new PaymentManager();
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
            if (defaultPath != null) {
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
    }
}
