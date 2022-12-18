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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ExportCommand extends SubATCommand {

    @Override
    public boolean onCommand(
        @NotNull final CommandSender sender,
        @NotNull final Command command,
        @NotNull final String s,
        @NotNull final String[] args
    ) {
        if (args.length == 0) {
            return false;
        }

        final var pluginHook = ImportCommand.getImportExportPlugin(sender, args);
        if (pluginHook == null) return true;

        if (args.length == 1) {
            CustomMessages.sendMessage(sender, "Info.exportStarted", "{plugin}", args[0]);
            Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), () -> {
                pluginHook.exportAll();
                CustomMessages.sendMessage(sender, "Info.exportFinished", "{plugin}", args[0]);
            });
            return true;
        }

        CustomMessages.sendMessage(sender, "Info.exportStarted", "{plugin}", args[0]);
        Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), () -> {
            switch (args[1].toLowerCase()) {
                case "homes" -> pluginHook.exportHomes();
                case "warps" -> pluginHook.exportWarps();
                case "lastlocs" -> pluginHook.exportLastLocations();
                case "spawns" -> pluginHook.exportSpawn();
                case "players" -> pluginHook.exportPlayerInformation();
                default -> pluginHook.exportAll();
            }
            CustomMessages.sendMessage(sender, "Info.exportFinished", "{plugin}", args[0]);
        });

        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(
        @NotNull final CommandSender sender,
        @NotNull final Command command,
        @NotNull final String s,
        @NotNull final String[] args
    ) {
        final var possibilities = new ArrayList<String>();

        if (args.length == 1) {
            possibilities.addAll(PluginHookManager.get()
                .getPluginHooks(ImportExportPlugin.class, true)
                .collect(ArrayList::new, (list, plugin) -> list.add(plugin.pluginName()), ArrayList::addAll)
            );
        }

        if (args.length == 2) {
            possibilities.addAll(Arrays.asList("all", "homes", "lastlocs", "warps", "spawns", "players"));
        }

        return StringUtil.copyPartialMatches(args[args.length - 1], possibilities, new ArrayList<>());
    }
}
