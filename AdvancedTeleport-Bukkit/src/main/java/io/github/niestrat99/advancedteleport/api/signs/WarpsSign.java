package io.github.niestrat99.advancedteleport.api.signs;

import io.github.niestrat99.advancedteleport.api.ATSign;
import io.github.niestrat99.advancedteleport.commands.warp.WarpsCommand;
import io.github.niestrat99.advancedteleport.config.MainConfig;
import net.kyori.adventure.text.Component;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class WarpsSign extends ATSign {

    public WarpsSign() {
        super("Warps", MainConfig.get().USE_WARPS.get());
    }

    @Override
    public void onInteract(@NotNull Sign sign, @NotNull Player player) {
        WarpsCommand.sendWarps(player);
    }

    @Override
    public boolean canCreate(final @NotNull List<Component> lines, final @NotNull Player player) {
        return true;
    }
}
