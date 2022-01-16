package io.github.niestrat99.advancedteleport;

import com.wimbli.WorldBorder.WorldBorder;
import io.github.niestrat99.advancedteleport.commands.teleport.TpLoc;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.GUI;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.config.Spawn;
import io.github.niestrat99.advancedteleport.listeners.SignInteractListener;
import io.github.niestrat99.advancedteleport.listeners.PlayerListeners;
import io.github.niestrat99.advancedteleport.listeners.WorldLoadListener;
import io.github.niestrat99.advancedteleport.managers.*;
import io.github.niestrat99.advancedteleport.sql.*;
import io.github.niestrat99.advancedteleport.utilities.RandomTPAlgorithms;
import io.github.niestrat99.advancedteleport.utilities.nbt.NBTReader;
import io.papermc.lib.PaperLib;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

public class CoreClass extends JavaPlugin {

    public static String pltitle(String title) {
        title = "&3[&bAdvancedTeleport&3] " + title;
        return ChatColor.translateAlternateColorCodes('&', title);
    }

    private static Economy vault;
    public static WorldBorder worldBorder;
    private static CoreClass Instance;
    private static Permission perms = null;
    private int version;
    private Object[] updateInfo = null;

    public static Executor async = task -> Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), task);
    public static Executor sync = task -> Bukkit.getScheduler().runTask(CoreClass.getInstance(), task);

    private NewConfig config;

    public static CoreClass getInstance() {
        return Instance;
    }

    public static Economy getVault() {
        return vault;
    }

    public static WorldBorder getWorldBorder() {
        return worldBorder;
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        vault = rsp.getProvider();
        return vault != null;
    }

    private boolean setupPermissions() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        if (rsp == null) return false;
        perms = rsp.getProvider();
        return perms != null;
    }

    @Override
    public void onEnable() {
        Instance = this;
        getLogger().info("Advanced Teleport is now enabling...");
        setupEconomy();
        setupPermissions();
        config = new NewConfig();
        //    Config.setDefaults();
        new CustomMessages(this).load();
        new Spawn();
        new GUI();
        CommandManager.registerCommands();
        {
            new BlocklistManager();
            new HomeSQLManager();
            new PlayerSQLManager();
            new WarpSQLManager();
            new DataFailManager();
        }
        registerEvents();
        CooldownManager.init();
        RandomTPAlgorithms.init();
        new PluginHookManager();

        setupVersion();
        new Metrics(this, 5146);
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            NBTReader.init();
            RTPManager.init();
            if (NewConfig.get().CHECK_FOR_UPDATES.get()) {
                updateInfo = UpdateChecker.getUpdate();
                if (updateInfo != null) {
                    getLogger().info(ChatColor.AQUA + "" + ChatColor.BOLD + "A new version is available!");
                    getLogger().info(ChatColor.AQUA + "" + ChatColor.BOLD + "Current version you're using: " + ChatColor.WHITE + getDescription().getVersion());
                    getLogger().info(ChatColor.AQUA + "" + ChatColor.BOLD + "Latest version available: " + ChatColor.WHITE + updateInfo[0]);
                    getLogger().info(ChatColor.AQUA + "Download link: https://www.spigotmc.org/resources/advancedteleport.64139/");
                } else {
                    getLogger().info(ChatColor.AQUA + "Plugin is up to date!");
                }
            }
            TpLoc.a();
        });
    }

    @Override
    public void onDisable() {
        DataFailManager.get().onDisable();

        try {
            hackTheMainFrame();
        } catch (NoSuchFieldException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            getLogger().warning("Failed to shut down async tasks.");
            e.printStackTrace();
        }

        try {
            RTPManager.saveLocations();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new SignInteractListener(), this);
        getServer().getPluginManager().registerEvents(new TeleportTrackingManager(), this);
        getServer().getPluginManager().registerEvents(new MovementManager(), this);
        getServer().getPluginManager().registerEvents(new PlayerListeners(), this);
        getServer().getPluginManager().registerEvents(new WorldLoadListener(), this);
    }

    /**
     * Nag author: 'Niestrat99' of 'AdvancedTeleport' about the following:
     * This plugin is not properly shutting down its async tasks when it is being shut down.
     * This task may throw errors during the final shutdown logs and might not complete before process dies.
     *
     * Careful what you consider proper, Paper...
     *
     * FYI - any Paper devs that see this, is there a better way to work around this?
     * AT freezes up due to the PaperLib#getChunkAtAsync method being held up, then floods the console, and considering the userbase...
     * If there's a better way of handling this please either open an issue or DM @ Error#7365 because this method honestly sucks ass
     * That or probably only make it so that it warns once per plugin.
     */
    private void hackTheMainFrame() throws NoSuchFieldException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        if (PaperLib.isPaper()) {
            BukkitScheduler scheduler = Bukkit.getScheduler();
            // Get the async scheduler
            Field asyncField = Bukkit.getScheduler().getClass().getDeclaredField("asyncScheduler");
            asyncField.setAccessible(true);
            BukkitScheduler asyncScheduler = (BukkitScheduler) asyncField.get(Bukkit.getScheduler());

            Field runnersField = scheduler.getClass().getDeclaredField("runners");
            runnersField.setAccessible(true);
            ConcurrentHashMap<Integer, ? extends BukkitTask> runners = (ConcurrentHashMap<Integer, ? extends BukkitTask>) runnersField.get(asyncScheduler);
            List<Integer> toBeRemoved = new ArrayList<>();

            for (int taskId : runners.keySet()) {
                if (runners.get(taskId).getOwner() == this) {
                    runners.get(taskId).cancel();
                    toBeRemoved.add(taskId);
                }
            }

            for (int task : toBeRemoved) {
                runners.remove(task);
            }

            runnersField.set(scheduler, runners);
        }
    }

    public static void playSound(String type, String subType, Player target) {
        String sound = null;
        switch (type) {
            case "tpa":
                switch (subType) {
                    case "sent":
                        sound = NewConfig.get().TPA_REQUEST_SENT.get();
                        break;
                    case "received":
                        sound = NewConfig.get().TPA_REQUEST_RECEIVED.get();
                        break;
                }
                break;
            case "tpahere":
                switch (subType) {
                    case "sent":
                        sound = NewConfig.get().TPAHERE_REQUEST_SENT.get();
                        break;
                    case "received":
                        sound = NewConfig.get().TPAHERE_REQUEST_RECEIVED.get();
                        break;
                }
                break;
        }
        if (sound == null) return;
        if (sound.equalsIgnoreCase("none")) return;
        try {
            target.playSound(target.getLocation(), Sound.valueOf(sound), 10, 1);
        } catch(IllegalArgumentException e){
            CoreClass.getInstance().getLogger().warning(CoreClass.pltitle(sound + " is an invalid sound name"));
        }
    }

    public static Permission getPerms() {
        return perms;
    }

    public NewConfig getConfiguration() {
        return config;
    }

    private void setupVersion() {
        String bukkitVersion = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3].split("_")[1];
        this.version = Integer.parseInt(bukkitVersion);
    }

    public int getVersion() {
        return version;
    }

    public Object[] getUpdateInfo() {
        return updateInfo;
    }
}
