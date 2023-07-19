package io.github.niestrat99.advancedteleport.commands.warp.alias;

import io.github.niestrat99.advancedteleport.api.AdvancedTeleportAPI;
import io.github.niestrat99.advancedteleport.commands.ATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.MainConfig;
import io.github.niestrat99.advancedteleport.utilities.MenuUtil;
import io.github.niestrat99.advancedteleport.utilities.PagedLists;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WarpAliasesCommand extends ATCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        // If there aren't enough arguments, let the sender know
        if (args.length < 2) {
            CustomMessages.sendMessage(sender, "Error.noAliasOrWarpInput");
            return false;
        }

        String type = args[0].toLowerCase();
        String name = args[1];
        int page = 1;
        if (args.length >= 3 && args[2].matches("^[0-9]+$")) {
            page = Integer.parseInt(args[2]);
        }

        switch (type) {
            case "warp" -> {

                // Find all the aliases where the warp is in
                List<String> aliases = new ArrayList<>();
                for (String alias : AdvancedTeleportAPI.getWarpAliases().keySet()) {
                    if (!AdvancedTeleportAPI.isAlias(alias, name)) continue;
                    aliases.add(alias);
                }

                // If the aliases are empty, say this warp has no aliases
                if (aliases.isEmpty()) {
                    CustomMessages.sendMessage(sender,
                            "Info.noWarpAliases",
                            Placeholder.unparsed("warp", name));
                    return true;
                }

                // Send the results
                MenuUtil.sendMenu(sender,
                        "warpAliases",
                        new PagedLists<>(aliases, 8),
                        warp -> MiniMessage.miniMessage().deserialize("<gray>> <aqua><warp>", Placeholder.unparsed("warp", warp)),
                        page,
                        Placeholder.unparsed("name", name),
                        Placeholder.unparsed("type", type));
                return true;
            }
            case "alias" -> {
                List<String> results = AdvancedTeleportAPI.getWarpAliases(name);

                // If there's no warps, then let the player know
                if (results.isEmpty()) {
                    CustomMessages.sendMessage(sender,
                            "Info.noWarpsInAlias",
                            Placeholder.unparsed("alias", name));
                    return true;
                }

                // Send a menu
                MenuUtil.sendMenu(sender,
                        "warpAliases",
                        new PagedLists<>(results, 8),
                        warp -> MiniMessage.miniMessage().deserialize("<gray>> <aqua><warp>", Placeholder.unparsed("warp", warp)),
                        page,
                        Placeholder.unparsed("name", name),
                        Placeholder.unparsed("type", type));
                return true;
            }
            default -> CustomMessages.sendMessage(sender, "Error.noAliasOrWarpInput");
        }

        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        // Store results
        List<String> results = new ArrayList<>();

        // Check the first argument
        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], Arrays.asList("warp", "alias"), results);
        }

        // Check the second argument
        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "warp" -> StringUtil.copyPartialMatches(args[1], AdvancedTeleportAPI.getWarps().keySet(), results);
                case "alias" -> StringUtil.copyPartialMatches(args[1], AdvancedTeleportAPI.getWarpAliases().keySet(), results);
            }
        }

        return results;
    }

    @Override
    public boolean getRequiredFeature() {
        return MainConfig.get().USE_WARPS.get();
    }

    @Override
    public @NotNull String getPermission() {
        return "at.admin.warpaliases";
    }
}
