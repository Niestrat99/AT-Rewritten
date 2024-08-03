package io.github.niestrat99.advancedteleport.api.signs;

import io.github.niestrat99.advancedteleport.api.ATSign;
import io.github.niestrat99.advancedteleport.api.AdvancedTeleportAPI;
import io.github.niestrat99.advancedteleport.commands.warp.WarpCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.MainConfig;
import io.github.niestrat99.advancedteleport.CoreClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class WarpSign extends ATSign {

    public WarpSign() {
        super("Warp", MainConfig.get().USE_WARPS.get());
    }

    @Override
    public void onInteract(@NotNull Sign sign, @NotNull Player player) {
        
	boolean contains = AdvancedTeleportAPI.getWarps().containsKey(sign.getLine(1));
	CoreClass.debug("Warp " + sign.getLine(1) + " exists: " + contains); 

	if (!contains) return;
        WarpCommand.warp(AdvancedTeleportAPI.getWarps().get(sign.getLine(1)), player, true);
    }

    @Override
    public boolean canCreate(final @NotNull List<Component> lines, final @NotNull Player player) {
        if (!(lines.get(1) instanceof TextComponent line)) return false;
        if (line.content().isEmpty()) {
            CustomMessages.sendMessage(player, "Error.noWarpInput");
            return false;
        } else {
            if (AdvancedTeleportAPI.getWarps().containsKey(line.content())) {
                CustomMessages.sendMessage(player, "Info.createdWarpSign");
                return true;
            } else {
                CustomMessages.sendMessage(player, "Error.noSuchWarp");
                return false;
            }
        }
    }
}
