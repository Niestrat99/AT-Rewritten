package io.github.niestrat99.advancedteleport.commands.teleport;

import io.github.niestrat99.advancedteleport.api.ATFloodgatePlayer;
import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.commands.PlayerCommand;
import io.github.niestrat99.advancedteleport.commands.TeleportATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.MainConfig;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

public final class TpoHere extends TeleportATCommand implements PlayerCommand {

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
                ((ATFloodgatePlayer) atPlayer).sendTpoHereForm();
            } else {
                CustomMessages.sendMessage(sender, "Error.noPlayerInput");
            }
            return true;
        }
        if (args[0].equalsIgnoreCase(player.getName())) {
            CustomMessages.sendMessage(sender, "Error.requestSentToSelf");
            return true;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            CustomMessages.sendMessage(sender, "Error.noSuchPlayer");
            return true;
        }
        CustomMessages.sendMessage(
                sender,
                "Teleport.teleportingPlayerToSelf",
                Placeholder.unparsed("player", target.getName()));
        CustomMessages.sendMessage(
                target,
                "Teleport.teleportingSelfToPlayer",
                Placeholder.unparsed("player", sender.getName()));
        ATPlayer.teleportWithOptions(
                target, player.getLocation(), PlayerTeleportEvent.TeleportCause.COMMAND);
        return true;
    }

    @Override
    public @NotNull String getPermission() {
        return "at.admin.tpohere";
    }
}
