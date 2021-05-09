package io.github.niestrat99.advancedteleport.commands.core;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.commands.SubATCommand;
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

public class ImportCommand implements SubATCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length > 0) {
            String pluginStr = args[0].toLowerCase();
            ImportExportPlugin plugin = PluginHookManager.getImportPlugin(pluginStr);
            if (plugin == null) {
                sender.sendMessage("No plugin");
                return true;
            }
            if (plugin.canImport()) {
                if (args.length > 1) {
                    sender.sendMessage("Starting import...");
                    Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), () -> {
                        switch (args[1].toLowerCase()) {
                            case "homes":
                                plugin.importHomes();
                                break;
                            case "warps":
                                plugin.importWarps();
                                break;
                            case "lastlocs":
                                plugin.importLastLocations();
                                break;
                            case "spawns":
                                plugin.importSpawn();
                                break;
                            case "players":
                                plugin.importPlayerInformation();
                                break;
                            default:
                                plugin.importAll();
                                break;
                        }
                        sender.sendMessage("Imported everything!");
                    });

                } else {
                    sender.sendMessage("Starting import...");
                    Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), () -> {
                        plugin.importAll();
                        sender.sendMessage("Imported everything!");
                    });
                }
            }
        }
        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        List<String> results = new ArrayList<>();
        List<String> possibilities = new ArrayList<>();
        if (args.length == 1) {
            possibilities.addAll(PluginHookManager.getImportPlugins().keySet());
        }
        if (args.length == 2) {
            possibilities.addAll(Arrays.asList("all", "homes", "lastlocs", "warps", "spawns", "players"));
        }
        StringUtil.copyPartialMatches(args[args.length - 1], possibilities, results);
        return results;
    }
}
