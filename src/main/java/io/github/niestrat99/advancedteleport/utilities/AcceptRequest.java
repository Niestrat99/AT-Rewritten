package io.github.niestrat99.advancedteleport.utilities;

import io.github.niestrat99.advancedteleport.api.ATTeleportEvent;
import io.github.niestrat99.advancedteleport.config.Config;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.events.CooldownManager;
import io.github.niestrat99.advancedteleport.events.MovementManager;
import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.payments.PaymentManager;
import io.papermc.lib.PaperLib;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class AcceptRequest {

    public static void acceptRequest(TPRequest request) {
        Player player = request.getResponder();
        request.getRequester().sendMessage(CustomMessages.getString("Info.requestAcceptedResponder").replaceAll("\\{player}", player.getName()));
        player.sendMessage(CustomMessages.getString("Info.requestAccepted"));
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
        ATTeleportEvent event = new ATTeleportEvent(fromPlayer, toPlayer.getLocation(), fromPlayer.getLocation(), "", ATTeleportEvent.TeleportType.valueOf(type.toUpperCase()));
        if (!event.isCancelled()) {
            int warmUp = NewConfig.getInstance().WARM_UPS.valueOf(type).get();
            Player payingPlayer = type.equalsIgnoreCase("tpahere") ? toPlayer : fromPlayer;
            if (warmUp > 0 && !fromPlayer.hasPermission("at.admin.bypass.timer")) {
                MovementManager.createMovementTimer(fromPlayer, toPlayer.getLocation(), type, "Teleport.eventTeleport", warmUp, payingPlayer);
            } else {
                PaperLib.teleportAsync(fromPlayer, toPlayer.getLocation(), PlayerTeleportEvent.TeleportCause.COMMAND);
                fromPlayer.sendMessage(CustomMessages.getString("Teleport.eventTeleport"));
                PaymentManager.getInstance().withdraw("tpahere", payingPlayer);
                // If the cooldown is to be applied after only after a teleport takes place, apply it now
                if(NewConfig.getInstance().APPLY_COOLDOWN_AFTER.get().equalsIgnoreCase("teleport")) {
                    CooldownManager.addToCooldown(type, payingPlayer);
                }
            }
        }
    }
}
