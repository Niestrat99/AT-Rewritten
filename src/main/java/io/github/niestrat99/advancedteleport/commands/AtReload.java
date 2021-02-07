package io.github.niestrat99.advancedteleport.commands;

import io.github.niestrat99.advancedteleport.config.*;
import io.github.niestrat99.advancedteleport.managers.CommandManager;
import io.github.niestrat99.advancedteleport.managers.CooldownManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

public class AtReload implements AsyncATCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!sender.hasPermission("at.admin.reload")) {
            sender.sendMessage(CustomMessages.getString("Error.noPermission"));
        } else {
            sender.sendMessage(CustomMessages.getString("Info.reloadingConfig"));
            try {
                NewConfig.getInstance().reload();
                CustomMessages.reloadConfig();
                LastLocations.reloadBackLocations();
                Spawn.reloadSpawn();
                GUI.reloadConfig();
                CooldownManager.init();
            } catch (IOException e) {
                e.printStackTrace();
            }
            CommandManager.registerCommands();
            sender.sendMessage(CustomMessages.getString("Info.reloadedConfig"));
        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return null;
    }
}
