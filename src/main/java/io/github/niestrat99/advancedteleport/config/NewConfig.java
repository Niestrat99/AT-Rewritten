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
                "(i.e. \"You will teleport in 3 seconds!\"");
        addDefault("cancel-warm-up-on-rotation", true, "Whether or not teleportation should be cancelled if the player rotates or moves.");
        addDefault("cancel-warm-up-on-movement", true, "Whether or not teleportation should be cancelled upon movement only.");

    }

    @Override
    public void postSave() {

    }

    public static class ConfigOption<T> {

        private String path;

        public ConfigOption(String path) {
            this.path = path;
        }

        public T get() {
            return (T) instance.getConfig().get(path);
        }
    }
}
