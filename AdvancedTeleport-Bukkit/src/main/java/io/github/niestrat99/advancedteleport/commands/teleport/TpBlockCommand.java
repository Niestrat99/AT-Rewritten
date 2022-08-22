package io.github.niestrat99.advancedteleport.commands.teleport;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.ATFloodgatePlayer;
import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.commands.AsyncATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.sql.SQLManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TpBlockCommand implements AsyncATCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, String s, String[] args) {

        if (!(sender instanceof Player)) {
            CustomMessages.sendMessage(sender, "Error.notAPlayer");
            return true;
        }
        if (!NewConfig.get().USE_BASIC_TELEPORT_FEATURES.get()) {
            CustomMessages.sendMessage(sender, "Error.featureDisabled");
            return true;
        }
        if (!sender.hasPermission("at.member.block")) {
            CustomMessages.sendMessage(sender, "Error.noPermission");
            return true;
        }

        Player player = (Player) sender;
        ATPlayer atPlayer = ATPlayer.getPlayer(player);

        if (args.length == 0) {
            if (atPlayer instanceof ATFloodgatePlayer && NewConfig.get().USE_FLOODGATE_FORMS.get()) {
                ((ATFloodgatePlayer) atPlayer).sendBlockForm();
            } else {
                CustomMessages.sendMessage(sender, "Error.noPlayerInput");
            }
            return true;
        }
        // Don't block ourselves lmao
        if (args[0].equalsIgnoreCase(player.getName())) {
            CustomMessages.sendMessage(sender, "Error.blockSelf");
            return true;
        }
        // Must be async due to searching for offline player
        Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), () -> {
            //
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);

            if (atPlayer.hasBlocked(target)) {
                CustomMessages.sendMessage(sender, "Error.alreadyBlocked");
                return;
            }

            SQLManager.SQLCallback<Boolean> callback = new SQLManager.SQLCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean data) {
                    CustomMessages.sendMessage(sender, "Info.blockPlayer", "{player}", args[0]);
                }

                @Override
                public void onFail() {
                    sender.sendMessage("Failed to save block");
                }
            };

            if (args.length > 1) {
                StringBuilder reason = new StringBuilder();
                for (int i = 1; i < args.length; i++) {
                    reason.append(args[i]).append(" ");
                }
                atPlayer.blockUser(target, reason.toString().trim(), callback);
            } else {
                atPlayer.blockUser(target, callback);
            }
        });
        return true;
    }
}
