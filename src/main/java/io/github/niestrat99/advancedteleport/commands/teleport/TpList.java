package io.github.niestrat99.advancedteleport.commands.teleport;

import io.github.niestrat99.advancedteleport.commands.AsyncATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.fanciful.FancyMessage;
import io.github.niestrat99.advancedteleport.utilities.PagedLists;
import io.github.niestrat99.advancedteleport.utilities.TPRequest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TpList implements AsyncATCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (NewConfig.get().USE_BASIC_TELEPORT_FEATURES.get()) {
            if (sender.hasPermission("at.member.list")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    // If there are actually any pending teleport requests.
                    if (!TPRequest.getRequests(player).isEmpty()) {
                        if (args.length > 0) {
                            // Check if the argument can be parsed as an actual number.
                            // ^ means at the start of the string.
                            // [0-9] means any number in the range of 0 to 9.
                            // + means one or more of, allowing two or three digits.
                            // $ means the end of the string.
                            if (args[0].matches("^[0-9]+$")) {
                                // args[0] is officially an int.
                                int page = Integer.parseInt(args[0]);
                                PagedLists<TPRequest> requests = new PagedLists<>(TPRequest.getRequests(player), 8);
                                CustomMessages.sendMessage(player, "Info.multipleRequestAccept");
                                try {
                                    for (TPRequest request : requests.getContentsInPage(page)) {
                                        new FancyMessage()
                                                .command("/tpayes " + request.getRequester().getName())
                                                .text(CustomMessages.getString("Info.multipleRequestsIndex")
                                                        .replaceAll("\\{player}", request.getRequester().getName()))
                                                .send(player);
                                    }
                                } catch (IllegalArgumentException ex) {
                                    CustomMessages.sendMessage(player, "Error.invalidPageNo");
                                }

                            } else {
                                CustomMessages.sendMessage(player, "Error.invalidPageNo");
                            }
                        } else {
                            PagedLists<TPRequest> requests = new PagedLists<>(TPRequest.getRequests(player), 8);
                            CustomMessages.sendMessage(player, "Info.multipleRequestAccept");
                            for (TPRequest request : requests.getContentsInPage(1)) {
                                new FancyMessage()
                                        .command("/tpayes " + request.getRequester().getName())
                                        .text(CustomMessages.getString("Info.multipleRequestsIndex")
                                                .replaceAll("\\{player}", request.getRequester().getName()))
                                        .send(player);
                            }
                            return true;
                        }
                    } else {
                        CustomMessages.sendMessage(player, "Error.noRequests");
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

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        return null;
    }
}
