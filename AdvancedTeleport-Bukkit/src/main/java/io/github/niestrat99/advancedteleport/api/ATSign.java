package io.github.niestrat99.advancedteleport.api;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public abstract class ATSign {

    @NotNull private final String requiredPermission;
    private final boolean enabled;
    @NotNull private final String adminPermission;
    @NotNull private final String name;

    @Contract(pure = true)
    protected ATSign(
        @NotNull final String name,
        final boolean enabled
    ) {
        this.requiredPermission = ("at.member." + name + ".use-sign").toLowerCase();
        this.enabled = enabled;
        this.adminPermission = ("at.admin.sign." + name + ".create").toLowerCase();
        this.name = name;
    }

    public abstract void onInteract(Sign sign, Player player);

    public abstract boolean canCreate(Sign sign, Player player);

    @Contract(pure = true)
    public boolean isEnabled() {
        return enabled;
    }

    @Contract(pure = true)
    public @NotNull String getAdminPermission() {
        return adminPermission;
    }

    @Contract(pure = true)
    public @NotNull String getRequiredPermission() {
        return requiredPermission;
    }

    @Contract(pure = true)
    public @NotNull String getName() {
        return name;
    }
}
