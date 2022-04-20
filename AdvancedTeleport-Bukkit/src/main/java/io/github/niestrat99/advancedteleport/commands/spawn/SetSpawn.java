package io.github.niestrat99.advancedteleport.commands.spawn;

import io.github.niestrat99.advancedteleport.commands.SpawnATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.config.Spawn;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SetSpawn extends SpawnATCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (NewConfig.get().USE_SPAWN.get()) {
            if (sender.hasPermission("at.admin.setspawn")){
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    String name = player.getWorld().getName();
                    String message = "Info.setSpawn";
                    if (args.length > 0 && sender.hasPermission("at.admin.setspawn.other")) {
                        if (!args[0].matches("^[0-9a-zA-Z_\\-]+$")) {
                            CustomMessages.sendMessage(sender, "Error.nonAlphanumericSpawn");
                            return false;
                        }
                        name = args[0];
                        message = "Info.setSpawnSpecial";
                    }
                    Spawn.get().setSpawn(player.getLocation(), name);
                    CustomMessages.sendMessage(sender, message, "{spawn}", name);
                } else {
                    CustomMessages.sendMessage(sender, "Error.notAPlayer");
                }
            } else {
                CustomMessages.sendMessage(sender, "Error.noPermission");
            }
        } else {
            CustomMessages.sendMessage(sender, "Error.featureDisabled");
        }
        return true;
    }

    @Override
    public String getPermission() {
        return "at.admin.setspawn";
    }
}
