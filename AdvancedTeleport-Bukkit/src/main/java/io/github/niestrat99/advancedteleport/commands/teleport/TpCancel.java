package io.github.niestrat99.advancedteleport.commands.teleport;

import io.github.niestrat99.advancedteleport.api.TeleportRequest;
import io.github.niestrat99.advancedteleport.api.events.players.TeleportCancelEvent;
import io.github.niestrat99.advancedteleport.commands.TeleportATCommand;
import io.github.niestrat99.advancedteleport.api.ATFloodgatePlayer;
import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.fanciful.FancyMessage;
import io.github.niestrat99.advancedteleport.utilities.PagedLists;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TpCancel extends TeleportATCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s,
                             @NotNull String[] args) {
        if (!canProceed(sender)) return true;
        if (!(sender instanceof Player)) {
            CustomMessages.sendMessage(sender, "Error.notAPlayer");
            return true;
        }
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
                        TeleportRequest request = TeleportRequest.getRequestByReqAndResponder(target, player);
                        if (request == null) {
                            CustomMessages.sendMessage(sender, "Error.noRequestsFromPlayer", "{player}",
                                    args[0]);
                        } else {
                            TeleportCancelEvent event = new TeleportCancelEvent(request.getRequester(),
                                    request.getRequester(), request.getType());
                            Bukkit.getPluginManager().callEvent(event);
                            if (event.isCancelled()) {
                                // Could not be cancelled
                                return true;
                            }
                            CustomMessages.sendMessage(sender, "Info.tpCancel");
                            CustomMessages.sendMessage(request.getResponder(), "Info.tpCancelResponder",
                                    "{player}", player.getName());
                            request.destroy();
                        }
                        return true;

                    } else {
                        // This utility helps in splitting lists into separate pages, like when you list your
                        // plots with PlotMe/PlotSquared.
                        ATPlayer atPlayer = ATPlayer.getPlayer(player);
                        if (atPlayer instanceof ATFloodgatePlayer && NewConfig.get().USE_FLOODGATE_FORMS.get()) {
                            ((ATFloodgatePlayer) atPlayer).sendCancelForm();
                            return true;
                        }
                        // This utility helps in splitting lists into separate pages, like when you list your plots with PlotMe/PlotSquared.
                        PagedLists<TeleportRequest> requests = new PagedLists<>(TeleportRequest.getRequestsByRequester(player), 8);
                        CustomMessages.sendMessage(player, "Info.multipleRequestsCancel");
                        // Displays the first 8 requests
                        for (int i = 0; i < requests.getContentsInPage(1).size(); i++) {
                            TeleportRequest request = requests.getContentsInPage(1).get(i);
                            new FancyMessage()
                                    .command("/tpcancel " + request.getResponder().getName())
                                    .text(CustomMessages.getStringRaw("Info.multipleRequestsIndex")
                                            .replaceAll("\\{player}", request.getResponder().getName()))
                                    .sendProposal(player, i);
                        }
                        if (requests.getTotalPages() > 1) {
                            FancyMessage.send(player);
                            CustomMessages.sendMessage(player, "Info.multipleRequestsList");
                        }
                    }
                } else {
                    TeleportRequest request = TeleportRequest.getRequestsByRequester(player).get(0);

                    TeleportCancelEvent event = new TeleportCancelEvent(request.getRequester(),
                            request.getRequester(), request.getType());
                    Bukkit.getPluginManager().callEvent(event);
                    if (event.isCancelled()) {
                        // Could not be cancelled
                        return true;
                    }

                    CustomMessages.sendMessage(request.getResponder(), "Info.tpCancelResponder", "{player}",
                            player.getName());
                    CustomMessages.sendMessage(player, "Info.tpCancel");
                    request.destroy();
                    return true;
                }
            } else {
                CustomMessages.sendMessage(sender, "Error.noRequests");
                return true;
            }
        return true;
    }

    @Override
    public @NotNull String getPermission() {
        return "at.member.cancel";
    }
}
