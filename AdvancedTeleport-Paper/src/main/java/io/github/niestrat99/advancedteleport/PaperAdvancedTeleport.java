package io.github.niestrat99.advancedteleport;

import org.bukkit.plugin.java.JavaPlugin;

public class PaperAdvancedTeleport extends JavaPlugin {

    private final PaperWrapper wrapper;

    public PaperAdvancedTeleport() {
        super();
        this.wrapper = new PaperWrapper(this);
    }

    @Override
    public void onLoad() {
        this.wrapper.onLoad();
    }

    @Override
    public void onEnable() {
        this.wrapper.onEnable();
    }

    @Override
    public void onDisable() {
        this.wrapper.onDisable();
    }
}
