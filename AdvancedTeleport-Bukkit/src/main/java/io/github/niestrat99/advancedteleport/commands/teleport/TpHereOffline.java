package io.github.niestrat99.advancedteleport.commands.teleport;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.commands.ATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.utilities.nbt.NBTReader;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TpHereOffline implements ATCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            CustomMessages.sendMessage(sender, "Error.notAPlayer");
            return true;
        }
        if (!NewConfig.get().USE_BASIC_TELEPORT_FEATURES.get()) {
            CustomMessages.sendMessage(sender, "Error.featureDisabled");
            return true;
        }

        if (args.length == 0) {
            CustomMessages.sendMessage(sender, "Error.noPlayerInput");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        Player player = (Player) sender;
        if (target != null) {
            Bukkit.getServer().dispatchCommand(sender, "tpohere " + args[0]);
            return true;
        }

        NBTReader.setLocation(args[0], player.getLocation(), new NBTReader.NBTCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean data) {
                Bukkit.getScheduler().runTask(CoreClass.getInstance(), () -> CustomMessages.sendMessage(sender, "Teleport.teleportedOfflinePlayerHere", "{player}", args[0]));
            }

            @Override
            public void onFail(String message) {
                sender.sendMessage(message);
            }
        });
        return true;
    }
}
