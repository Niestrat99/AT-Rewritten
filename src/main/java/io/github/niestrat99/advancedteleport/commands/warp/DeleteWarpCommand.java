package io.github.niestrat99.advancedteleport.commands.warp;

import io.github.niestrat99.advancedteleport.commands.ATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.Warps;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class DeleteWarpCommand extends AbstractWarpCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        if (sender.hasPermission("at.admin.warpdel")) {
            if (args.length > 1) {
                if (Warps.getWarps().containsKey(args[1])) {
                    try {
                        Warps.delWarp(args[1]);
                        sender.sendMessage(CustomMessages.getString("Info.deletedWarp").replaceAll("\\{warp}", args[1]));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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
