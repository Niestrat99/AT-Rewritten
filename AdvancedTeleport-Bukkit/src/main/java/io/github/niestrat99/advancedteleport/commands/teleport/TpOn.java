package io.github.niestrat99.advancedteleport.commands.teleport;

import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.commands.ATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TpOn implements ATCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            CustomMessages.sendMessage(sender, "Error.notAPlayer");
            return true;
        }
        if (!NewConfig.get().USE_BASIC_TELEPORT_FEATURES.get()) {
            CustomMessages.sendMessage(sender, "Error.featureDisabled");
            return true;
        }
        if (!sender.hasPermission("at.member.on")) {
            CustomMessages.sendMessage(sender, "Error.noPermission");
            return true;
        }
        Player player = (Player) sender;
        if (sender.hasPermission("at.member.on")) {
            ATPlayer atPlayer = ATPlayer.getPlayer(player);
            if (!atPlayer.isTeleportationEnabled()) {
                atPlayer.setTeleportationEnabled(true, callback -> CustomMessages.sendMessage(sender, "Info.tpOn"));
            } else {
                CustomMessages.sendMessage(sender, "Error.alreadyOn");
            }
        }
        return true;
    }
}
