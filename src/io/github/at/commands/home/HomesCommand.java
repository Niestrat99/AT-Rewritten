package io.github.at.commands.home;

import fanciful.FancyMessage;
import io.github.at.config.Config;
import io.github.at.config.CustomMessages;
import io.github.at.config.Homes;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;

public class HomesCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (Config.isFeatureEnabled("homes")) {
            if (sender.hasPermission("at.member.homes")) {
                if (args.length>0) {
                    if (sender.hasPermission("at.admin.homes")) {
                        Player player = Bukkit.getPlayer(args[0]);
                        if (player != null) {
                            FancyMessage hlist = new FancyMessage();
                            hlist.text(CustomMessages.getString("Info.homesOther").replaceAll("\\{player}", player.getName()));
                            if (Bukkit.getPlayer(args[0]) != null) {
                                try {
                                    if (Homes.getHomes(player).size()>0) {
                                        for (String home : Homes.getHomes(player).keySet()) {
                                            hlist.then(home)
                                                    .command("/home " + home)
                                                    .tooltip(CustomMessages.getString("Tooltip.homes").replaceAll("\\{home}", home))
                                                    .then(", ");
                                        }
                                        hlist.text(""); //Removes trailing comma
                                    } else {
                                        sender.sendMessage(CustomMessages.getString("Error.noHomesOther").replaceAll("\\{player}", player.getName()));
                                        return true;
                                    }

                                } catch (NullPointerException ex) {
                                    sender.sendMessage(CustomMessages.getString("Error.noHomesOther").replaceAll("\\{player}", player.getName()));
                                    return true;
                                }
                                hlist.send(sender);
                                return true;
                            }
                        } // Otherwise we'll just get the main player's homes
                    }
                }
                if (sender instanceof Player) {
                    Player player = (Player)sender;
                    FancyMessage hList = new FancyMessage();
                    hList.text(CustomMessages.getString("Info.homes"));
                    try {
                        if (Homes.getHomes(player).size()>0){
                            for(String home : Homes.getHomes(player).keySet()){
                                hList.then(home)
                                        .command("/home " + home)
                                        .tooltip(CustomMessages.getString("Tooltip.homes").replaceAll("\\{home}", home))
                                        .then(", ");
                            }
                            hList.text(""); //Removes trailing comma
                        } else {
                            sender.sendMessage(CustomMessages.getString("Error.noHomes"));
                            return true;
                        }

                    } catch (NullPointerException ex) { // If a player has never set any homes
                        sender.sendMessage(CustomMessages.getString("Error.noHomes"));
                        return true;
                    }
                    hList.send(player);
                }
            }
        } else {
            sender.sendMessage(CustomMessages.getString("Error.featureDisabled"));
        }
        return true;
    }
}
