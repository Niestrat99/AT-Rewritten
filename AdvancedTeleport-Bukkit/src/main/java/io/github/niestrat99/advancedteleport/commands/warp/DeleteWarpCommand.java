package io.github.niestrat99.advancedteleport.commands.warp;

import io.github.niestrat99.advancedteleport.api.ATFloodgatePlayer;
import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.api.AdvancedTeleportAPI;
import io.github.niestrat99.advancedteleport.api.Warp;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.MainConfig;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class DeleteWarpCommand extends AbstractWarpCommand {

    @Override
    public boolean onCommand(
            @NotNull final CommandSender sender,
            @NotNull final Command command,
            @NotNull final String s,
            @NotNull final String[] args
    ) {

        // If the command can't proceed due to being disabled, stop there
        if (!canProceed(sender)) return true;

        // If no arguments have been chosen, see if the sender is a floodgate player
        if (args.length == 0) {
            if (sender instanceof Player player) {
                ATPlayer atPlayer = ATPlayer.getPlayer(player);
                if (atPlayer instanceof ATFloodgatePlayer && MainConfig.get().USE_FLOODGATE_FORMS.get()) {
                    ((ATFloodgatePlayer) atPlayer).sendDeleteWarpForm();
                    return true;
                }
            }

            // Otherwise, tell the player to enter a warp.
            CustomMessages.sendMessage(sender, "Error.noWarpInput");
            return true;
        }

        // Get the warp to be deleted.
        Warp warp = AdvancedTeleportAPI.getWarp(args[0]);

        // If the warp exists, delete it.
         if (warp != null) {
             warp.delete(sender).whenCompleteAsync((ignored, exception) -> CustomMessages.failable(
                     sender,
                     "Info.deletedWarp",
                     "Error.deleteWarpFail",
                     exception,
                     "warp", args[0]
             ));
        } else {
            CustomMessages.sendMessage(sender, "Error.noWarpInput");
        }
        return true;
    }

    @Override
    public @NotNull String getPermission() {
        return "at.admin.delwarp";
    }
}
