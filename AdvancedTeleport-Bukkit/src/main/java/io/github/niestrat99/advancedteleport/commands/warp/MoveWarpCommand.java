package io.github.niestrat99.advancedteleport.commands.warp;

import io.github.niestrat99.advancedteleport.api.ATFloodgatePlayer;
import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.api.AdvancedTeleportAPI;
import io.github.niestrat99.advancedteleport.api.Warp;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MoveWarpCommand extends AbstractWarpCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s,
                             @NotNull String[] args) {
        if (!canProceed(sender)) return true;
        if (!(sender instanceof Player)) {
            CustomMessages.sendMessage(sender, "Error.notAPlayer");
            return true;
        }
        Player player = (Player) sender;
        if (args.length == 0) {
            ATPlayer atPlayer = ATPlayer.getPlayer(player);
            if (atPlayer instanceof ATFloodgatePlayer) {
                ((ATFloodgatePlayer) atPlayer).sendMoveWarpForm();
            } else {
                CustomMessages.sendMessage(sender, "Error.noWarpInput");
            }
            return true;
        }
        Location warpLoc = player.getLocation();
        Warp warp = AdvancedTeleportAPI.getWarps().get(args[0]);
        if (warp != null) {
            warp.setLocation(warpLoc, sender).thenAcceptAsync(result ->
                    CustomMessages.sendMessage(sender, "Info.movedWarp", "{warp}", args[0]));
        } else {
            CustomMessages.sendMessage(sender, "Error.notAPlayer");
        }

        return true;
    }

    @Override
    public String getPermission() {
        return "at.admin.movewarp";
    }
}
