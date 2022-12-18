package io.github.niestrat99.advancedteleport.commands.core;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.commands.SubATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.hooks.ImportExportPlugin;
import io.github.niestrat99.advancedteleport.hooks.PluginHook;
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

        // If there's no arguments contained, stop there
        if (args.length == 0) {
            // TODO - send message
            return true;
        }

        // Attempt to get the plugin
        final var pluginHook = getImportExportPlugin(sender, args);
        if (pluginHook == null) {
            CustomMessages.sendMessage(sender, "Error.noSuchPlugin");
            return true;
        }

        // If the plugin is unable to import/export data, let the player know
        if (!pluginHook.canImport()) {
            CustomMessages.sendMessage(sender, "Error.cantImport", "{plugin}", args[0]);
            return true;
        }

        // If only the plugin was specified, import everything
        if (args.length == 1) {
            CustomMessages.sendMessage(sender, "Info.importStarted", "{plugin}", args[0]);

            // Import it asynchronously
            Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), () -> {
                pluginHook.importAll();
                CustomMessages.sendMessage(sender, "Info.importFinished", "{plugin}", args[0]);
            });

            return true;
        }

        // Start the import with the specified section.
        CustomMessages.sendMessage(sender, "Info.importStarted", "{plugin}", args[0]);
        Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), () -> {
            switch (args[1].toLowerCase()) {
                case "homes" -> pluginHook.importHomes();
                case "warps" -> pluginHook.importWarps();
                case "lastlocs" -> pluginHook.importLastLocations();
                case "spawns" -> pluginHook.importSpawn();
                case "players" -> pluginHook.importPlayerInformation();
                default -> pluginHook.importAll();
            }
            CustomMessages.sendMessage(sender, "Info.importFinished", "{plugin}", args[0]);
        });

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        List<String> results = new ArrayList<>();
        List<String> possibilities = new ArrayList<>();
        if (args.length == 1) {
            possibilities.addAll(PluginHookManager.get().getPluginHooks(ImportExportPlugin.class).map(PluginHook::pluginName).toList());
        }
        if (args.length == 2) {
            possibilities.addAll(Arrays.asList("all", "homes", "lastlocs", "warps", "spawns", "players"));
        }
        StringUtil.copyPartialMatches(args[args.length - 1], possibilities, results);
        return results;
    }

    static @Nullable ImportExportPlugin getImportExportPlugin(
        @NotNull final CommandSender sender,
        @NotNull final String @NotNull [] args
    ) {
        String pluginStr = args[0].toLowerCase();
        final var plugin = PluginHookManager.get().getPluginHook(pluginStr, ImportExportPlugin.class);

        if (plugin == null) {
            CustomMessages.sendMessage(sender, "Error.noSuchPlugin");
            return null;
        }

        if (!plugin.canImport()) {
            CustomMessages.sendMessage(sender, "Error.cantImport", "{plugin}", args[0]);
            return null;
        }

        return plugin;
    }
}
