package io.github.at.commands.warp;

import fanciful.FancyMessage;
import io.github.at.config.Config;
import io.github.at.config.CustomMessages;
import io.github.at.config.Warps;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class WarpsCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (Config.isFeatureEnabled("warps")) {
            if (commandSender.hasPermission("at.member.warps")){
                FancyMessage wList = new FancyMessage();
                wList.text(CustomMessages.getString("Info.warps"));
                for (String warp: Warps.getWarps().keySet()) {
                    if (commandSender.hasPermission("at.member.warp.*") || commandSender.hasPermission("at.member.warp." + warp)) {
                        wList.then(warp)
                                .command("/warp " + warp)
                                .tooltip(CustomMessages.getString("Tooltip.warps").replaceAll("\\{warp}", warp));
                        wList.then(", ");
                    }
                    wList.text("");
                }
                wList.send(commandSender);
            }
        } else {
            commandSender.sendMessage(CustomMessages.getString("Error.featureDisabled"));
            return false;
        }
        return false;
    }
}
