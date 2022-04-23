package io.github.niestrat99.advancedteleport.commands.home;

import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.commands.ATCommand;
import io.github.niestrat99.advancedteleport.config.NewConfig;
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
                    if (!ATPlayer.isPlayerCached(args[0])) return new ArrayList<>();
                    ATPlayer target = ATPlayer.getPlayer(args[0]);
                    StringUtil.copyPartialMatches(args[1], target.getHomes().keySet(), results);
                    return results;
                }
            }
            if (args.length == 1) {
                ATPlayer atPlayer = ATPlayer.getPlayer(player);
                List<String> homes = new ArrayList<>();
                for (String home : atPlayer.getHomes().keySet()) {
                    if (atPlayer.canAccessHome(atPlayer.getHome(home)) || cmd.getName().equalsIgnoreCase("delhome")) {
                        homes.add(home);
                    }
                }
                StringUtil.copyPartialMatches(args[0], homes, results);
            }
        }
        return results;
    }

    @Override
    public boolean getRequiredFeature() {
        return NewConfig.get().USE_HOMES.get();
    }
}
