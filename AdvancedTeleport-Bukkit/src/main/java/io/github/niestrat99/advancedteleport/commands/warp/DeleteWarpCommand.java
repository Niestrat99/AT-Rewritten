package io.github.niestrat99.advancedteleport.commands.warp;

import io.github.niestrat99.advancedteleport.api.ATFloodgatePlayer;
import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.api.AdvancedTeleportAPI;
import io.github.niestrat99.advancedteleport.api.Warp;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class DeleteWarpCommand extends AbstractWarpCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s,
                             @NotNull String[] args) {
        if (!canProceed(sender)) return true;

        if (args.length == 0) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                ATPlayer atPlayer = ATPlayer.getPlayer(player);
                if (atPlayer instanceof ATFloodgatePlayer && NewConfig.get().USE_FLOODGATE_FORMS.get()) {
                    ((ATFloodgatePlayer) atPlayer).sendDeleteWarpForm();
                    return true;
                }
            }
            CustomMessages.sendMessage(sender, "Error.noWarpInput");
            return true;
        }

        Warp warp = AdvancedTeleportAPI.getWarp(args[0]);

         if (warp != null) {
             warp.delete(sender).handle((x, e) -> {
                 if (e != null) e.printStackTrace();

                 CustomMessages.sendMessage(sender, (e == null) ? "Info.deletedWarp" : "Error.deleteWarpFail",
                         "{warp}", args[0]);
                 return x;
         });
        } else {
            CustomMessages.sendMessage(sender, "Error.noWarpInput");
        }
        return true;
    }

    @Override
    public String getPermission() {
        return "at.admin.delwarp";
    }
}
