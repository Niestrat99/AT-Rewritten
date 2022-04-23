package io.github.niestrat99.advancedteleport.commands.warp;

import io.github.niestrat99.advancedteleport.api.AdvancedTeleportAPI;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SetWarpCommand extends AbstractWarpCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
                             @NotNull String[] args) {
        if (!canProceed(sender)) return true;
        if (sender instanceof Player) {
            Player player = (Player) sender;
            Location warp = player.getLocation();
            if (args.length > 0) {
                if (!AdvancedTeleportAPI.getWarps().containsKey(args[0])) {
                    AdvancedTeleportAPI.setWarp(args[0], player, warp).thenAcceptAsync(result ->
                            CustomMessages.sendMessage(sender, "Info.setWarp", "{warp}", args[0]));
                } else {
                    CustomMessages.sendMessage(sender, "Error.warpAlreadySet", "{warp}", args[0]);
                }
            } else {
                CustomMessages.sendMessage(sender, "Error.noWarpInput");
            }
        } else {
            CustomMessages.sendMessage(sender, "Error.notAPlayer");
        }
        return true;
    }

    @Override
    public String getPermission() {
        return "at.admin.setwarp";
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
                                      @NotNull String[] args) {
        return null;
    }
}
