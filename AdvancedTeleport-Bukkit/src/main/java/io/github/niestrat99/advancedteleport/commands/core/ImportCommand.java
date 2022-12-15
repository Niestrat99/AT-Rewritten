package io.github.niestrat99.advancedteleport.commands.core;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.commands.SubATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.hooks.ImportExportPlugin;
import io.github.niestrat99.advancedteleport.managers.PluginHookManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ImportCommand extends SubATCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length > 0) {
            String pluginStr = args[0].toLowerCase();
            ImportExportPlugin plugin = PluginHookManager.get().getImportPlugin(pluginStr);
            if (plugin == null) {
                CustomMessages.sendMessage(sender, "Error.noSuchPlugin");
                return true;
            }
            if (!plugin.canImport()) {
                CustomMessages.sendMessage(sender, "Error.cantImport", "{plugin}", args[0]);
                return true;
            }
            if (args.length > 1) {
                CustomMessages.sendMessage(sender, "Info.importStarted", "{plugin}", args[0]);
                Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), () -> {
                    switch (args[1].toLowerCase()) {
                        case "homes" -> plugin.importHomes();
                        case "warps" -> plugin.importWarps();
                        case "lastlocs" -> plugin.importLastLocations();
                        case "spawns" -> plugin.importSpawn();
                        case "players" -> plugin.importPlayerInformation();
                        default -> plugin.importAll();
                    }
                    CustomMessages.sendMessage(sender, "Info.importFinished", "{plugin}", args[0]);
                });

            } else {
                CustomMessages.sendMessage(sender, "Info.importStarted", "{plugin}", args[0]);
                Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), () -> {
                    plugin.importAll();
                    CustomMessages.sendMessage(sender, "Info.importFinished", "{plugin}", args[0]);
                });
            }

        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        List<String> results = new ArrayList<>();
        List<String> possibilities = new ArrayList<>();
        if (args.length == 1) {
            possibilities.addAll(PluginHookManager.get().getImportPlugins().keySet());
        }
        if (args.length == 2) {
            possibilities.addAll(Arrays.asList("all", "homes", "lastlocs", "warps", "spawns", "players"));
        }
        StringUtil.copyPartialMatches(args[args.length - 1], possibilities, results);
        return results;
    }
}
