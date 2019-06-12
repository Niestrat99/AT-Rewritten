package io.github.at.utilities;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class TeleportTests {

    private boolean teleportTests(Player player, String[] args, String type) {
        // Checks if any players have sent a request at all.
        if (!TeleportRequest.getRequests(player).isEmpty()) {
            // Checks if there's more than one request.
            if (TeleportRequest.getRequests(player).size() > 1) {
                // If the player has specified the request they're accepting.
                if (args.length > 0) {
                    // Get the player.
                    Player target = Bukkit.getPlayer(args[0]);
                    if (target == null) {
                        player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ERROR:" + ChatColor.RED + " Either the player is currently offline or doesn't exist.");
                        return false;
                    } else {
                        // Get the request that was sent by the target.
                        TeleportRequest request = TeleportRequest.getRequestByReqAndResponder(player, target);
                        if (request == null) {
                            player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ERROR:" + ChatColor.RED + "You don't have any pending requests from " + ChatColor.YELLOW + target.getName() + ChatColor.RED + "!");
                            return false;
                        } else {
                            // Yes, the teleport request can be accepted/declined/cancelled.
                            return true;
                        }
                    }
                } else {
                    // This utility helps in splitting lists into separate pages, like when you list your plots with PlotMe/PlotSquared.
                    PagedLists<TeleportRequest> requests = new PagedLists<>(TeleportRequest.getRequests(player), 8);
                    player.sendMessage(ChatColor.GREEN + "You have multiple teleport requests pending! Click one of the following to " + (type.equalsIgnoreCase("tpayes") ? "accept" : "deny") + ":");
                    for (TeleportRequest request : requests.getContentsInPage(1)) {
                        new FancyMessage()
                                .command("/" + type + " " + request.getRequester().getName())
                                .color(ChatColor.AQUA)
                                .text("> " + request.getRequester().getName())
                                .send(player);
                    }
                    if (requests.getTotalPages() > 1) {
                        player.sendMessage(ChatColor.GREEN + "Do /tpalist <Page Number> To check other requests.");
                    }

                }
            } else {
                return true;
            }
        } else {
            player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ERROR:" + ChatColor.RED + "You don't have any pending requests!");
            return false;
        }
        return false;
    }

}
