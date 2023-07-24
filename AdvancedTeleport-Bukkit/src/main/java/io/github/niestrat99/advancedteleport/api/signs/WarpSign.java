package io.github.niestrat99.advancedteleport.api.signs;

import io.github.niestrat99.advancedteleport.api.ATSign;
import io.github.niestrat99.advancedteleport.api.AdvancedTeleportAPI;
import io.github.niestrat99.advancedteleport.commands.warp.WarpCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.MainConfig;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class WarpSign extends ATSign {

    public WarpSign() {
        super("Warp", MainConfig.get().USE_WARPS.get());
    }

    @Override
    public void onInteract(@NotNull Sign sign, @NotNull Player player) {
        if (!AdvancedTeleportAPI.getWarps().containsKey(sign.getLine(1))) return;
        WarpCommand.warp(AdvancedTeleportAPI.getWarps().get(sign.getLine(1)), player, true);
    }

    @Override
    public boolean canCreate(@NotNull Sign sign, @NotNull Player player) {
        if (sign.getLine(1).isEmpty()) {
            CustomMessages.sendMessage(player, "Error.noWarpInput");
            return false;
        } else {
            if (AdvancedTeleportAPI.getWarps().containsKey(sign.getLine(1))) {
                String warpName = sign.getLine(1);
                sign.setLine(0, ChatColor.BLUE + "" + ChatColor.BOLD + "[Warp]");
                sign.setLine(1, warpName);
                CustomMessages.sendMessage(player, "Info.createdWarpSign");
                return true;
            } else {
                CustomMessages.sendMessage(player, "Error.noSuchWarp");
                return false;
            }
        }
    }
}
