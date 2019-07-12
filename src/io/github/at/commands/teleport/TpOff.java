package io.github.at.commands.teleport;

import io.github.at.config.Config;
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
                        sender.sendMessage(ChatColor.GREEN + "Successfully disabled teleport requests!");
                        sender.sendMessage(ChatColor.GREEN + "You can no longer receive any teleport requests.");
                        sender.sendMessage(ChatColor.AQUA + "If you want to receive teleport requests type " + ChatColor.YELLOW + "/tpon " + ChatColor.AQUA + "to enable it.");
                    }
                    return false;
                }
            }
        } return false;
    }
}
