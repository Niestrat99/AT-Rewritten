package io.github.niestrat99.advancedteleport.utilities;

import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.fanciful.FancyMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class TeleportTests {

    public static TPRequest teleportTests(Player player, String[] args, String type) {

        // Checks if any players have sent a request at all.
        if (!TPRequest.getRequests(player).isEmpty()) {

            // Checks if there's more than one request.
            if (TPRequest.getRequests(player).size() > 1) {

                // If the player has specified the request they're accepting.
                if (args.length > 0) {

                    // Get the player.
                    Player target = Bukkit.getPlayer(args[0]);
                    if (target == null) {
                        CustomMessages.sendMessage(player, "Error.noSuchPlayer");
                        return null;
                    } else {

                        // Get the request that was sent by the target.
                        TPRequest request = TPRequest.getRequestByReqAndResponder(player, target);
                        if (request == null) {
                            CustomMessages.sendMessage(player, "Error.noRequests");
                            return null;
                        } else {
                            // Yes, the teleport request can be accepted/declined/cancelled.
                            return request;
                        }
                    }
                } else {
                    // This utility helps in splitting lists into separate pages, like when you list your plots with PlotMe/PlotSquared.
                    PagedLists<TPRequest> requests = new PagedLists<>(TPRequest.getRequests(player), 8);
                    if (type.equalsIgnoreCase("tpayes")) {
                        CustomMessages.sendMessage(player, "Info.multipleRequestAccept");
                    } else {
                        CustomMessages.sendMessage(player, "Info.multipleRequestDeny");
                    }
                    for (int i = 0; i < requests.getContentsInPage(1).size(); i++) {
                        TPRequest request = requests.getContentsInPage(1).get(i);
                        new FancyMessage()
                                .command("/" + type + " " + request.getRequester().getName())
                                .text(CustomMessages.getStringA("Info.multipleRequestsIndex").replaceAll("\\{player}", request.getRequester().getName()))
                                .sendProposal(player, i);
                    }
                    if (requests.getTotalPages() > 1) {
                        CustomMessages.sendMessage(player, "Info.multipleRequestsList");
                        FancyMessage.send(player);
                    }

                }
            } else {
                return TPRequest.getRequests(player).get(0);
            }
        } else {
            CustomMessages.sendMessage(player, "Error.noRequests");
            return null;
        }
        return null;
    }

}
