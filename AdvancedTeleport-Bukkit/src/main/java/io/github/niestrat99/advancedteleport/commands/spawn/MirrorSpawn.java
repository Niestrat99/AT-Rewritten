package io.github.niestrat99.advancedteleport.commands.spawn;

import io.github.niestrat99.advancedteleport.commands.ATCommand;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.config.Spawn;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MirrorSpawn implements ATCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        if (NewConfig.get().USE_SPAWN.get()) {
            if (sender.hasPermission("at.admin.mirrorspawn")) {
                String fromWorld = "";
                String toWorld = "";
                if (args.length == 0) {
                    sender.sendMessage("Insufficient Arguments");
                    return false;
                }

                if (args.length == 1) {
                    toWorld = args[0];
                    if (sender instanceof Player) {
                        fromWorld = ((Player) sender).getWorld().getName();
                    } else {
                        sender.sendMessage("Must be a player");
                        return false;
                    }
                }

                if (args.length >= 2) {
                    fromWorld = args[0];
                    toWorld = args[1];
                }

                Spawn.get().mirrorSpawn(fromWorld, toWorld);
                sender.sendMessage("Mirrored spawns");
                return true;
            }
        }
        return false;
    }
}
