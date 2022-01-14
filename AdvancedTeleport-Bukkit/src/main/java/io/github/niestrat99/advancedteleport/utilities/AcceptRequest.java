package io.github.niestrat99.advancedteleport.utilities;

import io.github.niestrat99.advancedteleport.api.events.ATTeleportEvent;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.managers.CooldownManager;
import io.github.niestrat99.advancedteleport.managers.MovementManager;
import io.github.niestrat99.advancedteleport.payments.PaymentManager;
import io.papermc.lib.PaperLib;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

public class AcceptRequest {

    public static void acceptRequest(TPRequest request) {
        Player player = request.getResponder();

        CustomMessages.sendMessage(request.getRequester(), "Info.requestAcceptedResponder", "{player}", player.getName());
        CustomMessages.sendMessage(player, "Info.requestAccepted");
        // Check again
        if (PaymentManager.getInstance().canPay(request.getType().name().toLowerCase().replaceAll("_", ""), request.getRequester())) {
            if (request.getType() == TPRequest.TeleportType.TPAHERE) {
                teleport(request.getRequester(), player, "tpahere");
            } else {
                teleport(player, request.getRequester(), "tpa");
            }
        }
        request.destroy();
    }

    private static void teleport(Player toPlayer, Player fromPlayer, String type) {
        final Location toLocation = toPlayer.getLocation();
        ATTeleportEvent event = new ATTeleportEvent(fromPlayer, toLocation, fromPlayer.getLocation(), "", ATTeleportEvent.TeleportType.valueOf(type.toUpperCase()));
        if (event.isCancelled()) return;
        int warmUp = NewConfig.get().WARM_UPS.valueOf(type).get();
        Player payingPlayer = type.equalsIgnoreCase("tpahere") ? toPlayer : fromPlayer;
        if (warmUp > 0 && !fromPlayer.hasPermission("at.admin.bypass.timer")) {
            MovementManager.createMovementTimer(fromPlayer, toLocation, type, "Teleport.eventTeleport", warmUp, payingPlayer);
            return;
        }
        PaperLib.teleportAsync(fromPlayer, toLocation, PlayerTeleportEvent.TeleportCause.COMMAND);
        CustomMessages.sendMessage(fromPlayer, "Teleport.eventTeleport");
        PaymentManager.getInstance().withdraw(type, payingPlayer);
        // If the cooldown is to be applied after only after a teleport takes place, apply it now
        if (NewConfig.get().APPLY_COOLDOWN_AFTER.get().equalsIgnoreCase("teleport")) {
            CooldownManager.addToCooldown(type, payingPlayer);
        }
    }
}
