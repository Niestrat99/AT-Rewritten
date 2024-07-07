package io.github.niestrat99.advancedteleport.commands.home;

import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.api.Home;
import io.github.niestrat99.advancedteleport.commands.ATCommand;
import io.github.niestrat99.advancedteleport.config.MainConfig;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractHomeCommand extends ATCommand {

    @Override
    public @Nullable List<String> onTabComplete(
            @NotNull final CommandSender sender,
            @NotNull final Command cmd,
            @NotNull final String s,
            @NotNull final String[] args) {
        if (!(sender instanceof Player player)) return Collections.emptyList();

        if (player.hasPermission(getAdminPermission()) && !args[0].isEmpty() && args.length == 2) {
            final var atTarget = ATPlayer.getPlayer(args[0]);
            if (atTarget == null) return Collections.emptyList();
            return StringUtil.copyPartialMatches(
                    args[1], atTarget.getHomes().keySet(), new ArrayList<>());
        }

        if (args.length == 1) {
            final var atSender = ATPlayer.getPlayer(player);
            final List<String> accessibleHomes = new ArrayList<>();
            for (final Home home : atSender.getHomes().values()) {
                if (cmd.getName().equals("delhome") || atSender.canAccessHome(home)) {
                    accessibleHomes.add(home.getName());
                }
            }

            return StringUtil.copyPartialMatches(args[0], accessibleHomes, new ArrayList<>());
        }

        return Collections.emptyList();
    }

    @Override
    public boolean getRequiredFeature() {
        return MainConfig.get().USE_HOMES.get();
    }

    public abstract @NotNull String getAdminPermission();
}
