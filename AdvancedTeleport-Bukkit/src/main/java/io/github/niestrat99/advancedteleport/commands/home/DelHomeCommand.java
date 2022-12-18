package io.github.niestrat99.advancedteleport.commands.home;

import io.github.niestrat99.advancedteleport.api.ATFloodgatePlayer;
import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.commands.PlayerCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.managers.PluginHookManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class DelHomeCommand extends AbstractHomeCommand implements PlayerCommand {

    @Override
    public boolean onCommand(
        @NotNull final CommandSender sender,
        @NotNull final Command command,
        @NotNull final String s,
        @NotNull final String[] args
    ) {
        if (!canProceed(sender)) return true;

        final var player = (Player) sender;

        if (args.length > 0) {
            if (sender.hasPermission(getPermission()) && args.length > 1) {
                final var target = Bukkit.getOfflinePlayer(args[0]);
                delHome(target, player, args[1]);
                return true;
            }

            delHome(player, args[0]);
            return true;
        }

        final var atPlayer = ATPlayer.getPlayer(player);
        if (PluginHookManager.get().floodgateEnabled() && atPlayer instanceof ATFloodgatePlayer atFloodgatePlayer && NewConfig.get().USE_FLOODGATE_FORMS.get()) {
            atFloodgatePlayer.sendDeleteHomeForm();
        } else CustomMessages.sendMessage(sender, "Error.noHomeInput");

        return true;
    }

    private void delHome(OfflinePlayer player, Player sender, String name) {
        ATPlayer atPlayer = ATPlayer.getPlayer(player);

        if (!atPlayer.hasHome(name)) {
            CustomMessages.sendMessage(sender, "Error.noSuchHome");
            return;
        }

        atPlayer.removeHome(name, sender).whenComplete((ignored, err) -> CustomMessages.failableContextualPath(
            sender,
            player,
            "Info.homeDeleted",
            "Error.deleteHomeFailed",
            () -> err == null,
            "home",name,
            "player", player.getName() // TOOD: Displayname
        ));
    }

    private void delHome(
        @NotNull final Player player,
        @NotNull final String name
    ) {
        delHome(player, player, name);
    }

    @Override
    public @NotNull String getPermission() {
        return "at.member.delhome";
    }
}
