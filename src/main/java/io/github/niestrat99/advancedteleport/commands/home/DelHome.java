package io.github.niestrat99.advancedteleport.commands.home;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DelHome implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (NewConfig.getInstance().USE_HOMES.get()) {
            if (sender instanceof Player) {
                Player player = (Player)sender;
                if (sender.hasPermission("at.member.delhome")) {
                    if (args.length>0) {
                        if (sender.hasPermission("at.admin.delhome")) {
                            if (args.length>1) {
                                OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
                                delHome(target, player, args[1]);
                                return true;
                            }
                        }
                        delHome(player, args[0]);
                    } else {
                        sender.sendMessage(CustomMessages.getString("Error.noHomeInput"));
                    }
                }
            } else {
                sender.sendMessage(CustomMessages.getString("Error.notAPlayer"));
            }
        } else {
            sender.sendMessage(CustomMessages.getString("Error.featureDisabled"));
        }
        return true;
    }

    private void delHome(OfflinePlayer player, Player sender, String name) {
        Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), () -> {
            ATPlayer atPlayer = ATPlayer.getPlayer(player);

            if (atPlayer.getHome(name) != null) {
                sender.sendMessage(CustomMessages.getString("Error.homeAlreadySet").replaceAll("\\{home}", name));
                return;
            }

            atPlayer.removeHome(name, data -> {
                if (sender.getUniqueId() == player.getUniqueId()) {
                    sender.sendMessage(CustomMessages.getString("Info.deletedHome").replaceAll("\\{home}", name));
                } else {
                    sender.sendMessage(CustomMessages.getString("Info.deletedHomeOther").replaceAll("\\{home}", name).replaceAll("\\{player}", player.getName()));
                }
            });
        });
    }
    private void delHome(Player player, String name) {
        delHome(player, player, name);
    }
}
