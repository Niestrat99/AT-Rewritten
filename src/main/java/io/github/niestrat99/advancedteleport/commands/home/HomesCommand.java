package io.github.niestrat99.advancedteleport.commands.home;

import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.Homes;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.fanciful.FancyMessage;
import io.github.niestrat99.advancedteleport.config.Config;
import io.github.niestrat99.advancedteleport.CoreClass;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class HomesCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (NewConfig.getInstance().USE_HOMES.get()) {
            if (sender.hasPermission("at.member.homes")) {
                Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), () -> {
                    if (args.length>0) {
                        if (sender.hasPermission("at.admin.homes")) {
                            OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
                            if (player != null) {
                                String uuid = player.getUniqueId().toString();
                                FancyMessage hlist = new FancyMessage();
                                hlist.text(CustomMessages.getString("Info.homesOther").replaceAll("\\{player}", player.getName()));
                                try {
                                    if (Homes.getHomes(uuid).size()>0) {
                                        for (String home : Homes.getHomes(uuid).keySet()) {
                                            hlist.then(home)
                                                    .command("/home " + args[0] + " " + home)
                                                    .tooltip(getTooltip(player, sender, home))
                                                    .then(", ");
                                        }
                                        if (player.getBedSpawnLocation() != null && NewConfig.getInstance().ADD_BED_TO_HOMES.get()) {
                                            hlist.then("bed")
                                                    .command("/home " + args[0] + " bed")
                                                    .tooltip(getTooltip(player, sender, "bed"))
                                                    .then(", ");
                                        }
                                        hlist.text(""); //Removes trailing comma
                                    } else {
                                        sender.sendMessage(CustomMessages.getString("Error.noHomesOther").replaceAll("\\{player}", player.getName()));
                                        return;
                                    }

                                } catch (NullPointerException ex) {
                                    sender.sendMessage(CustomMessages.getString("Error.noHomesOther").replaceAll("\\{player}", player.getName()));
                                    return;
                                }
                                Bukkit.getScheduler().runTask(CoreClass.getInstance(), () -> hlist.send(sender));

                                return;
                            } // Otherwise we'll just get the main player's homes
                        }
                    }
                    if (sender instanceof Player) {
                        Player player = (Player)sender;
                        String uuid = player.getUniqueId().toString();
                        FancyMessage hList = new FancyMessage();
                        hList.text(CustomMessages.getString("Info.homes"));

                        try {
                            if (Homes.getHomes(uuid).size()>0){
                                for(String home : Homes.getHomes(uuid).keySet()){

                                    hList.then(home)
                                            .command("/home " + home)
                                            .tooltip(getTooltip(player, home))
                                            .then(", ");
                                }
                                if (player.getBedSpawnLocation() != null && NewConfig.getInstance().ADD_BED_TO_HOMES.get()) {
                                    hList.then("bed")
                                            .command("/home bed")
                                            .tooltip(getTooltip(player, "bed"))
                                            .then(", ");
                                }
                                hList.text(""); //Removes trailing comma
                            } else {
                                sender.sendMessage(CustomMessages.getString("Error.noHomes"));
                                return;
                            }

                        } catch (NullPointerException ex) { // If a player has never set any homes
                            sender.sendMessage(CustomMessages.getString("Error.noHomes"));
                            return;
                        }
                        Bukkit.getScheduler().runTask(CoreClass.getInstance(), () -> hList.send(sender));
                    }
                });

            }
        } else {
            sender.sendMessage(CustomMessages.getString("Error.featureDisabled"));
        }
        return true;
    }

    private List<String> getTooltip(OfflinePlayer player, String home) {
        return getTooltip(player, player.getPlayer(), home);
    }

    private List<String> getTooltip(OfflinePlayer player, CommandSender sender, String home) {
        List<String> tooltip = new ArrayList<>(Collections.singletonList(CustomMessages.getString("Tooltip.homes")));
        if (sender.hasPermission("at.member.homes.location")) {
            tooltip.addAll(Arrays.asList(CustomMessages.getString("Tooltip.location").split("\n")));
        }
        List<String> homeTooltip = new ArrayList<>(tooltip);
        for (int i = 0; i < homeTooltip.size(); i++) {
            Location homeLoc;
            if (home.equals("bed")) {
                homeLoc = player.getBedSpawnLocation();
            } else {
                homeLoc = Homes.getHomes(player.getUniqueId().toString()).get(home);
            }

            homeTooltip.set(i, homeTooltip.get(i).replaceAll("\\{home}", home)
                    .replaceAll("\\{x}", String.valueOf(homeLoc.getX()))
                    .replaceAll("\\{y}", String.valueOf(homeLoc.getY()))
                    .replaceAll("\\{z}", String.valueOf(homeLoc.getZ()))
                    .replaceAll("\\{world}", homeLoc.getWorld().getName()));
        }
        return homeTooltip;
    }

}
