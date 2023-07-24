package io.github.niestrat99.advancedteleport.commands.warp;

import io.github.niestrat99.advancedteleport.api.ATFloodgatePlayer;
import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.api.AdvancedTeleportAPI;
import io.github.niestrat99.advancedteleport.commands.PlayerCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.MainConfig;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class SetWarpCommand extends AbstractWarpCommand implements PlayerCommand {

    @Override
    public boolean onCommand(
            @NotNull final CommandSender sender,
            @NotNull final Command command,
            @NotNull final String s,
            @NotNull final String[] args) {
        if (!canProceed(sender)) return true;

        Player player = (Player) sender;

        if (args.length == 0) {
            ATPlayer atPlayer = ATPlayer.getPlayer(player);
            if (atPlayer instanceof ATFloodgatePlayer
                    && MainConfig.get().USE_FLOODGATE_FORMS.get()) {
                ((ATFloodgatePlayer) atPlayer).sendSetWarpForm();
            } else {
                CustomMessages.sendMessage(sender, "Error.noWarpInput");
                return false;
            }
            return true;
        }

        Location warp = player.getLocation();

        if (!AdvancedTeleportAPI.isWarpSet(args[0])) {
            AdvancedTeleportAPI.setWarp(args[0], player, warp)
                    .whenComplete(
                            (result, err) ->
                                    CustomMessages.failableContextualPath(
                                            sender,
                                            player.getUniqueId(),
                                            "Info.setWarp",
                                            "Error.setWarpFail",
                                            err,
                                            Placeholder.unparsed("warp", result.getName())));
        } else {
            CustomMessages.sendMessage(
                    sender, "Error.warpAlreadySet", Placeholder.unparsed("warp", args[0]));
        }
        return true;
    }

    @Override
    public @NotNull String getPermission() {
        return "at.admin.setwarp";
    }

    @Override
    public @Nullable List<String> onTabComplete(
            @NotNull final CommandSender sender,
            @NotNull final Command command,
            @NotNull final String s,
            @NotNull final String[] args) {
        return null;
    }
}
