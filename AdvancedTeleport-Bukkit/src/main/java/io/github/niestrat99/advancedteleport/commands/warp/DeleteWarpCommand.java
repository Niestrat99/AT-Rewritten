package io.github.niestrat99.advancedteleport.commands.warp;

import io.github.niestrat99.advancedteleport.api.ATFloodgatePlayer;
import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.api.AdvancedTeleportAPI;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
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
                if (atPlayer instanceof ATFloodgatePlayer) {
                    ((ATFloodgatePlayer) atPlayer).sendDeleteWarpForm();
                    return true;
                }
            }
            CustomMessages.sendMessage(sender, "Error.noWarpInput");
            return true;
        }

        if (AdvancedTeleportAPI.getWarps().containsKey(args[0])) {
            AdvancedTeleportAPI.getWarps().get(args[0]).delete(sender).thenAcceptAsync(result ->
                    CustomMessages.sendMessage(sender, result ? "Info.deletedWarp" : "Error.deleteWarpFail",
                            "{warp}", args[0]));
        } else {
            CustomMessages.sendMessage(sender, "Error.noSuchWarp");
        }
        return true;
    }

    @Override
    public String getPermission() {
        return "at.admin.delwarp";
    }
}
