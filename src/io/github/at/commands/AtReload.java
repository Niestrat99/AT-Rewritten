package io.github.at.commands;

import io.github.at.config.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.io.IOException;

public class AtReload implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!sender.hasPermission("at.admin.reload")) {
            sender.sendMessage(CustomMessages.getString("Error.noPermission"));
            return false;
        } else {
            sender.sendMessage(CustomMessages.getString("Info.reloadingConfig"));
            try {
                Config.reloadConfig();
                CustomMessages.reloadConfig();
                Warps.reloadWarps();
                Homes.reloadHomes();
                LastLocations.reloadBackLocations();
                TpBlock.reloadBlocks();
                Spawn.reloadSpawn();
            } catch (IOException e) {
                e.printStackTrace();
            }
            sender.sendMessage(CustomMessages.getString("Info.reloadedConfig"));
        }
        return false;
    }
}
