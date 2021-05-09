package io.github.niestrat99.advancedteleport.hooks.imports;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.Warp;
import io.github.niestrat99.advancedteleport.config.Spawn;
import io.github.niestrat99.advancedteleport.hooks.ImportExportPlugin;
import io.github.niestrat99.advancedteleport.sql.WarpSQLManager;
import io.github.niestrat99.advancedteleport.utilities.ConditionChecker;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.UUID;

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
        debug("Importing warps...");
        Plugin essentials = Bukkit.getPluginManager().getPlugin("Essentials");
        File warpsFolder = new File(essentials.getDataFolder(), "warps");
        if (!warpsFolder.exists() || !warpsFolder.isDirectory() || warpsFolder.listFiles() == null) {
            debug("Warps folder doesn't exist/wasn't found, skipping...");
            return;
        }

        for (File file : warpsFolder.listFiles()) {
            try {
                YamlConfiguration warpFile = YamlConfiguration.loadConfiguration(file);
                String name = warpFile.getString("name");
                if (name == null) continue;
                Location loc = getLocationFromSection(warpFile);
                BasicFileAttributes attributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
                long created = attributes.creationTime().toMillis();
                long updated = attributes.lastModifiedTime().toMillis();
                String creatorStr = warpFile.getString("lastowner");
                UUID creator = null;
                if (creatorStr != null) {
                    creator = UUID.fromString(creatorStr);
                }
                WarpSQLManager.get().addWarp(new Warp(creator, name, loc, created, updated), null);
            } catch (Exception ignored) {
            }
        }

        debug("Finished importing warps!");
    }

    @Override
    public void importSpawn() {
        debug("Importing spawnpoints...");
        Plugin essentials = Bukkit.getPluginManager().getPlugin("Essentials");
        File spawnFileEss = new File(essentials.getDataFolder(), "spawn.yml");
        if (!spawnFileEss.exists() || !spawnFileEss.isFile()) {
            debug("Spawn file doesn't exist/wasn't found, skipping...");
            return;
        }

        YamlConfiguration spawn = YamlConfiguration.loadConfiguration(spawnFileEss);
        ConfigurationSection spawns = spawn.getConfigurationSection("spawns");
        if (spawns == null) {
            debug("Spawns section of the spawn file doesn't exist, skipping...");
            return;
        }

        boolean setMainSpawn = false;
        for (String key : spawns.getKeys(false)) {
            ConfigurationSection spawnSection = spawns.getConfigurationSection(key);
            Location loc = getLocationFromSection(spawnSection);
            Spawn.get().setSpawn(loc, key);
            debug("Set spawn for " + key);
            if (key.equals("default")) {
                setMainSpawn = true;
                Spawn.get().setMainSpawn("default", loc);
                debug("Set main spawn");
            } else {
                if (CoreClass.getPerms() != null) {
                    CoreClass.getPerms().groupAdd((String) null, key, "at.member.spawn." + key);
                    debug("Added at.member.spawn." + key + " to group "+ key);
                }
            }
        }

        if (!setMainSpawn) {
            debug("Removed main spawn");
            Spawn.get().setMainSpawn(null, null);
        }

        debug("Finished importing spawns");
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

    private static Location getLocationFromSection(ConfigurationSection section) {
        World world = Bukkit.getWorld(ConditionChecker.validate(section.getString("world"), "World is null"));
        double x = section.getDouble("x");
        double y = section.getDouble("y");
        double z = section.getDouble("z");
        float yaw = (float) section.getDouble("yaw");
        float pitch = (float) section.getDouble("pitch");
        return new Location(world, x, y, z, yaw, pitch);
    }
}
