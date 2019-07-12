package io.github.at.main;

import io.github.at.commands.*;
import io.github.at.commands.home.DelHome;
import io.github.at.commands.home.Home;
import io.github.at.commands.home.SetHome;
import io.github.at.commands.spawn.SetSpawn;
import io.github.at.commands.spawn.SpawnCommand;
import io.github.at.commands.teleport.*;
import io.github.at.commands.warp.Warp;
import io.github.at.commands.warp.WarpsCommand;
import io.github.at.events.AtSigns;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.WorldBorder;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

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
        System.out.println("Advanced Teleport is now enabling...");
        registerCommands();
        registerEvents();
        setupEconomy();
        Instance = this;
    }

    // Separate method for registering commands
    private void registerCommands() {

        // Main commands
        getCommand("athelp").setExecutor(new AtHelp());
        getCommand("atreload").setExecutor(new AtReload());

        // TP commands
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

        // Home commands
        getCommand("home").setExecutor(new Home());
        getCommand("sethome").setExecutor(new SetHome());
        getCommand("delhome").setExecutor(new DelHome());

        // Warp commands
        getCommand("warp").setExecutor(new Warp());
        getCommand("warps").setExecutor(new WarpsCommand());

        // Spawn commands
        getCommand("spawn").setExecutor(new SpawnCommand());
        getCommand("setspawn").setExecutor(new SetSpawn());
    }

    // Lonely event :c
    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new AtSigns(), this);
    }
}
