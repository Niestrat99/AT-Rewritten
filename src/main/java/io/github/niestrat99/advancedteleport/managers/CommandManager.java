package io.github.niestrat99.advancedteleport.managers;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.commands.ATCommand;
import io.github.niestrat99.advancedteleport.commands.AsyncATCommand;
import io.github.niestrat99.advancedteleport.commands.AtHelp;
import io.github.niestrat99.advancedteleport.commands.home.HomeCommand;
import io.github.niestrat99.advancedteleport.commands.teleport.Back;
import io.github.niestrat99.advancedteleport.commands.warp.*;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class CommandManager {

    public static void registerCommands() {
        register("athelp", new AtHelp());

        register("back", new Back());

        register("home", new HomeCommand());

        register("warp", new WarpCommand());
        register("setwarp", new SetWarpCommand());
        register("delwarp", new DeleteWarpCommand());
        register("movewarp", new MoveWarpCommand());
        register("warps", new WarpsCommand());
    }

    private static void register(String name, ATCommand atCommand) {
        PluginCommand command = Bukkit.getPluginCommand(name);
        if (command == null) return;
        if (command.getPlugin() != CoreClass.getInstance()) return;
        CommandMap map = getMap();
        if (map == null) return;
        if (NewConfig.getInstance().DISABLED_COMMANDS.get().contains(name)) {
            if (command.isRegistered()) {
                command.unregister(map);
                return;
            }
        } else {
            if (!command.isRegistered()) {
                command.register(map);
            }
        }
        if (atCommand instanceof AsyncATCommand) {
            command.setExecutor((sender, cmd, label, args) -> {
                Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), () -> atCommand.onCommand(sender, cmd, label, args))
                return false;
            });
        } else {
            command.setExecutor(atCommand);
        }
        command.setTabCompleter(atCommand);

    }

    private static CommandMap getMap() {
        try {
            Method map = Bukkit.getServer().getClass().getDeclaredMethod("getCommandMap");
            return (CommandMap) map.invoke(Bukkit.getServer());
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }
}
