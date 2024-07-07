package io.github.niestrat99.advancedteleport.commands.teleport;

import io.github.niestrat99.advancedteleport.api.ATFloodgatePlayer;
import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.api.TeleportRequest;
import io.github.niestrat99.advancedteleport.api.events.players.TeleportCancelEvent;
import io.github.niestrat99.advancedteleport.commands.PlayerCommand;
import io.github.niestrat99.advancedteleport.commands.TeleportATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.MainConfig;
import io.github.niestrat99.advancedteleport.utilities.PagedLists;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class TpCancel extends TeleportATCommand implements PlayerCommand {

    @Override
    public boolean onCommand(
            @NotNull final CommandSender sender,
            @NotNull final Command command,
            @NotNull final String s,
            @NotNull final String[] args) {
        if (!canProceed(sender)) return true;

        Player player = (Player) sender;
        // Checks if any players have sent a request at all.
        if (!TeleportRequest.getRequestsByRequester(player).isEmpty()) {
            // Checks if there's more than one request.
            if (TeleportRequest.getRequestsByRequester(player).size() > 1) {
                // If the player has specified the request they're accepting.
                if (args.length > 0) {
                    Player target = Bukkit.getPlayer(args[0]);
                    // Player is offline
                    if (target == null || !player.canSee(target)) {
                        CustomMessages.sendMessage(sender, "Errors.noSuchPlayer");
                        return true;
                    }
                    TeleportRequest request =
                            TeleportRequest.getRequestByReqAndResponder(target, player);
                    if (request == null) {
                        CustomMessages.sendMessage(
                                sender,
                                "Error.noRequestsFromPlayer",
                                Placeholder.unparsed("player", args[0]));
                    } else {
                        TeleportCancelEvent event =
                                new TeleportCancelEvent(
                                        request.responder(), request.requester(), request.type());
                        Bukkit.getPluginManager().callEvent(event);
                        if (event.isCancelled()) {
                            // Could not be cancelled
                            return true;
                        }

                        CustomMessages.sendMessage(sender, "Info.tpCancel");
                        CustomMessages.sendMessage(
                                request.responder(),
                                "Info.tpCancelResponder",
                                Placeholder.unparsed(
                                        "player",
                                        player
                                                .getDisplayName())); // TODO: Try use player
                                                                     // DisplayName
                        request.destroy();
                    }
                    return true;
                }

                ATPlayer atPlayer = ATPlayer.getPlayer(player);
                if (atPlayer instanceof ATFloodgatePlayer atFloodgatePlayer
                        && MainConfig.get().USE_FLOODGATE_FORMS.get()) {
                    final var responders =
                            TeleportRequest.getRequestsByRequester(atFloodgatePlayer.getPlayer()).stream()
                                    .map(request -> request.requester().getName())
                                    .toList();
                    if (!responders.isEmpty()) {
                        ((ATFloodgatePlayer) atPlayer).sendCancelForm(responders);
                    } else {
                        CustomMessages.sendMessage(sender, "Error.noRequests");
                    }
                    return true;
                }

                // This utility helps in splitting lists into separate pages, like when you list
                // your plots with PlotMe/PlotSquared.
                PagedLists<TeleportRequest> requests =
                        new PagedLists<>(TeleportRequest.getRequestsByRequester(player), 8);
                CustomMessages.sendMessage(player, "Info.multipleRequestsCancel");
                // Displays the first 8 requests
                final var component =
                        Component.join(
                                JoinConfiguration.newlines(),
                                requests.getContentsInPage(1).stream()
                                        .map(
                                                request ->
                                                        CustomMessages.get(
                                                                "Info.multipleRequestsIndex",
                                                                Placeholder.unparsed(
                                                                        "command", "/tpcancel"),
                                                                Placeholder.unparsed(
                                                                        "player",
                                                                        request.requester()
                                                                                .getName()) // TODO:
                                                                                            // Try
                                                                                            // use
                                                                                            // player DisplayName
                                                                ))
                                        .toList() // TODO: Ensure order is correct
                                );

                if (requests.getTotalPages() > 1) {
                    CustomMessages.sendMessage(player, component);
                    CustomMessages.sendMessage(player, "Info.multipleRequestsList");
                }
            } else {
                TeleportRequest request = TeleportRequest.getRequestsByRequester(player).get(0);

                TeleportCancelEvent event =
                        new TeleportCancelEvent(
                                request.requester(), request.requester(), request.type());
                Bukkit.getPluginManager().callEvent(event);
                if (event.isCancelled()) {
                    // Could not be cancelled
                    return true;
                }

                CustomMessages.sendMessage(
                        request.responder(),
                        "Info.tpCancelResponder",
                        Placeholder.unparsed(
                                "player", player.getName()) // TODO: Try use player DisplayName
                        );
                CustomMessages.sendMessage(player, "Info.tpCancel");

                request.destroy();
                return true;
            }
        } else {
            CustomMessages.sendMessage(sender, "Error.noRequests");
        }
        return true;
    }

    @Override
    public @NotNull String getPermission() {
        return "at.member.cancel";
    }
}
