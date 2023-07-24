package io.github.niestrat99.advancedteleport.commands.core;

import io.github.niestrat99.advancedteleport.commands.SubATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.MainConfig;
import io.github.niestrat99.advancedteleport.hooks.ParticlesPlugin;
import io.github.niestrat99.advancedteleport.managers.ParticleManager;
import io.github.niestrat99.advancedteleport.managers.PluginHookManager;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public final class ParticlesCommand extends SubATCommand {

    private final HashSet<String> types =
            new HashSet<>(Arrays.asList("home", "tpa", "tpahere", "tpr", "warp", "spawn", "back"));

    @Override
    public boolean onCommand(
            @NotNull final CommandSender sender,
            @NotNull final Command command,
            @NotNull final String s,
            @NotNull final String[] args) {

        // If particles aren't enabled, stop there.
        if (!MainConfig.get().USE_PARTICLES.get()) {
            CustomMessages.sendMessage(sender, "Error.featureDisabled");
            return true;
        }

        // If there aren't any particle plugins to use, stop there.
        if (PluginHookManager.get().getPluginHooks(ParticlesPlugin.class).findAny().isEmpty()) {
            CustomMessages.sendMessage(sender, "Error.noParticlePlugins");
            return true;
        }

        // If the sender isn't a player, stop there.
        if (!(sender instanceof Player player)) {
            CustomMessages.sendMessage(sender, "Error.notAPlayer");
            return true;
        }

        // Get the player's particle data.
        var data = ParticleManager.getData(player);

        // If no arguments have been specified, get the player's current particles and set it to the
        // default particles.
        if (args.length == 0) {
            if (data == null) data = "";
            MainConfig.get().set("default-waiting-particles", data);
            CustomMessages.sendMessage(sender, "Info.defaultParticlesUpdated");
        } else {

            // Otherwise, set it for the specific command type
            if (data == null) data = "default";
            String type = args[0];
            if (!types.contains(type)) {
                return false;
            }
            MainConfig.get().set("waiting-particles." + type, data);
            CustomMessages.sendMessage(
                    sender, "Info.specificParticlesUpdated", Placeholder.unparsed("type", type));
        }

        // Then save the config.
        try {
            MainConfig.get().save();
        } catch (Exception e) {
            e.printStackTrace(); // TODO - let's handle this more gracefully
        }

        return true;
    }

    @Override
    @Contract(pure = true)
    public @NotNull List<String> onTabComplete(
            @NotNull final CommandSender sender,
            @NotNull final Command command,
            @NotNull final String s,
            @NotNull final String[] args) {
        if (args.length != 1) return Collections.emptyList();

        return StringUtil.copyPartialMatches(args[0], types, new ArrayList<>());
    }
}
