package io.github.niestrat99.advancedteleport.commands.spawn;

import io.github.niestrat99.advancedteleport.api.AdvancedTeleportAPI;
import io.github.niestrat99.advancedteleport.commands.PlayerCommand;
import io.github.niestrat99.advancedteleport.commands.SpawnATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class SetSpawn extends SpawnATCommand implements PlayerCommand {

    @Override
    public boolean onCommand(
        @NotNull final CommandSender sender,
        @NotNull final Command command,
        @NotNull final String s,
        @NotNull final String[] args
    ) {
        if (!canProceed(sender)) return true;

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
        String finalName = name;
        String finalMessage = message;
        AdvancedTeleportAPI.setSpawn(name, player, player.getLocation()).thenAcceptAsync(result ->
                CustomMessages.sendMessage(sender, finalMessage, "spawn", finalName));
        return true;
    }

    @Override
    public @NotNull String getPermission() {
        return "at.admin.setspawn";
    }
}
