package io.github.niestrat99.advancedteleport.utilities;

import io.github.niestrat99.advancedteleport.api.TeleportRequest;
import io.github.niestrat99.advancedteleport.api.TeleportRequestType;
import io.github.niestrat99.advancedteleport.api.events.ATTeleportEvent;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.MainConfig;
import io.github.niestrat99.advancedteleport.managers.CooldownManager;
import io.github.niestrat99.advancedteleport.managers.MovementManager;
import io.github.niestrat99.advancedteleport.payments.PaymentManager;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

public class AcceptRequest {

    public static void acceptRequest(TeleportRequest request) {
        Player player = request.responder();

        CustomMessages.sendMessage(request.requester(), "Info.requestAcceptedResponder", "player", player.getName());
        CustomMessages.sendMessage(player, "Info.requestAccepted");
        // Check again
        if (PaymentManager.getInstance().canPay(request.type().name().toLowerCase().replaceAll("_", ""), request.requester())) {
            if (request.type() == TeleportRequestType.TPAHERE) {
                teleport(request.requester(), player, "tpahere");
            } else {
                teleport(player, request.requester(), "tpa");
            }
        }
        request.destroy();
    }

    private static void teleport(
        Player toPlayer,
        Player fromPlayer,
        String type
    ) {
        final Location toLocation = toPlayer.getLocation();
        ATTeleportEvent event = new ATTeleportEvent(fromPlayer, toLocation, fromPlayer.getLocation(), "", ATTeleportEvent.TeleportType.valueOf(type.toUpperCase()));
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;
        int warmUp = MainConfig.get().WARM_UPS.valueOf(type).get();
        Player payingPlayer = type.equalsIgnoreCase("tpahere") ? toPlayer : fromPlayer;
        if (warmUp > 0 && !fromPlayer.hasPermission("at.admin.bypass.timer")) {
            MovementManager.createMovementTimer(fromPlayer, toLocation, type, "Teleport.eventTeleport", warmUp, payingPlayer);
            return;
        }
        PaperLib.teleportAsync(fromPlayer, toLocation, PlayerTeleportEvent.TeleportCause.COMMAND);
        CustomMessages.sendMessage(fromPlayer, "Teleport.eventTeleport");
        PaymentManager.getInstance().withdraw(type, payingPlayer);

        // If the cooldown is to be applied after only after a teleport takes place, apply it now
        if (MainConfig.get().APPLY_COOLDOWN_AFTER.get().equalsIgnoreCase("teleport")) {
            CooldownManager.addToCooldown(type, payingPlayer);
        }
    }
}
