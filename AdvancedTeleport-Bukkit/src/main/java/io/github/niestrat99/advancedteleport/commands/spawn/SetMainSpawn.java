package io.github.niestrat99.advancedteleport.commands.spawn;

import io.github.niestrat99.advancedteleport.api.AdvancedTeleportAPI;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SetMainSpawn extends SpawnATCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s,
                             @NotNull String[] args) {
        if (!canProceed(sender)) return true;
        if (!NewConfig.get().USE_SPAWN.get()) {
            CustomMessages.sendMessage(sender, "Error.featureDisabled");
            return true;
        }
        if (!sender.hasPermission("at.admin.setmainspawn")) {
            CustomMessages.sendMessage(sender, "Error.noPermission");
            return true;
        }
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

        Location loc = ((Player) sender).getLocation();
        if (!Spawn.get().doesSpawnExist(id)) {
            if (sender.hasPermission("at.admin.setspawn")
                    && (world || sender.hasPermission("at.admin.setspawn.other"))) {
                AdvancedTeleportAPI.setSpawn(id, sender, loc).join();
            } else {
                CustomMessages.sendMessage(sender, "Error.cannotSetMainSpawn");
                return true;
            }
        }
        AdvancedTeleportAPI.setMainSpawn(id, sender).thenAcceptAsync(result ->
                CustomMessages.sendMessage(sender, Spawn.get().setMainSpawn(id, loc), "{spawn}", id));
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
