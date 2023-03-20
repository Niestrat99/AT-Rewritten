package io.github.niestrat99.advancedteleport.commands.spawn;

import io.github.niestrat99.advancedteleport.api.AdvancedTeleportAPI;
import io.github.niestrat99.advancedteleport.commands.PlayerCommand;
import io.github.niestrat99.advancedteleport.commands.SpawnATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
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

        // If the sender can't set the spawn, stop there
        if (!canProceed(sender)) return true;

        // Get the player object
        final var player = (Player) sender;

        // Get the name and message to use
        String name = player.getWorld().getName();
        String message = "Info.setSpawn";

        // If the player is an admin, pick out the spawn that they choose
        if (args.length > 0 && sender.hasPermission("at.admin.setspawn.other")) {
            name = args[0];
            message = "Info.setSpawnSpecial";
        }

        // Get the final variables set
        final String finalName = name;
        final String finalMessage = message;

        // Set the spawn
        AdvancedTeleportAPI.setSpawn(name, player, player.getLocation()).whenComplete((v, err) ->
                CustomMessages.failable(sender,
                        finalMessage,
                        "Error.setSpawnFail",
                        err,
                        Placeholder.unparsed("spawn", finalName)));
        return true;
    }

    @Override
    public @NotNull String getPermission() {
        return "at.admin.setspawn";
    }
}
