package io.github.niestrat99.advancedteleport.hooks.imports;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.earth2me.essentials.UserMap;
import com.earth2me.essentials.Warps;
import com.earth2me.essentials.commands.WarpNotFoundException;
import com.earth2me.essentials.spawn.EssentialsSpawn;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.api.AdvancedTeleportAPI;
import io.github.niestrat99.advancedteleport.api.Spawn;
import io.github.niestrat99.advancedteleport.api.Warp;
import io.github.niestrat99.advancedteleport.hooks.ImportExportPlugin;
import io.github.niestrat99.advancedteleport.sql.PlayerSQLManager;
import io.github.niestrat99.advancedteleport.utilities.ConditionChecker;

import net.ess3.api.InvalidWorldException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.UUID;

public final class EssentialsHook extends ImportExportPlugin<Essentials, Void> {

    private Essentials essentials;

    public EssentialsHook() {
        super("Essentials");
    }

    @Override
    @Contract(pure = true)
    public boolean canImport() {

        // Makes sure the plugin exists and because there's so many Ess clones out there, ensure
        // it's the right one
        return this.plugin()
                .map(
                        essentials -> {
                            this.essentials = essentials;
                            return true;
                        })
                .orElse(false);
    }

    @Override
    public void importHomes() {
        debug("Importing homes...");

        // If the plugin or usermap isn't set up properly, stop there
        if (essentials == null) return;
        UserMap userMap = essentials.getUserMap();
        if (userMap == null) return;

        // For each registered UUID...
        for (UUID uuid : userMap.getAllUniqueUsers()) {
            try {
                User user = getUser(uuid);
                if (user == null) continue;
                if (user.getName() == null) continue;
                for (String home : user.getHomes()) {

                    Location loc = user.getHome(home);

                    ATPlayer.getPlayerFuture(user.getName()).thenAcceptAsync(player -> {
                        if (!player.hasHome(home)) {
                            player.addHome(home, loc, null, false);
                        } else player.moveHome(home, loc);
                    }, CoreClass.sync);
                }
            } catch (Exception ex) {
                debug("Failed to export previous locations for UUID " + uuid.toString() + ":");
                ex.printStackTrace();
            }
        }
        debug("Finished importing homes!");
    }

    private @Nullable User getUser(@NotNull final UUID uuid) {
        try {
            UserMap userMap = essentials.getUserMap();
            if (userMap == null) return null;
            return userMap.getUser(uuid);
        } catch (RuntimeException e) {
            return null;
        }
    }

    @Override
    public void importLastLocations() {
        debug("Importing last locations...");
        if (essentials == null) return;
        UserMap userMap = essentials.getUserMap();
        if (userMap == null) return;

        for (UUID uuid : userMap.getAllUniqueUsers()) {
            try {
                User user = userMap.getUser(uuid);
                if (user == null
                        || user.getName() == null
                        || user.getLastLocation() == null
                        || user.getLastLocation().getWorld() == null) continue;
                if (ATPlayer.isPlayerCached(user.getName())) {
                    ATPlayer player = ATPlayer.getPlayer(user.getName());
                    player.setPreviousLocation(user.getLastLocation());
                } else {
                    PlayerSQLManager.get()
                            .setPreviousLocation(user.getName(), user.getLastLocation());
                }
            } catch (Exception ex) {
                debug("Failed to export previous locations for UUID " + uuid.toString() + ":");
                ex.printStackTrace();
            }
        }
        debug("Finished importing last locations!");
    }

    @Override
    public void importWarps() {
        debug("Importing warps...");
        if (essentials == null) return;
        Warps warps = essentials.getWarps();
        if (warps == null) return;

        for (String warp : warps.getList()) {
            try {
                Warp warpObj = AdvancedTeleportAPI.getWarp(warp);
                if (warpObj != null) {
                    Location loc = warps.getWarp(warp);
                    warpObj.setLocation(loc);
                } else {
                    UUID uuid = warps.getLastOwner(warp);
                    Location loc = warps.getWarp(warp);
                    AdvancedTeleportAPI.setWarp(warp, Bukkit.getPlayer(uuid), loc);
                }
            } catch (WarpNotFoundException ex) {
                CoreClass.getInstance().getLogger().warning("Failed to import warp " + warp + " from Essentials - apparently it does not exist.");
            } catch (InvalidWorldException e) {
                CoreClass.getInstance().getLogger().warning("Failed to import warp " + warp + " from Essentials - the world is not loaded/invalid.");
            }
        }
        debug("Finished importing warps!");
    }

