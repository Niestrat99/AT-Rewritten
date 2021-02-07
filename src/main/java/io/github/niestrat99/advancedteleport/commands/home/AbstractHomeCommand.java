package io.github.niestrat99.advancedteleport.commands.home;

import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.commands.ATCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractHomeCommand implements ATCommand {

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        List<String> results = new ArrayList<>();
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (player.hasPermission("at.admin." + cmd.getName())) {
                if (!args[0].isEmpty() && args.length == 2) {
                    ATPlayer target = ATPlayer.getPlayer(args[0]);
                    if (target == null) return new ArrayList<>();
                    StringUtil.copyPartialMatches(args[1], target.getHomes().keySet(), results);
                }
            }
            if (args.length == 1) {
                ATPlayer atPlayer = ATPlayer.getPlayer(player);
                StringUtil.copyPartialMatches(args[0], atPlayer.getHomes().keySet(), results);
            }
        }
        return results;
    }
}
