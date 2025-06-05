package io.github.niestrat99.advancedteleport.commands.teleport;

import io.github.niestrat99.advancedteleport.api.ATFloodgatePlayer;
import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.commands.TeleportATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.MainConfig;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

public final class Tpo extends TeleportATCommand {

    @Override
    public boolean onCommand(
            @NotNull final CommandSender sender,
            @NotNull final Command command,
            @NotNull final String s,
            @NotNull final String[] args) {

        if (args.length > 1) {
            if (!sender.hasPermission("at.admin.tpo.others")) {
                CustomMessages.sendMessage(sender, "Error.noPermission");
                return true;
            }

            Player target1 = Bukkit.getPlayerExact(args[0]);
            Player target2 = Bukkit.getPlayerExact(args[1]);

            if (target1 != null && target2 != null) {
                if (sender instanceof Player player && (target1 == player && target2 == player)) {
                    CustomMessages.sendMessage(player, "Error.requestSentToSelf");
                    return true;
                }

                CustomMessages.sendMessage(sender,
                        "Teleport.teleportingOneToAnother",
                        Placeholder.unparsed("player1", target1.getName()),
                        Placeholder.unparsed("player2", target2.getName())
                );
                CustomMessages.sendMessage(target1,
                        "Teleport.teleporting",
                        Placeholder.unparsed("player", target2.getName())
                );
                ATPlayer.teleportWithOptions(
                        target1, target2.getLocation(),
                        PlayerTeleportEvent.TeleportCause.COMMAND
                );
            } else {
                CustomMessages.sendMessage(sender, "Error.noSuchPlayer");
                return true;
            }
        } else if (args.length > 0){
            if (sender instanceof Player player) {
                if (args[0].equalsIgnoreCase(player.getName())) {
                    CustomMessages.sendMessage(sender, "Error.requestSentToSelf");
                    return true;
                }

                Player target = Bukkit.getPlayerExact(args[0]);
                if (target == null) {
                    CustomMessages.sendMessage(sender, "Error.noSuchPlayer");
                    return true;
                } else {
                    CustomMessages.sendMessage(
                            sender, "Teleport.teleporting",
                            Placeholder.unparsed("player", target.getName())
                    );
                    ATPlayer.teleportWithOptions(
                            player, target.getLocation(),
                            PlayerTeleportEvent.TeleportCause.COMMAND
                    );
                }

            } else {
                CustomMessages.sendMessage(sender,"Error.notAPlayer");
                return true;
            }
        } else {
            if (sender instanceof Player player) {
                ATPlayer atPlayer = ATPlayer.getPlayer(player);
                if (atPlayer instanceof ATFloodgatePlayer atFloodgatePlayer
                        && MainConfig.get().USE_FLOODGATE_FORMS.get()) {
                    if (!atFloodgatePlayer.getVisiblePlayerNames().isEmpty()) {
                        atFloodgatePlayer.sendTpoForm();
                    } else {
                        CustomMessages.sendMessage(sender, "Error.noOthersToTP");
                    }
                } else {
                    CustomMessages.sendMessage(sender, "Error.noPlayerInput");
                }
            }
        }
        return true;
    }


    @Override
    public @NotNull String getPermission() {
        return "at.admin.tpo";
    }
}
