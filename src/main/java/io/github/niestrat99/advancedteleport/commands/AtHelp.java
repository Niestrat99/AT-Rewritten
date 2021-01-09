package io.github.niestrat99.advancedteleport.commands;

import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class AtHelp implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("at.member.help")) {
            if (args.length == 0) {
                for (String str : CustomMessages.Config.getStringList("Help.mainHelp")) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', str));
                }
                if (sender.hasPermission("at.admin.help")) {
                    for (String str : CustomMessages.Config.getStringList("Help.mainHelpAdmin")) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', str));
                    }
                }
                return true;
            } else if (args[0].equalsIgnoreCase("teleport")) {
                if (NewConfig.getInstance().USE_BASIC_TELEPORT_FEATURES.get()) {
                    for (String str : CustomMessages.Config.getStringList("Help.teleport")) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', str));
                    }
                    if (sender.hasPermission("at.admin.help")) {
                        for (String str : CustomMessages.Config.getStringList("Help.teleportAdmin")) {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', str));
                        }
                        return true;
                    }
                } else {
                    sender.sendMessage(CustomMessages.getString("Error.featureDisabled"));
                    return true;
                }
            } else if (args[0].equalsIgnoreCase("warps")) {
                if (NewConfig.getInstance().USE_WARPS.get()) {
                    for (String str : CustomMessages.Config.getStringList("Help.warps")) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', str));
                    }
                    if (sender.hasPermission("at.admin.help")) {
                        for (String str : CustomMessages.Config.getStringList("Help.warpsAdmin")) {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', str));
                        }
                        return true;
                    }
                } else {
                    sender.sendMessage(CustomMessages.getString("Error.featureDisabled"));
                    return true;
                }
            } else if (args[0].equalsIgnoreCase("Spawn")) {
                if (NewConfig.getInstance().USE_SPAWN.get()) {
                    for (String str : CustomMessages.Config.getStringList("Help.spawn")) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', str));
                    }
                    if (sender.hasPermission("at.admin.help")) {
                        for (String str : CustomMessages.Config.getStringList("Help.spawnAdmin")) {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', str));
                        }
                        return true;
                    }
                } else {
                    sender.sendMessage(CustomMessages.getString("Error.featureDisabled"));
                    return true;
                }
            } else if (args[0].equalsIgnoreCase("RandomTP")) {
                if (NewConfig.getInstance().USE_RANDOMTP.get()) {
                    for (String str : CustomMessages.Config.getStringList("Help.randomTP")) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', str));
                    }
                } else {
                    sender.sendMessage(CustomMessages.getString("Error.featureDisabled"));
                }
                return true;
            } else if (args[0].equalsIgnoreCase("homes")) {
                if (NewConfig.getInstance().USE_HOMES.get()) {
                    for (String str : CustomMessages.Config.getStringList("Help.homes")) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', str));
                    }
                    if (sender.hasPermission("at.admin.help")) {
                        for (String str : CustomMessages.Config.getStringList("Help.homesAdmin")) {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', str));
                        }
                        return true;
                    }
                } else {
                    sender.sendMessage(CustomMessages.getString("Error.featureDisabled"));
                    return true;
                }
            } else if (args[0].equalsIgnoreCase("Admin")) {
                if (sender.hasPermission("at.admin.help")) {
                    for (String str : CustomMessages.Config.getStringList("Help.admin")) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', str));
                    }
                }
                return true;
            }
        }
        return true;
    }
}
