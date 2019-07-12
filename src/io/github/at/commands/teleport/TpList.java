package io.github.at.commands.teleport;

import fanciful.FancyMessage;
import io.github.at.config.Config;
import io.github.at.utilities.PagedLists;
import io.github.at.utilities.TPRequest;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TpList implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (Config.isFeatureEnabled("teleport")) {
            if (sender.hasPermission("tbh.tp.member.list")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    // If there are actually any pending teleport requests.
                    if (!TPRequest.getRequests(player).isEmpty()) {
                        if (args.length > 0) {
                            // Check if the argument can be parsed as an actual number.
                            // ^ means at the start of the string.
                            // [0-9] means any number in the range of 0 to 9.
                            // + means one or more of, allowing two or three digits.
                            // $ means the end of the string.
                            if (args[0].matches("^[0-9]+$")) {
                                // args[0] is officially an int.
                                int page = Integer.parseInt(args[0]);
                                PagedLists<TPRequest> requests = new PagedLists<>(TPRequest.getRequests(player), 8);
                                player.sendMessage(ChatColor.GREEN + "Click one of the following to accept:");
                                try {
                                    for (TPRequest request : requests.getContentsInPage(page)) {
                                        new FancyMessage()
                                                .command("/tpayes " + request.getRequester().getName())
                                                .color(ChatColor.AQUA)
                                                .text("> " + request.getRequester().getName())
                                                .send(player);
                                    }
                                } catch (IllegalArgumentException ex) {
                                    player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ERROR:" + ChatColor.RED + " You've inserted an invalid page number!");
                                }

                            } else {
                                player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ERROR:" + ChatColor.RED + " You've inserted an invalid page number!");

                            }
                        } else {
                            PagedLists<TPRequest> requests = new PagedLists<>(TPRequest.getRequests(player), 8);
                            player.sendMessage(ChatColor.GREEN + "Click one of the following to accept:");
                            for (TPRequest request : requests.getContentsInPage(1)) {
                                new FancyMessage()
                                        .command("/tpayes " + request.getRequester().getName())
                                        .color(ChatColor.AQUA)
                                        .text("> " + request.getRequester().getName())
                                        .send(player);
                            }
                            return false;
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ERROR:" + ChatColor.RED + "You don't have any pending requests!");
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
