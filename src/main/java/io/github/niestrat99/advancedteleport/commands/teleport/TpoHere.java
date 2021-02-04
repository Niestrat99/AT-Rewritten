package io.github.niestrat99.advancedteleport.commands.teleport;

import io.github.niestrat99.advancedteleport.commands.ATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.sql.PlayerSQLManager;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

public class TpoHere implements ATCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (NewConfig.getInstance().USE_BASIC_TELEPORT_FEATURES.get()) {
            if (sender.hasPermission("at.admin.tpohere")){
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if (args.length>0) {
                        if (args[0].equalsIgnoreCase(player.getName())) {
                            sender.sendMessage(CustomMessages.getString("Error.requestSentToSelf"));
                            return true;
                        }
                        Player target = Bukkit.getPlayer(args[0]);
                        if (target == null) {
                            if (sender.hasPermission("at.admin.tpohere.offline")) {
                                PlayerSQLManager.get().movePlayer(args[0], player.getLocation(), callback -> {
                                    sender.sendMessage("Teleported offline player " + args[0]);
                                });
                                return true;
                            }
                            sender.sendMessage(CustomMessages.getString("Error.noSuchPlayer"));
                        } else {
                            sender.sendMessage(CustomMessages.getString("Teleport.teleportingPlayerToSelf").replaceAll("\\{player}", target.getName()));
                            target.sendMessage(CustomMessages.getString("Teleport.teleportingSelfToPlayer").replaceAll("\\{player}", sender.getName()));
                            PaperLib.teleportAsync(target, player.getLocation(), PlayerTeleportEvent.TeleportCause.COMMAND);
                        }
                    } else {
                        sender.sendMessage(CustomMessages.getString("Error.noPlayerInput"));
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
