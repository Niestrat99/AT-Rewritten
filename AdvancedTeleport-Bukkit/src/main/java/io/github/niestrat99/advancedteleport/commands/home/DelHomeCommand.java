package io.github.niestrat99.advancedteleport.commands.home;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.ATFloodgatePlayer;
import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.api.AdvancedTeleportAPI;
import io.github.niestrat99.advancedteleport.commands.PlayerCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.MainConfig;
import io.github.niestrat99.advancedteleport.managers.PluginHookManager;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

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
            @NotNull final String[] args) {

        // If they can't proceed, stop there
        if (!canProceed(sender)) return true;

        final var player = (Player) sender;

        // If they've specified a home, see if they are an admin and can delete others' homes
        if (args.length > 0) {
            if (sender.hasPermission(getPermission()) && args.length > 1) {
                AdvancedTeleportAPI.getOfflinePlayer(args[0])
                        .whenCompleteAsync(
                                (target, err) -> delHome(target, player, args[1]), CoreClass.sync);
                return true;
            }

            delHome(player, args[0]);
            return true;
        }

        final var atPlayer = ATPlayer.getPlayer(player);

        // If the player is a floodgate player, send them the form to delete their home, otherwise,
        // tell them player they need a home to delete
        if (PluginHookManager.get().floodgateEnabled()
                && atPlayer instanceof ATFloodgatePlayer atFloodgatePlayer
                && MainConfig.get().USE_FLOODGATE_FORMS.get()) {
            atFloodgatePlayer.sendDeleteHomeForm();
        } else {
            CustomMessages.sendMessage(sender, "Error.noHomeInput");
            return false;
        }

        return true;
    }

    @Override
    public @NotNull String getPermission() {
        return "at.member.delhome";
    }

    private void delHome(OfflinePlayer player, Player sender, String name) {
        ATPlayer atPlayer = ATPlayer.getPlayer(player);

        // If the player doesn't have such a home, let them know
        if (!atPlayer.hasHome(name)) {
            CustomMessages.sendMessage(sender, "Error.noSuchHome");
            return;
        }

        // Remove the home asynchronously
        atPlayer.removeHome(name, sender)
                .whenComplete(
                        (ignored, err) ->
                                CustomMessages.failableContextualPath(
                                        sender,
                                        player,
                                        "Info.deletedHome",
                                        "Error.deleteHomeFail",
                                        err,
                                        Placeholder.unparsed("home", name),
                                        Placeholder.unparsed(
                                                "player", player.getName()) // TODO: Displayname
                                        ));
    }

    private void delHome(@NotNull final Player player, @NotNull final String name) {
        delHome(player, player, name);
    }
}
