package io.github.at.commands.warp;

import fanciful.FancyMessage;
import io.github.at.config.Config;
import io.github.at.config.CustomMessages;
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
                if(Config.isUsingWarpsGUIMenu()){
                        ConfigurationSection warps = Config.getWarpsMenu();
                        IconMenu menu = new IconMenu(CustomMessages.getString("Info.warps"), Config.getWarpsMenuSlots(), CoreClass.getInstance());
                        for (String warpName : warps.getKeys(false)) {
                            ConfigurationSection warp = warps.getConfigurationSection(warpName);
                            if (commandSender.hasPermission("at.member.warp.*") || commandSender.hasPermission("at.member.warp." + warpName) || !warp.getBoolean("hideIfNoPermission")) {
                                menu.setOption(warp.getInt("slot"), new ItemStack(Material.valueOf(warp.getString("item")), 1), warp.getString("name"), warp.getString("tooltip"));
                                menu.setCommand(warp.getInt("slot"), "/warp " + warpName);
                            }
                        }
                        menu.open((Player)commandSender);
                }else{
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
