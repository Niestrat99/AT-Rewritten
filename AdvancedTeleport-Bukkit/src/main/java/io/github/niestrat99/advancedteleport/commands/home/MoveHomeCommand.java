package io.github.niestrat99.advancedteleport.commands.home;

import io.github.niestrat99.advancedteleport.api.ATFloodgatePlayer;
import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.api.Home;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MoveHomeCommand extends AbstractHomeCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s,
                             @NotNull String[] args) {
        if (!canProceed(sender)) return true;
        if (sender instanceof Player) {
            Player player = (Player) sender;
            ATPlayer atPlayer = ATPlayer.getPlayer(player);
            if (args.length == 0) {
                if (atPlayer instanceof ATFloodgatePlayer) {
                    ((ATFloodgatePlayer) atPlayer).sendMoveHomeForm();
                } else {
                    CustomMessages.sendMessage(sender, "Error.noHomeInput");
                }
                return true;
            }
            if (sender.hasPermission("at.admin.movehome")) {
                // We'll just assume that the admin command overrides the homes limit.
                if (args.length > 1) {
                    ATPlayer.getPlayerFuture(args[0]).thenAccept(atTarget -> {
                        if (!atTarget.getHomes().containsKey(args[1])) {
                            CustomMessages.sendMessage(sender, "Error.noSuchHome");
                            return;
                        }
                        atTarget.moveHome(args[1], player.getLocation()).thenAcceptAsync(result ->
                                CustomMessages.sendMessage(sender, result ? "Info.movedHomeOther" :
                                        "Error.moveHomeFail", "{player}", args[0], "{home}", args[1]));

                    });
                }
                Home home = atPlayer.getHome(args[0]);

                if (home == null) {
                    CustomMessages.sendMessage(sender, "Error.noSuchHome");
                    return true;
                }

                atPlayer.moveHome(args[0], player.getLocation()).thenAcceptAsync(result ->
                        CustomMessages.sendMessage(sender, result ? "Info.movedHome" : "Error.moveHomeFail",
                                "{home}", args[0]));


            } else {
                CustomMessages.sendMessage(sender, "Error.noHomeInput");
            }

        } else {
            CustomMessages.sendMessage(sender, "Error.notAPlayer");
        }
        return true;
    }

    @Override
    public String getPermission() {
        return "at.member.movehome";
    }
}
