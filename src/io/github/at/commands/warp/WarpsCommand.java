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
                if(GUI.isUsingWarpsGUIMenu()){
                        ConfigurationSection warps = GUI.getWarpsMenu();
                        /* Instantiate IconMenu
                         * e.g: new IconMenu("Your Homes", 9, 1, CoreClass.getInstance()) will create a menu with "Your Homes" as the title with 1 page and has 9 slots for each page
                         * The slots have to be multiple of 9
                         * You have to pass an instance of the plugin on the 4th parameters for it to work
                         */
                        IconMenu menu = new IconMenu(CustomMessages.getString("Info.warps"), GUI.getWarpsMenuSlots(), 1, CoreClass.getInstance());
                        for (String warpName : warps.getKeys(false)) {
                            ConfigurationSection warp = warps.getConfigurationSection(warpName);
                            if (commandSender.hasPermission("at.member.warp.*")
                                    || commandSender.hasPermission("at.member.warp." + warpName)
                                    || !warp.getBoolean("hideIfNoPermission")) {

                                /* Set an option and command to the inventory menu
                                 * e.g:
                                 * menu.setOption(0, 11, new ItemStack(Material.GRASS_BLOCK, 1), "RTP", "Teleports you to random location");
                                 * menu.setCommand(0, 11, "/rtp");
                                 * will set item inventory on page 0 slot 11 with 1x grass block, and rename the item to "RTP" and shows "Teleports you to random location" on the tooltip, and dispatch "/rtp" command on the commandSender
                                 *
                                 * You can also do menu.setClickEventHandler() if you want to do more than just a simple command
                                 * e.g:
                                 * menu.setClickEventHandler(11, new IconMenu.OptionClickEventHandler() {
                                 *      @Override
                                 *      public void onOptionClick(IconMenu.OptionClickEvent event){
                                 *          System.out.println(event.getPlayer().getDisplayName() + " just clicked something!");
                                 *      }
                                 * });
                                 */
                                menu.setIcon(0, warp.getInt("slot"),
                                        new IconMenu.Icon(
                                                new ItemStack(
                                                        Material.valueOf(warp.getString("item")),
                                                        1,
                                                        (byte) warp.getInt("data-value")))
                                                .withNameAndLore(
                                                        warp.getString("name"),
                                                        (String[]) warp.getStringList("tooltip").toArray())
                                                .withCommands("/warp " + warpName)
                                                .withTexture("texture"));
                            }
                        }
                        menu.open((Player)commandSender);
                    }
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
            }else {
            commandSender.sendMessage(CustomMessages.getString("Error.featureDisabled"));
        }
        return true;
    }
}
