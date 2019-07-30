package io.github.at.commands.teleport;

import io.github.at.config.Config;
import io.github.at.config.CustomMessages;
import io.github.at.events.TeleportTrackingManager;
import io.github.at.utilities.DistanceLimiter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TpoHere implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (Config.isFeatureEnabled("teleport")) {
            if (sender.hasPermission("at.admin.tpohere")){
                if (sender instanceof Player){
                    Player player = (Player)sender;
                    if (args.length>0){
                        if (args[0].equalsIgnoreCase(player.getName())){
                            sender.sendMessage(CustomMessages.getString("Error.requestSentToSelf"));
                            return false;
                        }
                        Player target = Bukkit.getPlayer(args[0]);
                        if (target == null){
                            sender.sendMessage(CustomMessages.getString("Error.noSuchPlayer"));
                            return false;
                        } else {
                            sender.sendMessage(CustomMessages.getString("Teleport.teleportingPlayerToSelf").replaceAll("\\{player}", target.getName()));
                            target.sendMessage(CustomMessages.getString("Teleport.teleportingSelfToPlayer").replaceAll("\\{player}", sender.getName()));
                            TeleportTrackingManager.getLastLocations().put(target, target.getLocation());
                            target.teleport(player);
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
