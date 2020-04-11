package io.github.at.commands.home;

import io.github.at.config.Homes;
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
            String uuid2 = ((Player) sender).getUniqueId().toString();
            if (!args[0].isEmpty() && Bukkit.getOfflinePlayer(args[0]) != null && sender.hasPermission("at.admin.home")) {
                Player p = Bukkit.getOfflinePlayer(args[0]).getPlayer();
                String uuid = p.getUniqueId().toString();
                if (args.length < 3) {
                    try {
                        List<String> possibleHomes = new ArrayList<>();
                        StringUtil.copyPartialMatches(args[1], Homes.getHomes(uuid).keySet(), possibleHomes);
                        Collections.sort(possibleHomes);
                        return possibleHomes;
                    } catch (IndexOutOfBoundsException e) {
                        try {
                            List<String> possibleHomes = new ArrayList<>(Homes.getHomes(uuid).keySet());
                            Collections.sort(possibleHomes);
                            return possibleHomes;
                        } catch (NullPointerException ex) { // DEAR GOD
                            List<String> possibleHomes = new ArrayList<>();
                            StringUtil.copyPartialMatches(args[0], Homes.getHomes(uuid2).keySet(), possibleHomes);
                            Collections.sort(possibleHomes);
                            return possibleHomes;
                        }

                    } catch (NullPointerException e) { // For REAL Spigot, pick up your game and stop whining!
                        List<String> possibleHomes = new ArrayList<>();
                        StringUtil.copyPartialMatches(args[0], Homes.getHomes(uuid2).keySet(), possibleHomes);
                        Collections.sort(possibleHomes);
                        return possibleHomes;
                    }
                } else {
                    return new ArrayList<>();
                }

            } else {
                if (args.length < 2) {
                    if (args[0].isEmpty()) {
                        List<String> possibleHomes = new ArrayList<>(Homes.getHomes(uuid2).keySet());
                        Collections.sort(possibleHomes);
                        return possibleHomes;
                    } else {
                        List<String> possibleHomes = new ArrayList<>();
                        StringUtil.copyPartialMatches(args[0], Homes.getHomes(uuid2).keySet(), possibleHomes);
                        Collections.sort(possibleHomes);
                        return possibleHomes;
                    }
                } else {
                    return new ArrayList<>();
                }
            }
        } else {
            return new ArrayList<>();
        }
    }
}
