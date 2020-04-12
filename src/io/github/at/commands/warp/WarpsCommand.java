package io.github.at.commands.warp;

import fanciful.FancyMessage;
import io.github.at.config.Config;
import io.github.at.config.CustomMessages;
import io.github.at.config.GUI;
import io.github.at.config.Warps;
import io.github.at.main.CoreClass;
import io.github.at.utilities.IconMenu;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class WarpsCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (Config.isFeatureEnabled("warps")) {
            if (commandSender.hasPermission("at.member.warps")){
                if(GUI.isUsingWarpsGUIMenu()) {
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
                        if (commandSender.hasPermission("at.member.warp.*")
                                || commandSender.hasPermission("at.member.warp." + warpName)
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
                    menu.open((Player) commandSender);

                } else {
                    FancyMessage wList = new FancyMessage();
                    wList.text(CustomMessages.getString("Info.warps"));
                    for(String warp: Warps.getWarps().keySet()){
                        if (commandSender.hasPermission("at.member.warp.*") || commandSender.hasPermission("at.member.warp." + warp)) {
                            wList.then(warp)
                                    .command("/warp " + warp)
                                    .tooltip(CustomMessages.getString("Tooltip.warps").replaceAll("\\{warp}", warp))
                                    .then(", ");
                        }
                    }
                    wList.text(""); //Removes trailing comma
                    wList.send(commandSender);
                }
            }
        } else {
            commandSender.sendMessage(CustomMessages.getString("Error.featureDisabled"));
        }
        return true;
    }
}
