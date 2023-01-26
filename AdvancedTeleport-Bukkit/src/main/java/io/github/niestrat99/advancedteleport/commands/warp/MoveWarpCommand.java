package io.github.niestrat99.advancedteleport.commands.warp;

import io.github.niestrat99.advancedteleport.api.ATFloodgatePlayer;
import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.api.AdvancedTeleportAPI;
import io.github.niestrat99.advancedteleport.api.Warp;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class MoveWarpCommand extends AbstractWarpCommand {

    @Override
    public boolean onCommand(
            @NotNull final CommandSender sender,
            @NotNull final Command command,
            @NotNull final String s,
            @NotNull final String[] args
    ) {

        // If the command can't proceed due to being disabled, stop there
        if (!canProceed(sender)) return true;

        Player player = (Player) sender;

        // If there's been no arguments specified, see if it's a floodgate player and otherwise continue
        if (args.length == 0) {
            ATPlayer atPlayer = ATPlayer.getPlayer(player);
            if (atPlayer instanceof ATFloodgatePlayer && NewConfig.get().USE_FLOODGATE_FORMS.get()) {
                ((ATFloodgatePlayer) atPlayer).sendMoveWarpForm();
            } else {
                CustomMessages.sendMessage(sender, "Error.noWarpInput");
            }
            return true;
        }

        // Get the new warp location
        Location warpLoc = player.getLocation();
        Warp warp = AdvancedTeleportAPI.getWarp(args[0]);

        // If the warp exists, move it, otherwise, the warp doesn't exist
        if (warp != null) {
            warp.setLocation(warpLoc, sender).thenAcceptAsync(result ->
                    CustomMessages.sendMessage(sender, "Info.movedWarp", "{warp}", args[0]));
        } else {
            CustomMessages.sendMessage(sender, "Error.noSuchWarp");
        }

        return true;
    }

    @Override
    public @NotNull String getPermission() {
        return "at.admin.movewarp";
    }
}
