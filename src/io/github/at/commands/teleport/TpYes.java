package io.github.at.commands.teleport;

import io.github.at.utilities.AcceptRequest;
import io.github.at.utilities.TPRequest;
import io.github.at.utilities.TeleportTests;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TpYes implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (sender.hasPermission("tbh.tp.member.yes")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (TeleportTests.teleportTests(player, args, "tpayes")) {
                    Player target;
                    if (args.length > 0) {
                        target = Bukkit.getPlayer(args[0]);
                    } else {
                        target = TPRequest.getRequests(player).get(0).getRequester();
                    }

                    // Gets the request
                    TPRequest request = TPRequest.getRequestByReqAndResponder(player, target);

                    // It's not null, we've already run the tests to make sure it isn't
                    AcceptRequest.acceptRequest(request);
                    return false;
                }
            }
        }
        return false;
    }
}
