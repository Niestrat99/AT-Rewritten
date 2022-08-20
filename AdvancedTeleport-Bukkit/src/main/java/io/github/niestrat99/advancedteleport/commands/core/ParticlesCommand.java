package io.github.niestrat99.advancedteleport.commands.core;

import io.github.niestrat99.advancedteleport.commands.SubATCommand;
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
        if (!NewConfig.get().USE_PARTICLES.get()) {
            return true;
        }
        if (PluginHookManager.get().getParticlesPlugins().size() == 0) {
            return true;
        }
        if (!(sender instanceof Player)) {
            return true;
        }
        Player player = (Player) sender;
        String data = ParticleManager.getData(player);

        if (args.length == 0) {
            if (data == null) data = "";
            NewConfig.get().set("default-waiting-particles", data);
        } else {
            if (data == null) data = "default";
            String type = args[0];
            if (!types.contains(type)) {
                return false;
            }
            NewConfig.get().set("waiting-particles." + type, data);
        }
        try {
            NewConfig.get().save();
        } catch (IOException e) {
            e.printStackTrace();
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
