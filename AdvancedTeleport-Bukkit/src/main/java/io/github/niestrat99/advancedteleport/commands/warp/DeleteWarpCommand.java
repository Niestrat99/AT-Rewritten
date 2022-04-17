package io.github.niestrat99.advancedteleport.commands.warp;

import io.github.niestrat99.advancedteleport.api.Warp;
import io.github.niestrat99.advancedteleport.commands.AsyncATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.sql.SQLManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class DeleteWarpCommand extends AbstractWarpCommand implements AsyncATCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        if (sender.hasPermission("at.admin.delwarp")) {
            if (args.length > 0) {
                if (Warp.getWarps().containsKey(args[0])) {
                    Warp.getWarps().get(args[0]).delete().thenAcceptAsync(result ->
                            CustomMessages.sendMessage(sender, result ? "Info.deletedWarp" : "Error.deleteWarpFail",
                            "{warp}", args[0]));
                } else {
                    CustomMessages.sendMessage(sender, "Error.noSuchWarp");
                }
            } else {
                CustomMessages.sendMessage(sender, "Error.noWarpInput");
            }
        } else {
            CustomMessages.sendMessage(sender, "Error.noPermission");
        }
        return true;
    }
}
