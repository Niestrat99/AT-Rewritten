package io.github.niestrat99.advancedteleport.commands.warp.alias;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.AdvancedTeleportAPI;
import io.github.niestrat99.advancedteleport.api.Warp;
import io.github.niestrat99.advancedteleport.commands.ATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.MainConfig;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RemoveWarpAliasCommand extends ATCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        // Check the args
        if (args.length < 1) {
            CustomMessages.sendMessage(sender, "Error.noWarpInput");
            return false;
        }

        if (args.length < 2) {
            CustomMessages.sendMessage(sender, "Error.noWarpAliasInput");
            return false;
        }

        // Get the alias and warp name
        Warp warp = AdvancedTeleportAPI.getWarp(args[0]);
        String alias = args[1];

        // If the warp doesn't exist, stop there, since there's no warp to add an alias to
        if (warp == null) {
            CustomMessages.sendMessage(sender, "Error.noSuchWarp");
            return true;
        }

        // See if it's already an alias
        if (!AdvancedTeleportAPI.isAlias(alias, warp.getName())) {
            CustomMessages.sendMessage(sender,
                    "Error.notAnAlias",
                    Placeholder.unparsed("alias", alias),
                    Placeholder.unparsed("warp", warp.getName()));
            return true;
        }

        // Add the alias
        AdvancedTeleportAPI.removeWarpAlias(alias, warp, sender).whenCompleteAsync((result, err) ->
                CustomMessages.failable(sender,
                        "Info.addedWarpAlias",
                        "Error.addWarpAliasFailed",
                        err,
                        Placeholder.unparsed("alias", alias),
                        Placeholder.unparsed("warp", warp.getName())), CoreClass.sync);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        List<String> results = new ArrayList<>();
        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], AdvancedTeleportAPI.getWarps().keySet(), results);
        }

        if (args.length == 2) {
            StringUtil.copyPartialMatches(args[1], AdvancedTeleportAPI.getWarpAliases(args[0]), results);
        }

        return results;
    }

    @Override
    public boolean getRequiredFeature() {
        return MainConfig.get().USE_WARPS.get();
    }

    @Override
    public @NotNull String getPermission() {
        return "at.admin.removewarpalias";
    }
}
