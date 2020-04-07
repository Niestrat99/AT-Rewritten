package io.github.at.commands.teleport;

import io.github.at.config.Config;
import io.github.at.config.CustomMessages;
import io.github.at.utilities.TPRequest;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static io.github.at.utilities.TeleportTests.teleportTests;

public class TpNo implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (Config.isFeatureEnabled("teleport")) {
            if (sender.hasPermission("at.member.no")) {
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
                        request.getRequester().sendMessage(CustomMessages.getString("Info.requestDeclinedResponder").replaceAll("\\{player}", player.getName()));
                        player.sendMessage(CustomMessages.getString("Info.requestDeclined"));
                        request.destroy();
                    }
                } else {
                    sender.sendMessage(CustomMessages.getString("Error.notAPlayer"));
                }
            }
        } else {
            sender.sendMessage(CustomMessages.getString("Error.featureDisabled"));
        }
        return true;
    }
}
