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

public class SetSpawn implements ATCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, String[] args) {

        if (!(sender instanceof Player)) {
            CustomMessages.sendMessage(sender, "Error.notAPlayer");
            return true;
        }
        if (!NewConfig.get().USE_SPAWN.get()) {
            CustomMessages.sendMessage(sender, "Error.featureDisabled");
            return true;
        }
        if (!sender.hasPermission("at.admin.setspawn")) {
            CustomMessages.sendMessage(sender, "Error.noPermission");
            return true;
        }

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
        try {
            Spawn.get().setSpawn(player.getLocation(), name);
            CustomMessages.sendMessage(sender, message, "{spawn}", name);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}
