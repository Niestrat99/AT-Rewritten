package io.github.niestrat99.advancedteleport.commands.teleport;

import io.github.niestrat99.advancedteleport.api.ATFloodgatePlayer;
import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.api.AdvancedTeleportAPI;
import io.github.niestrat99.advancedteleport.commands.PlayerCommand;
import io.github.niestrat99.advancedteleport.commands.TeleportATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.MainConfig;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class TpBlockCommand extends TeleportATCommand implements PlayerCommand {

    @Override
    public boolean onCommand(
        @NotNull final CommandSender sender,
        @NotNull final Command command,
        @NotNull final String s,
        @NotNull final String[] args
    ) {
        if (!canProceed(sender)) return true;

        Player player = (Player) sender;
        ATPlayer atPlayer = ATPlayer.getPlayer(player);

        if (args.length == 0) {
            if (atPlayer instanceof ATFloodgatePlayer && MainConfig.get().USE_FLOODGATE_FORMS.get()) {
                ((ATFloodgatePlayer) atPlayer).sendBlockForm();
            } else {
                CustomMessages.sendMessage(sender, "Error.noPlayerInput");
                return false;
            }
            return true;
        }
        // Don't block ourselves lmao
        if (args[0].equalsIgnoreCase(player.getName())) {
            CustomMessages.sendMessage(sender, "Error.blockSelf");
            return true;
        }
        // Must be async due to searching for offline player
        AdvancedTeleportAPI.getOfflinePlayer(args[0]).whenComplete((target, err1) -> {

            if (atPlayer.hasBlocked(target)) {
                CustomMessages.sendMessage(sender, "Error.alreadyBlocked");
                return;
            }

            if (args.length > 1) {
                StringBuilder reason = new StringBuilder();
                for (int i = 1; i < args.length; i++) {
                    reason.append(args[i]).append(" ");
                }

                atPlayer.blockUser(target, reason.toString().trim()).whenCompleteAsync((ignored, err) -> CustomMessages.failable(
                    sender,
                    "Info.block",
                    "Error.blockFail",
                    err,
                    Placeholder.unparsed("player", target.getName())
                ));
            } else {
                atPlayer.blockUser(target).whenCompleteAsync((ignored, err) -> CustomMessages.failable(
                    sender,
                    "Info.blockPlayer",
                    "Error.blockFail",
                    err,
                    Placeholder.unparsed("player", target.getName())
                ));
            }
        });

        return true;
    }

    @Override
    public @NotNull String getPermission() {
        return "at.member.block";
    }
}
