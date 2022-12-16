package io.github.niestrat99.advancedteleport.commands.teleport;

import io.github.niestrat99.advancedteleport.api.TeleportRequest;
import io.github.niestrat99.advancedteleport.api.events.players.TeleportDenyEvent;
import io.github.niestrat99.advancedteleport.commands.PlayerCommand;
import io.github.niestrat99.advancedteleport.commands.TeleportATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.utilities.TeleportTests;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TpNo extends TeleportATCommand implements PlayerCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s,
                             @NotNull String[] args) {
        if (!canProceed(sender)) return true;
        Player player = (Player) sender;
        TeleportRequest request = TeleportTests.teleportTests(player, args, "tpano");
        if (request == null) return true;
        TeleportDenyEvent event = new TeleportDenyEvent(request.responder(), request.requester(), request.type());
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            // Could not deny request
            return true;
        }
        Player target;
        if (args.length > 0) {
            target = Bukkit.getPlayer(args[0]);
        } else {
            target = request.requester();
        }
        CustomMessages.sendMessage(target, "Info.requestDeclinedResponder", "{player}", player.getName());
        CustomMessages.sendMessage(player, "Info.requestDeclined");
        request.destroy();
        return true;
    }

    @Override
    public @NotNull String getPermission() {
        return "at.member.no";
    }
}
