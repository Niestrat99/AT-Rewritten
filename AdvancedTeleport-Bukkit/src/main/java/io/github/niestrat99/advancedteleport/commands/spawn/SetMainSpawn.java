package io.github.niestrat99.advancedteleport.commands.spawn;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.AdvancedTeleportAPI;
import io.github.niestrat99.advancedteleport.api.spawn.Spawn;
import io.github.niestrat99.advancedteleport.commands.SpawnATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
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

        // If the player can't proceed, stop there
        if (!canProceed(sender)) return true;

        // Note the ID of the spawn and whether it is in a given world
        String id;
        boolean world = true;

        // If an ID has been specified, use that, otherwise, use the player's world
        if (args.length > 0) {
            id = args[0];
            world = false;
        } else if (sender instanceof Player player) {
            id = player.getWorld().getName();
        } else {
            CustomMessages.sendMessage(sender, "Error.cannotSetMainSpawnConsole");
            return true;
        }

        // Get the spawn - if it doesn't exist, see if the admin can set it
        Spawn spawn = AdvancedTeleportAPI.getSpawn(id);
        if (spawn == null) {
            if (sender.hasPermission("at.admin.setspawn")
                && (world || sender.hasPermission("at.admin.setspawn.other"))) {

                // Set it
                Location loc = ((Player) sender).getLocation();
                AdvancedTeleportAPI.setSpawn(id, sender, loc).thenAcceptAsync(newSpawn ->
                        setMainSpawn(newSpawn, sender), CoreClass.sync);
            } else {

                // Otherwise, stop them
                CustomMessages.sendMessage(sender, "Error.cannotSetMainSpawn");
            }
            return true;
        }

        // Set it
        setMainSpawn(spawn, sender);
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
            StringUtil.copyPartialMatches(args[0], AdvancedTeleportAPI.getSpawns().keySet(), spawns);
            return spawns;
        }
        return null;
    }

    private void setMainSpawn(Spawn spawn, CommandSender sender) {
        AdvancedTeleportAPI.setMainSpawn(spawn, sender).whenComplete((v, e) ->
                CustomMessages.failable(sender,
                        "Info.setMainSpawn",
                        "Error.setMainSpawnFail",
                        e,
                        Placeholder.unparsed("spawn", spawn.getName())));
    }
}
