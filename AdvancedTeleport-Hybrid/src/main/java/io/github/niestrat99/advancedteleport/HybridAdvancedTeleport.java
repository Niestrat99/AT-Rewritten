package io.github.niestrat99.advancedteleport;

import org.bukkit.plugin.java.JavaPlugin;

public class HybridAdvancedTeleport extends JavaPlugin {

    private IAdvancedTeleport advancedTeleport;

    @Override
    public void onLoad() {
        if (isPaper()) {
            this.advancedTeleport = new PaperWrapper(this);
        } else {
            this.advancedTeleport = new SpigotWrapper(this);
        }

        this.advancedTeleport.onLoad();
    }

    @Override
    public void onEnable() {
        this.advancedTeleport.onEnable();
    }

    @Override
    public void onDisable() {
        this.advancedTeleport.onDisable();
    }

    private static boolean isPaper() {
        try {
            Class.forName("com.destroystokyo.paper.PaperConfig");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
