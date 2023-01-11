package io.github.niestrat99.advancedteleport.api;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public abstract class ATSign {

    private final @NotNull String requiredPermission;
    private final @NotNull String adminPermission;
    private final @NotNull String name;
    private final boolean enabled;

    public ATSign(@NotNull String name, boolean enabled) {
        this.requiredPermission = ("at.member." + name + ".use-sign").toLowerCase();
        this.adminPermission = ("at.admin.sign." + name + ".create").toLowerCase();
        this.name = name;
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public abstract void onInteract(@NotNull Sign sign, @NotNull Player player);

    public abstract boolean canCreate(@NotNull Sign sign, @NotNull Player player);

    @NotNull
    public String getAdminPermission() {
        return adminPermission;
    }

    @NotNull
    public String getRequiredPermission() {
        return requiredPermission;
    }

    @NotNull
    public String getName() {
        return name;
    }
}
