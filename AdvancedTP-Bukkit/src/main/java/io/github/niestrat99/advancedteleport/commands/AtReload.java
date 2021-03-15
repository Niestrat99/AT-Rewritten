package io.github.niestrat99.advancedteleport.commands;

import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.GUI;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.config.Spawn;
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
    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!sender.hasPermission("at.admin.reload")) {
            CustomMessages.sendMessage(sender, "Error.noPermission");
        } else {
            CustomMessages.sendMessage(sender, "Info.reloadingConfig");
            try {
                NewConfig.get().reload();
                CustomMessages.config.reload();
                Spawn.reloadSpawn();
                GUI.reloadConfig();
                CooldownManager.init();
            } catch (IOException e) {
                e.printStackTrace();
            }
            CommandManager.registerCommands();
            CustomMessages.sendMessage(sender, "Info.reloadedConfig");
        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return null;
    }
}
