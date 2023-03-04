package io.github.niestrat99.advancedteleport.commands.spawn;

import io.github.niestrat99.advancedteleport.api.AdvancedTeleportAPI;
import io.github.niestrat99.advancedteleport.api.spawn.Spawn;
import io.github.niestrat99.advancedteleport.commands.SpawnATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class RemoveSpawn extends SpawnATCommand {

    @Override
    public boolean onCommand(
        @NotNull final CommandSender sender,
        @NotNull final Command command,
        @NotNull final String s,
        @NotNull final String[] args
    ) {

        // If the player cannot proceed, stop there
        if (!canProceed(sender)) return true;

        // Note the ID of the spawn being removed
        String removingSpawn = "";

        // If there's no arguments specified, use the player's world - unless they aren't a player, in which case, banish
        if (args.length == 0) {
            if (sender instanceof Player player) {
                removingSpawn = player.getWorld().getName();
            } else {
                CustomMessages.sendMessage(sender, "Error.removeSpawnNoArgs");
                return true;
            }
        }

        // If there have been arguments specified, use that
        if (args.length > 0) {
            removingSpawn = args[0];
        }

        // If the spawn does not exist, stop there
        Spawn spawn = AdvancedTeleportAPI.getSpawn(removingSpawn);
        if (spawn == null) {
            CustomMessages.sendMessage(sender, "Error.noSuchSpawn", "{spawn}", removingSpawn);
            return true;
        }

        // Remove the spawn
        spawn.delete(sender).whenComplete((ignored, err) -> CustomMessages.failable(sender,
                        "Info.removedSpawn",
                        "Error.removeSpawnFail",
                        () -> err != null,
                        "{spawn}", spawn.getName()));
        return true;
    }

    @Override
    public @NotNull String getPermission() {
        return "at.admin.removespawn";
    }

    @Override
    public @Nullable List<String> onTabComplete(
        @NotNull final CommandSender sender,
        @NotNull final Command command,
        @NotNull final String s,
        @NotNull final String[] args
    ) {
        if (sender.hasPermission("at.admin.removespawn") && sender instanceof Player && args.length == 1) {
            List<String> spawns = new ArrayList<>();
            StringUtil.copyPartialMatches(args[0], AdvancedTeleportAPI.getSpawns().keySet(), spawns);
            return spawns;
        }
        return null;
    }
}
