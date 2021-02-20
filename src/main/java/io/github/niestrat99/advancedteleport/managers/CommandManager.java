package io.github.niestrat99.advancedteleport.managers;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.commands.*;
import io.github.niestrat99.advancedteleport.commands.home.*;
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
import java.util.ArrayList;
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
        register("tpalist", new TpList());
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
        register("sethome", new SetHomeCommand());
        register("delhome", new DelHome());
        register("homes", new HomesCommand());
        register("movehome", new MoveHomeCommand());
        register("setmainhome", new SetMainHomeCommand());

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
        HashMap<String, Command> commands = getCommands(map);
        if (commands == null) return;

        List<String> aliases = new ArrayList<>(command.getAliases());
        aliases.add(name);

        boolean removed = false;
        for (String alias : aliases) {
            if (NewConfig.get().DISABLED_COMMANDS.get().contains(alias) || removed) {
                if (command.isRegistered()) {
                    removed = true;
                    command.unregister(map);

                }
            }
        }

        if (removed) {
            for (String alias : aliases) {
                commands.remove(alias);
                commands.remove("advancedteleport:" + alias);
                // Let another plugin take over
                Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), () -> {
                    for (String otherCmd : commands.keySet()) {
                        String[] parts = otherCmd.split(":");
                        if (parts.length < 2) continue;
                        if (parts[1].equals(alias)) {
                            if (parts[0].equals("advancedteleport")) continue;
                            commands.put(alias, commands.get(otherCmd));
                            break;
                        }
                    }
                });
            }
            return;
        }

        if (!command.isRegistered()) {
            command.register(map);
            commands.put(name, command);
            commands.put("advancedteleport:" + name, command);
        }

        if (atCommand instanceof AsyncATCommand) {
            command.setExecutor((sender, cmd, label, args) -> {
                Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), () -> atCommand.onCommand(sender, cmd, label, args));
                return true;
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

    private static HashMap<String, Command> getCommands(CommandMap map) {
        try {
            Field commands = SimpleCommandMap.class.getDeclaredField("knownCommands");
            commands.setAccessible(true);
            return (HashMap) commands.get(map);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
