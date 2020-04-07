package io.github.at.commands.home;

import io.github.at.config.Config;
import io.github.at.config.CustomMessages;
import io.github.at.config.Homes;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;

public class DelHome implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (Config.isFeatureEnabled("homes")) {
            if (sender instanceof Player) {
                Player player = (Player)sender;
                if (sender.hasPermission("at.member.delhome")) {
                    if (args.length>0) {
                        if (Bukkit.getPlayer(args[0]) != null) {
                            if (sender.hasPermission("at.admin.delhome")) {
                                if (args.length>1) {
                                    Player target = Bukkit.getOfflinePlayer(args[0]).getPlayer();
                                    delHome(target, player, args[1]);
                                }
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

    private void delHome(Player player, Player sender, String name) {
        try {
            if (Homes.getHomes(player).containsKey(name)) {
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
