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

public class ExportCommand implements SubATCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length > 0) {
            String pluginStr = args[0].toLowerCase();
            ImportExportPlugin plugin = PluginHookManager.get().getImportPlugin(pluginStr);
            if (plugin == null) {
                CustomMessages.sendMessage(sender, "Error.noSuchPlugin");
                return true;
            }
            if (plugin.canImport()) {
                if (args.length > 1) {
                    CustomMessages.sendMessage(sender, "Info.exportStarted", "{plugin}", args[0]);
                    Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), () -> {
                        switch (args[1].toLowerCase()) {
                            case "homes":
                                plugin.exportHomes();
                                break;
                            case "warps":
                                plugin.exportWarps();
                                break;
                            case "lastlocs":
                                plugin.exportLastLocations();
                                break;
                            case "spawns":
                                plugin.exportSpawn();
                                break;
                            case "players":
                                plugin.exportPlayerInformation();
                                break;
                            default:
                                plugin.exportAll();
                                break;
                        }
                        CustomMessages.sendMessage(sender, "Info.exportFinished", "{plugin}", args[0]);
                    });

                } else {
                    CustomMessages.sendMessage(sender, "Info.exportStarted", "{plugin}", args[0]);
                    Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), () -> {
                        plugin.exportAll();
                        CustomMessages.sendMessage(sender, "Info.exportFinished", "{plugin}", args[0]);
                    });
                }
            } else {
                CustomMessages.sendMessage(sender, "Error.cantExport", "{plugin}", args[0]);
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
            possibilities.addAll(PluginHookManager.get().getImportPlugins().keySet());
        }
        if (args.length == 2) {
            possibilities.addAll(Arrays.asList("all", "homes", "lastlocs", "warps", "spawns", "players"));
        }
        StringUtil.copyPartialMatches(args[args.length - 1], possibilities, results);
        return results;
    }
}
