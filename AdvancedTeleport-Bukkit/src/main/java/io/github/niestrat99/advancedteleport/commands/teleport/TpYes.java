package io.github.niestrat99.advancedteleport.commands.teleport;

import io.github.niestrat99.advancedteleport.api.TeleportRequest;
import io.github.niestrat99.advancedteleport.api.TeleportRequestType;
import io.github.niestrat99.advancedteleport.api.events.players.TeleportAcceptEvent;
import io.github.niestrat99.advancedteleport.commands.PlayerCommand;
import io.github.niestrat99.advancedteleport.commands.TeleportATCommand;
import io.github.niestrat99.advancedteleport.config.MainConfig;
import io.github.niestrat99.advancedteleport.managers.CooldownManager;
import io.github.niestrat99.advancedteleport.utilities.AcceptRequest;
import io.github.niestrat99.advancedteleport.utilities.TeleportTests;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class TpYes extends TeleportATCommand implements PlayerCommand {

    @Override
    public boolean onCommand(
        @NotNull final CommandSender sender,
        @NotNull final Command command,
        @NotNull final String s,
        @NotNull final String[] args
    ) {
        if (!canProceed(sender)) return true;
        Player player = (Player) sender;
        TeleportRequest request = TeleportTests.teleportTests(player, args, "tpayes");
        if (request == null) return true;
        TeleportAcceptEvent event = new TeleportAcceptEvent(request.responder(), request.requester(), request.type());
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            // Could not accept request.
            return true;
        }
        // It's not null, we've already run the tests to make sure it isn't
        AcceptRequest.acceptRequest(request);
        // If the cooldown is to be applied after the request is accepted, apply it now
        if (MainConfig.get().APPLY_COOLDOWN_AFTER.get().equalsIgnoreCase("accept")) {
            CooldownManager.addToCooldown(request.type() == TeleportRequestType.TPAHERE ? "tpahere" :
                                          "tpa", request.requester());
        }
        return true;
    }

    @Override
    public @NotNull String getPermission() {
        return "at.member.yes";
    }
}
