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
import io.github.niestrat99.advancedteleport.api.Warp;
import io.github.niestrat99.advancedteleport.config.Spawn;
import io.github.niestrat99.advancedteleport.hooks.ImportExportPlugin;
import io.github.niestrat99.advancedteleport.sql.HomeSQLManager;
import io.github.niestrat99.advancedteleport.sql.PlayerSQLManager;
import io.github.niestrat99.advancedteleport.sql.SQLManager;
import io.github.niestrat99.advancedteleport.sql.WarpSQLManager;
import io.github.niestrat99.advancedteleport.utilities.ConditionChecker;
import net.ess3.api.InvalidWorldException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class EssentialsHook extends ImportExportPlugin<Essentials, Void> {

    private Essentials essentials;

    public EssentialsHook() {
        super("Essentials");
    }

    @Override
    @Contract(pure = true)
    public boolean canImport() {

        // Makes sure the plugin exists and because there's so many Ess clones out there, ensure it's the right one
        return this.plugin().map(essentials -> {
            this.essentials = essentials;
            return true;
        }).orElse(false);
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
                    if (ATPlayer.isPlayerCached(user.getName())) {
                        ATPlayer player = ATPlayer.getPlayer(user.getName());
                        if (!player.hasHome(home)) {
                            player.addHome(home, user.getHome(home), null, false);
                        } else {
                            player.moveHome(home, user.getHome(home));
                        }
                    } else {
                        try (Connection connection = HomeSQLManager.get().implementConnection()) {
                            PreparedStatement query = connection.prepareStatement("SELECT uuid_owner FROM " + SQLManager.getTablePrefix() + "_homes WHERE uuid-owner=? AND home=?");
                            query.setString(1, uuid.toString());
                            query.setString(2, home);

                            if (query.executeQuery().next()) {
                                HomeSQLManager.get().moveHome(user.getHome(home), uuid, home, null, false);
                            } else {
                                HomeSQLManager.get().addHome(user.getHome(home), uuid, home, null, false);
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                debug("Failed to export previous locations for UUID " + uuid.toString() + ":");
                ex.printStackTrace();
            }

        }
        debug("Finished importing homes!");
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
                    PlayerSQLManager.get().setPreviousLocation(user.getName(), user.getLastLocation(), null);
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
                if (AdvancedTeleportAPI.getWarps().containsKey(warp)) {
                    AdvancedTeleportAPI.getWarps().get(warp).setLocation(warps.getWarp(warp));
                } else {
                    WarpSQLManager.get().addWarp(new Warp(warps.getLastOwner(warp),
                            warp,
                            warps.getWarp(warp),
                            System.currentTimeMillis(),
                            System.currentTimeMillis()), null);
                }
            } catch (WarpNotFoundException | InvalidWorldException e) {
                e.printStackTrace();
            }
        }
        debug("Finished importing warps!");
    }

    @Override
    public void importSpawn() {
        debug("Importing spawnpoints...");
        EssentialsSpawn spawnPlugin = (EssentialsSpawn) Bukkit.getPluginManager().getPlugin("EssentialsSpawn");
        if (spawnPlugin == null || !spawnPlugin.isEnabled()) return;

        final var spawnFileEss = this.plugin().map(essentials -> new File(essentials.getDataFolder(), "spawn.yml")).get();
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
            try {
                Spawn.get().setSpawn(loc, key);
            } catch (IOException e) {
                CoreClass.getInstance().getLogger().severe("Failed to set spawn " + key + ": " + e.getMessage());
                e.printStackTrace();
                continue;
            }
            debug("Set spawn for " + key);
            if (key.equals("default")) {
                setMainSpawn = true;
                Spawn.get().setMainSpawn("default", loc);
                debug("Set main spawn");
            } else {
                if (CoreClass.getPerms() != null && CoreClass.getPerms().hasGroupSupport()) {
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
                    PlayerSQLManager.get().setTeleportationOn(uuid, user.isTeleportEnabled(), null);
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
        debug("WARNING: Essentials does not have a \"main home\" system so all main homes in AT will be ignored when exporting.");

        if (essentials == null) return;
        UserMap userMap = essentials.getUserMap();
        if (userMap == null) return;

        for (UUID uuid : userMap.getAllUniqueUsers()) {
            try {
                User user = getUser(uuid);
                if (user == null) continue;
                if (user.getName() == null) continue;
                if (ATPlayer.isPlayerCached(user.getName())) {
                    ATPlayer player = ATPlayer.getPlayer(user.getName());
                    for (String home : player.getHomes().keySet()) {
                        user.setHome(home, player.getHome(home).getLocation());
                    }
                } else {
                    try (Connection connection = HomeSQLManager.get().implementConnection()) {
                        PreparedStatement statement = connection.prepareStatement("SELECT home, x, y, z, yaw, pitch, world FROM " + SQLManager.getTablePrefix() + "_homes WHERE uuid_owner = ?");
                        statement.setString(1, uuid.toString());
                        ResultSet set = statement.executeQuery();
                        connection.close();
                        while (set.next()) {
                            String name = set.getString("home");
                            double[] pos = new double[]{set.getDouble("x"), set.getDouble("y"), set.getDouble("z")};
                            float[] rot = new float[]{set.getFloat("yaw"), set.getFloat("pitch")};
                            String world = set.getString("world");
                            user.setHome(name, new Location(Bukkit.getWorld(world), pos[0], pos[1], pos[2], rot[0], rot[1]));
                        }
                    }

                }
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
                if (ATPlayer.isPlayerCached(user.getName())) {
                    ATPlayer player = ATPlayer.getPlayer(user.getName());
                    user.setLastLocation(player.getPreviousLocation());
                } else {
                    try (Connection connection = HomeSQLManager.get().implementConnection()) {
                        PreparedStatement statement = connection.prepareStatement("SELECT x, y, z, yaw, pitch, world FROM " + SQLManager.getTablePrefix() + "_players WHERE uuid = ?");
                        statement.setString(1, uuid.toString());
                        ResultSet set = statement.executeQuery();
                        connection.close();
                        // should run once but this is just standard
                        while (set.next()) {
                            double[] pos = new double[]{set.getDouble("x"), set.getDouble("y"), set.getDouble("z")};
                            float[] rot = new float[]{set.getFloat("yaw"), set.getFloat("pitch")};
                            String world = set.getString("world");
                            if (world == null) {
                                CoreClass.getInstance().getLogger().warning("World for previous location of " + user.getName() + " is null. Cannot export it.");
                                continue;
                            }
                            user.setLastLocation(new Location(Bukkit.getWorld(world), pos[0], pos[1], pos[2], rot[0], rot[1]));
                        }
                    }
                }
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
        debug("WARNING - any changes made to the spawnpoints may be dodgy due to the differences between Essentials and AT's spawn systems.");
        debug("If you notice any problems, please fix them yourself.");
        EssentialsSpawn spawnPlugin = (EssentialsSpawn) Bukkit.getPluginManager().getPlugin("EssentialsSpawn");
        if (spawnPlugin == null || !spawnPlugin.isEnabled()) return;

        String mainSpawn = Spawn.get().getMainSpawn();
        if (mainSpawn != null) {
            Location mainSpawnLoc = Spawn.get().getSpawn(mainSpawn);
            spawnPlugin.setSpawn(mainSpawnLoc, "default");
        }
        for (String atSpawn : Spawn.get().getSpawns()) {
            for (String group : CoreClass.getPerms().getGroups()) {
                if (CoreClass.getPerms().groupHas((World) null, group, atSpawn)) {
                    Location spawnLoc = Spawn.get().getSpawn(atSpawn);
                    spawnPlugin.setSpawn(spawnLoc, group);
                }
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
                if (ATPlayer.isPlayerCached(user.getName())) {
                    ATPlayer player = ATPlayer.getPlayer(user.getName());
                    user.setTeleportEnabled(player.isTeleportationEnabled());
                } else {
                    try (Connection connection = HomeSQLManager.get().implementConnection()) {
                        PreparedStatement statement = connection.prepareStatement("SELECT teleportation_on FROM " + SQLManager.getTablePrefix() + "_players WHERE uuid = ?");
                        statement.setString(1, uuid.toString());
                        ResultSet set = statement.executeQuery();
                        // also should run once but this is also just standard
                        while (set.next()) {
                            user.setTeleportEnabled(set.getBoolean("teleportation_on"));
                        }
                    }
                }
            } catch (Exception ex) {
                debug("Failed to export player information for UUID " + uuid.toString() + ":");
                ex.printStackTrace();
            }

        }
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
