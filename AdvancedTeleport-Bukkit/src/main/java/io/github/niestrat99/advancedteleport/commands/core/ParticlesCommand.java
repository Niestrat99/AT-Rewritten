package io.github.niestrat99.advancedteleport.commands.core;

import io.github.niestrat99.advancedteleport.commands.SubATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.hooks.ParticlesPlugin;
import io.github.niestrat99.advancedteleport.managers.ParticleManager;
import io.github.niestrat99.advancedteleport.managers.PluginHookManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;

public final class ParticlesCommand extends SubATCommand {

    private final HashSet<String> types =  new HashSet<>(Arrays.asList("home", "tpa", "tpahere", "tpr", "warp", "spawn", "back"));

    @Override
    public boolean onCommand(
        @NotNull final CommandSender sender,
        @NotNull final Command command,
        @NotNull final String s,
        @NotNull final String[] args
) {
        if (!NewConfig.get().USE_PARTICLES.get()) {
            CustomMessages.sendMessage(sender, "Error.featureDisabled");
            return true;
        }

        if (PluginHookManager.get().getPluginHooks(ParticlesPlugin.class).findAny().isEmpty()) {
            CustomMessages.sendMessage(sender, "Error.noParticlePlugins");
            return true;
        }

        if (!(sender instanceof Player player)) {
            CustomMessages.sendMessage(sender, "Error.notAPlayer");
            return true;
        }

        var data = ParticleManager.getData(player);

        if (args.length == 0) {
            if (data == null) data = "";
            NewConfig.get().set("default-waiting-particles", data);
            CustomMessages.sendMessage(sender, "Info.defaultParticlesUpdated");
        } else {
            if (data == null) data = "default";
            String type = args[0];
            if (!types.contains(type)) {
                return false;
            }
            NewConfig.get().set("waiting-particles." + type, data);
            CustomMessages.sendMessage(sender, "Info.specificParticlesUpdated", "{type}", type);
        }

        try {
            NewConfig.get().save();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    @Contract(pure = true)
    public @NotNull List<String> onTabComplete(
        @NotNull final CommandSender sender,
        @NotNull final Command command,
        @NotNull final String s,
        @NotNull final String[] args
    ) {
        if (args.length != 1) return Collections.emptyList();

        return StringUtil.copyPartialMatches(args[0], types, new ArrayList<>());
    }

}
