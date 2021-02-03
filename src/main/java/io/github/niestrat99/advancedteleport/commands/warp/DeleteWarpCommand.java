package io.github.niestrat99.advancedteleport.commands.warp;

import io.github.niestrat99.advancedteleport.api.Warp;
import io.github.niestrat99.advancedteleport.commands.AsyncATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class DeleteWarpCommand extends AbstractWarpCommand implements AsyncATCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        if (sender.hasPermission("at.admin.delwarp")) {
            if (args.length > 0) {
                if (Warp.getWarps().containsKey(args[0])) {
                    Warp.getWarps().get(args[0]).delete(callback ->
                            sender.sendMessage(CustomMessages.getString("Info.deletedWarp").replaceAll("\\{warp}", args[0])));

                } else {
                    sender.sendMessage(CustomMessages.getString("Error.noSuchWarp"));
                }
            } else {
                sender.sendMessage(CustomMessages.getString("Error.noWarpInput"));
            }
        } else {
            sender.sendMessage(CustomMessages.getString("Error.noPermission"));
        }
        return true;
    }
}
