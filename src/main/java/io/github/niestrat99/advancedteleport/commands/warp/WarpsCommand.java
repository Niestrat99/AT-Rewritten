package io.github.niestrat99.advancedteleport.commands.warp;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.config.*;
import io.github.niestrat99.advancedteleport.fanciful.FancyMessage;
import io.github.niestrat99.advancedteleport.utilities.IconMenu;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class WarpsCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (Config.isFeatureEnabled("warps")) {
            if (commandSender.hasPermission("at.member.warps")){
                sendWarps(commandSender);
            }
        } else {
            commandSender.sendMessage(CustomMessages.getString("Error.featureDisabled"));
        }
        return true;
    }

    public static void sendWarps(CommandSender sender) {
        if (GUI.isUsingWarpsGUIMenu()) {
            ConfigurationSection warps = GUI.getWarpsMenu();
            int minPage = 999;
            int maxPage = 0;
            for (String warp : warps.getKeys(false)) {
                int page = warps.getInt(warp + ".page");
                if (page < minPage) {
                    minPage = page;
                }
                if (page > maxPage) {
                    maxPage = page;
                }
            }
            int pages = maxPage - minPage + 1;
            IconMenu menu = new IconMenu(CustomMessages.getString("Info.warps"), GUI.getWarpsMenuSlots(), pages, CoreClass.getInstance());

            for (String warpName : warps.getKeys(false)) {
                ConfigurationSection warp = warps.getConfigurationSection(warpName);
                if (sender.hasPermission("at.member.warp.*")
                        || sender.hasPermission("at.member.warp." + warpName)
                        || !warp.getBoolean("hideIfNoPermission")) {

                    menu.setIcon(warp.getInt("page"), warp.getInt("slot"),
                            new IconMenu.Icon(
                                    new ItemStack(
                                            Material.valueOf(warp.getString("item")),
                                            1,
                                            (byte) warp.getInt("data-value")))
                                    .withNameAndLore(
                                            warp.getString("name"),
                                            warp.getStringList("tooltip"))
                                    .withCommands("warp " + warpName)
                                    .withTexture("texture"));
                }
            }
            // Next page icons will override warps
            for (int i = 0; i < pages; i++) {
                if (i != 0) {
                    ConfigurationSection lastPage = GUI.getLastPageIcon();
                    menu.setIcon(i, lastPage.getInt("slot"),
                            new IconMenu.Icon(
                                    new ItemStack(Material.valueOf(lastPage.getString("item")),
                                            1,
                                            (byte) lastPage.getInt("data-value")))
                                    .withTexture(lastPage.getString("texture"))
                                    .withNameAndLore(lastPage.getString("name"), lastPage.getStringList("tooltip"))
                                    .withHandler(handler -> {
                                        handler.setWillClose(false);
                                        handler.setWillDestroy(false);
                                        menu.openPreviousPage();
                                    }));
                }
                if (i != pages - 1) {
                    ConfigurationSection nextPage = GUI.getNextPageIcon();
                    menu.setIcon(i, nextPage.getInt("slot"),
                            new IconMenu.Icon(
                                    new ItemStack(Material.valueOf(nextPage.getString("item")),
                                            1,
                                            (byte) nextPage.getInt("data-value")))
                                    .withTexture(nextPage.getString("texture"))
                                    .withNameAndLore(nextPage.getString("name"), nextPage.getStringList("tooltip"))
                                    .withHandler(handler -> {
                                        handler.setWillClose(false);
                                        handler.setWillDestroy(false);
                                        menu.openNextPage();
                                    }));
                }
            }
            menu.open((Player) sender);

        } else {
            if (Warps.getWarps().size() > 0) {
                FancyMessage wList = new FancyMessage();
                wList.text(CustomMessages.getString("Info.warps"));
                for(String warp: Warps.getWarps().keySet()){
                    if (sender.hasPermission("at.member.warp.*") || sender.hasPermission("at.member.warp." + warp)) {
                        wList.then(warp)
                                .command("/warp " + warp)
                                .tooltip(getTooltip(sender, warp))
                                .then(", ");
                    }
                }
                wList.text(""); //Removes trailing comma
                wList.send(sender);
            } else {
                sender.sendMessage(CustomMessages.getString("Error.noWarps"));
            }

        }
    }

    private static List<String> getTooltip(CommandSender sender, String warp) {
        List<String> tooltip = new ArrayList<>(Collections.singletonList(CustomMessages.getString("Tooltip.warps")));
        if (sender.hasPermission("at.member.warps.location")) {
            tooltip.addAll(Arrays.asList(CustomMessages.getString("Tooltip.location").split("\n")));
        }
        List<String> homeTooltip = new ArrayList<>(tooltip);
        for (int i = 0; i < homeTooltip.size(); i++) {
            Location warpLoc = Warps.getWarps().get(warp);

            homeTooltip.set(i, homeTooltip.get(i).replaceAll("\\{warp}", warp)
                    .replaceAll("\\{x}", String.valueOf(warpLoc.getBlockX()))
                    .replaceAll("\\{y}", String.valueOf(warpLoc.getBlockY()))
                    .replaceAll("\\{z}", String.valueOf(warpLoc.getBlockZ()))
                    .replaceAll("\\{world}", warpLoc.getWorld().getName()));
        }
        return homeTooltip;
    }
}
