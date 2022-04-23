package io.github.niestrat99.advancedteleport.commands.teleport;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.commands.TeleportATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.utilities.nbt.NBTReader;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TpOffline extends TeleportATCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s,
                             @NotNull String[] args) {
        if (!canProceed(sender)) return true;
        if (!(sender instanceof Player)) {
            CustomMessages.sendMessage(sender, "Error.notAPlayer");
            return true;
        }
        if (args.length == 0) {
            CustomMessages.sendMessage(sender, "Error.noPlayerInput");
            return true;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target != null) {
            Bukkit.getServer().dispatchCommand(sender, "tpo " + args[0]);
            return true;
        }
        NBTReader.getLocation(args[0], new NBTReader.NBTCallback<Location>() {
            @Override
            public void onSuccess(Location data) {
                Bukkit.getScheduler().runTask(CoreClass.getInstance(), () -> {
                    PaperLib.teleportAsync((Player) sender, data);
                    CustomMessages.sendMessage(sender, "Teleport.teleportedToOfflinePlayer", "{player}", args[0]);
                });
            }

            @Override
            public void onFail(String message) {
                sender.sendMessage(message);
            }
        });
        return true;

    }

    @Override
    public String getPermission() {
        return "at.admin.tpoffline";
    }
}
