package io.github.at.commands.spawn;

import io.github.at.config.Config;
import io.github.at.config.CustomMessages;
import io.github.at.config.Spawn;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;

public class SetSpawn implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if (Config.isFeatureEnabled("spawn")) {
            if (sender.hasPermission("at.admin.setspawn")){
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    Location spawn = player.getLocation();
                    try {
                        Spawn.setSpawn(spawn);
                        sender.sendMessage(CustomMessages.getString("Info.setSpawn"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    sender.sendMessage(CustomMessages.getString("Error.notAPlayer"));
                }
            } else {
                sender.sendMessage(CustomMessages.getString("Error.noPermission"));
            }
        } else {
            sender.sendMessage(CustomMessages.getString("Error.featureDisabled"));
        }
        return true;
    }
}
