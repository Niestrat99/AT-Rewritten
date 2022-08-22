package io.github.niestrat99.advancedteleport.commands.warp;

import io.github.niestrat99.advancedteleport.api.ATFloodgatePlayer;
import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.api.Warp;
import io.github.niestrat99.advancedteleport.commands.AsyncATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.sql.SQLManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class DeleteWarpCommand extends AbstractWarpCommand implements AsyncATCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        if (!sender.hasPermission("at.admin.delwarp")) {
            CustomMessages.sendMessage(sender, "Error.noPermission");
            return true;
        }
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

        if (Warp.getWarps().containsKey(args[0])) {
            Warp.getWarps().get(args[0]).delete(SQLManager.SQLCallback.getDefaultCallback(
                    sender, "Info.deletedWarp", "Error.deleteWarpFail", "{warp}", args[0]));

        } else {
            CustomMessages.sendMessage(sender, "Error.noSuchWarp");
        }
        return true;
    }
}
