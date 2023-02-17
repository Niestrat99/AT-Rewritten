package io.github.niestrat99.advancedteleport.commands.teleport;

import io.github.niestrat99.advancedteleport.api.TeleportRequest;
import io.github.niestrat99.advancedteleport.commands.PlayerCommand;
import io.github.niestrat99.advancedteleport.commands.TeleportATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.MainConfig;
import io.github.niestrat99.advancedteleport.fanciful.FancyMessage;
import io.github.niestrat99.advancedteleport.utilities.PagedLists;
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
        @NotNull final String[] args
    ) {
        if (!canProceed(sender)) return true;

        Player player = (Player) sender;

        // If there are actually any pending teleport requests.
        if (TeleportRequest.getRequests(player).isEmpty()) {
            CustomMessages.sendMessage(player, "Error.noRequests");
            return true;
        }

        if (args.length == 0) {
            PagedLists<TeleportRequest> requests = new PagedLists<>(TeleportRequest.getRequests(player), 8);
            CustomMessages.sendMessage(player, "Info.multipleRequestAccept");
            for (int i = 0; i < requests.getContentsInPage(1).size(); i++) {
                TeleportRequest request = requests.getContentsInPage(1).get(i);
                new FancyMessage()
                        .command("/tpayes " + request.requester().getName())
                        .text(CustomMessages.getStringRaw("Info.multipleRequestsIndex")
                                .replaceAll("\\{player}", request.requester().getName()))
                        .sendProposal(player, i);
            }
            FancyMessage.send(player);
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
            PagedLists<TeleportRequest> requests = new PagedLists<>(TeleportRequest.getRequests(player), 8);
            CustomMessages.sendMessage(player, "Info.multipleRequestAccept");
            try {
                for (int i = 0; i < requests.getContentsInPage(page).size(); i++) {
                    TeleportRequest request = requests.getContentsInPage(page).get(i);
                    new FancyMessage()
                            .command("/tpayes " + request.requester().getName())
                            .text(CustomMessages.getStringRaw("Info.multipleRequestsIndex")
                                    .replaceAll("\\{player}", request.requester().getName()))
                            .sendProposal(player, i);
                }
            } catch (IllegalArgumentException ex) {
                CustomMessages.sendMessage(player, "Error.invalidPageNo");
            }

        } else {
            CustomMessages.sendMessage(player, "Error.invalidPageNo");
        }
        return true;
    }

    @Override
    public boolean getRequiredFeature() {
        return MainConfig.get().USE_BASIC_TELEPORT_FEATURES.get();
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
        @NotNull final String[] args
    ) {
        return null;
    }
}
