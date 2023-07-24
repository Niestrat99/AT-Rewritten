package io.github.niestrat99.advancedteleport.commands.home;

import io.github.niestrat99.advancedteleport.api.ATFloodgatePlayer;
import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.api.Home;
import io.github.niestrat99.advancedteleport.commands.PlayerCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.MainConfig;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class MoveHomeCommand extends AbstractHomeCommand implements PlayerCommand {

    @Override
    public boolean onCommand(
        @NotNull final CommandSender sender,
        @NotNull final Command command,
        @NotNull final String s,
        @NotNull final String[] args
    ) {

        if (!canProceed(sender)) return true;

        Player player = (Player) sender;
        ATPlayer atPlayer = ATPlayer.getPlayer(player);

        if (args.length == 0) {
            if (atPlayer instanceof ATFloodgatePlayer && MainConfig.get().USE_FLOODGATE_FORMS.get()) {
                ((ATFloodgatePlayer) atPlayer).sendMoveHomeForm();
            } else {
                CustomMessages.sendMessage(sender, "Error.noHomeInput");
                return false;
            }
            return true;
        }

        if (sender.hasPermission("at.admin.movehome") && args.length > 1) {
            // We'll just assume that the admin command overrides the homes limit.
            ATPlayer.getPlayerFuture(args[0]).thenAccept(atTarget -> {

                if (!atTarget.getHomes().containsKey(args[1])) {
                    CustomMessages.sendMessage(sender, "Error.noSuchHome");
                    return;
                }

                atTarget.moveHome(args[0], player.getLocation(), sender).whenCompleteAsync((ignored, err) -> CustomMessages.failableContextualPath(
                    sender,
                    atTarget,
                    "Info.movedHome",
                    "Error.moveHomeFail",
                    err,
                    Placeholder.unparsed("home", args[1]),
                    Placeholder.unparsed("player", args[0])
                ));
            });
            return true;
        }
        Home home = atPlayer.getHome(args[0]);

        if (home == null) {
            CustomMessages.sendMessage(sender, "Error.noSuchHome");
            return true;
        }

        atPlayer.moveHome(args[0], player.getLocation(), sender).whenCompleteAsync((ignored, err) -> CustomMessages.failableContextualPath(
                sender,
                atPlayer,
                "Info.movedHome",
                "Error.moveHomeFail",
                err,
                Placeholder.unparsed("home", args[0])
        ));

        return true;
    }

    @Override
    public @NotNull String getPermission() {
        return "at.member.movehome";
    }
}
