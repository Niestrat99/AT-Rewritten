package io.github.niestrat99.advancedteleport.utilities;

import io.github.niestrat99.advancedteleport.api.TeleportRequest;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.function.Supplier;

public class TeleportTests {

    public static TeleportRequest teleportTests(
        Player player,
        String[] args,
        String type
    ) {

        // Checks if any players have sent a request at all.
        if (TeleportRequest.getRequests(player).isEmpty()) {
            CustomMessages.sendMessage(player, "Error.noRequests");
            return null;
        }

        // Checks if there's only one request.
        if (TeleportRequest.getRequests(player).size() == 1) {
            return TeleportRequest.getRequests(player).get(0);
        }

        // If the player has specified the request they're accepting.
        if (args.length > 0) {

            // Get the player.
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                CustomMessages.sendMessage(player, "Error.noSuchPlayer");
                return null;
            }

            // Get the request that was sent by the target.
            TeleportRequest request = TeleportRequest.getRequestByReqAndResponder(player, target);
            if (request == null) {
                CustomMessages.sendMessage(player, "Error.noRequests");
                return null;
            }

            return request;
        } else {

            // This utility helps in splitting lists into separate pages, like when you list your plots with PlotMe/PlotSquared.
            PagedLists<TeleportRequest> requests = new PagedLists<>(TeleportRequest.getRequests(player), 8);
            if (type.equalsIgnoreCase("tpayes")) {
                CustomMessages.sendMessage(player, "Info.multipleRequestAccept");
            } else {
                CustomMessages.sendMessage(player, "Info.multipleRequestDeny");
            }

            final var body = CustomMessages.getPagesComponent(1, requests, request -> CustomMessages.get(
                "Info.multipleRequestsIndex",
                "command", type,
                "player", (Supplier<String>) () -> request.requester().getName() // TODO: Try use player DisplayName
            ));

            if (requests.getTotalPages() > 1) {
                CustomMessages.sendMessage(player, "Info.multipleRequestsList");
                CustomMessages.asAudience(player).sendMessage(body);
            }
        }

        return null;
    }
}
