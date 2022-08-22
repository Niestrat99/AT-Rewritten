package io.github.niestrat99.advancedteleport.commands.teleport;

import io.github.niestrat99.advancedteleport.api.ATFloodgatePlayer;
import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.commands.ATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.fanciful.FancyMessage;
import io.github.niestrat99.advancedteleport.utilities.PagedLists;
import io.github.niestrat99.advancedteleport.utilities.TPRequest;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TpCancel implements ATCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            CustomMessages.sendMessage(sender, "Error.notAPlayer");
            return true;
        }
        if (!NewConfig.get().USE_BASIC_TELEPORT_FEATURES.get()) {
            CustomMessages.sendMessage(sender, "Error.featureDisabled");
            return true;
        }
        if (!sender.hasPermission("at.member.cancel")) {
            CustomMessages.sendMessage(sender, "Error.noPermission");
            return true;
        }

        Player player = (Player) sender;
        // Checks if any players have sent a request at all.
        if (TPRequest.getRequestsByRequester(player).isEmpty()) {
            CustomMessages.sendMessage(sender, "Error.noRequests");
            return true;
        }

        // Checks if there's more than one request.
        if (TPRequest.getRequestsByRequester(player).size() > 1) {
            // If the player has specified the request they're accepting.
            if (args.length > 0) {
                Player target = Bukkit.getPlayer(args[0]);
                // Player is offline
                if (target == null || !player.canSee(target)) {
                    CustomMessages.sendMessage(sender, "Errors.noSuchPlayer");
                } else {
                    TPRequest request = TPRequest.getRequestByReqAndResponder(target, player);
                    if (request == null) {
                        CustomMessages.sendMessage(sender, "Error.noRequestsFromPlayer", "{player}", args[0]);
                    } else {
                        CustomMessages.sendMessage(sender, "Info.tpCancel");
                        CustomMessages.sendMessage(request.getResponder(), "Info.tpCancelResponder", "{player}", player.getName());
                        request.destroy();
                    }
                }
                return true;
            } else {
                ATPlayer atPlayer = ATPlayer.getPlayer(player);
                if (atPlayer instanceof ATFloodgatePlayer) {
                    ((ATFloodgatePlayer) atPlayer).sendCancelForm();
                    return true;
                }
                // This utility helps in splitting lists into separate pages, like when you list your plots with PlotMe/PlotSquared.
                PagedLists<TPRequest> requests = new PagedLists<>(TPRequest.getRequestsByRequester(player), 8);
                CustomMessages.sendMessage(player, "Info.multipleRequestsCancel");
                // Displays the first 8 requests
                for (int i = 0; i < requests.getContentsInPage(1).size(); i++) {
                    TPRequest request = requests.getContentsInPage(1).get(i);
                    new FancyMessage()
                            .command("/tpcancel " + request.getResponder().getName())
                            .text(CustomMessages.getStringA("Info.multipleRequestsIndex")
                                    .replaceAll("\\{player}", request.getResponder().getName()))
                            .sendProposal(player, i);
                }
                if (requests.getTotalPages() > 1) {
                    FancyMessage.send(player);
                    CustomMessages.sendMessage(player, "Info.multipleRequestsList");
                }
            }
        } else {
            TPRequest request = TPRequest.getRequestsByRequester(player).get(0);
            CustomMessages.sendMessage(request.getResponder(), "Info.tpCancelResponder", "{player}", player.getName());
            CustomMessages.sendMessage(player, "Info.tpCancel");
            request.destroy();
            return true;
        }

        return true;
    }
}
