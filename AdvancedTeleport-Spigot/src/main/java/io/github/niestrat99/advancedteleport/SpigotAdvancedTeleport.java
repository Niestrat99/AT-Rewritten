package io.github.niestrat99.advancedteleport;

import org.bukkit.plugin.java.JavaPlugin;

public class SpigotAdvancedTeleport extends JavaPlugin {

    private final SpigotWrapper wrapper;

    public SpigotAdvancedTeleport() {
        super();
        this.wrapper = new SpigotWrapper(this);
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
