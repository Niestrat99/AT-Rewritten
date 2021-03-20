package io.github.niestrat99.advancedteleport.hooks.imports;

import io.github.niestrat99.advancedteleport.hooks.ImportExportPlugin;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.File;

/**
 * ESSENTIALS FORMAT
 *
 * Spawn:
 * spawns:
 *   default: - Need to research further
 *     world: world
 *     x: 288.5618027389669
 *     y: 75.0
 *     z: 193.3904219678909
 *     yaw: 184.20215
 *     pitch: 14.849949
 *
 * Homes:
 *
 * Warps:
 * world: world
 * x: -270.74110040141517
 * y: 91.0
 * z: -51.6039130384459
 * yaw: -2.8515792
 * pitch: 29.249918
 * name: 'yes'
 * lastowner: 26f4567f-8007-3e0e-ac35-a5f13f41c4ca
 */
public class EssentialsHook extends ImportExportPlugin {

    @Override
    public boolean canImport() {
        Plugin essentials = Bukkit.getPluginManager().getPlugin("Essentials");
        // Makes sure the plugin exists and because there's so many Ess clones out there, ensure it's the right one
        return essentials != null && essentials.getDescription().getMain().equals("com.earth2me.essentials.Essentials");
    }

    @Override
    public void importHomes() {

    }

    @Override
    public void importLastLocations() {

    }

    @Override
    public void importWarps() {
        Plugin essentials = Bukkit.getPluginManager().getPlugin("Essentials");
        File warpsFolder = new File(essentials.getDataFolder(), "warps");
        if (!warpsFolder.exists() || !warpsFolder.isDirectory() || warpsFolder.listFiles() == null) {
            debug("Warps folder doesn't exist/wasn't found, skipping...");
            return;
        }

        for (File file : warpsFolder.listFiles()) {
            try {

            } catch (Exception ignored) {

            }
        }
    }

    @Override
    public void importSpawn() {

    }

    @Override
    public void importPlayerInformation() {

    }

    @Override
    public void exportHomes() {

    }

    @Override
    public void exportLastLocations() {

    }

    @Override
    public void exportWarps() {

    }

    @Override
    public void exportSpawn() {

    }
}
