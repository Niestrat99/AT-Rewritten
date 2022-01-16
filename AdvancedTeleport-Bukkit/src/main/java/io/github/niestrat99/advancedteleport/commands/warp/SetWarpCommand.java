package io.github.niestrat99.advancedteleport.commands.warp;

import io.github.niestrat99.advancedteleport.api.Warp;
import io.github.niestrat99.advancedteleport.commands.ATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.sql.WarpSQLManager;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SetWarpCommand extends AbstractWarpCommand implements ATCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            CustomMessages.sendMessage(sender, "Error.notAPlayer");
            return true;
        }

        if (sender.hasPermission("at.admin.setwarp")) {
            CustomMessages.sendMessage(sender, "Error.noPermission");
            return true;
        }

        if (args.length == 0) {
            CustomMessages.sendMessage(sender, "Error.noWarpInput");
            return true;
        }

        Player player = (Player) sender;
        Location warp = player.getLocation();

        if (!Warp.getWarps().containsKey(args[0])) {
            WarpSQLManager.get().addWarp(new Warp(player.getUniqueId(),
                    args[0],
                    warp,
                    System.currentTimeMillis(),
                    System.currentTimeMillis()), callback ->
                    CustomMessages.sendMessage(sender, "Info.setWarp", "{warp}", args[0]));
        } else {
            CustomMessages.sendMessage(sender, "Error.warpAlreadySet", "{warp}", args[0]);
        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return null;
    }
}
