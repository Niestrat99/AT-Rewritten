package io.github.niestrat99.advancedteleport.commands.spawn;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.AdvancedTeleportAPI;
import io.github.niestrat99.advancedteleport.api.spawn.Spawn;
import io.github.niestrat99.advancedteleport.commands.SpawnATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class MirrorSpawn extends SpawnATCommand {

    @Override
    public boolean onCommand(
        @NotNull final CommandSender sender,
        @NotNull final Command command,
        @NotNull final String s,
        @NotNull final String[] args
    ) {
        if (!canProceed(sender)) return true;

        // Declare the spawns to be used
        final Spawn fromSpawn;
        final Spawn toSpawn;

        // If no argument has been specified, stop there
        if (args.length == 0) {
            CustomMessages.sendMessage(sender, "Error.mirrorSpawnNoArguments");
            return true;
        }

        // If just one spawn has been specified, then use the first argument as the spawn to mirror to.
        if (args.length == 1) {
            toSpawn = getSpawn(args[0]);

            // If the sender is a player, get the spawn that they are coming from
            if (sender instanceof Player player) {
                fromSpawn = AdvancedTeleportAPI.getSpawn(player.getWorld());
            } else {
                CustomMessages.sendMessage(sender, "Error.mirrorSpawnLackOfArguments");
                return false;
            }
        } else {
            fromSpawn = getSpawn(args[0]);
            toSpawn = getSpawn(args[1]);
        }

        // See if the from and to spawns match up
        if (fromSpawn == toSpawn) {
            CustomMessages.sendMessage(sender, "Info.mirrorSpawnSame",
                    Placeholder.unparsed("spawn", args[0]),
                    Placeholder.unparsed("from", (args.length == 1 ? ((Player) sender).getWorld().getName() : args[1])));
            return true;
        }

        fromSpawn.setMirroringSpawn(toSpawn, sender).whenCompleteAsync((v, e) ->
                handleCommandFeedback(e, sender, "Info.mirroredSpawn", "Error.mirrorSpawnFail",
                        Placeholder.unparsed("spawn", v.getMirroringSpawn().getName()),
                        Placeholder.unparsed("from", fromSpawn.getName())
                ), CoreClass.sync);
        return true;
    }

    @Override
    public @NotNull String getPermission() {
        return "at.admin.mirrorspawn";
    }

    private @NotNull Spawn getSpawn(String name) {

        Spawn spawn = AdvancedTeleportAPI.getSpawn(name);

        // If it's null, get the world instead.
        if (spawn == null) {
            World world = Bukkit.getWorld(name);

            // If the world isn't loaded/doesn't exist, stop there
            if (world == null) {
                throw new IllegalArgumentException("World " + name + " does not exist!");
            }

            spawn = AdvancedTeleportAPI.getSpawn(world);
        }

        return spawn;
    }
}
