package io.github.niestrat99.advancedteleport.commands.home;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.api.Home;
import io.github.niestrat99.advancedteleport.commands.AsyncATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.fanciful.FancyMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class HomesCommand implements AsyncATCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (NewConfig.getInstance().USE_HOMES.get()) {
            if (sender.hasPermission("at.member.homes")) {
                if (args.length>0) {
                    if (sender.hasPermission("at.admin.homes")) {
                        OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
                        ATPlayer atPlayer = ATPlayer.getPlayer(player);
                        if (atPlayer.getHomes().size() > 0) {
                            getHomes(sender, player);
                            return true;
                        }
                    }
                }
                if (sender instanceof Player) {
                    getHomes(sender, (Player) sender);
                }
            }
        } else {
            sender.sendMessage(CustomMessages.getString("Error.featureDisabled"));
        }
        return true;
    }

    private void getHomes(CommandSender sender, OfflinePlayer target) {
        ATPlayer atPlayer = ATPlayer.getPlayer(target);
        FancyMessage hList = new FancyMessage();
        hList.text(CustomMessages.getString("Info.homes"));

        if (atPlayer.getHomes().size() > 0) {
            for (Home home : atPlayer.getHomes().values()) {
                if (atPlayer.canAccessHome(home)) {
                    hList.then(home.getName())
                            .command("/home " + home.getName())
                            .tooltip(getTooltip(sender, home))
                            .then(", ");
                } else if (!NewConfig.getInstance().HIDE_HOMES_IF_DENIED.get()) {
                    hList.then(home.getName())
                            .tooltip(getTooltip(sender, home))
                            .color(ChatColor.GRAY)
                            .style(ChatColor.ITALIC)
                            .then(", ");
                }

            }
            if (atPlayer.getBedSpawn() != null && NewConfig.getInstance().ADD_BED_TO_HOMES.get()) {
                hList.then("bed")
                        .command("/home bed")
                        .tooltip(getTooltip(sender, atPlayer.getBedSpawn()))
                        .then(", ");
            }
            hList.text(""); //Removes trailing comma
        } else {
            return;
        }

        Bukkit.getScheduler().runTask(CoreClass.getInstance(), () -> hList.send(sender));
    }

    private List<String> getTooltip(CommandSender sender, Home home) {
        List<String> tooltip = new ArrayList<>(Collections.singletonList(CustomMessages.getString("Tooltip.homes")));
        if (sender.hasPermission("at.member.homes.location")) {
            tooltip.addAll(Arrays.asList(CustomMessages.getString("Tooltip.location").split("\n")));
        }
        List<String> homeTooltip = new ArrayList<>(tooltip);
        for (int i = 0; i < homeTooltip.size(); i++) {
            Location homeLoc = home.getLocation();

            homeTooltip.set(i, homeTooltip.get(i).replace("{home}", home.getName())
                    .replaceAll("\\{x}", String.valueOf(homeLoc.getX()))
                    .replaceAll("\\{y}", String.valueOf(homeLoc.getY()))
                    .replaceAll("\\{z}", String.valueOf(homeLoc.getZ()))
                    .replaceAll("\\{world}", homeLoc.getWorld().getName()));
        }
        return homeTooltip;
    }
}
