package io.github.niestrat99.advancedteleport.commands.core;

import io.github.niestrat99.advancedteleport.commands.SubATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.hooks.ImportExportPlugin;
import io.github.niestrat99.advancedteleport.hooks.PluginHook;
import io.github.niestrat99.advancedteleport.managers.PluginHookManager;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ImportCommand extends SubATCommand {

    static @Nullable ImportExportPlugin<?, ?> getImportExportPlugin(
            @NotNull final CommandSender sender, @NotNull final String @NotNull [] args) {
        String pluginStr = args[0].toLowerCase();
        final var plugin =
                PluginHookManager.get().getPluginHook(pluginStr, ImportExportPlugin.class);

        if (plugin == null) {
            CustomMessages.sendMessage(sender, "Error.noSuchPlugin");
            return null;
        }

        if (!plugin.canImport()) {
            CustomMessages.sendMessage(
                    sender, "Error.cantImport", Placeholder.unparsed("plugin", args[0]));
            return null;
        }

        return plugin;
    }

    @Override
    public boolean onCommand(
            @NotNull final CommandSender sender,
            @NotNull final Command command,
            @NotNull final String s,
            @NotNull final String[] args) {
        if (args.length == 0) {
            CustomMessages.sendMessage(sender, "Error.noPluginSpecified");
            return true;
        }

        final var pluginHook = getImportExportPlugin(sender, args);
        if (pluginHook == null) {
            CustomMessages.sendMessage(sender, "Error.noSuchPlugin");
            return true;
        }

        final var arg = args.length == 1 ? "all" : args[1].toLowerCase();
        CustomMessages.sendMessage(sender, "Info.importStarted", Placeholder.unparsed("plugin", args[0]));
        switch (arg) {
            case "homes" -> pluginHook.importHomes();
            case "warps" -> pluginHook.importWarps();
            case "lastlocs" -> pluginHook.importLastLocations();
            case "spawns" -> pluginHook.importSpawn();
            case "players" -> pluginHook.importPlayerInformation();
            case "all" -> pluginHook.importAll();
            default -> {
                CustomMessages.sendMessage(sender, "Error.invalidOption");
                return false;
            }
        }
        CustomMessages.sendMessage(
                sender,
                "Info.importFinished",
                Placeholder.unparsed("plugin", args[0]));
        return true;

    }

    @Override
    public @NotNull List<String> onTabComplete(
            @NotNull final CommandSender sender,
            @NotNull final Command command,
            @NotNull final String s,
            @NotNull final String[] args) {
        List<String> possibilities = new ArrayList<>();

        if (args.length == 1) {
            possibilities.addAll(
                    PluginHookManager.get()
                            .getPluginHooks(ImportExportPlugin.class)
                            .map(PluginHook::pluginName)
                            .toList());
        }

        if (args.length == 2) {
            possibilities.addAll(
                    Arrays.asList("all", "homes", "lastlocs", "warps", "spawns", "players"));
        }

        return StringUtil.copyPartialMatches(
                args[args.length - 1], possibilities, new ArrayList<>());
    }
}
