package io.github.niestrat99.advancedteleport.commands.teleport;

import io.github.niestrat99.advancedteleport.api.events.players.TeleportDenyEvent;
import io.github.niestrat99.advancedteleport.commands.TeleportATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.api.TeleportRequest;
import io.github.niestrat99.advancedteleport.utilities.TeleportTests;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TpNo extends TeleportATCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s,
                             @NotNull String[] args) {
        if (!canProceed(sender)) return true;
        if (!(sender instanceof Player)) {
            CustomMessages.sendMessage(sender, "Error.notAPlayer");
            return true;
        }
        Player player = (Player) sender;
        TeleportRequest request = TeleportTests.teleportTests(player, args, "tpano");
        if (request == null) return true;
        TeleportDenyEvent event = new TeleportDenyEvent(request.getResponder(), request.getRequester(), request.getType());
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            // Could not deny request
            return true;
        }
        Player target;
        if (args.length > 0) {
            target = Bukkit.getPlayer(args[0]);
        } else {
            target = request.getRequester();
        }
        CustomMessages.sendMessage(target, "Info.requestDeclinedResponder", "{player}", player.getName());
        CustomMessages.sendMessage(player, "Info.requestDeclined");
        request.destroy();
        return true;
    }

    @Override
    public String getPermission() {
        return "at.member.no";
    }
}
