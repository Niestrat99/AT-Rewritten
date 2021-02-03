package io.github.niestrat99.advancedteleport.commands.warp;

import io.github.niestrat99.advancedteleport.api.Warp;
import io.github.niestrat99.advancedteleport.commands.ATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.Warps;
import io.github.niestrat99.advancedteleport.sql.WarpSQLManager;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

public class SetWarpCommand extends AbstractWarpCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender.hasPermission("at.admin.setwarp")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                Location warp = player.getLocation();
                if (args.length > 0) {
                //    if (args[0].matches("^[a-zA-Z0-9]+$")) {
                    WarpSQLManager.get().addWarp(new Warp(player.getUniqueId(),
                            args[0],
                            warp,
                            System.currentTimeMillis(),
                            System.currentTimeMillis()), callback ->
                                    sender.sendMessage(
                                            CustomMessages.getString("Info.setWarp")
                                                    .replaceAll("\\{warp}", args[0])));

                    //    } else {
                //        sender.sendMessage(CustomMessages.getString("Error.invalidName"));
                //    }
                } else {
                    sender.sendMessage(CustomMessages.getString("Error.noWarpInput"));
                }
            }
        } else {
            sender.sendMessage(CustomMessages.getString("Error.noPermission"));
        }

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return null;
    }
}
