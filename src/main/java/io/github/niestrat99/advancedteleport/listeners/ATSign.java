package io.github.niestrat99.advancedteleport.listeners;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

public abstract class ATSign {

    private String requiredPermission;
    private boolean enabled;
    private String adminPermission;
    private String name;

    public ATSign(String name, boolean enabled) {
        this.requiredPermission = ("at.member." + name + ".use-sign").toLowerCase();
        this.enabled = enabled;
        this.adminPermission = ("at.admin.sign." + name + ".create").toLowerCase();
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public abstract void onInteract(Sign sign, Player player);

    public abstract boolean canCreate(Sign sign, Player player);

    public String getAdminPermission() {
        return adminPermission;
    }

    public String getRequiredPermission() {
        return requiredPermission;
    }

    public String getName() {
        return name;
    }
}