    @Override
    public void importSpawn() {
        debug("Importing spawnpoints...");

        // Verify the spawns plugin is there, otherwise stop
        EssentialsSpawn spawnPlugin =
                (EssentialsSpawn) Bukkit.getPluginManager().getPlugin("EssentialsSpawn");
        if (spawnPlugin == null || !spawnPlugin.isEnabled()) return;

        // Get the spawn.yml file - if it doesn't exist though, skip it
        final var spawnFileEss =
                this.plugin()
                        .map(essentials -> new File(essentials.getDataFolder(), "spawn.yml"))
                        .get();
        if (!spawnFileEss.exists() || !spawnFileEss.isFile()) {
            debug("Spawn file doesn't exist/wasn't found, skipping...");
            return;
        }

        // Get the spawns section
        YamlConfiguration spawn = YamlConfiguration.loadConfiguration(spawnFileEss);
        ConfigurationSection spawns = spawn.getConfigurationSection("spawns");
        if (spawns == null) {
            debug("Spawns section of the spawn file doesn't exist, skipping...");
            return;
        }

        // Track whether the main spawnpoint has been set
        boolean setMainSpawn = false;

        // Go through each set spawnpoint in Essentials
        for (String key : spawns.getKeys(false)) {

            // Get the config section for the spawnpoint
            ConfigurationSection spawnSection = spawns.getConfigurationSection(key);
            if (spawnSection == null) continue;

            // Get the location of the spawnpoint
            Location loc = getLocationFromSection(spawnSection);

            // Set the spawnpoint
            Spawn spawn1 = AdvancedTeleportAPI.setSpawn(key, null, loc).join();
            debug("Set spawn for " + key);

            // If it's marked as the default spawn, then mark it as the main spawnpoint
            if (key.equals("default")) {
                setMainSpawn = true;
                AdvancedTeleportAPI.setMainSpawn(spawn1, null);
                debug("Set main spawn");
            } else {

                // Otherwise, if it's a group spawn, add the AT permission to the group
                if (CoreClass.getPerms() != null && CoreClass.getPerms().hasGroupSupport()) {
                    CoreClass.getPerms().groupAdd((String) null, key, "at.member.spawn." + key);
                    debug("Added at.member.spawn." + key + " to group " + key);
                }
            }
        }

        // If the main spawn has not been set, remove it
        if (!setMainSpawn) {
            debug("Removed main spawn");
            AdvancedTeleportAPI.setMainSpawn(null, null);
        }

        debug("Finished importing spawns");
    }

    @Override
    public void importPlayerInformation() {
        debug("Importing player information...");
        if (essentials == null) return;
        UserMap userMap = essentials.getUserMap();
        if (userMap == null) return;

        for (UUID uuid : userMap.getAllUniqueUsers()) {
            try {
                User user = getUser(uuid);
                if (user == null) continue;
                if (user.getName() == null) continue;
                if (!ATPlayer.isPlayerCached(user.getName())) {
                    PlayerSQLManager.get().setTeleportationOn(uuid, user.isTeleportEnabled());
                } else {
                    ATPlayer player = ATPlayer.getPlayer(user.getName());
                    player.setTeleportationEnabled(user.isTeleportEnabled());
                }
            } catch (Exception ex) {
                debug("Failed to import player data for UUID " + uuid.toString() + ":");
                ex.printStackTrace();
            }
        }
        debug("Imported player information!");
    }

