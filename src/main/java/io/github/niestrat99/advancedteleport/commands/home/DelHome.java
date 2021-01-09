package io.github.niestrat99.advancedteleport.commands.home;

import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.Homes;
import io.github.niestrat99.advancedteleport.config.Config;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;

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
        try {
            if (Homes.getHomes(player.getUniqueId().toString()).containsKey(name)) {
                try {
                    Homes.delHome(player, name);
                    if (sender == player) {
                        sender.sendMessage(CustomMessages.getString("Info.deletedHome").replaceAll("\\{home}", name));
                    } else {
                        sender.sendMessage(CustomMessages.getString("Info.deletedHomeOther").replaceAll("\\{home}", name).replaceAll("\\{player}", player.getName()));
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                sender.sendMessage(CustomMessages.getString("Error.noSuchHome"));
            }
        } catch (NullPointerException ex) {
            try {
                Homes.delHome(player, name);
                if (sender == player) {
                    sender.sendMessage(CustomMessages.getString("Info.deletedHome").replaceAll("\\{home}", name));
                } else {
                    sender.sendMessage(CustomMessages.getString("Info.deletedHomeOther").replaceAll("\\{home}", name).replaceAll("\\{player}", player.getName()));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void delHome(Player player, String name) {
        delHome(player, player, name);
    }
}
