package io.github.at.config;

import io.github.at.main.CoreClass;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class GUI {

    public static File configFile = new File(CoreClass.getInstance().getDataFolder(),"guis.yml");
    public static FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

    public static void save() throws IOException {
        config.save(configFile);
    }

    public static void setDefaults() {
        //Warps Menu
        config.addDefault("warps.slots", 27);
        config.addDefault("warps.gui-enabled", false);
        // Last Page icon
        config.addDefault("warps.icons.last-page.name", "&bLast Page");
        config.addDefault("warps.icons.last-page.item", "ARROW");
        config.addDefault("warps.icons.last-page.tooltip", new ArrayList<>());
        config.addDefault("warps.icons.last-page.slot", 18);
        // Next Page icon
        config.addDefault("warps.icons.next-page.name", "&bNext Page");
        config.addDefault("warps.icons.next-page.item", "ARROW");
        config.addDefault("warps.icons.next-page.tooltip", new ArrayList<>());
        config.addDefault("warps.icons.next-page.slot", 26);
        //Warps Menu Example
        config.addDefault("warps.warps.warpName1.name", "&aWarp Name 1");
        config.addDefault("warps.warps.warpName1.item", "DIAMOND_SWORD");
        config.addDefault("warps.warps.warpName1.tooltip", Collections.singletonList("&7Teleports you to warpName1"));
        config.addDefault("warps.warps.warpName1.slot", 11);
        config.addDefault("warps.warps.warpName1.page", 0);
        config.addDefault("warps.warps.warpName1.hide-if-no-permission", false);

        config.addDefault("warps.warps.warpName2.name", "&7Warp Name 2");
        config.addDefault("warps.warps.warpName2.item", "IRON_PICKAXE");
        config.addDefault("warps.warps.warpName2.tooltip", Collections.singletonList("&7Teleports you to warpName2"));
        config.addDefault("warps.warps.warpName2.slot", 13);
        config.addDefault("warps.warps.warpName2.page", 0);
        config.addDefault("warps.warps.warpName2.hide-if-no-permission", true);
        config.options().copyDefaults(true);
        try {
            save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void reloadConfig() throws IOException {
        if (configFile == null) {
            configFile = new File(CoreClass.getInstance().getDataFolder(), "guis.yml");
        }
        config = YamlConfiguration.loadConfiguration(configFile);
        setDefaults();
        save();
    }

    public static ConfigurationSection getWarpsMenu() { return config.getConfigurationSection("warps.warps"); }
    public static int getWarpsMenuSlots() { return config.getInt("warps.slots"); }
    public static boolean isUsingWarpsGUIMenu() { return config.getBoolean("warps.gui-enabled"); }
    public static ConfigurationSection getLastPageIcon() { return config.getConfigurationSection("warps.icons.last-page"); }
    public static ConfigurationSection getNextPageIcon() { return config.getConfigurationSection("warps.icons.next-page"); }
}
