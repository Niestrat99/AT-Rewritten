package io.github.niestrat99.advancedteleport.commands.teleport;

import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.Config;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ToggleTP implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (sender instanceof Player) {
            if (NewConfig.getInstance().USE_BASIC_TELEPORT_FEATURES.get()) {
                if (sender.hasPermission("at.member.toggletp")) {
                    if (args.length>0) {
                        if (sender.hasPermission("at.admin.toggletp")) {
                            Player target = Bukkit.getPlayer(args[0]);
                            UUID uuid = target.getUniqueId();
                            if (target.isOnline()) {
                                if (TpOff.getTpOff().contains(uuid)) {
                                    TpOff.getTpOff().remove(uuid);
                                    sender.sendMessage(CustomMessages.getString("Info.tpAdminOn"));
                                    target.sendMessage(CustomMessages.getString("Info.tpOn"));
                                } else {
                                    TpOff.getTpOff().add(uuid);
                                    sender.sendMessage(CustomMessages.getString("Info.tpAdminOff"));
                                    target.sendMessage(CustomMessages.getString("Info.tpOff"));
                                }
                            } else {
                                sender.sendMessage(CustomMessages.getString("Error.noSuchPlayer"));
                            }
                        }
                    } else {
                        Player player = (Player) sender;
                        UUID uuid = player.getUniqueId();
                        if (TpOff.getTpOff().contains(uuid)) {
                            TpOff.getTpOff().remove(uuid);
                            sender.sendMessage(CustomMessages.getString("Info.tpOn"));
                        } else {
                            TpOff.getTpOff().add(uuid);
                            sender.sendMessage(CustomMessages.getString("Info.tpOff"));
                        }
                    }
                }
            } else {
                sender.sendMessage(CustomMessages.getString("Error.featureDisabled"));
            }
        }
        return true;
    }
}
