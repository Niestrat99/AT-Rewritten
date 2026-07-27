package io.github.niestrat99.advancedteleport.commands.teleport;

import io.github.niestrat99.advancedteleport.commands.PlayerCommand;
import io.github.niestrat99.advancedteleport.commands.TeleportATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.papermc.lib.PaperLib;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class TpOffline extends TeleportATCommand implements PlayerCommand {

    @Override
    public boolean onCommand(
            @NotNull final CommandSender sender,
            @NotNull final Command command,
            @NotNull final String s,
            @NotNull final String[] args) {
        if (!canProceed(sender)) return true;
        if (args.length == 0) {
            CustomMessages.sendMessage(sender, "Error.noPlayerInput");
            return false;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target != null) {
            Bukkit.getServer().dispatchCommand(sender, "tpo " + args[0]);
            return true;
        }

        OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
        Location location = player.getLocation();
        if (location == null) {
            CustomMessages.sendMessage(sender,
                            "Error.noOfflineLocation",
                            Placeholder.unparsed("player", args[0]));
            return true;
        }

        PaperLib.teleportAsync((Player) sender, location);  // TODO use proper method
        CustomMessages.sendMessage(
                sender,
                "Teleport.teleportedToOfflinePlayer",
                Placeholder.unparsed("player", args[0]));
        return true;
    }

    @Override
    public @NotNull String getPermission() {
        return "at.admin.tpoffline";
    }
}
