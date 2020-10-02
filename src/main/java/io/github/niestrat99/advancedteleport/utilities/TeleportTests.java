package io.github.niestrat99.advancedteleport.utilities;

import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.fanciful.FancyMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class TeleportTests {

    public static boolean teleportTests(Player player, String[] args, String type) {

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
                        return false;
                    } else {

                        // Get the request that was sent by the target.
                        TPRequest request = TPRequest.getRequestByReqAndResponder(player, target);
                        if (request == null) {
                            player.sendMessage(CustomMessages.getString("Error.noRequests"));
                            return false;
                        } else {
                            // Yes, the teleport request can be accepted/declined/cancelled.
                            return true;
                        }
                    }
                } else {
                    // This utility helps in splitting lists into separate pages, like when you list your plots with PlotMe/PlotSquared.
                    PagedLists<TPRequest> requests = new PagedLists<>(TPRequest.getRequests(player), 8);
                    player.sendMessage(ChatColor.GREEN + "You have multiple teleport requests pending! Click one of the following to " + (type.equalsIgnoreCase("tpayes") ? "accept" : "deny") + ":");
                    for (TPRequest request : requests.getContentsInPage(1)) {
                        new FancyMessage()
                                .command("/" + type + " " + request.getRequester().getName())
                                .color(ChatColor.AQUA)
                                .text("> " + request.getRequester().getName())
                                .send(player);
                    }
                    if (requests.getTotalPages() > 1) {
                        player.sendMessage(CustomMessages.getString("Info.multipleRequestsList"));
                    }

                }
            } else {
                return true;
            }
        } else {
            player.sendMessage(CustomMessages.getString("Error.noRequests"));
            return false;
        }
        return false;
    }

}
