package io.github.niestrat99.advancedteleport.commands.home;

import io.github.niestrat99.advancedteleport.api.ATFloodgatePlayer;
import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.api.AdvancedTeleportAPI;
import io.github.niestrat99.advancedteleport.commands.PlayerCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.MainConfig;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class SetHomeCommand extends AbstractHomeCommand implements PlayerCommand {

    @Override
    public boolean onCommand(
            @NotNull final CommandSender sender,
            @NotNull final Command command,
            @NotNull final String s,
            @NotNull final String[] args) {
        if (!canProceed(sender)) return true;

        // Specify player variables
        final var player = (Player) sender;
        final var atPlayer = ATPlayer.getPlayer(player);

        // If no arguments have been specified, just use the information from that
        if (args.length == 0) {

            // Get the player's homes limit
            final int limit = atPlayer.getHomesLimit();

            // If the homes list is empty, set a new home called "home".
            if (atPlayer.getHomes().size() == 0 && (limit > 0 || limit == -1)) {
                setHome(player, "home");
                return true;
            }

            // If the player is a floodgate player, send them a form, otherwise tell the player to
            // enter some arguments
            if (atPlayer instanceof ATFloodgatePlayer
                    && MainConfig.get().USE_FLOODGATE_FORMS.get()) {
                ((ATFloodgatePlayer) atPlayer).sendSetHomeForm();
            } else {
                CustomMessages.sendMessage(sender, "Error.noHomeInput");
            }

            return true;
        }

        // We'll just assume that the admin command overrides the homes limit.
        if (args.length > 1 && sender.hasPermission(getAdminPermission())) {

            // Get the player to be targeted.
            AdvancedTeleportAPI.getOfflinePlayer(args[0])
                    .whenCompleteAsync((target, err) -> setHome(player, target, args[1], args[0]));
            return true;
        }

        // If the player can set more homes,
        if (atPlayer.canSetMoreHomes()
                || (MainConfig.get().OVERWRITE_SETHOME.get() && atPlayer.hasHome(args[0]))) {
            setHome(player, args[0]);

        } else {
            CustomMessages.sendMessage(sender, "Error.reachedHomeLimit");
        }
        return true;
    }

    private void setHome(Player sender, String name) {
        setHome(sender, sender, name, sender.getName());
    }

    // Separated this into a separate method so that the code is easier to read.
    // Player player - the player which is having the home set.
    // String name - the name of the home.
    private void setHome(Player sender, OfflinePlayer target, String homeName, String playerName) {

        ATPlayer atPlayer = ATPlayer.getPlayer(target);

        // If the home with that name already exists, and we can't overwrite homes, let the player
        // know
        if (!MainConfig.get().OVERWRITE_SETHOME.get() && atPlayer.hasHome(homeName)) {
            CustomMessages.sendMessage(
                    sender, "Error.homeAlreadySet", Placeholder.unparsed("home", homeName));
            return;
        }

        // Attempt to add the home.
        atPlayer.addHome(homeName, sender.getLocation(), sender)
                .whenComplete(
                        (ignored, err) ->
                                CustomMessages.failableContextualPath(
                                        sender,
                                        target,
                                        "Info.setHome",
                                        "Error.setHomeFail",
                                        err,
                                        Placeholder.unparsed("home", homeName),
                                        Placeholder.unparsed("player", playerName)));
    }

    @Override
    public boolean getRequiredFeature() {
        return MainConfig.get().USE_HOMES.get();
    }

    @Override
    public @NotNull String getPermission() {
        return "at.member.sethome";
    }

    @Override
    public @NotNull String getAdminPermission() {
        return "at.admin.sethome";
    }

    @Override
    public @NotNull List<String> onTabComplete(
            @NotNull final CommandSender sender,
            @NotNull final Command command,
            @NotNull final String s,
            @NotNull final String[] args) {
        return new ArrayList<>();
    }
}
