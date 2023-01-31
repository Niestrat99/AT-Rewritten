package io.github.niestrat99.advancedteleport.commands.spawn;

import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.api.events.ATTeleportEvent;
import io.github.niestrat99.advancedteleport.commands.SpawnATCommand;
import io.github.niestrat99.advancedteleport.commands.TimedATCommand;
import io.github.niestrat99.advancedteleport.config.Spawn;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SpawnCommand extends SpawnATCommand implements TimedATCommand {

    @Override
    public boolean onCommand(
        @NotNull final CommandSender sender,
        @NotNull final Command command,
        @NotNull final String s,
        @NotNull final String[] args
    ) {
        if (!canProceed(sender)) return true;

        Player player = (Player) sender;
        String location = player.getWorld().getName();
        if (args.length > 0 &&
            (player.hasPermission("at.admin.spawn") || player.hasPermission("at.member.spawn." + args[0].toLowerCase()))) {
            if (args[0].matches("^[0-9A-Za-z\\-_]+$")) {
                location = args[0];
            }
        }
        spawn(player, location);
        return true;
    }

    public static void spawn(
        Player player,
        String name
    ) {

        Location spawn = Spawn.get().getSpawn(name, player, false);
        if (spawn == null) {
            spawn = player.getWorld().getSpawnLocation();
        }

        ATTeleportEvent event = new ATTeleportEvent(player, spawn, player.getLocation(), "spawn", ATTeleportEvent.TeleportType.SPAWN);
        Bukkit.getPluginManager().callEvent(event);
        ATPlayer.getPlayer(player).teleport(event, "spawn", "Teleport.teleportingToSpawn");
    }

    @Override
    public @NotNull String getPermission() {
        return "at.member.spawn";
    }

    @Override
    public @Nullable List<String> onTabComplete(
        @NotNull final CommandSender sender,
        @NotNull final Command command,
        @NotNull final String s,
        @NotNull final String[] args
    ) {
        if (sender.hasPermission("at.admin.spawn") && sender instanceof Player && args.length == 1) {
            List<String> spawns = new ArrayList<>();
            StringUtil.copyPartialMatches(args[0], Spawn.get().getSpawns(), spawns);
            return spawns;
        }
        return null;
    }

    @Override
    public @NotNull String getSection() {
        return "spawn";
    }
}
