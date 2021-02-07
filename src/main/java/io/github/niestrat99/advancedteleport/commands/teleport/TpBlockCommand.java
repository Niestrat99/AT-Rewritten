package io.github.niestrat99.advancedteleport.commands.teleport;

import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.commands.AsyncATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.sql.SQLManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TpBlockCommand implements AsyncATCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        // If teleporting features are enabled...
        if (NewConfig.getInstance().USE_BASIC_TELEPORT_FEATURES.get()) {
            // If the user has permission...
            if (sender.hasPermission("at.member.block")) {
                // If the sender is a player...
                if (sender instanceof Player){
                    // Get the sender as a player.
                    Player player = (Player)sender;
                    // Make sure we've included a player name.
                    if (args.length>0){
                        // Don't block ourselves lmao
                        if (args[0].equalsIgnoreCase(player.getName())){
                            sender.sendMessage(CustomMessages.getString("Error.blockSelf"));
                            return true;
                        }
                        ATPlayer atPlayer = ATPlayer.getPlayer(player);
                        // Must be async due to searching for offline player
                        Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), () -> {
                            //
                            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);

                            if (atPlayer.hasBlocked(target)) {
                                sender.sendMessage(CustomMessages.getString("Error.alreadyBlocked"));
                                return;
                            }

                            SQLManager.SQLCallback<Boolean> callback = new SQLManager.SQLCallback<Boolean>() {
                                @Override
                                public void onSuccess(Boolean data) {
                                    sender.sendMessage(CustomMessages.getString("Info.blockPlayer").replaceAll("\\{player}", target.getName()));

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
                    } else {
                        sender.sendMessage(CustomMessages.getString("Error.noPlayerInput"));
                    }
                    return true;
                } else {
                    sender.sendMessage(CustomMessages.getString("Error.notAPlayer"));
                }
            }
        } else {
            sender.sendMessage(CustomMessages.getString("Error.featureDisabled"));
            return true;
        }
        return true;
    }
}
