package io.github.niestrat99.advancedteleport.commands.teleport;

import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.commands.AsyncATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ToggleTP implements AsyncATCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (sender instanceof Player) {
            if (NewConfig.get().USE_BASIC_TELEPORT_FEATURES.get()) {
                if (sender.hasPermission("at.member.toggletp")) {
                    if (args.length>0) {
                        if (sender.hasPermission("at.admin.toggletp")) {
                            Player target = Bukkit.getPlayer(args[0]);
                            if (target != null) {
                                ATPlayer atPlayer = ATPlayer.getPlayer(target);
                                if (atPlayer.isTeleportationEnabled()) {
                                    atPlayer.setTeleportationEnabled(false, callback -> {
                                        CustomMessages.sendMessage(sender, "Info.tpAdminOff");
                                        CustomMessages.sendMessage(target, "Info.tpOff");
                                    });
                                } else {
                                    atPlayer.setTeleportationEnabled(true, callback -> {
                                        CustomMessages.sendMessage(sender, "Info.tpAdminOn");
                                        CustomMessages.sendMessage(target, "Info.tpOn");
                                    });
                                }
                            } else {
                                CustomMessages.sendMessage(sender, "Error.noSuchPlayer");
                            }
                        }
                    } else {
                        Player player = (Player) sender;
                        ATPlayer atPlayer = ATPlayer.getPlayer(player);
                        if (atPlayer.isTeleportationEnabled()) {
                            atPlayer.setTeleportationEnabled(false, callback -> CustomMessages.sendMessage(sender, "Info.tpOff"));
                        } else {
                            atPlayer.setTeleportationEnabled(true, callback -> CustomMessages.sendMessage(sender, "Info.tpOn"));
                        }
                    }
                }
            } else {
                CustomMessages.sendMessage(sender, "Error.featureDisabled");
            }
        }
        return true;
    }
}
