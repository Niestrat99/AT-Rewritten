package io.github.niestrat99.advancedteleport.commands.teleport;

import io.github.niestrat99.advancedteleport.commands.ATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.utilities.PagedLists;
import io.github.niestrat99.advancedteleport.utilities.TPRequest;
import io.github.niestrat99.advancedteleport.fanciful.FancyMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TpCancel implements ATCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (NewConfig.get().USE_BASIC_TELEPORT_FEATURES.get()) {
            if (sender.hasPermission("at.member.cancel")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    // Checks if any players have sent a request at all.
                    if (!TPRequest.getRequestsByRequester(player).isEmpty()) {
                        // Checks if there's more than one request.
                        if (TPRequest.getRequestsByRequester(player).size() > 1) {
                            // If the player has specified the request they're accepting.
                            if (args.length > 0) {
                                Player target = Bukkit.getPlayer(args[0]);
                                // Player is offline
                                if (target == null || !player.canSee(target)) {
                                    CustomMessages.sendMessage(sender, "Errors.noSuchPlayer");
                                    return true;
                                } else {
                                    TPRequest request = TPRequest.getRequestByReqAndResponder(target, player);
                                    if (request == null) {
                                        CustomMessages.sendMessage(sender, "Error.noRequestsFromPlayer", "{player}", args[0]);
                                    } else {
                                        CustomMessages.sendMessage(sender, "Info.tpCancel");
                                        CustomMessages.sendMessage(request.getResponder(), "Info.tpCancelResponder", "{player}", player.getName());
                                        request.destroy();
                                    }
                                    return true;
                                }
                            } else {
                                // This utility helps in splitting lists into separate pages, like when you list your plots with PlotMe/PlotSquared.
                                PagedLists<TPRequest> requests = new PagedLists<>(TPRequest.getRequestsByRequester(player), 8);
                                CustomMessages.sendMessage(player, "Info.multipleRequestsCancel");
                                // Displays the first 8 requests
                                for (TPRequest request : requests.getContentsInPage(1)) {
                                    new FancyMessage()
                                            .command("/tpcancel " + request.getResponder().getName())
                                            .text(CustomMessages.getString("Info.multipleRequestsIndex")
                                                    .replaceAll("\\{player}", request.getResponder().getName()))
                                            .send(player);
                                }
                                if (requests.getTotalPages() > 1) {
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
                    } else {
                        CustomMessages.sendMessage(sender, "Error.noRequests");
                        return true;
                    }
                } else {
                    CustomMessages.sendMessage(sender, "Error.notAPlayer");
                }
            }
        } else {
            CustomMessages.sendMessage(sender, "Error.featureDisabled");
            return true;
        }
        return true;
    }
}
