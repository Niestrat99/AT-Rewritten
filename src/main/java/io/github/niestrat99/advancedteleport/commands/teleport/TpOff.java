package io.github.niestrat99.advancedteleport.commands.teleport;

import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.commands.AsyncATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TpOff implements AsyncATCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (NewConfig.get().USE_BASIC_TELEPORT_FEATURES.get()) {
                if (sender.hasPermission("at.member.off")) {
                    ATPlayer atPlayer = ATPlayer.getPlayer(player);
                    if (atPlayer.isTeleportationEnabled()) {
                        atPlayer.setTeleportationEnabled(false, callback -> {
                            sender.sendMessage(CustomMessages.getString("Info.tpOff"));
                        });

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
