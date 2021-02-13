package io.github.niestrat99.advancedteleport.commands.warp;

import io.github.niestrat99.advancedteleport.api.Warp;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MoveWarpCommand extends AbstractWarpCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (sender.hasPermission("at.admin.movewarp")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                Location warpLoc = player.getLocation();
                if (args.length > 0) {
                    Warp warp = Warp.getWarps().get(args[0]);
                    if (warp != null) {
                        warp.setLocation(warpLoc, callback -> CustomMessages.sendMessage(sender, "Info.setWarp", "{warp}", args[0]));
                    }

                } else {
                    CustomMessages.sendMessage(sender, "Error.noWarpInput");
                }
            }
        } else {
            CustomMessages.sendMessage(sender, "Error.noPermission");
        }

        return true;
    }

}
