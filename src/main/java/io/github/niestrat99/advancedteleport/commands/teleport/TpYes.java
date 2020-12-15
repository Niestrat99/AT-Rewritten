package io.github.niestrat99.advancedteleport.commands.teleport;

import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.utilities.AcceptRequest;
import io.github.niestrat99.advancedteleport.utilities.TPRequest;
import io.github.niestrat99.advancedteleport.utilities.TeleportTests;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TpYes implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (sender.hasPermission("at.member.yes")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                TPRequest request = TeleportTests.teleportTests(player, args, "tpayes");
                if (request != null) {
                    // It's not null, we've already run the tests to make sure it isn't
                    AcceptRequest.acceptRequest(request);
                }
            } else {
                sender.sendMessage(CustomMessages.getString("Error.notAPlayer"));
            }
        }
        return true;
    }
}
