package io.github.niestrat99.advancedteleport.commands.warp;

import io.github.niestrat99.advancedteleport.api.AdvancedTeleportAPI;
import io.github.niestrat99.advancedteleport.api.ATFloodgatePlayer;
import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.NewConfig;
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
        if (!canProceed(sender)) return true;

        if (args.length == 0) {
            if (sender instanceof Player player) {
                ATPlayer atPlayer = ATPlayer.getPlayer(player);
                if (atPlayer instanceof ATFloodgatePlayer && NewConfig.get().USE_FLOODGATE_FORMS.get()) {
                    ((ATFloodgatePlayer) atPlayer).sendDeleteWarpForm();
                    return true;
                }
            }
            CustomMessages.sendMessage(sender, "Error.noWarpInput");
            return true;
        }

         if (AdvancedTeleportAPI.getWarps().containsKey(args[0])) {
             AdvancedTeleportAPI.getWarps().get(args[0]).delete(sender).whenCompleteAsync((ignored, exception) -> CustomMessages.failable(
                 sender,
                 "Error.deleteWarpFail",
                 "Info.deletedWarp,",
                 () -> exception != null,
                "{warp}", args[0]
             ));
        } else CustomMessages.sendMessage(sender, "Error.noWarpInput");

        return true;
    }

    @Override
    public @NotNull String getPermission() {
        return "at.admin.delwarp";
    }
}
