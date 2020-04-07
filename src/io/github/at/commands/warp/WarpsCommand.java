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
                String[] warps = (String[]) Warps.getWarps().keySet().toArray();
                for (int i = 0; i < warps.length; i++) {
                    if (commandSender.hasPermission("at.member.warp.*") || commandSender.hasPermission("at.member.warp." + warps[i])) {
                        wList.then(warps[i])
                                .command("/warp " + warps[i])
                                .tooltip(CustomMessages.getString("Tooltip.warps").replaceAll("\\{warp}", warps[i]));
                        if (i != warps.length - 1) {
                            wList.then(", ");
                        }
                    }
                    wList.text("");
                }
                wList.send(commandSender);
            }
        } else {
            commandSender.sendMessage(CustomMessages.getString("Error.featureDisabled"));
        }
        return true;
    }
}
