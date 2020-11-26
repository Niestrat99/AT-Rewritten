package io.github.niestrat99.advancedteleport.commands.teleport;

import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.utilities.TPRequest;
import io.github.niestrat99.advancedteleport.utilities.TeleportTests;
import io.github.niestrat99.advancedteleport.config.Config;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TpNo implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (Config.isFeatureEnabled("teleport")) {
            if (sender.hasPermission("at.member.no")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if (TeleportTests.teleportTests(player, args, "tpano")) {
                        Player target;
                        if (args.length > 0) {
                            target = Bukkit.getPlayer(args[0]);
                        } else {
                            TPRequest request = TPRequest.getRequests(player).get(0);
                            target = request.getRequester();
                        }

                        // Again, not null
                        TPRequest request = TPRequest.getRequestByReqAndResponder(player, target);
                        target.sendMessage(CustomMessages.getString("Info.requestDeclinedResponder").replaceAll("\\{player}", player.getName()));
                        player.sendMessage(CustomMessages.getString("Info.requestDeclined"));
                        if (request != null) {
                            request.destroy();
                        }
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
