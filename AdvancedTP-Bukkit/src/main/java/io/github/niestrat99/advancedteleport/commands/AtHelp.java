package io.github.niestrat99.advancedteleport.commands;

import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AtHelp implements ATCommand {

    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender.hasPermission("at.member.help")) {
            if (args.length == 0) {
                for (String str : CustomMessages.config.getStringList("Help.mainHelp")) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', str));
                }
                if (sender.hasPermission("at.admin.help")) {
                    for (String str : CustomMessages.config.getStringList("Help.mainHelpAdmin")) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', str));
                    }
                }
                return true;
            } else if (args[0].equalsIgnoreCase("teleport")) {
                if (NewConfig.get().USE_BASIC_TELEPORT_FEATURES.get()) {
                    for (String str : CustomMessages.config.getStringList("Help.teleport")) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', str));
                    }
                    if (sender.hasPermission("at.admin.help")) {
                        for (String str : CustomMessages.config.getStringList("Help.teleportAdmin")) {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', str));
                        }
                        return true;
                    }
                } else {
                    CustomMessages.sendMessage(sender, "Error.featureDisabled");
                    return true;
                }
            } else if (args[0].equalsIgnoreCase("warps")) {
                if (NewConfig.get().USE_WARPS.get()) {
                    for (String str : CustomMessages.config.getStringList("Help.warps")) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', str));
                    }
                    if (sender.hasPermission("at.admin.help")) {
                        for (String str : CustomMessages.config.getStringList("Help.warpsAdmin")) {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', str));
                        }
                        return true;
                    }
                } else {
                    CustomMessages.sendMessage(sender, "Error.featureDisabled");
                    return true;
                }
            } else if (args[0].equalsIgnoreCase("Spawn")) {
                if (NewConfig.get().USE_SPAWN.get()) {
                    for (String str : CustomMessages.config.getStringList("Help.spawn")) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', str));
                    }
                    if (sender.hasPermission("at.admin.help")) {
                        for (String str : CustomMessages.config.getStringList("Help.spawnAdmin")) {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', str));
                        }
                        return true;
                    }
                } else {
                    CustomMessages.sendMessage(sender, "Error.featureDisabled");
                    return true;
                }
            } else if (args[0].equalsIgnoreCase("RandomTP")) {
                if (NewConfig.get().USE_RANDOMTP.get()) {
                    for (String str : CustomMessages.config.getStringList("Help.randomTP")) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', str));
                    }
                } else {
                    CustomMessages.sendMessage(sender, "Error.featureDisabled");
                }
                return true;
            } else if (args[0].equalsIgnoreCase("homes")) {
                if (NewConfig.get().USE_HOMES.get()) {
                    for (String str : CustomMessages.config.getStringList("Help.homes")) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', str));
                    }
                    if (sender.hasPermission("at.admin.help")) {
                        for (String str : CustomMessages.config.getStringList("Help.homesAdmin")) {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', str));
                        }
                        return true;
                    }
                } else {
                    CustomMessages.sendMessage(sender, "Error.featureDisabled");
                    return true;
                }
            } else if (args[0].equalsIgnoreCase("Admin")) {
                if (sender.hasPermission("at.admin.help")) {
                    for (String str : CustomMessages.config.getStringList("Help.admin")) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', str));
                    }
                }
                return true;
            }
        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return null;
    }
}
