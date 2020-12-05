package io.github.niestrat99.advancedteleport.utilities;

import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.fanciful.FancyMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
                        player.sendMessage(CustomMessages.getString("Error.noSuchPlayer"));
                        return null;
                    } else {

                        // Get the request that was sent by the target.
                        TPRequest request = TPRequest.getRequestByReqAndResponder(player, target);
                        if (request == null) {
                            player.sendMessage(CustomMessages.getString("Error.noRequests"));
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
                        player.sendMessage(CustomMessages.getString("Info.multipleRequestAccept"));
                    } else {
                        player.sendMessage(CustomMessages.getString("Info.multipleRequestDeny"));
                    }
                    for (TPRequest request : requests.getContentsInPage(1)) {
                        new FancyMessage()
                                .command("/" + type + " " + request.getRequester().getName())
                                .text(CustomMessages.getString("Info.multipleRequestsIndex").replaceAll("\\{player}", request.getRequester().getName()))
                                .send(player);
                    }
                    if (requests.getTotalPages() > 1) {
                        player.sendMessage(CustomMessages.getString("Info.multipleRequestsList"));
                    }

                }
            } else {
                return TPRequest.getRequests(player).get(0);
            }
        } else {
            player.sendMessage(CustomMessages.getString("Error.noRequests"));
            return null;
        }
        return null;
    }

}
