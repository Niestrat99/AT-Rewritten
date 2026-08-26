package io.github.niestrat99.advancedteleport.utilities;

import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.api.TeleportRequest;
import io.github.niestrat99.advancedteleport.api.TeleportRequestType;
import io.github.niestrat99.advancedteleport.api.events.ATTeleportEvent;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.managers.MovementManager;
import io.github.niestrat99.advancedteleport.payments.PaymentManager;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class AcceptRequest {

    public static void acceptRequest(TeleportRequest request) {
        Player player = request.responder();

        CustomMessages.sendMessage(
                request.requester(),
                "Info.requestAcceptedResponder",
                Placeholder.unparsed("player", player.getName()));
        CustomMessages.sendMessage(player, "Info.requestAccepted");

        Location toLoc =
                request.type() == TeleportRequestType.TPAHERE
                        ? request.requester().getLocation()
                        : request.responder().getLocation();

        // Check again
        if (PaymentManager.getInstance()
                .canPay(
                        request.type().name().toLowerCase().replaceAll("_", ""),
                        request.requester(),
                        toLoc.getWorld())) {
            if (request.type() == TeleportRequestType.TPAHERE) {
                teleport(request.requester(), player, "tpahere");
            } else {
                teleport(player, request.requester(), "tpa");
            }
        }
        request.destroy();
    }

    private static void teleport(Player toPlayer, Player fromPlayer, String type) {
        final Location toLocation = toPlayer.getLocation();
        ATTeleportEvent event =
                new ATTeleportEvent(
                        fromPlayer,
                        toPlayer::getLocation,
                        fromPlayer::getLocation,
                        "",
                        ATTeleportEvent.TeleportType.valueOf(type.toUpperCase()));
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;
        ATPlayer atPlayer = ATPlayer.getPlayer(fromPlayer);
        int warmUp = atPlayer.getWarmUp(type, toLocation.getWorld());
        Player payingPlayer = type.equalsIgnoreCase("tpahere") ? toPlayer : fromPlayer;
        if (warmUp > 0 && !fromPlayer.hasPermission("at.admin.bypass.timer")) {
            MovementManager.createMovementTimer(
                    fromPlayer, toPlayer::getLocation, type, "Teleport.eventTeleport", "N/A", warmUp, payingPlayer);
            return;
        }

        ATPlayer.teleport(fromPlayer, type, "N/A", "Teleport.eventTeleport", toPlayer::getLocation);
    }
}
