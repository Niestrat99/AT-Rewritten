package io.github.niestrat99.advancedteleport.commands.core;

import io.github.niestrat99.advancedteleport.commands.SubATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.managers.CommandManager;
import io.github.niestrat99.advancedteleport.utilities.PagedLists;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class HelpCommand extends SubATCommand {

    @Override
    public boolean onCommand(
        @NotNull final CommandSender sender,
        @NotNull final Command cmd,
        @NotNull final String s,
        @NotNull String[] args
    ) {

        // Form the list of commands and subcommands to gather.
        List<String> commands = new ArrayList<>();
        List<String> subcommands = new ArrayList<>();
        List<String> possibleCommands = new ArrayList<>();
        int page = 1;

        // Collect all commands.
        if (args.length > 0) {
            switch (args[0].toLowerCase()) {

                // Warps
                case "warps" -> {
                    commands.add("warp");
                    commands.add("warps");
                    commands.add("movewarp");
                    commands.add("setwarp");
                    commands.add("delwarp");
                }

                // Teleportation
                case "teleporting" -> {
                    commands.add("back");
                    commands.add("toggletp");
                    commands.add("tpa");
                    commands.add("tpahere");
                    commands.add("tpall");
                    commands.add("tpalist");
                    commands.add("tpblock");
                    commands.add("tpcancel");
                    commands.add("tploc");
                    commands.add("tpno");
                    commands.add("tpo");
                    commands.add("tpoff");
                    commands.add("tpoffline");
                    commands.add("tpofflinehere");
                    commands.add("tpohere");
                    commands.add("tpon");
                    commands.add("tpr");
                    commands.add("tpunblock");
                    commands.add("tpyes");
                }

                // Core commands
                case "core" -> {
                    subcommands.add("import");
                    subcommands.add("help");
                    subcommands.add("export");
                    subcommands.add("info");
                    subcommands.add("reload");
                    subcommands.add("purge");
                }

                // Home commands
                case "homes" -> {
                    commands.add("delhome");
                    commands.add("home");
                    commands.add("homes");
                    commands.add("movehome");
                    commands.add("sethome");
                    commands.add("setmainhome");
                }

                // Spawn commands
                case "spawns" -> {
                    commands.add("mirrorspawn");
                    commands.add("removespawn");
                    commands.add("setmainspawn");
                    commands.add("setspawn");
                    commands.add("spawn");
                }

                // Anything that just isn't what we expect, such as a number
                default -> {
                    if (args[0].matches("^\\d+$")) {
                        page = Integer.parseInt(args[0]);
                    }
                    commands.addAll(CommandManager.registeredCommands.keySet());
                    subcommands.addAll(CommandManager.subcommands.keySet());
                }
            }

            // If there's a section specified and the second argument is a number, treat it as a page.
            if (args.length > 1 && args[1].matches("^\\d+$")) {
                page = Integer.parseInt(args[1]);
            }
        } else {

            // If no arguments were specified, add everything.
            commands.addAll(CommandManager.registeredCommands.keySet());
            subcommands.addAll(CommandManager.subcommands.keySet());
        }

        // Pick out ones the user has permission to.
        for (String command : commands) {

            // Get the command object in question.
            PluginCommand pluginCommand = CommandManager.registeredCommands.get(command);
            if (pluginCommand == null) continue;

            // If there's no permission(?) or if the player has permission to the command, continue.
            if (pluginCommand.getPermission() != null && !sender.hasPermission(pluginCommand.getPermission())) continue;
            possibleCommands.add(command);
        }

        // Add subcommands that the player has access to.
        for (String subcommand : subcommands) {
            if (!sender.hasPermission("at.member.core." + subcommand)) continue;
            possibleCommands.add("Subcommands." + subcommand);
        }

        // Then create a help menu out of it
        PagedLists<String> commandList = new PagedLists<>(possibleCommands, 7);
        if (page > commandList.getTotalPages()) {
            sender.sendMessage("Invalid page"); // TODO - proper message
            return true;
        }

        // Send the main title
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b・．&7━━━━━━━━━━━ &8❰ §b§lAdvanced Teleport &7" + page + "/" + commandList.getTotalPages() + " &8❱ &7━━━━━━━━━━━&b．・") );

        // Go through each command and format it
        for (String command : commandList.getContentsInPage(page)) {
            String commandStr = CustomMessages.getStringRaw("Usages." + command);

            // If the user is an admin, add some extra usage details.
            if (sender.hasPermission("at.admin." + command)
                    || sender.hasPermission("at.admin." + command + ".other")) {
                String newUsage = CustomMessages.getStringRaw("Usages-Admin." + command);
                if (newUsage != null && !newUsage.isEmpty()) {
                    commandStr = newUsage;
                }
            }

            // Set the description, and send the message.
            String description = CustomMessages.getStringRaw("Descriptions." + command);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&8» &b" + commandStr + " &8~ &7" + description));
        }
        return true;
    }

    @Override
    @Contract(pure = true)
    public @NotNull List<String> onTabComplete(
        @NotNull final CommandSender sender,
        @NotNull final Command command,
        @NotNull final String s,
        @NotNull final String[] args
    ) {
        if (args.length != 1) return List.of();

        return StringUtil.copyPartialMatches(args[0], Arrays.asList("core", "homes", "spawns", "teleporting", "warps"), new ArrayList<>());
    }
}
