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

        if (!(sender instanceof Player)) {
            CustomMessages.sendMessage(sender, "Error.notAPlayer");
            return true;
        }
        if (sender.hasPermission("at.admin.movewarp")) {
            CustomMessages.sendMessage(sender, "Error.noPermission");
            return true;
        }
        if (args.length == 0) {
            CustomMessages.sendMessage(sender, "Error.noWarpInput");
            return true;
        }
        Player player = (Player) sender;
        Location warpLoc = player.getLocation();
        Warp warp = Warp.getWarps().get(args[0]);
        if (warp != null) {
            warp.setLocation(warpLoc, callback -> CustomMessages.sendMessage(sender, "Info.movedWarp", "{warp}", args[0]));
        } else {
            CustomMessages.sendMessage(sender, "Error.noSuchWarp");
        }
        return true;
    }

}
