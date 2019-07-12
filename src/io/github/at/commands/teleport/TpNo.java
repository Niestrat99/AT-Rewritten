package io.github.at.commands.teleport;

import io.github.at.config.Config;
import io.github.at.utilities.TPRequest;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static io.github.at.utilities.TeleportTests.teleportTests;

public class TpNo implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (Config.isFeatureEnabled("teleport")) {
            if (sender.hasPermission("tbh.tp.member.no")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if (teleportTests(player, args, "tpano")) {
                        Player target;
                        if (args.length > 0) {
                            target = Bukkit.getPlayer(args[0]);
                        } else {
                            target = TPRequest.getRequests(player).get(0).getRequester();
                        }

                        // Again, not null
                        TPRequest request = TPRequest.getRequestByReqAndResponder(player, target);
                        request.getRequester().sendMessage(ChatColor.YELLOW + "" + player.getName() + ChatColor.GREEN + " has declined your teleport request!");
                        player.sendMessage(ChatColor.GREEN + "You've declined the teleport request!");
                        request.destroy();
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
