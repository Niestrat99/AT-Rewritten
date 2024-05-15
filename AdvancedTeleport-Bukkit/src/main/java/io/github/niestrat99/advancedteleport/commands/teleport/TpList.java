package io.github.niestrat99.advancedteleport.commands.teleport;

import io.github.niestrat99.advancedteleport.api.TeleportRequest;
import io.github.niestrat99.advancedteleport.commands.PlayerCommand;
import io.github.niestrat99.advancedteleport.commands.TeleportATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.MainConfig;
import io.github.niestrat99.advancedteleport.utilities.PagedLists;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class TpList extends TeleportATCommand implements PlayerCommand {

    @Override
    public boolean onCommand(
            @NotNull final CommandSender sender,
            @NotNull final Command command,
            @NotNull final String s,
            @NotNull final String[] args) {
        if (!canProceed(sender)) return true;

        Player player = (Player) sender;

        // If there are actually any pending teleport requests.
        if (TeleportRequest.getRequests(player).isEmpty()) {
            CustomMessages.sendMessage(player, "Error.noRequests");
            return true;
        }

        if (args.length == 0) {
            PagedLists<TeleportRequest> requests =
                    new PagedLists<>(TeleportRequest.getRequests(player), 8);
            sendWithHeader(1, requests, player);

            return true;
        }
        // Check if the argument can be parsed as an actual number.
        // ^ means at the start of the string.
        // [0-9] means any number in the range of 0 to 9.
        // + means one or more of, allowing two or three digits.
        // $ means the end of the string.
        if (args[0].matches("^[0-9]+$")) {
            // args[0] is officially an int.
            int page = Integer.parseInt(args[0]);
            PagedLists<TeleportRequest> requests =
                    new PagedLists<>(TeleportRequest.getRequests(player), 8);
            try {
                sendWithHeader(page, requests, player);
            } catch (IllegalArgumentException ex) {
                CustomMessages.sendMessage(player, "Error.invalidPageNo");
            }

        } else {
            CustomMessages.sendMessage(player, "Error.invalidPageNo");
        }
        return true;
    }

    private static void sendWithHeader(
            final int page,
            @NotNull final PagedLists<TeleportRequest> requests,
            @NotNull final Player player) {
        final var body =
                CustomMessages.getPagesComponent(
                        page,
                        requests,
                        request ->
                                CustomMessages.get(
                                        "Info.multipleRequestsIndex",
                                        Placeholder.unparsed("command", "/tpayes"),
                                        Placeholder.unparsed(
                                                "player",
                                                request.requester()
                                                        .getName()) // TODO: Try use player
                                                                    // DisplayName
                                        ));

        CustomMessages.sendMessage(player, "Info.multipleRequestAccept");
        CustomMessages.sendMessage(player, body);
    }

    @Override
    public @NotNull String getPermission() {
        return "at.member.list";
    }

    @Override
    public @Nullable List<String> onTabComplete(
            @NotNull final CommandSender sender,
            @NotNull final Command command,
            @NotNull final String s,
            @NotNull final String[] args) {
        return null;
    }
}
