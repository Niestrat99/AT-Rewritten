package io.github.niestrat99.advancedteleport.commands.warp;

import io.github.niestrat99.advancedteleport.api.AdvancedTeleportAPI;
import io.github.niestrat99.advancedteleport.api.ATFloodgatePlayer;
import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.NewConfig;
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

        if (!(sender instanceof Player)) {
            CustomMessages.sendMessage(sender, "Error.notAPlayer");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            ATPlayer atPlayer = ATPlayer.getPlayer(player);
            if (atPlayer instanceof ATFloodgatePlayer && NewConfig.get().USE_FLOODGATE_FORMS.get()) {
                ((ATFloodgatePlayer) atPlayer).sendSetWarpForm();
            } else {
                CustomMessages.sendMessage(sender, "Error.noWarpInput");
            }
            return true;
        }

        Location warp = player.getLocation();

        if (!AdvancedTeleportAPI.getWarps().containsKey(args[0])) {
            AdvancedTeleportAPI.setWarp(args[0], player, warp).thenAcceptAsync(result ->
                    CustomMessages.sendMessage(sender, "Info.setWarp", "{warp}", args[0]));
        } else {
            CustomMessages.sendMessage(sender, "Error.warpAlreadySet", "{warp}", args[0]);
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
