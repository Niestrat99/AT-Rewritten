package io.github.niestrat99.advancedteleport.commands.teleport;

import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.Config;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TpOff implements CommandExecutor {

    private static List<UUID> tpoff = new ArrayList<>();

    public static List<UUID> getTpOff() {
        return tpoff;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            UUID uuid = player.getUniqueId();
            if (Config.isFeatureEnabled("teleport")) {
                if (sender.hasPermission("at.member.off")) {
                    if (!tpoff.contains(uuid)) {
                        tpoff.add(uuid);
                        sender.sendMessage(CustomMessages.getString("Info.tpOff"));
                    } else {
                        sender.sendMessage(CustomMessages.getString("Error.alreadyOff"));
                    }
                }
            }
        } else {
            sender.sendMessage(CustomMessages.getString("Error.notAPlayer"));
        }
        return true;
    }
}
