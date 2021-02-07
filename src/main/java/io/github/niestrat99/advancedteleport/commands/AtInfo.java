package io.github.niestrat99.advancedteleport.commands;

import io.github.niestrat99.advancedteleport.CoreClass;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AtInfo implements AsyncATCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("at.admin.info")) {
            sender.sendMessage(ChatColor.GOLD + "--[" + ChatColor.AQUA + "AdvancedTeleport" + ChatColor.GOLD + "]--");
            sender.sendMessage(ChatColor.GOLD + "- Developed by " + ChatColor.AQUA + "Niestrat99" + ChatColor.GOLD + " and " + ChatColor.AQUA + "Thatsmusic99");
            sender.sendMessage(ChatColor.GOLD + "- Version: " + ChatColor.AQUA + CoreClass.getInstance().getDescription().getVersion());
            sender.sendMessage(ChatColor.GOLD + "- Spigot Link: " + ChatColor.AQUA + "https://www.spigotmc.org/resources/advanced-teleport.64139/");
            sender.sendMessage(ChatColor.GOLD + "---" + ChatColor.AQUA + "Found bugs?" + ChatColor.GOLD + "---");
            sender.sendMessage(ChatColor.GOLD + "- Discord: " + ChatColor.AQUA + "https://discord.gg/nbT7wC2");
            sender.sendMessage(ChatColor.GOLD + "- Spigot Discussions: " + ChatColor.AQUA + "https://www.spigotmc.org/threads/advanced-teleport.356369/");
            sender.sendMessage(ChatColor.GOLD + "- GitHub: " + ChatColor.AQUA + "https://github.com/Niestrat99/AT-Rewritten");
            sender.sendMessage(ChatColor.AQUA + "Do you like this plugin? Then please leave a review on the Spigot page!");
        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return null;
    }
}
