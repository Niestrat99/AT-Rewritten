package io.github.niestrat99.advancedteleport.commands.warp;

import io.github.niestrat99.advancedteleport.commands.ATCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractWarpCommand implements ATCommand {

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        List<String> results = new ArrayList<>();
        List<String> warps = new ArrayList<>();
        for (String warp : io.github.niestrat99.advancedteleport.api.Warp.getWarps().keySet()) {
            if (sender.hasPermission("at.member.warp." + warp.toLowerCase())) {
                warps.add(warp);
            } else if (!sender.isPermissionSet("at.member.warp." + warp.toLowerCase())
                    && sender.hasPermission("at.member.warp.*")) {
                warps.add(warp);
            }
        }
        StringUtil.copyPartialMatches(args[0], warps, results);
        return results;
    }
}
