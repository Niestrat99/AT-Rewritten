package io.github.niestrat99.advancedteleport.config;

public class NewConfig extends ConfigurationMaster {

    public static ConfigOption<Boolean> USE_BASIC_TELEPORT_FEATURES = new ConfigOption<>("use-basic-teleport-features");
    public static ConfigOption<Boolean> USE_WARPS = new ConfigOption<>("use-warps");
    public static ConfigOption<Boolean> USE_RANDOMTP = new ConfigOption<>("use-randomtp");
    public static ConfigOption<Boolean> USE_SPAWN = new ConfigOption<>("use-spawn");
    public static ConfigOption<Boolean> USE_HOMES = new ConfigOption<>("use-homes");

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
        addDefault("use-basic-teleport-features", true, "Features", "Whether basic teleportation features should be enable or not." +
                "\nThis includes /tpa, /tpahere, /tpblock, /tpunblock and /back." +
                "\nThis does not disable the command for other plugins - if you want other plugins to use the provided commands, use Bukkit's commands.yml file." +
                "\nPlease refer to https://bukkit.gamepedia.com/Commands.yml for this!");

        addDefault("use-warps", true, "Whether warps should be enabled in the plugin.");
        addDefault("use-spawn", true, "Whether the plugin should modify spawn/spawn properties.");
        addDefault("use-randomtp", true, "Whether the plugin should allow random teleportation.");
        addDefault("use-homes", true, "Whether homes should be enabled in the plugin.");

        addDefault("warp-up-timer-duration", 3, "Warm-Up Timers", "The number of seconds it takes for the teleportation to take place following confirmation.\n" +
                "(i.e. \"You will teleport in 3 seconds!\"\n" +
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

        addDefault("per-command-cooldowns.tpa", "default", "Cooldown for /tpa.");
        addDefault("per-command-cooldowns.tpahere", "default", "Cooldown for /tpahere");
        addDefault("per-command-cooldowns.tpr", "default", "Cooldown for /tpr, or /rtp.");
        addDefault("per-command-cooldowns.warp", "default", "Cooldown for /warp");
        addDefault("per-command-cooldowns.spawn", "default", "Cooldown for /spawn");
        addDefault("per-command-cooldowns.home", "default", "Cooldown for /home");
        addDefault("per-command-cooldowns.back", "default", "Cooldown for /back");

    }

    @Override
    public void postSave() {

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
}
