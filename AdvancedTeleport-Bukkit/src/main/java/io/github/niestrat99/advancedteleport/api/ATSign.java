package io.github.niestrat99.advancedteleport.api;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public abstract class ATSign {

    private final @NotNull String requiredPermission;
    private final @NotNull String adminPermission;
    private final @NotNull String name;
    private final boolean enabled;

    @Contract(pure = true)
    protected ATSign(
        @NotNull final String name,
        final boolean enabled
    ) {
        this.requiredPermission = ("at.member." + name + ".use-sign").toLowerCase();
        this.adminPermission = ("at.admin.sign." + name + ".create").toLowerCase();
        this.name = name;
        this.enabled = enabled;
    }

    public abstract void onInteract(
        @NotNull Sign sign,
        @NotNull Player player
    );

    public abstract boolean canCreate(
        @NotNull Sign sign,
        @NotNull Player player
    );

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