    @Override
    public void exportHomes() {
        debug("Exporting homes...");
        debug(
                "WARNING: Essentials does not have a \"main home\" system so all main homes in AT will be ignored when exporting.");

        if (essentials == null) return;
        UserMap userMap = essentials.getUserMap();
        if (userMap == null) return;

        for (UUID uuid : userMap.getAllUniqueUsers()) {
            try {
                User user = getUser(uuid);
                if (user == null) continue;
                if (user.getName() == null) continue;

                ATPlayer.getPlayerFuture(user.getName()).thenAcceptAsync(player -> {
                    for (String home : player.getHomes().keySet()) {
                        user.setHome(home, player.getHome(home).getLocation());
                    }
                }, CoreClass.sync);

            } catch (Exception ex) {
                debug("Failed to export home data for UUID " + uuid.toString() + ":");
                ex.printStackTrace();
            }
        }
        debug("Finished exporting homes!");
    }

    @Override
    public void exportLastLocations() {
        debug("Exporting previous locations...");

        if (essentials == null) return;
        UserMap userMap = essentials.getUserMap();
        if (userMap == null) return;

        for (UUID uuid : userMap.getAllUniqueUsers()) {
            try {
                User user = getUser(uuid);
                if (user == null) continue;
                if (user.getName() == null) continue;

                ATPlayer.getPlayerFuture(user.getName()).thenAcceptAsync(player -> {
                    user.setLastLocation(player.getPreviousLocation());
                }, CoreClass.sync);

            } catch (Exception ex) {
                debug("Failed to export previous locations for UUID " + uuid.toString() + ":");
                ex.printStackTrace();
            }
        }

        debug("Finished exporting previous locations!");
    }

    @Override
    public void exportWarps() {
        debug("Exporting warps...");
        if (essentials == null) return;
        Warps warps = essentials.getWarps();
        if (warps == null) return;

        for (Warp warp : AdvancedTeleportAPI.getWarps().values()) {
            try {
                warps.setWarp(warp.getName(), warp.getLocation());
            } catch (Exception e) {
                debug("Failed to export warp " + warp.getName() + ":");
                e.printStackTrace();
            }
        }
        debug("Finished exporting warps!");
    }

    @Override
    public void exportSpawn() {
        debug("Exporting spawnpoints...");
        debug(
                "WARNING - any changes made to the spawnpoints may be dodgy due to the differences between Essentials and AT's spawn systems.");
        debug("If you notice any problems, please fix them yourself.");

        // If the spawn plugin doesn't exist, stop there
        EssentialsSpawn spawnPlugin =
                (EssentialsSpawn) Bukkit.getPluginManager().getPlugin("EssentialsSpawn");
        if (spawnPlugin == null || !spawnPlugin.isEnabled()) return;

        // Get the main spawnpoint - if it's not null, set it as the default spawn
        Spawn mainSpawn = AdvancedTeleportAPI.getMainSpawn();
        if (mainSpawn != null) spawnPlugin.setSpawn(mainSpawn.getLocation(), "default");

        // Go through each group and spawn set
        for (Spawn atSpawn : AdvancedTeleportAPI.getSpawns().values()) {
            for (String group : CoreClass.getPerms().getGroups()) {
                if (!CoreClass.getPerms().groupHas((World) null, group, atSpawn.getName()))
                    continue;
                spawnPlugin.setSpawn(atSpawn.getLocation(), group);
            }
        }

        debug("Finished exporting spawns!");
    }

    @Override
    public void exportPlayerInformation() {
        debug("Exporting player information...");

        if (essentials == null) return;
        UserMap userMap = essentials.getUserMap();
        if (userMap == null) return;

        for (UUID uuid : userMap.getAllUniqueUsers()) {
            try {
                User user = getUser(uuid);
                if (user == null) continue;
                if (user.getName() == null) continue;

                ATPlayer.getPlayerFuture(user.getName()).thenAcceptAsync(player ->
                    user.setTeleportEnabled(player.isTeleportationEnabled()), CoreClass.async);

            } catch (Exception ex) {
                debug("Failed to export player information for UUID " + uuid.toString() + ":");
                ex.printStackTrace();
            }
        }
    }

    private static Location getLocationFromSection(ConfigurationSection section) {
        World world =
                Bukkit.getWorld(
                        ConditionChecker.validate(section.getString("world"), "World is null"));
        double x = section.getDouble("x");
        double y = section.getDouble("y");
        double z = section.getDouble("z");
        float yaw = (float) section.getDouble("yaw");
        float pitch = (float) section.getDouble("pitch");
        return new Location(world, x, y, z, yaw, pitch);
    }
}
