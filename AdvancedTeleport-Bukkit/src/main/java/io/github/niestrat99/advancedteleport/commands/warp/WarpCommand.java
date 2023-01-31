package io.github.niestrat99.advancedteleport.commands.warp;

import io.github.niestrat99.advancedteleport.api.ATFloodgatePlayer;
import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.api.AdvancedTeleportAPI;
import io.github.niestrat99.advancedteleport.api.Warp;
import io.github.niestrat99.advancedteleport.api.events.ATTeleportEvent;
import io.github.niestrat99.advancedteleport.commands.TimedATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.MainConfig;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class WarpCommand extends AbstractWarpCommand implements TimedATCommand {

    @Override
    public boolean onCommand(
        @NotNull final CommandSender sender,
        @NotNull final Command command,
        @NotNull final String s,
        @NotNull final String[] args
    ) {

        // If the feature isn't enabled/no permission, stop there
        if (!canProceed(sender)) return true;

        Player player = (Player) sender;

        // If there's no arguments specified, see if the player is a Bedrock player and use a form
        if (args.length == 0) {
            ATPlayer atPlayer = ATPlayer.getPlayer(player);
            if (atPlayer instanceof ATFloodgatePlayer && MainConfig.get().USE_FLOODGATE_FORMS.get()) {
                ((ATFloodgatePlayer) atPlayer).sendWarpForm();
            } else {
                CustomMessages.sendMessage(sender, "Error.noWarpInput");
            }
            return true;
        }

        // If the warp exists and the player isn't already teleporting, may as well warp them
        Warp warp = AdvancedTeleportAPI.getWarps().get(args[0]);
        if (warp != null) {
            warp(warp, player, false);
        } else {
            CustomMessages.sendMessage(sender, "Error.noSuchWarp");
        }
        return true;
    }

    public static void warp(
        Warp warp,
        Player player,
        boolean useSign
    ) {
        String warpPrefix = "at.member.warp." + (useSign ? "sign." : "");

        boolean found = player.hasPermission(warpPrefix + "*");
        if (player.isPermissionSet(warpPrefix + warp.getName().toLowerCase())) {
            found = player.hasPermission(warpPrefix + warp.getName().toLowerCase());
        }
        if (!found) {
            CustomMessages.sendMessage(player, "Error.noPermissionWarp", "warp", warp.getName());
            return;
        }
        ATTeleportEvent event = new ATTeleportEvent(player, warp.getLocation(), player.getLocation(), warp.getName(), ATTeleportEvent.TeleportType.WARP);
        Bukkit.getPluginManager().callEvent(event);
        ATPlayer.getPlayer(player).teleport(event, "warp", "Teleport.teleportingToWarp");
    }

    @Override
    public @NotNull String getPermission() {
        return "at.member.warp";
    }

    @Override
    public @NotNull String getSection() {
        return "warp";
    }
}
