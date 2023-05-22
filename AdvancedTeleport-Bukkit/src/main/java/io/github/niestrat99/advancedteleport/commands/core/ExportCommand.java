package io.github.niestrat99.advancedteleport.commands.core;

import io.github.niestrat99.advancedteleport.commands.SubATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.folia.RunnableManager;
import io.github.niestrat99.advancedteleport.hooks.ImportExportPlugin;
import io.github.niestrat99.advancedteleport.managers.PluginHookManager;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
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
        // If there's no arguments contained, stop there
        if (args.length == 0) {
            CustomMessages.sendMessage(sender, "Error.noPluginSpecified");
            return true;
        }

        // Attempt to get the plugin
        final var pluginHook = ImportCommand.getImportExportPlugin(sender, args);
        if (pluginHook == null) {
            CustomMessages.sendMessage(sender, "Error.noSuchPlugin");
            return true;
        }

        // If the plugin is unable to import/export data, let the player know
        if (!pluginHook.canImport()) {
            CustomMessages.sendMessage(sender, "Error.cantExport", Placeholder.unparsed("plugin", args[0]));
            return true;
        }

        // Start the export with the specified section.
        CustomMessages.sendMessage(sender, "Info.exportStarted", Placeholder.unparsed("plugin", args[0]));
        RunnableManager.setupRunnerAsync(() -> {
            final var arg = args.length == 1 ? "all" : args[1];
            switch (arg.toLowerCase()) {
                case "homes" -> pluginHook.exportHomes();
                case "warps" -> pluginHook.exportWarps();
                case "lastlocs" -> pluginHook.exportLastLocations();
                case "spawns" -> pluginHook.exportSpawn();
                case "players" -> pluginHook.exportPlayerInformation();
                case "all" -> pluginHook.exportAll();
                default -> {
                    CustomMessages.sendMessage(sender, "Error.invalidOption");
                    return;
                }
            }
            CustomMessages.sendMessage(sender, "Info.exportFinished", Placeholder.unparsed("plugin", args[0]));
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
