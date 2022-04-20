package io.github.niestrat99.advancedteleport.commands;

import io.github.niestrat99.advancedteleport.config.CustomMessages;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public interface ATCommand extends CommandExecutor, TabCompleter {

    default boolean canProceed(@NotNull CommandSender sender) {
        if (!getRequiredFeature()) {
            CustomMessages.sendMessage(sender, "Error.featureDisabled");
            return false;
        }
        return sender.hasPermission(getPermission());
    }

    boolean getRequiredFeature();

    String getPermission();

    @Nullable
    @Override
    default List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        List<String> results = new ArrayList<>();
        List<String> players = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (sender instanceof Player && ((Player) sender).canSee(player)) {
                players.add(player.getName());
            }
        }
        StringUtil.copyPartialMatches(args[args.length - 1], players, results);
        return results;
    }
}
