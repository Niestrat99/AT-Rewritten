package io.github.niestrat99.advancedteleport.commands.spawn;

import io.github.niestrat99.advancedteleport.api.AdvancedTeleportAPI;
import io.github.niestrat99.advancedteleport.commands.SpawnATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MirrorSpawn extends SpawnATCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s,
                             @NotNull String[] args) {
        if (!canProceed(sender)) return true;
        String fromWorld = "";
        String toWorld = "";
        if (args.length == 0) {
            CustomMessages.sendMessage(sender, "Error.mirrorSpawnNoArguments");
            return true;
        }

        if (args.length == 1) {
            toWorld = args[0];
            if (sender instanceof Player) {
                fromWorld = ((Player) sender).getWorld().getName();
            } else {
                CustomMessages.sendMessage(sender, "Error.mirrorSpawnLackOfArguments");
                return false;
            }
        }

        if (args.length >= 2) {
            fromWorld = args[0];
            toWorld = args[1];
        }

        String finalToWorld = toWorld;
        String finalFromWorld = fromWorld;
        AdvancedTeleportAPI.mirrorSpawn(fromWorld, toWorld, sender).handleAsync((v, e) ->
                handleCommandFeedback(e, sender, "Info.mirroredSpawn", "Error.noSpawn", "{spawn}",
                        finalToWorld, "{from}", finalFromWorld));
        return true;
    }

    @Override
    public String getPermission() {
        return "at.admin.mirrorspawn";
    }
}
