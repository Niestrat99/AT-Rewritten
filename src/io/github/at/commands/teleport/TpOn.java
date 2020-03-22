package io.github.at.commands.teleport;

import io.github.at.config.Config;
import io.github.at.config.CustomMessages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class TpOn implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player)sender;
            UUID uuid = player.getUniqueId();
            if (Config.isFeatureEnabled("teleport")) {
                if (sender.hasPermission("at.member.on")) {
                    if (TpOff.getTpOff().contains(uuid)) {
                        TpOff.getTpOff().remove(uuid);
                        sender.sendMessage(CustomMessages.getString("Info.tpOn"));
                    } else {
                        sender.sendMessage(CustomMessages.getString("Error.alreadyOn"));
                    }
                }
            } else {
                sender.sendMessage(CustomMessages.getString("Error.featureDisabled"));
                return false;
            }
        } else {
            sender.sendMessage(CustomMessages.getString("Error.notAPlayer"));
        }
        return false;
    }
}
