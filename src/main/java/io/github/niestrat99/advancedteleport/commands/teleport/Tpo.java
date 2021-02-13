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
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

public class Tpo implements ATCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (NewConfig.get().USE_BASIC_TELEPORT_FEATURES.get()) {
            if (sender.hasPermission("at.admin.tpo")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if (args.length > 0) {
                        if (args[0].equalsIgnoreCase(player.getName())){
                            CustomMessages.sendMessage(sender, "Error.requestSentToSelf");
                            return true;
                        }
                        Player target = Bukkit.getPlayer(args[0]);
                        if (target == null) {
                            if (sender.hasPermission("at.admin.tpo.offline")) {
                                NBTReader.getLocation(args[0], new NBTReader.NBTCallback<Location>() {
                                    @Override
                                    public void onSuccess(Location data) {
                                        Bukkit.getScheduler().runTask(CoreClass.getInstance(), () -> {
                                            PaperLib.teleportAsync(player, data);
                                            sender.sendMessage("Teleported to " + args[0]);
                                        });
                                    }

                                    @Override
                                    public void onFail(String message) {
                                        sender.sendMessage(message);
                                    }
                                });
                                return true;
                            }
                            CustomMessages.sendMessage(sender, "Error.noSuchPlayer");
                        } else {
                            CustomMessages.sendMessage(sender, "Teleport.teleporting", "{player}", target.getName());
                            PaperLib.teleportAsync(player, target.getLocation(), PlayerTeleportEvent.TeleportCause.COMMAND);
                        }
                        return true;
                    } else {
                        CustomMessages.sendMessage(sender, "Error.noPlayerInput");
                    }
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
}
