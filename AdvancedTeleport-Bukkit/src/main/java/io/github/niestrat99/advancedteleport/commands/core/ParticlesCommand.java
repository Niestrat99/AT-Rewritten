package io.github.niestrat99.advancedteleport.commands.core;

import io.github.niestrat99.advancedteleport.commands.SubATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.managers.ParticleManager;
import io.github.niestrat99.advancedteleport.managers.PluginHookManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;

public class ParticlesCommand implements SubATCommand {

    private final HashSet<String> types =  new HashSet<>(Arrays.asList("home", "tpa", "tpahere", "tpr", "warp", "spawn", "back"));

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s,
                             @NotNull String[] args) {

        // If particles aren't enabled, stop there.
        if (!NewConfig.get().USE_PARTICLES.get()) {
            CustomMessages.sendMessage(sender, "Error.featureDisabled");
            return true;
        }

        // If there aren't any particle plugins to use, stop there.
        if (PluginHookManager.get().getParticlesPlugins().size() == 0) {
            CustomMessages.sendMessage(sender, "Error.noParticlePlugins");
            return true;
        }

        // If the sender isn't a player, stop there.
        if (!(sender instanceof Player player)) {
            CustomMessages.sendMessage(sender, "Error.notAPlayer");
            return true;
        }

        // Get the player's particle data.
        String data = ParticleManager.getData(player);

        // If no arguments have been specified, get the player's current particles and set it to the default particles.
        if (args.length == 0) {
            if (data == null) data = "";
            NewConfig.get().set("default-waiting-particles", data);
            CustomMessages.sendMessage(sender, "Info.defaultParticlesUpdated");
        } else {

            // Otherwise, set it for the specific command type
            if (data == null) data = "default";
            String type = args[0];
            if (!types.contains(type)) {
                return false;
            }
            NewConfig.get().set("waiting-particles." + type, data);
            CustomMessages.sendMessage(sender, "Info.specificParticlesUpdated", "{type}", type);
        }

        // Then save the config.
        try {
            NewConfig.get().save();
        } catch (IOException e) {
            e.printStackTrace(); // TODO - let's handle this more gracefully
        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s,
                                      @NotNull String[] args) {
        List<String> results = new ArrayList<>();
        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], types, results);
        }
        return results;
    }

}
