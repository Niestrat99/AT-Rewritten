package io.github.niestrat99.advancedteleport.commands.core;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.commands.SubATCommand;
import io.github.niestrat99.advancedteleport.config.*;
import io.github.niestrat99.advancedteleport.managers.CommandManager;
import io.github.niestrat99.advancedteleport.managers.CooldownManager;
import io.github.niestrat99.advancedteleport.utilities.RandomCoords;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public final class ReloadCommand extends SubATCommand {

    @Override
    @Contract("_, _, _, _ -> true")
    public boolean onCommand(
        @NotNull final CommandSender sender,
        @NotNull final Command command,
        @NotNull final String s,
        @NotNull final String[] args
    ) {
        CustomMessages.sendMessage(sender, "Info.reloadingConfig");
        for (ATConfig config : Arrays.asList(MainConfig.get(), CustomMessages.config, SpawnConfig.get(), GUIConfig.get())) {
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

    @Override
    @Contract(value = "_, _, _, _ -> null", pure = true)
    public @Nullable List<String> onTabComplete(
        @NotNull final CommandSender sender,
        @NotNull final Command command,
        @NotNull final String s,
        @NotNull final String[] args
    ) {
        return null;
    }
}
