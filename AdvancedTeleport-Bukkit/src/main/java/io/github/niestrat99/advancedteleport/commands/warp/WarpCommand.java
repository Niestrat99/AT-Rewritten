package io.github.niestrat99.advancedteleport.commands.warp;

import io.github.niestrat99.advancedteleport.api.ATFloodgatePlayer;
import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.api.AdvancedTeleportAPI;
import io.github.niestrat99.advancedteleport.api.Warp;
import io.github.niestrat99.advancedteleport.api.events.ATTeleportEvent;
import io.github.niestrat99.advancedteleport.commands.TimedATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.MainConfig;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class WarpCommand extends AbstractWarpCommand implements TimedATCommand {

    @Override
    public boolean onCommand(
            @NotNull final CommandSender sender,
            @NotNull final Command command,
            @NotNull final String s,
            @NotNull final String[] args) {

        // If the feature isn't enabled/no permission, stop there
        if (!canProceed(sender)) return true;

        Player player = (Player) sender;

        // If there's no arguments specified, see if the player is a Bedrock player and use a form
        if (args.length == 0) {
            ATPlayer atPlayer = ATPlayer.getPlayer(player);
            if (atPlayer instanceof ATFloodgatePlayer
                    && MainConfig.get().USE_FLOODGATE_FORMS.get()) {
                ((ATFloodgatePlayer) atPlayer).sendWarpForm();
            } else {
                CustomMessages.sendMessage(sender, "Error.noWarpInput");
            }
            return true;
        }

        // If the warp exists and the player isn't already teleporting, may as well warp them
        Warp warp = AdvancedTeleportAPI.fetchWarp(args[0], player, false);
        if (warp != null) {
            warp(warp, player);
        } else {
            CustomMessages.sendMessage(sender, "Error.noSuchWarp");
        }
        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        // Set up the resulting tab completion options
        List<String> results = super.onTabComplete(sender, command, s, args);
        if (results == null) results = new ArrayList<>();

        // Go through aliases
        List<String> aliases = new ArrayList<>();
        if (sender instanceof Player player) {
            for (String alias : AdvancedTeleportAPI.getWarpAliases().keySet()) {
                if (!AdvancedTeleportAPI.canAccessWarp(player, alias, false)) continue;
                aliases.add(alias);
            }
        } else {
            aliases.addAll(AdvancedTeleportAPI.getWarpAliases().keySet());
        }

        // Copy partial matches over and return the results
        StringUtil.copyPartialMatches(args[0], aliases, results);
        return results;
    }

    public static void warp(Warp warp, Player player) {
        ATTeleportEvent event = new ATTeleportEvent(player, warp.getLocation(), player.getLocation(), warp.getName(), ATTeleportEvent.TeleportType.WARP);
        Bukkit.getPluginManager().callEvent(event);
        ATPlayer.getPlayer(player).teleport(event, "warp", "Teleport.teleportingToWarp");
    }

    @Override
    public @NotNull String getPermission() {
        return "at.member.warp";
    }

    @Override
    public @NotNull String getSection() {
        return "warp";
    }
}
