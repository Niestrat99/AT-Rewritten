package io.github.niestrat99.advancedteleport.commands.teleport;

import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.commands.AsyncATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TpOn implements AsyncATCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player)sender;
            if (NewConfig.getInstance().USE_BASIC_TELEPORT_FEATURES.get()) {
                if (sender.hasPermission("at.member.on")) {
                    ATPlayer atPlayer = ATPlayer.getPlayer(player);
                    if (!atPlayer.isTeleportationEnabled()) {
                        atPlayer.setTeleportationEnabled(true, callback -> {
                            sender.sendMessage(CustomMessages.getString("Info.tpOn"));
                        });
                    } else {
                        sender.sendMessage(CustomMessages.getString("Error.alreadyOn"));
                    }
                }
            } else {
                sender.sendMessage(CustomMessages.getString("Error.featureDisabled"));
            }
        } else {
            sender.sendMessage(CustomMessages.getString("Error.notAPlayer"));
        }
        return true;
    }
}
