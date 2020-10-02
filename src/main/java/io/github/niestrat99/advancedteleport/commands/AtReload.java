package io.github.niestrat99.advancedteleport.commands;

import io.github.niestrat99.advancedteleport.config.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.io.IOException;

public class AtReload implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!sender.hasPermission("at.admin.reload")) {
            sender.sendMessage(CustomMessages.getString("Error.noPermission"));
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
                GUI.reloadConfig();
            } catch (IOException e) {
                e.printStackTrace();
            }
            sender.sendMessage(CustomMessages.getString("Info.reloadedConfig"));
        }
        return true;
    }
}
