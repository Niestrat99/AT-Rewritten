package io.github.niestrat99.advancedteleport.commands.home;

import io.github.niestrat99.advancedteleport.api.ATFloodgatePlayer;
import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.api.Home;
import io.github.niestrat99.advancedteleport.commands.PlayerCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class SetMainHomeCommand extends AbstractHomeCommand implements PlayerCommand {

    @Override
    public boolean onCommand(
        @NotNull final CommandSender sender,
        @NotNull final Command command,
        @NotNull final String s,
        @NotNull final String[] args
    ) {
        if (!canProceed(sender)) return true;

        final var player = (Player) sender;
        final var atPlayer = ATPlayer.getPlayer(player);

        if (args.length == 0) {
            if (atPlayer instanceof ATFloodgatePlayer && NewConfig.get().USE_FLOODGATE_FORMS.get()) {
                ((ATFloodgatePlayer) atPlayer).sendSetMainHomeForm();
            } else {
                CustomMessages.sendMessage(sender, "Error.noHomeInput");
            }
            return true;
        }
        // TODO deprecated code
        if (args.length > 1 && sender.hasPermission(getPermission())) {
            final var target = Bukkit.getOfflinePlayer(args[0]);

            if (target != player) {
                ATPlayer atTarget = ATPlayer.getPlayer(target);
                String homeName = args[1];

                if (atTarget.hasHome(homeName)) {
                    atTarget.setMainHome(homeName, sender).whenCompleteAsync((ignored, err) -> CustomMessages.failableContextualPath(
                        sender,
                        target,
                        "Info.setMainHome",
                        "Error.setMainHomeFail",
                        () -> err != null,
                        "home", homeName,
                        "player", target.getName() // TODO: DisplyName
                    ));

                    return true;
                }

                if (atPlayer.canSetMoreHomes()) {
                    addAndMaybeSetHome(sender, atTarget, player, homeName);
                    return true;
                }

                atTarget.setMainHome(homeName, sender).whenCompleteAsync((ignore, err) -> CustomMessages.failableContextualPath(
                    sender,
                    target,
                    "Info.setMainHome",
                    "Error.setMainHomeFail",
                    () -> err != null,
                    "home", homeName,
                    "player", args[0] // TODO: Displyname
                ));

                return true;
            }
        }

        final var homeName = args[0];
        if (!atPlayer.hasHome(homeName)) {
            if (atPlayer.canSetMoreHomes()) { // TODO - message to mention no more homes can be set
                addAndMaybeSetHome(sender, atPlayer, player, homeName);
            }
        } else {
            Home home = atPlayer.getHome(homeName);
            if (atPlayer.canAccessHome(home)) {
                atPlayer.setMainHome(homeName, sender).whenCompleteAsync((ignored, err) -> CustomMessages.failable(
                    sender,
                    "Info.setMainHome",
                    "Error.setMainHomeFail",
                    () -> err != null,
                    "home", homeName
                ));
            } else CustomMessages.sendMessage(sender, "Error.noAccessHome", "home", home.getName());
        }

        return true;
    }

    @Override
    public @NotNull String getPermission() {
        return "at.member.setmainhome";
    }

    private void addAndMaybeSetHome(
        @NotNull final CommandSender sender,
        @NotNull final ATPlayer atTarget,
        @NotNull final Player player,
        @NotNull final String homeName
    ) {
        atTarget.addHome(homeName, player.getLocation(), player).whenCompleteAsync((ignored, err) -> {
            if (err != null) {
                CustomMessages.sendMessage(sender, "Error.setHomeFail", "home", homeName);
                return;
            }

            atTarget.setMainHome(homeName, sender).whenCompleteAsync((ignored2, err2) -> CustomMessages.failableContextualPath(
                sender,
                atTarget,
                "Info.setAndMadeMainHome",
                "Error.setMainHomeFail",
                () -> err2 != null,
                "home", homeName,
                "player", atTarget.getPlayer().displayName()
            ));
        });
    }
}
