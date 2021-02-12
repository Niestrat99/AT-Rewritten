package io.github.niestrat99.advancedteleport;

import io.github.niestrat99.advancedteleport.commands.teleport.TpLoc;
import io.github.niestrat99.advancedteleport.config.*;
import io.github.niestrat99.advancedteleport.listeners.AtSigns;
import io.github.niestrat99.advancedteleport.listeners.PlayerListeners;
import io.github.niestrat99.advancedteleport.managers.CommandManager;
import io.github.niestrat99.advancedteleport.managers.CooldownManager;
import io.github.niestrat99.advancedteleport.managers.MovementManager;
import io.github.niestrat99.advancedteleport.managers.TeleportTrackingManager;
import io.github.niestrat99.advancedteleport.sql.*;
import io.github.niestrat99.advancedteleport.utilities.RandomTPAlgorithms;
import io.github.niestrat99.advancedteleport.utilities.nbt.NBTReader;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;

public class CoreClass extends JavaPlugin {

    public static String pltitle(String title) {
        title = "&3[&bAdvancedTeleport&3] " + title;
        return ChatColor.translateAlternateColorCodes('&', title);
    }

    private static Economy Vault;
    public static WorldBorder worldBorder;
    private static CoreClass Instance;
    private static Permission perms = null;
    private int version;

    private NewConfig config;

    public static CoreClass getInstance() {
        return Instance;
    }

    public static Economy getVault() {
        return Vault;
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
        Vault = rsp.getProvider();
        return Vault != null;
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
        System.out.println("Advanced Teleport is now enabling...");
        setupEconomy();
        setupPermissions();
        try {
            config = new NewConfig();
        //    Config.setDefaults();
            CustomMessages.setDefaults();
            LastLocations.save();
            Spawn.save();
            GUI.setDefaults();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        setupVersion();
        new Metrics(this, 5146);
        new BukkitRunnable() {
            @Override
            public void run() {
                // Config.setupDefaults();
                NBTReader.init();
                Object[] update = UpdateChecker.getUpdate();
                if (update != null) {
                    getServer().getConsoleSender().sendMessage(pltitle(ChatColor.AQUA + "" + ChatColor.BOLD + "A new version is available!") + "\n" + pltitle(ChatColor.AQUA + "" + ChatColor.BOLD + "Current version you're using: " + ChatColor.WHITE + getDescription().getVersion()) + "\n" + pltitle(ChatColor.AQUA + "" + ChatColor.BOLD + "Latest version available: " + ChatColor.WHITE + update[0]));
                    getLogger().info(pltitle(ChatColor.AQUA + "Download link: https://www.spigotmc.org/resources/advanced-teleport.64139/"));
                } else {
                    getLogger().info(pltitle(ChatColor.AQUA + "Plugin is up to date!"));
                }
                TpLoc.a();
            }
        }.runTaskAsynchronously(this);
    }

    @Override
    public void onDisable() {
        LastLocations.saveLocations();
        DataFailManager.get().onDisable();
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new AtSigns(), this);
        getServer().getPluginManager().registerEvents(new TeleportTrackingManager(), this);
        getServer().getPluginManager().registerEvents(new MovementManager(), this);
        getServer().getPluginManager().registerEvents(new PlayerListeners(), this);
    }

    public static void playSound(String type, String subType, Player target) {
        String sound = null;
        switch (type) {
            case "tpa":
                switch (subType) {
                    case "sent":
                        sound = NewConfig.getInstance().TPA_REQUEST_SENT.get();
                        break;
                    case "received":
                        sound = NewConfig.getInstance().TPA_REQUEST_RECEIVED.get();
                        break;
                }
                break;
            case "tpahere":
                switch (subType) {
                    case "sent":
                        sound = NewConfig.getInstance().TPAHERE_REQUEST_SENT.get();
                        break;
                    case "received":
                        sound = NewConfig.getInstance().TPAHERE_REQUEST_RECEIVED.get();
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
}
