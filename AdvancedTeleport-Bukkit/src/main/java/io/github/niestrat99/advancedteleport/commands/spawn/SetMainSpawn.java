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

import java.util.ArrayList;
import java.util.List;

public final class SetMainSpawn extends SpawnATCommand {

    @Override
    public boolean onCommand(
        @NotNull final CommandSender sender,
        @NotNull final Command command,
        @NotNull final String s,
        @NotNull final String[] args
    ) {
        if (!canProceed(sender)) return true;
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

            // Set the main spawn
            setMainSpawn(id, sender);
        } else {
            loc = Spawn.get().getSpawn(id);

            // Attempt to set the spawn before setting the main spawn
            AdvancedTeleportAPI.setSpawn(id, sender, loc).handleAsync((v, e) -> {
                if (e != null) {
                    CustomMessages.sendMessage(sender, "Error.setMainSpawnFail", "{spawn}", id);
                    e.printStackTrace();
                    return v;
                }

                // Set the main spawn itself
                setMainSpawn(id, sender);
                return v;
            });
        }
        return true;
    }

    @Override
    public @NotNull String getPermission() {
        return "at.admin.setmainspawn";
    }

    @Override
    public @Nullable List<String> onTabComplete(
        @NotNull final CommandSender sender,
        @NotNull final Command command,
        @NotNull final String s,
        @NotNull final String[] args
    ) {
        if (sender.hasPermission("at.admin.setmainspawn") && sender instanceof Player && args.length == 1) {
            List<String> spawns = new ArrayList<>();
            StringUtil.copyPartialMatches(args[0], Spawn.get().getSpawns(), spawns);
            return spawns;
        }
        return null;
    }

    private void setMainSpawn(String id, CommandSender sender) {
        AdvancedTeleportAPI.setMainSpawn(id, sender).handleAsync((v, e) -> {
            if (e != null) {
                CustomMessages.sendMessage(sender, "Error.setMainSpawnFail", "{spawn}", id);
                e.printStackTrace();
                return v;
            }

            CustomMessages.sendMessage(sender, "Info.setMainSpawn","{spawn}", id);
            return v;
        });
    }
}
