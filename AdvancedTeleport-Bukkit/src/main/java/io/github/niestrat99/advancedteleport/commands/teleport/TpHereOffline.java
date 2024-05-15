package io.github.niestrat99.advancedteleport.commands.teleport;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.commands.PlayerCommand;
import io.github.niestrat99.advancedteleport.commands.TeleportATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.utilities.nbt.NBTReader;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class TpHereOffline extends TeleportATCommand implements PlayerCommand {

    @Override
    public boolean onCommand(
            @NotNull final CommandSender sender,
            @NotNull final Command command,
            @NotNull final String s,
            @NotNull final String[] args) {
        if (!canProceed(sender)) return true;

        Player player = (Player) sender;

        if (args.length == 0) {
            CustomMessages.sendMessage(sender, "Error.noPlayerInput");
            return true;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            NBTReader.setLocation(
                    args[0],
                    player.getLocation(),
                    new NBTReader.NBTCallback<>() {
                        @Override
                        public void onSuccess(Boolean data) {
                            Bukkit.getScheduler()
                                    .runTask(
                                            CoreClass.getInstance(),
                                            () ->
                                                    CustomMessages.sendMessage(
                                                            sender,
                                                            "Teleport.teleportedOfflinePlayerHere",
                                                            Placeholder.unparsed(
                                                                    "player", args[0])));
                        }

                        @Override
                        public void onFail(@NotNull final Component message) {
                            CustomMessages.sendMessage(sender, message);
                        }
                    });
            return true;
        } else {
            Bukkit.getServer().dispatchCommand(sender, "tpohere " + args[0]);
        }
        return true;
    }

    @Override
    public @NotNull String getPermission() {
        return "at.admin.tpofflinehere";
    }
}
