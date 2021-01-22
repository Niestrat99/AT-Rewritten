package io.github.niestrat99.advancedteleport.commands.home;

import io.github.niestrat99.advancedteleport.config.Homes;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        if (sender instanceof Player) {
            List<String> possibleHomes;
            String uuid2 = ((Player) sender).getUniqueId().toString();
            if (!args[0].isEmpty()
                    && args.length > 1
                    && Bukkit.getOfflinePlayer(args[0]) != null
                    && sender.hasPermission("at.admin.home")) {
                String uuid = Bukkit.getOfflinePlayer(args[0]).getUniqueId().toString();
                if (args.length < 3) {
                    try {
                        possibleHomes = new ArrayList<>();
                        StringUtil.copyPartialMatches(args[1], Homes.getHomes(uuid).keySet(), possibleHomes);
                        Collections.sort(possibleHomes);
                        return possibleHomes;
                    } catch (IndexOutOfBoundsException e) {
                        try {
                            possibleHomes = new ArrayList<>(Homes.getHomes(uuid).keySet());
                        } catch (NullPointerException ex) { // DEAR GOD
                            possibleHomes = new ArrayList<>();
                            StringUtil.copyPartialMatches(args[0], Homes.getHomes(uuid2).keySet(), possibleHomes);
                        }
                    } catch (NullPointerException e) { // For REAL Spigot, pick up your game and stop whining!
                        possibleHomes = new ArrayList<>();
                        StringUtil.copyPartialMatches(args[0], Homes.getHomes(uuid2).keySet(), possibleHomes);
                    }
                    Collections.sort(possibleHomes);
                    return possibleHomes;
                } else {
                    return new ArrayList<>();
                }

            } else {
                if (args.length < 2) {
                    if (args[0].isEmpty()) {
                        possibleHomes = new ArrayList<>(Homes.getHomes(uuid2).keySet());
                    } else {
                        possibleHomes = new ArrayList<>();
                        StringUtil.copyPartialMatches(args[0], Homes.getHomes(uuid2).keySet(), possibleHomes);
                    }
                    Collections.sort(possibleHomes);
                    return possibleHomes;
                } else {
                    return new ArrayList<>();
                }
            }
        } else {
            return new ArrayList<>();
        }
    }
}
