package io.github.niestrat99.advancedteleport.commands.teleport;

import io.github.niestrat99.advancedteleport.CoreClass;
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

public class TpUnblock implements AsyncATCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (NewConfig.get().USE_BASIC_TELEPORT_FEATURES.get()) {
            if (sender.hasPermission("at.member.unblock")) {
                if (sender instanceof Player){
                    Player player = (Player)sender;
                    if (args.length>0){
                        if (args[0].equalsIgnoreCase(player.getName())){
                            CustomMessages.sendMessage(sender, "Error.blockSelf");
                            return true;
                        }
                        ATPlayer atPlayer = ATPlayer.getPlayer(player);
                        Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), () -> {
                            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);

                            if (!atPlayer.hasBlocked(target)) {
                                sender.sendMessage("Player never blocked");
                                return;
                            }

                            atPlayer.unblockUser(target.getUniqueId(), new SQLManager.SQLCallback<Boolean>() {
                                @Override
                                public void onSuccess(Boolean data) {
                                    CustomMessages.sendMessage(sender, "Info.unblockPlayer", "{player}", args[0]);

                                }

                                @Override
                                public void onFail() {
                                    sender.sendMessage("Failed to unblock");
                                }
                            });

                        });
                    } else {
                        CustomMessages.sendMessage(sender, "Error.noPlayerInput");
                    }
                } else {
                    CustomMessages.sendMessage(sender, "Error.notAPlayer");
                }
            }
        } else {
            CustomMessages.sendMessage(sender, "Error.featureDisabled");
        }
        return true;
    }
}
