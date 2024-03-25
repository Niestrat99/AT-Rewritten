package io.github.niestrat99.advancedteleport.managers;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.commands.ATCommand;
import io.github.niestrat99.advancedteleport.commands.CoreCommand;
import io.github.niestrat99.advancedteleport.commands.SubATCommand;
import io.github.niestrat99.advancedteleport.commands.core.*;
import io.github.niestrat99.advancedteleport.commands.home.*;
import io.github.niestrat99.advancedteleport.commands.spawn.*;
import io.github.niestrat99.advancedteleport.commands.teleport.*;
import io.github.niestrat99.advancedteleport.commands.warp.*;
import io.github.niestrat99.advancedteleport.config.MainConfig;
import io.github.niestrat99.advancedteleport.folia.RunnableManager;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class CommandManager {

    private static final LinkedHashMap<String, PluginCommand> atCommands = new LinkedHashMap<>();
    public static final LinkedHashMap<String, SubATCommand> subcommands = new LinkedHashMap<>();
    public static final LinkedHashMap<String, PluginCommand> registeredCommands =
            new LinkedHashMap<>();

    public static void registerCommands() {
        register("at", new CoreCommand());

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
        register("tpoffline", new TpOffline());
        register("tpofflinehere", new TpHereOffline());

        register("home", new HomeCommand());
        register("sethome", new SetHomeCommand());
        register("delhome", new DelHomeCommand());
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
        register("mirrorspawn", new MirrorSpawn());
        register("removespawn", new RemoveSpawn());
        register("setmainspawn", new SetMainSpawn());

        subcommands.put("clearcache", new ClearCacheCommand());
        subcommands.put("import", new ImportCommand());
        subcommands.put("help", new HelpCommand());
        subcommands.put("reload", new ReloadCommand());
        subcommands.put("info", new InfoCommand());
        subcommands.put("export", new ExportCommand());
        subcommands.put("purge", new PurgeCommand());
        subcommands.put("particles", new ParticlesCommand());
        subcommands.put("map", new MapCommand());

        syncCommands();
    }

    private static void register(String name, ATCommand atCommand) {
        PluginCommand command = Bukkit.getPluginCommand("advancedteleport:" + name);
        CoreClass.debug("Fetching " + command + " - " + command);
        if (command == null) command = atCommands.get(name);
        if (command == null) {
            CoreClass.getInstance().getLogger().warning("Could not add command " + name + " - has it been set up properly?");
            return;
        }

        atCommands.put(name, command);
        CommandMap map = getMap();
        if (map == null) return;

        Map<String, Command> commands = getCommands(map);
        if (commands == null) return;

        List<String> aliases = new ArrayList<>(command.getAliases());
        aliases.add(command.getName());
        boolean removed = false;
        for (String alias : aliases) {
            if (MainConfig.get().DISABLED_COMMANDS.get().contains(alias)
                    || removed
                    || !atCommand.getRequiredFeature()) {
                CoreClass.debug(alias + " has been marked for removal.");
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

                CoreClass.debug("Removed " + alias + ".");

                // Let another plugin take over
                RunnableManager.setupRunnerDelayed((run) -> {
                    Iterator<String> commandIterator = commands.keySet().iterator();
                    HashMap<String, Command> pendingChanges = new HashMap<>();

                    // Ignore warning, can yield CME
                    while (commandIterator.hasNext()) {
                        String otherCmd = commandIterator.next();
                        String[] parts = otherCmd.split(":");
                        if (parts.length < 2) continue;
                        if (parts[1].equals(alias)) {
                            if (parts[0].equals("advancedteleport")) continue;
                            CoreClass.debug("Letting " + parts[0] + "'s " + alias + " take over...");
                            pendingChanges.put(alias, commands.get(otherCmd));
                            break;
                        }
                    }
                    commands.putAll(pendingChanges);
                }, 100);
            }
            return;
        }

        if (!command.isRegistered()) {
            command.register(map);
            commands.put(name, command);
            commands.put("advancedteleport:" + name, command);

            // Re-add all aliases
            for (String alias : aliases) {
                commands.put(alias, command);
                commands.put("advancedteleport:" + alias, command);
            }
        }

        if (command.getExecutor() != atCommand) {
            command.setExecutor(atCommand);
            command.setTabCompleter(atCommand);
        }

        CoreClass.debug(aliases + " has " + (command.isRegistered() ? "" : "not ") + "been registed successfully.");

        registeredCommands.put(name, command);
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

    private static Map<String, Command> getCommands(CommandMap map) {
        try {
            return map.getKnownCommands();
        } catch (NoSuchMethodError ignored) {
        }
        try {
            Field commands = SimpleCommandMap.class.getDeclaredField("knownCommands");
            commands.setAccessible(true);
            return (Map<String, Command>) commands.get(map);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void syncCommands() {

        try {
            Method method = Bukkit.getServer().getClass().getDeclaredMethod("syncCommands");
            method.invoke(Bukkit.getServer());
        } catch (Exception ignored) {
        }
    }
}
