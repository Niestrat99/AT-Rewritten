package io.github.niestrat99.advancedteleport.commands.teleport;

import io.github.niestrat99.advancedteleport.api.TeleportRequest;
import io.github.niestrat99.advancedteleport.api.TeleportRequestType;
import io.github.niestrat99.advancedteleport.api.events.players.TeleportAcceptEvent;
import io.github.niestrat99.advancedteleport.commands.TeleportATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.managers.CooldownManager;
import io.github.niestrat99.advancedteleport.utilities.AcceptRequest;
import io.github.niestrat99.advancedteleport.utilities.TeleportTests;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TpYes extends TeleportATCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s,
                             @NotNull String[] args) {
        if (!canProceed(sender)) return true;
        if (!(sender instanceof Player)) {
            CustomMessages.sendMessage(sender, "Error.notAPlayer");
            return true;
        }
        Player player = (Player) sender;
        TeleportRequest request = TeleportTests.teleportTests(player, args, "tpayes");
        if (request == null) return true;
        TeleportAcceptEvent event = new TeleportAcceptEvent(request.getResponder(), request.getRequester(), request.getType());
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            // Could not accept request.
            return true;
        }
        // It's not null, we've already run the tests to make sure it isn't
        AcceptRequest.acceptRequest(request);
        // If the cooldown is to be applied after the request is accepted, apply it now
        if (NewConfig.get().APPLY_COOLDOWN_AFTER.get().equalsIgnoreCase("accept")) {
            CooldownManager.addToCooldown(request.getType() == TeleportRequestType.TPAHERE ? "tpahere" :
                    "tpa", request.getRequester());
        }
        return true;
    }

    @Override
    public String getPermission() {
        return "at.member.yes";
    }
}
