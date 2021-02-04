package io.github.niestrat99.advancedteleport.managers;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.commands.*;
import io.github.niestrat99.advancedteleport.commands.home.DelHome;
import io.github.niestrat99.advancedteleport.commands.home.HomeCommand;
import io.github.niestrat99.advancedteleport.commands.home.HomesCommand;
import io.github.niestrat99.advancedteleport.commands.home.SetHome;
import io.github.niestrat99.advancedteleport.commands.spawn.SetSpawn;
import io.github.niestrat99.advancedteleport.commands.spawn.SpawnCommand;
import io.github.niestrat99.advancedteleport.commands.teleport.*;
import io.github.niestrat99.advancedteleport.commands.warp.*;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class CommandManager {

    public static void registerCommands() {
        register("athelp", new AtHelp());
        register("atreload", new AtReload());
        register("atinfo", new AtInfo());

        register("back", new Back());
        register("toggletp", new ToggleTP());
        register("tpa", new Tpa());
        register("tpahere", new TpaHere());
        register("tpall", new TpAll());
        register("tpblock", new TpBlockCommand());
        register("tpcancel", new TpCancel());
        register("tplist", new TpList());
        register("tploc", new TpLoc());
        register("tpno", new TpNo());
        register("tpo", new Tpo());
        register("tpoff", new TpOff());
        register("tpohere", new TpoHere());
        register("tpon", new TpOn());
        register("tpr", new Tpr());
        register("tpunblock", new TpUnblock());
        register("tpyes", new TpYes());

        register("home", new HomeCommand());
        register("sethome", new SetHome());
        register("delhome", new DelHome());
        register("homes", new HomesCommand());

        register("warp", new WarpCommand());
        register("setwarp", new SetWarpCommand());
        register("delwarp", new DeleteWarpCommand());
        register("movewarp", new MoveWarpCommand());
        register("warps", new WarpsCommand());

        register("spawn", new SpawnCommand());
        register("setspawn", new SetSpawn());
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
