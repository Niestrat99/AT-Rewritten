package io.github.at.main;

import io.github.at.commands.AtHelp;
import io.github.at.commands.AtInfo;
import io.github.at.commands.AtReload;
import io.github.at.commands.home.DelHome;
import io.github.at.commands.home.Home;
import io.github.at.commands.home.HomesCommand;
import io.github.at.commands.home.SetHome;
import io.github.at.commands.spawn.SetSpawn;
import io.github.at.commands.spawn.SpawnCommand;
import io.github.at.commands.teleport.*;
import io.github.at.commands.warp.Warp;
import io.github.at.commands.warp.WarpsCommand;
import io.github.at.config.*;
import io.github.at.events.AtSigns;
import io.github.at.events.MovementManager;
import io.github.at.events.TeleportTrackingManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.WorldBorder;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public class Main extends JavaPlugin {


    // TODO SUGGESTIONS THAT HAVE BEEN MADE
    // Back command
    // /rtp <World name> (done)
    // Custom messages (doing)
    // Payment for more than just /tpa and /tpahere
    // Payment using items (maybe???)
    // MySQL compatibility (AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA)

    private static Economy Vault;
    public static WorldBorder worldBorder;
    private static Main Instance;

    public static Main getInstance() {
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

    @Override
    public void onEnable() {
        Instance = this;
        System.out.println("Advanced Teleport is now enabling...");
        registerCommands();
        registerEvents();
        try {
            Config.setDefaults();
            CustomMessages.setDefaults();
            Homes.save();
            LastLocations.save();
            Warps.save();
            Spawn.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
        setupEconomy();
        new Metrics(this);
    }

    @Override
    public void onDisable() {
        LastLocations.saveLocations();
    }

    // Separate method for registering commands
    private void registerCommands() {

        // Main commands
        getCommand("athelp").setExecutor(new AtHelp());
        getCommand("atreload").setExecutor(new AtReload());
        getCommand("atinfo").setExecutor(new AtInfo());

        // TP commands
        getCommand("back").setExecutor(new Back());
        getCommand("tpa").setExecutor(new Tpa());
        getCommand("tpr").setExecutor(new Tpr());
        getCommand("tpoff").setExecutor(new TpOff());
        getCommand("tpon").setExecutor(new TpOn());
        getCommand("tpblock").setExecutor(new TpBlockCommand());
        getCommand("tpcancel").setExecutor(new TpCancel());
        getCommand("tpno").setExecutor(new TpNo());
        getCommand("tpo").setExecutor(new Tpo());
        getCommand("tpohere").setExecutor(new TpoHere());
        getCommand("tpunblock").setExecutor(new TpUnblock());
        getCommand("tpyes").setExecutor(new TpYes());
        getCommand("tpahere").setExecutor(new TpaHere());
        getCommand("tpall").setExecutor(new TpAll());
        getCommand("tpalist").setExecutor(new TpList());

        // Home commands
        getCommand("home").setExecutor(new Home());
        getCommand("sethome").setExecutor(new SetHome());
        getCommand("delhome").setExecutor(new DelHome());
        getCommand("homes").setExecutor(new HomesCommand());

        // Warp commands
        getCommand("warp").setExecutor(new Warp());
        getCommand("warps").setExecutor(new WarpsCommand());

        // Spawn commands
        getCommand("spawn").setExecutor(new SpawnCommand());
        getCommand("setspawn").setExecutor(new SetSpawn());
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new AtSigns(), this);
        getServer().getPluginManager().registerEvents(new TeleportTrackingManager(), this);
        getServer().getPluginManager().registerEvents(new MovementManager(), this);
    }
}
