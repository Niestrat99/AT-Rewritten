package io.github.niestrat99.advancedteleport.commands.spawn;

import io.github.niestrat99.advancedteleport.commands.SpawnATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.Spawn;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RemoveSpawn extends SpawnATCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s,
                             @NotNull String[] args) {
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
        CustomMessages.sendMessage(sender, Spawn.get().removeSpawn(removingSpawn), "{spawn}", removingSpawn);
        return false;
    }

    @Override
    public String getPermission() {
        return "at.admin.removespawn";
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s,
                                      @NotNull String[] args) {
        if (sender.hasPermission("at.admin.removespawn") && sender instanceof Player && args.length == 1) {
            List<String> spawns = new ArrayList<>();
            StringUtil.copyPartialMatches(args[0], Spawn.get().getSpawns(), spawns);
            return spawns;
        }
        return null;
    }
}
