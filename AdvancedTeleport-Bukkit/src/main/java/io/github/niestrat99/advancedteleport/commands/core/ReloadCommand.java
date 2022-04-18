package io.github.niestrat99.advancedteleport.commands.core;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.commands.SubATCommand;
import io.github.niestrat99.advancedteleport.config.*;
import io.github.niestrat99.advancedteleport.managers.CommandManager;
import io.github.niestrat99.advancedteleport.managers.CooldownManager;
import io.github.niestrat99.advancedteleport.utilities.RandomCoords;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ReloadCommand implements SubATCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s,
                             @NotNull String[] args) {
        CustomMessages.sendMessage(sender, "Info.reloadingConfig");
        for (ATConfig config : Arrays.asList(NewConfig.get(), CustomMessages.config, Spawn.get(), GUI.get())) {
            try {
                config.reload();
            } catch (IOException ex) {
                CoreClass.getInstance().getLogger().warning("Failed to load " + config.getFile().getName() + ": " + ex.getMessage());
                ex.printStackTrace();
            }
        }
        RandomCoords.reload();
        CooldownManager.init();
        CommandManager.registerCommands();
        CustomMessages.sendMessage(sender, "Info.reloadedConfig");
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return null;
    }
}
