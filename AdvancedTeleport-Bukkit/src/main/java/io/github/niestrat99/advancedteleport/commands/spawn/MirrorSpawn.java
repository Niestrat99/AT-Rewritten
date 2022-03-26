package io.github.niestrat99.advancedteleport.commands.spawn;

import io.github.niestrat99.advancedteleport.commands.ATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.config.Spawn;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class MirrorSpawn implements ATCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {

        if (!NewConfig.get().USE_SPAWN.get()) {
            CustomMessages.sendMessage(sender, "Error.featureDisabled");
            return true;
        }
        if (!sender.hasPermission("at.admin.mirrorspawn")) {
            CustomMessages.sendMessage(sender, "Error.noPermission");
            return true;
        }

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

        try {
            CustomMessages.sendMessage(sender, Spawn.get().mirrorSpawn(fromWorld, toWorld), "{spawn}", toWorld,
                        "{from}", fromWorld);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;

    }
}
