package io.github.niestrat99.advancedteleport.commands.teleport;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.commands.TeleportATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TpUnblock extends TeleportATCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s,
                             @NotNull String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase(player.getName())) {
                    CustomMessages.sendMessage(sender, "Error.blockSelf");
                    return true;
                }
                ATPlayer atPlayer = ATPlayer.getPlayer(player);
                Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), () -> {
                    OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
                    if (!atPlayer.hasBlocked(target)) {
                        sender.sendMessage("Player never blocked");
                        return;
                    }

                    atPlayer.unblockUser(target.getUniqueId()).thenAcceptAsync(result ->
                            CustomMessages.sendMessage(sender, result ? "Info.unblockPlayer" : "Error.unblockFail",
                                    "{player}", args[0]), CoreClass.async);

                });
            } else {
                CustomMessages.sendMessage(sender, "Error.noPlayerInput");
            }
        } else {
            CustomMessages.sendMessage(sender, "Error.notAPlayer");
        }
        return true;
    }

    @Override
    public String getPermission() {
        return "at.member.unblock";
    }
}
