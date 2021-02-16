package io.github.niestrat99.advancedteleport.commands.teleport;

import io.github.niestrat99.advancedteleport.commands.ATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.utilities.TPRequest;
import io.github.niestrat99.advancedteleport.utilities.TeleportTests;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TpNo implements ATCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (NewConfig.get().USE_BASIC_TELEPORT_FEATURES.get()) {
            if (sender.hasPermission("at.member.no")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    TPRequest request = TeleportTests.teleportTests(player, args, "tpano");
                    if (request != null) {
                        Player target;
                        if (args.length > 0) {
                            target = Bukkit.getPlayer(args[0]);
                        } else {
                            target = request.getRequester();
                        }
                        CustomMessages.sendMessage(target, "Info.requestDeclinedResponder", "{player}", player.getName());
                        CustomMessages.sendMessage(player, "Info.requestDeclined");
                        request.destroy();
                    }
                } else {
                    CustomMessages.sendMessage(sender, "Error.notAPlayer");
                }
            }
        } else {
            CustomMessages.sendMessage(sender, "Error.featureDisabled");
        }
        return true;
    }
}
