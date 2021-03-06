package io.github.niestrat99.advancedteleport.commands.teleport;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.commands.ATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.utilities.nbt.NBTReader;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TpOffline implements ATCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (NewConfig.get().USE_BASIC_TELEPORT_FEATURES.get()) {
            if (sender instanceof Player) {
                if (args.length > 0) {
                    Player target = Bukkit.getPlayer(args[0]);
                    if (target == null) {
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
                    } else {
                        Bukkit.getServer().dispatchCommand(sender, "tpo " + args[0]);
                    }
                } else {
                    CustomMessages.sendMessage(sender, "Error.noPlayerInput");
                }
            } else {
                CustomMessages.sendMessage(sender, "Error.notAPlayer");
            }
        } else {
            CustomMessages.sendMessage(sender, "Error.featureDisabled");
        }

        return true;
    }
}
