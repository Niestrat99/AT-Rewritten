package io.github.niestrat99.advancedteleport.commands.warp;

import io.github.niestrat99.advancedteleport.config.Warps;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class WarpTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        List<String> possibleWarps = new ArrayList<>();
        // Checks if sender has warp permissions, and if not, filter them out/remove them
        List<String> permittedWarps = Warps.getWarps().keySet().stream().filter(st -> sender.hasPermission("at.member.warp." + st) || sender.hasPermission("at.member.warp.*")).collect(Collectors.toList());
        StringUtil.copyPartialMatches(args[0], permittedWarps, possibleWarps);
        Collections.sort(possibleWarps);
        return possibleWarps;

    }
}
