package io.github.niestrat99.advancedteleport.commands.spawn;

import io.github.niestrat99.advancedteleport.api.AdvancedTeleportAPI;
import io.github.niestrat99.advancedteleport.api.events.spawn.SpawnRemoveEvent;
import io.github.niestrat99.advancedteleport.commands.SpawnATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.Spawn;
import org.bukkit.Bukkit;
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
        if (!canProceed(sender)) return true;
        String removingSpawn = "";
        if (args.length == 0) {
            if (sender instanceof Player) {
                removingSpawn = ((Player) sender).getWorld().getName();
            } else {
                CustomMessages.sendMessage(sender, "Error.removeSpawnNoArgs");
                return true;
            }
        }

        if (args.length > 0) {
            removingSpawn = args[0];
        }
        if (!Spawn.get().doesSpawnExist(removingSpawn)) {
            CustomMessages.sendMessage(sender, "Error.noSuchSpawn", "{spawn}", removingSpawn);
            return true;
        }

        SpawnRemoveEvent event = new SpawnRemoveEvent(removingSpawn, sender);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            // Could not cancel event at this time
            return true;
        }

        String finalRemovingSpawn = removingSpawn;
        AdvancedTeleportAPI.removeSpawn(removingSpawn, sender).thenAcceptAsync(result ->
                CustomMessages.sendMessage(sender, "Info.removedSpawn", "{spawn}", finalRemovingSpawn));
        return false;
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
            StringUtil.copyPartialMatches(args[0], Spawn.get().getSpawns(), spawns);
            return spawns;
        }
        return null;
    }
}
