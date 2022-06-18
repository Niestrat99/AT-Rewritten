package io.github.niestrat99.advancedteleport.commands.warp;

import io.github.niestrat99.advancedteleport.api.AdvancedTeleportAPI;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class DeleteWarpCommand extends AbstractWarpCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s,
                             @NotNull String[] args) {
        if (!canProceed(sender)) return true;
        if (args.length > 0) {
            if (AdvancedTeleportAPI.getWarps().containsKey(args[0])) {
                AdvancedTeleportAPI.getWarps().get(args[0]).delete(sender).thenAcceptAsync(result ->
                        CustomMessages.sendMessage(sender, result ? "Info.deletedWarp" : "Error.deleteWarpFail",
                                "{warp}", args[0]));
            } else {
                CustomMessages.sendMessage(sender, "Error.noSuchWarp");
            }
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
