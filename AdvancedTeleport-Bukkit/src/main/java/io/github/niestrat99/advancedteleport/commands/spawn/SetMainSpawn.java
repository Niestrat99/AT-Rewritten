package io.github.niestrat99.advancedteleport.commands.spawn;

import io.github.niestrat99.advancedteleport.commands.SpawnATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.Spawn;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SetMainSpawn extends SpawnATCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s,
                             @NotNull String[] args) {
        String id;
        boolean world = true;
        if (args.length > 0) {
            if (!args[0].matches("^[0-9A-Za-z\\-_]+$")) {
                CustomMessages.sendMessage(sender, "Error.nonAlphanumericSpawn");
                return true;
            }
            id = args[0];
            world = false;
        } else if (sender instanceof Player) {
            id = ((Player) sender).getWorld().getName();
        } else {
            CustomMessages.sendMessage(sender, "Error.cannotSetMainSpawnConsole");
            return true;
        }

        Location loc;
        if (!Spawn.get().doesSpawnExist(id)) {
            if (sender instanceof Player) {
                if (sender.hasPermission("at.member.setspawn")
                        && (world || sender.hasPermission("at.member.setspawn.other"))) {
                    loc = ((Player) sender).getLocation();
                    Spawn.get().setSpawn(loc, id);
                } else {
                    CustomMessages.sendMessage(sender, "Error.cannotSetMainSpawn");
                    return true;
                }
            } else {
                CustomMessages.sendMessage(sender, "Error.cannotSetMainSpawnConsole");
                return true;
            }
        } else {
            loc = Spawn.get().getSpawn(id);
        }
        CustomMessages.sendMessage(sender, Spawn.get().setMainSpawn(id, loc), "{spawn}", id);
        return true;
    }

    @Override
    public String getPermission() {
        return "at.admin.setmainspawn";
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s,
                                      @NotNull String[] args) {
        if (sender.hasPermission("at.admin.setmainspawn") && sender instanceof Player && args.length == 1) {
            List<String> spawns = new ArrayList<>();
            StringUtil.copyPartialMatches(args[0], Spawn.get().getSpawns(), spawns);
            return spawns;
        }
        return null;
    }
}
