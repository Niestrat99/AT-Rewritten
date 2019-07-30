package io.github.at.commands.teleport;

import io.github.at.config.Config;
import io.github.at.config.CustomMessages;
import io.github.at.events.TeleportTrackingManager;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Tpo implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (Config.isFeatureEnabled("teleport")) {
            if (sender.hasPermission("at.admin.tpo")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if (args.length > 0) {
                        if (args[0].equalsIgnoreCase(player.getName())){
                            sender.sendMessage(CustomMessages.getString("Error.requestSentToSelf"));
                            return false;
                        }
                        Player target = Bukkit.getPlayer(args[0]);
                        if (target == null) {
                            sender.sendMessage(CustomMessages.getString("Error.noSuchPlayer"));
                            return false;
                        } else {
                            sender.sendMessage(CustomMessages.getString("Teleport.teleporting").replaceAll("\\{player}", target.getName()));
                            TeleportTrackingManager.getLastLocations().put(player, player.getLocation());
                            player.teleport(target);
                            return false;
                        }
                    } else {
                        sender.sendMessage(CustomMessages.getString("Error.noPlayerInput"));
                        return false;
                    }
                } else {
                    sender.sendMessage(CustomMessages.getString("Error.notAPlayer"));
                }
            } else {
                sender.sendMessage(CustomMessages.getString("Error.noPermission"));
                return false;
            }
        } else {
            sender.sendMessage(CustomMessages.getString("Error.featureDisabled"));
            return false;
        }
        return false;
    }
}
