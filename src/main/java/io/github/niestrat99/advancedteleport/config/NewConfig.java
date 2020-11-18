package io.github.niestrat99.advancedteleport.config;

public class NewConfig extends ConfigurationMaster{
    /**
     *
     */
    public NewConfig() {
        super("config-new");

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

        addDefault("test.yes", true, "Test 1");
        addDefault("test.yes-2", true, "Test 2");
    }

    @Override
    public void postSave() {

    }
}
