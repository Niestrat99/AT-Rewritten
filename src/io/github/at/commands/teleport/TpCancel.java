package io.github.at.commands.teleport;

import fanciful.FancyMessage;
import io.github.at.config.Config;
import io.github.at.utilities.PagedLists;
import io.github.at.utilities.TPRequest;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TpCancel implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (Config.featTP()) {
            if (sender.hasPermission("tbh.tp.member.cancel")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    // Checks if any players have sent a request at all.
                    if (!TPRequest.getRequestsByRequester(player).isEmpty()) {
                        // Checks if there's more than one request.
                        if (TPRequest.getRequestsByRequester(player).size() > 1) {
                            // If the player has specified the request they're accepting.
                            if (args.length > 0) {
                                Player target = Bukkit.getPlayer(args[0]);
                                // Player is offline
                                if (target == null) {
                                    sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ERROR:" + ChatColor.RED + " Either the player is currently offline or doesn't exist.");
                                    return false;
                                } else {
                                    TPRequest request = TPRequest.getRequestByReqAndResponder(player, target);
                                    if (request == null) {
                                        sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ERROR:" + ChatColor.RED + "You don't have any pending requests for " + ChatColor.YELLOW + target.getName() + ChatColor.RED + "!");
                                        return false;
                                    } else {
                                        player.sendMessage(ChatColor.GREEN + "You have canceled your teleport request.");
                                        request.getResponder().sendMessage(ChatColor.YELLOW + "" + sender.getName() + ChatColor.RED + " has canceled their teleport request.");
                                        request.destroy();
                                        return false;
                                    }
                                }
                            } else {
                                // This utility helps in splitting lists into separate pages, like when you list your plots with PlotMe/PlotSquared.
                                PagedLists<TPRequest> requests = new PagedLists<>(TPRequest.getRequests(player), 8);
                                player.sendMessage(ChatColor.GREEN + "You have multiple teleport requests pending! Click one of the following to cancel:");
                                // Displays the first 8 requests
                                for (TPRequest request : requests.getContentsInPage(1)) {
                                    new FancyMessage()
                                            .command("/tpacancel " + request.getRequester().getName())
                                            .color(ChatColor.AQUA)
                                            .text("> " + request.getRequester().getName())
                                            .send(player);
                                }
                                if (requests.getTotalPages() > 1) {
                                    player.sendMessage(ChatColor.GREEN + "Do /tpalist <Page Number> To check other requests.");
                                }

                            }
                        } else {
                            TPRequest request = TPRequest.getRequestsByRequester(player).get(0);
                            request.getResponder().sendMessage(ChatColor.YELLOW + "" + player.getName() + ChatColor.GREEN + " has cancelled teleport request!");
                            player.sendMessage(ChatColor.GREEN + "You've cancelled the teleport request!");
                            request.destroy();
                            return false;
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ERROR: " + ChatColor.RED + "You don't have any pending requests!");
                        return false;
                    }
                }
            }
        } else {
            sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ERROR: " + ChatColor.RED + "The feature " + ChatColor.GOLD + "Teleport " + ChatColor.RED + "is disabled!");
            return false;
        }
        return false;
    }
}
