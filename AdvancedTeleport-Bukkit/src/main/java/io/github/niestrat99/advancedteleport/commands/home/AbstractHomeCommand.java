package io.github.niestrat99.advancedteleport.commands.home;

import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.api.Home;
import io.github.niestrat99.advancedteleport.commands.ATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.NewConfig;
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
    public boolean getRequiredFeature() {
        return NewConfig.get().USE_HOMES.get();
    }

    @Override
    public boolean canProceed(@NotNull final CommandSender sender) {
        if (!super.canProceed(sender)) return false;

        if (!(sender instanceof Player)) {
            CustomMessages.sendMessage(sender, "Error.notAPlayer");
            return false;
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(
        @NotNull final CommandSender sender,
        @NotNull final Command cmd,
        @NotNull final String s,
        @NotNull final String[] args
    ) {
        if (!(sender instanceof Player player)) return Collections.emptyList();

        if (player.hasPermission(getPermission()) && !args[0].isEmpty() && args.length == 2) {
            if (!ATPlayer.isPlayerCached(args[0])) return Collections.emptyList();
            final var atTarget = ATPlayer.getPlayer(args[0]);
            return StringUtil.copyPartialMatches(args[1], atTarget.getHomes().keySet(), new ArrayList<>());
        }

        if (args.length == 1) {
            final var atSender = ATPlayer.getPlayer(player);
            final var accessibleHomes = atSender.getHomes().values().stream()
                .filter(home -> cmd.getName().equals("delHome") || atSender.canAccessHome(home))
                .map(Home::getName)
                .toList();

            return StringUtil.copyPartialMatches(args[0], accessibleHomes, new ArrayList<>());
        }

        return Collections.emptyList();
    }
}
