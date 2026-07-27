package io.github.niestrat99.advancedteleport.commands;

import io.github.niestrat99.advancedteleport.CoreAdvancedTeleport;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.tpoffline.PlayerDataReader;

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
            return false;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            PlayerDataReader.setLocation(
                    args[0],
                    player.getLocation(),
                    new PlayerDataReader.NBTCallback<>() {
                        @Override
                        public void onSuccess(Boolean data) {
                            Bukkit.getScheduler()
                                    .runTask(
                                            CoreAdvancedTeleport.getInstance().getPlugin(),
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
