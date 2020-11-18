package io.github.niestrat99.advancedteleport.config;

public class NewConfig extends ConfigurationMaster{
    /**
     *
     */
    public NewConfig() {
        super("config-new");
        addDefault("use-basic-teleport-features", true, "Features", "Whether basic teleportation features should be enable or not." +
                "\nThis includes /tpa, /tpahere, ");
    }

    @Override
    public void loadDefaults() {

    }

    @Override
    public void postSave() {

    }
}
