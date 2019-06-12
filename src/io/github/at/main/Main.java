package io.github.at.main;

import io.github.at.commands.*;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.WorldBorder;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

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
        System.out.println("Advanced Teleport is now enabled.");
        getCommand("athelp").setExecutor(new AtHelp());
        getCommand("tpa").setExecutor(new Tpa());
        getCommand("tpr").setExecutor(new Tpr());
        getCommand("tpoff").setExecutor(new TpOff());
        getCommand("tpon").setExecutor(new TpOn());
        Instance = this;
    }
}
