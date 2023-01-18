package io.github.niestrat99.advancedteleport.commands.core;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.commands.SubATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.managers.RTPManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ClearCacheCommand implements SubATCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        // If the RTP manager isn't set up, stop there
        if (!RTPManager.isInitialised()) {
            CustomMessages.sendMessage(sender, "Error.rtpManagerNotUsed");
            return true;
        }

        // If a world hasn't been specified, clear everything
        if (args.length == 0) {
            RTPManager.clearEverything();

            // Reload the data
            Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), () -> {
                for (World world : Bukkit.getWorlds()) {
                    RTPManager.loadWorldData(world);
                }
            });

            CustomMessages.sendMessage(sender, "Info.clearEverything");
            return true;
        }

        // Get the world
        World world = Bukkit.getWorld(args[0]);

        // If it doesn't exist though, stop them there
        if (world == null) {
            CustomMessages.sendMessage(sender, "Error.noSuchWorld");
            return true;
        }

        // Reset the data
        RTPManager.unloadWorldData(world);
        Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), () -> RTPManager.loadWorldData(world));
        CustomMessages.sendMessage(sender, "Info.clearWorld", "world", args[0]);
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        // If we're on the first argument, set up a list of worlds
        List<String> results = new ArrayList<>();
        if (args.length == 1) {
            List<String> options = new ArrayList<>();
            Bukkit.getWorlds().forEach(world -> options.add(world.getName()));

            StringUtil.copyPartialMatches(args[0], options, results);
        }

        return results;
    }
}
