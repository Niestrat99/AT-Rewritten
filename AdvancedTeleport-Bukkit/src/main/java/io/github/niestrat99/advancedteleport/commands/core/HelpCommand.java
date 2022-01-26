package io.github.niestrat99.advancedteleport.commands.core;

import io.github.niestrat99.advancedteleport.commands.SubATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.managers.CommandManager;
import io.github.niestrat99.advancedteleport.utilities.PagedLists;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HelpCommand implements SubATCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        List<String> commands = new ArrayList<>();
        List<String> subcommands = new ArrayList<>();
        List<String> possibleCommands = new ArrayList<>();
        int page = 1;
        // Collect all commands
        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "warps":
                    commands.add("warp");
                    commands.add("warps");
                    commands.add("movewarp");
                    commands.add("setwarp");
                    commands.add("delwarp");
                    break;
                case "teleporting":
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
                    break;
                case "core":
                    subcommands.add("import");
                    subcommands.add("help");
                    subcommands.add("export");
                    subcommands.add("info");
                    subcommands.add("reload");
                    subcommands.add("purge");
                    break;
                case "homes":
                    commands.add("delhome");
                    commands.add("home");
                    commands.add("homes");
                    commands.add("movehome");
                    commands.add("sethome");
                    commands.add("setmainhome");
                    break;
                case "spawns":
                    commands.add("mirrorspawn");
                    commands.add("removespawn");
                    commands.add("setmainspawn");
                    commands.add("setspawn");
                    commands.add("spawn");
                    break;
                default:
                    if (args[0].matches("^[0-9]+$")) {
                        page = Integer.parseInt(args[0]);
                    }
                    commands.addAll(CommandManager.registeredCommands.keySet());
                    subcommands.addAll(CommandManager.subcommands.keySet());
            }

            if (args.length > 1 && args[1].matches("^[0-9]+$")) {
                page = Integer.parseInt(args[1]);
            }
        } else {
            commands.addAll(CommandManager.registeredCommands.keySet());
            subcommands.addAll(CommandManager.subcommands.keySet());
        }
        // Pick out ones the user has permission to
        for (String command : commands) {
            if (sender.hasPermission(CommandManager.registeredCommands.get(command).getPermission())) {
                possibleCommands.add(command);
            }
        }
        for (String subcommand : subcommands) {
            if (sender.hasPermission("at.member.core." + subcommand)) {
                possibleCommands.add("Subcommands." + subcommand);
            }
        }

        // Then create a help menu out of it
        PagedLists<String> commandList = new PagedLists<>(possibleCommands, 7);
        if (page > commandList.getTotalPages()) {
            sender.sendMessage("Invalid page");
            return true;
        }
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b・．&7━━━━━━━━━━━ &8❰ §b§lAdvanced Teleport &7" + page + "/" + commandList.getTotalPages() + " &8❱ &7━━━━━━━━━━━&b．・") );
        for (String command : commandList.getContentsInPage(page)) {
            String commandStr = CustomMessages.getStringA("Usages." + command);
            if (sender.hasPermission("at.admin." + command)
                    || sender.hasPermission("at.admin." + command + ".other")) {
                String newUsage = CustomMessages.getStringA("Usages-Admin." + command);
                if (newUsage != null && !newUsage.isEmpty()) {
                    commandStr = newUsage;
                }
            }
            String description = CustomMessages.getStringA("Descriptions." + command);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&8» &b" + commandStr + " &8~ &7" + description));
        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        List<String> results = new ArrayList<>();
        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], Arrays.asList("core", "homes", "spawns", "teleporting", "warps"), results);
        }
        return results;
    }
}
