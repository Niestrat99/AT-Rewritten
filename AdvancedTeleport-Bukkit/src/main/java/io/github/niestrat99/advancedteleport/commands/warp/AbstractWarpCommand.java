package io.github.niestrat99.advancedteleport.commands.warp;

import io.github.niestrat99.advancedteleport.api.AdvancedTeleportAPI;
import io.github.niestrat99.advancedteleport.commands.ATCommand;
import io.github.niestrat99.advancedteleport.config.MainConfig;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractWarpCommand extends ATCommand {

    @Override
    public @Nullable List<String> onTabComplete(
        @NotNull final CommandSender sender,
        @NotNull final Command command,
        @NotNull final String s,
        @NotNull final String[] args
    ) {

        // Set up the resulting tab completion options
        List<String> results = new ArrayList<>();
        List<String> warps = new ArrayList<>();

        // Go through each warp and see if they have permission to it
        for (String warp : AdvancedTeleportAPI.getWarps().keySet()) {
            if (sender.hasPermission("at.member.warp." + warp.toLowerCase())) {
                warps.add(warp);
            } else if (!sender.isPermissionSet("at.member.warp." + warp.toLowerCase())
                && sender.hasPermission("at.member.warp.*")) {
                warps.add(warp);
            }
        }

        // Copy partial matches over and return the results
        StringUtil.copyPartialMatches(args[0], warps, results);
        return results;
    }

    @Override
    public boolean getRequiredFeature() {
        return MainConfig.get().USE_WARPS.get();
    }
}
