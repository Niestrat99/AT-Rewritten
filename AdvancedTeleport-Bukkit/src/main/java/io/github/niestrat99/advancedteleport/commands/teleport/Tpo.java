package io.github.niestrat99.advancedteleport.commands.teleport;

import io.github.niestrat99.advancedteleport.commands.TeleportATCommand;
import io.github.niestrat99.advancedteleport.api.ATFloodgatePlayer;
import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

public class Tpo extends TeleportATCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s,
                             @NotNull String[] args) {
        if (!canProceed(sender)) return true;
        if (!(sender instanceof Player)) {
            CustomMessages.sendMessage(sender, "Error.notAPlayer");
            return true;
        }

        Player player = (Player) sender;
        if (args.length == 0) {
            ATPlayer atPlayer = ATPlayer.getPlayer(player);
            if (atPlayer instanceof ATFloodgatePlayer && NewConfig.get().USE_FLOODGATE_FORMS.get()) {
                ((ATFloodgatePlayer) atPlayer).sendTpoForm();
            } else {
                CustomMessages.sendMessage(sender, "Error.noPlayerInput");
            }
            return true;
        }
        if (args[0].equalsIgnoreCase(player.getName())) {
            CustomMessages.sendMessage(sender, "Error.requestSentToSelf");
            return true;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            CustomMessages.sendMessage(sender, "Error.noSuchPlayer");
        } else {
            CustomMessages.sendMessage(sender, "Teleport.teleporting", "{player}", target.getName());
            PaperLib.teleportAsync(player, target.getLocation(), PlayerTeleportEvent.TeleportCause.COMMAND);
        }
        return true;
    }

    @Override
    public @NotNull String getPermission() {
        return "at.admin.tpo";
    }
}
