package io.github.at.config;

import io.github.at.main.CoreClass;
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
        config.addDefault("warps.autofill", false);
        // Last Page icon
        config.addDefault("warps.icons.last-page.name", "&bLast Page");
        config.addDefault("warps.last-page.item", "ARROW");
        config.addDefault("warps.icons.last-page.tooltip", new ArrayList<>());
        //Warps Menu Example
        config.addDefault("warps.warps.warpName1.name", "&aWarp Name 1");
        config.addDefault("warps.warps.warpName1.item", "GRASS_BLOCK");
        config.addDefault("warps.warps.warpName1.tooltip", Collections.singletonList("Teleports you to warpName1"));
        config.addDefault("warps.warps.warpName1.slot", 11);
        config.addDefault("warps.warps.warpName1.page", 0);
        config.addDefault("warps.warps.warpName1.hide-if-no-permission", false);
        config.addDefault("warps.warps.warpName2.name", "&7Warp Name 2");
        config.addDefault("warps.warps.warpName2.item", "IRON_PICKAXE");
        config.addDefault("warps.warps.warpName2.tooltip", Collections.singletonList("Teleports you to warpName2"));
        config.addDefault("warps.warps.warpName2.slot", 13);
        config.addDefault("warps.warps.warpName2.hide-if-no-permission", true);
        config.options().copyDefaults(true);
        try {
            save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    
}
