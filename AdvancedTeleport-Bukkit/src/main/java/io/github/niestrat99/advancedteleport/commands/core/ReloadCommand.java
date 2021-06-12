package io.github.niestrat99.advancedteleport.commands.core;

import io.github.niestrat99.advancedteleport.commands.SubATCommand;
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

import java.util.List;

public class ReloadCommand implements SubATCommand {

    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        CustomMessages.sendMessage(sender, "Info.reloadingConfig");
        NewConfig.get().reload();
        CustomMessages.config.reload();
        Spawn.get().reload();
        GUI.get().reload();
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
