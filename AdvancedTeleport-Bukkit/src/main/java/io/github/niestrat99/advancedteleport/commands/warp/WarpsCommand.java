package io.github.niestrat99.advancedteleport.commands.warp;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.Warp;
import io.github.niestrat99.advancedteleport.commands.ATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.GUI;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.fanciful.FancyMessage;
import io.github.niestrat99.advancedteleport.utilities.IconMenu;
import io.github.thatsmusic99.configurationmaster.api.ConfigSection;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class WarpsCommand implements ATCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!canProceed(sender)) return true;
        sendWarps(sender);
        return true;
    }

    public static void sendWarps(CommandSender sender) {
        if (GUI.isUsingWarpsGUIMenu()) {
            ConfigSection warps = GUI.getWarpsMenu();
            int minPage = 999;
            int maxPage = 0;
            for (String warp : warps.getKeys(false)) {
                int page = warps.getInteger(warp + ".page");
                if (page < minPage) {
                    minPage = page;
                }
                if (page > maxPage) {
                    maxPage = page;
                }
            }
            int pages = maxPage - minPage + 1;
            IconMenu menu = new IconMenu(CustomMessages.getStringA("Info.warps"), GUI.getWarpsMenuSlots(), pages, CoreClass.getInstance());

            for (String warpName : warps.getKeys(false)) {
                ConfigSection warp = warps.getConfigSection(warpName);
                if (sender.hasPermission("at.member.warp.*")
                        || sender.hasPermission("at.member.warp." + warpName)
                        || !warp.getBoolean("hideIfNoPermission")) {

                    menu.setIcon(warp.getInteger("page"), warp.getInteger("slot"),
                            new IconMenu.Icon(
                                    new ItemStack(
                                            Material.valueOf(warp.getString("item")),
                                            1,
                                            (byte) warp.getInteger("data-value")))
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
                    ConfigSection lastPage = GUI.getLastPageIcon();
                    menu.setIcon(i, lastPage.getInteger("slot"),
                            new IconMenu.Icon(
                                    new ItemStack(Material.valueOf(lastPage.getString("item")),
                                            1,
                                            (byte) lastPage.getInteger("data-value")))
                                    .withTexture(lastPage.getString("texture"))
                                    .withNameAndLore(lastPage.getString("name"), lastPage.getStringList("tooltip"))
                                    .withHandler(handler -> {
                                        handler.setWillClose(false);
                                        handler.setWillDestroy(false);
                                        menu.openPreviousPage();
                                    }));
                }
                if (i != pages - 1) {
                    ConfigSection nextPage = GUI.getNextPageIcon();
                    menu.setIcon(i, nextPage.getInteger("slot"),
                            new IconMenu.Icon(
                                    new ItemStack(Material.valueOf(nextPage.getString("item")),
                                            1,
                                            (byte) nextPage.getInteger("data-value")))
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
            if (Warp.getWarps().size() > 0) {
                FancyMessage wList = new FancyMessage();
                wList.text(CustomMessages.getStringA("Info.warps"));
                int count = 0;
                for(String warp: Warp.getWarps().keySet()){
                    if (sender.hasPermission("at.member.warp.*") || sender.hasPermission("at.member.warp." + warp)) {
                        wList.then(warp)
                                .command("/warp " + warp)
                                .tooltip(getTooltip(sender, warp))
                                .then(", ");
                        count++;
                    }
                }
                wList.text(""); //Removes trailing comma
                if (count > 0) {
                    wList.sendProposal(sender, 0);
                    FancyMessage.send(sender);
                } else {
                    sender.sendMessage(CustomMessages.getStringA("Error.noWarps"));
                }

            } else {
                CustomMessages.sendMessage(sender, "Error.noWarps");
            }

        }
    }

    private static List<String> getTooltip(CommandSender sender, String warp) {
        List<String> tooltip = new ArrayList<>(Collections.singletonList(CustomMessages.getStringA("Tooltip.warps")));
        if (sender.hasPermission("at.member.warps.location")) {
            tooltip.addAll(Arrays.asList(CustomMessages.getStringA("Tooltip.location").split("\n")));
        }
        List<String> homeTooltip = new ArrayList<>(tooltip);
        for (int i = 0; i < homeTooltip.size(); i++) {
            Location warpLoc = Warp.getWarps().get(warp).getLocation();

            homeTooltip.set(i, homeTooltip.get(i).replaceAll("\\{warp}", warp)
                    .replaceAll("\\{x}", String.valueOf(warpLoc.getBlockX()))
                    .replaceAll("\\{y}", String.valueOf(warpLoc.getBlockY()))
                    .replaceAll("\\{z}", String.valueOf(warpLoc.getBlockZ()))
                    .replaceAll("\\{world}", warpLoc.getWorld().getName()));
        }
        return homeTooltip;
    }

    @Override
    public boolean getRequiredFeature() {
        return NewConfig.get().USE_WARPS.get();
    }

    @Override
    public String getPermission() {
        return "at.member.warps";
    }
}
