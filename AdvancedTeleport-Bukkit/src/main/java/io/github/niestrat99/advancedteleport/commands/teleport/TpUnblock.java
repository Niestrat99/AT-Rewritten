package io.github.niestrat99.advancedteleport.commands.teleport;

import io.github.niestrat99.advancedteleport.CoreClass;
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

public final class TpUnblock extends TeleportATCommand implements PlayerCommand {

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
                ((ATFloodgatePlayer) atPlayer).sendUnblockForm();
            } else {
                CustomMessages.sendMessage(sender, "Error.noPlayerInput");
                return false;
            }
            return true;
        }
        if (args[0].equalsIgnoreCase(player.getName())) {
            CustomMessages.sendMessage(sender, "Error.blockSelf");
            return true;
        }
        AdvancedTeleportAPI.getOfflinePlayer(args[0]).whenComplete((target, err1) -> {

            if (!atPlayer.hasBlocked(target)) {
                sender.sendMessage("Player never blocked");
                return;
            }

            atPlayer.unblockUser(target.getUniqueId()).whenCompleteAsync((ignored, err) -> CustomMessages.failable(
                sender,
                "Info.unblockPlayer",
                "Error.unblockFail",
                err,
                Placeholder.unparsed("player", target.getName())
            ), CoreClass.async);
        });
        return true;
    }

    @Override
    public @NotNull String getPermission() {
        return "at.member.unblock";
    }
}
