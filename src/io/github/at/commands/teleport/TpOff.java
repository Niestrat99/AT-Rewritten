package io.github.at.commands.teleport;

import io.github.at.config.Config;
import io.github.at.config.CustomMessages;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TpOff implements CommandExecutor {

    private static List<Player> tpoff = new ArrayList<>();

    public static List<Player> getTpOff() {
        return tpoff;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (Config.isFeatureEnabled("teleport")) {
                if (sender.hasPermission("at.member.off")) {
                    if (!tpoff.contains(player)) {
                        tpoff.add(player);
                        sender.sendMessage(CustomMessages.getString("Info.tpOff"));
                    }
                    return false;
                }
            }
        } return false;
    }
}
