package io.github.niestrat99.advancedteleport.hooks.imports;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.api.Warp;
import io.github.niestrat99.advancedteleport.config.Spawn;
import io.github.niestrat99.advancedteleport.hooks.ImportExportPlugin;
import io.github.niestrat99.advancedteleport.sql.HomeSQLManager;
import io.github.niestrat99.advancedteleport.sql.PlayerSQLManager;
import io.github.niestrat99.advancedteleport.sql.SQLManager;
import io.github.niestrat99.advancedteleport.sql.WarpSQLManager;
import io.github.niestrat99.advancedteleport.utilities.ConditionChecker;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class EssentialsHook extends ImportExportPlugin {

    @Override
    public boolean canImport() {
        Plugin essentials = Bukkit.getPluginManager().getPlugin("Essentials");
        // Makes sure the plugin exists and because there's so many Ess clones out there, ensure it's the right one
        return essentials != null && essentials.getDescription().getMain().equals("com.earth2me.essentials.Essentials");
    }

    @Override
    public void importHomes() {
        debug("Importing homes...");
        Plugin essentials = Bukkit.getPluginManager().getPlugin("Essentials");
        File userFolder = new File(essentials.getDataFolder(), "userdata");
        if (!userFolder.exists() || !userFolder.isDirectory() || userFolder.listFiles() == null) {
            debug("User data folder doesn't exist/wasn't found, skipping...");
            return;
        }

        for (File file : userFolder.listFiles()) {
            YamlConfiguration user = YamlConfiguration.loadConfiguration(file);
            UUID uuid = UUID.fromString(file.getName().substring(0, file.getName().lastIndexOf(".")));
            ConfigurationSection homes = user.getConfigurationSection("homes");
            if (homes == null) continue;
            for (String home : homes.getKeys(false)) {
                ConfigurationSection homeSection = homes.getConfigurationSection(home);
                String name = user.getString("lastAccountName");
                if (homeSection == null) continue;
                Location loc = getLocationFromSection(homeSection);
                if (name != null && ATPlayer.getPlayer(name) != null) {
                    ATPlayer.getPlayer(name).addHome(name, loc, null);
                } else {
                    try {
                        PreparedStatement query = SQLManager.getConnection().prepareStatement("SELECT uuid_owner FROM ? WHERE uuid-owner=? AND home=?");
                        query.setString(1, SQLManager.getTablePrefix() + "_homes");
                        query.setString(2, uuid.toString());
                        query.setString(3, home);

                        if (query.executeQuery().next()) {
                            HomeSQLManager.get().moveHome(loc, uuid, home, null);
                        } else {
                            HomeSQLManager.get().addHome(loc, uuid, home, null);
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }

                }
            }
        }
        debug("Finished importing homes!");
    }

    @Override
    public void importLastLocations() {
        debug("Importing last locations...");
        Plugin essentials = Bukkit.getPluginManager().getPlugin("Essentials");
        File userFolder = new File(essentials.getDataFolder(), "userdata");
        if (!userFolder.exists() || !userFolder.isDirectory() || userFolder.listFiles() == null) {
            debug("User data folder doesn't exist/wasn't found, skipping...");
            return;
        }

        for (File file : userFolder.listFiles()) {
            YamlConfiguration user = YamlConfiguration.loadConfiguration(file);
            String name = user.getString("lastAccountName");
            ConfigurationSection lastLoc = user.getConfigurationSection("lastlocation");
            if (lastLoc == null) continue;
            if (name != null && ATPlayer.getPlayer(name) != null) {
                ATPlayer.getPlayer(name).setPreviousLocation(getLocationFromSection(lastLoc));
            } else {
                PlayerSQLManager.get().setPreviousLocation(name, getLocationFromSection(lastLoc), null);
            }
        }
        debug("Finished importing last locations!");
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
                try {
                    PreparedStatement query = SQLManager.getConnection().prepareStatement("SELECT uuid_creator FROM ? WHERE uuid-creator=? AND warp=?");
                    query.setString(1, SQLManager.getTablePrefix() + "_warps");
                    query.setString(2, creatorStr);
                    query.setString(3, name);

                    if (query.executeQuery().next()) {
                        WarpSQLManager.get().moveWarp(loc, name, null);
                    } else {
                        WarpSQLManager.get().addWarp(new Warp(creator, name, loc, created, updated), null);
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
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
        Plugin essentials = Bukkit.getPluginManager().getPlugin("Essentials");
        File userFolder = new File(essentials.getDataFolder(), "userdata");
        if (!userFolder.exists() || !userFolder.isDirectory() || userFolder.listFiles() == null) {
            debug("User data folder doesn't exist/wasn't found, skipping...");
            return;
        }

        for (File file : userFolder.listFiles()) {
            YamlConfiguration user = YamlConfiguration.loadConfiguration(file);
            String name = user.getString("lastAccountName");
            UUID uuid = UUID.fromString(file.getName().substring(0, file.getName().lastIndexOf(".")));
            if (name != null && ATPlayer.getPlayer(name) != null) {
                ATPlayer.getPlayer(name).setTeleportationEnabled(user.getBoolean("teleportenabled", true), null);
            } else {
                PlayerSQLManager.get().setTeleportationOn(uuid, user.getBoolean("teleportenabled", true), null);
            }
        }
        debug("Imported player information!");
    }

    @Override
    public void exportHomes() {
        debug("Exporting homes...");
        debug("WARNING: Essentials does not have a \"main home\" system so all main homes in AT will be ignored when exporting.");
        Plugin essentials = Bukkit.getPluginManager().getPlugin("Essentials");
        File userFolder = new File(essentials.getDataFolder(), "userdata");
        if (!userFolder.exists()) {
            userFolder.mkdirs();
        }
        try {
            PreparedStatement statement = SQLManager.getConnection().prepareStatement("SELECT uuid_owner, home, x, y, z, yaw, pitch, world FROM ?");
            statement.setString(1, SQLManager.getTablePrefix() + "_homes");
            ResultSet set = statement.executeQuery();

            HashMap<UUID, YamlConfiguration> configFiles = new HashMap<>();
            while (set.next()) {
                UUID uuid = UUID.fromString(set.getString("uuid_owner"));
                String name = set.getString("home");
                double[] pos = new double[]{set.getDouble("x"), set.getDouble("y"), set.getDouble("z")};
                float[] rot = new float[]{set.getFloat("yaw"), set.getFloat("pitch")};
                String world = set.getString("world");

                YamlConfiguration userConf;

                if (!configFiles.containsKey(uuid)) {
                    File userFile = new File(userFolder, uuid + ".yml");
                    if (!userFile.exists()) {
                        try {
                            userFile.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                            continue;
                        }
                    }

                    userConf = YamlConfiguration.loadConfiguration(userFile);
                    configFiles.put(uuid, userConf);
                } else {
                    userConf = configFiles.get(uuid);
                }

                ConfigurationSection homes = userConf.getConfigurationSection("homes");
                if (homes == null) {
                    homes = userConf.createSection("homes");
                }

                ConfigurationSection home = homes.createSection(name);
                home.set("x", pos[0]);
                home.set("y", pos[1]);
                home.set("z", pos[2]);
                home.set("yaw", rot[0]);
                home.set("pitch", rot[1]);
                home.set("world", world);
            }

            for (UUID uuid : configFiles.keySet()) {
                configFiles.get(uuid).save(new File(userFolder, uuid + ".yml"));
            }
            configFiles.clear();
        } catch (SQLException | IOException exception) {
            exception.printStackTrace();
        }
        debug("Finished exporting homes!");
    }

    @Override
    public void exportLastLocations() {
        debug("Exporting previous locations...");

        Plugin essentials = Bukkit.getPluginManager().getPlugin("Essentials");
        File userFolder = new File(essentials.getDataFolder(), "userdata");
        if (!userFolder.exists()) {
            userFolder.mkdirs();
        }

        try {
            PreparedStatement statement = SQLManager.getConnection().prepareStatement("SELECT uuid, x, y, z, yaw, pitch, world FROM ?");
            statement.setString(1, SQLManager.getTablePrefix() + "_players");
            ResultSet set = statement.executeQuery();

            while (set.next()) {
                UUID uuid = UUID.fromString(set.getString("uuid"));
                double[] pos = new double[]{set.getDouble("x"), set.getDouble("y"), set.getDouble("z")};
                float[] rot = new float[]{set.getFloat("yaw"), set.getFloat("pitch")};
                String world = set.getString("world");

                File userFile = new File(userFolder, uuid.toString() + ".yml");
                if (!userFile.exists()) {
                    try {
                        userFile.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                        continue;
                    }
                }

                YamlConfiguration userConf = YamlConfiguration.loadConfiguration(userFile);
                userConf.set("lastlocation.world", world);
                userConf.set("lastlocation.x", pos[0]);
                userConf.set("lastlocation.y", pos[1]);
                userConf.set("lastlocation.z", pos[2]);
                userConf.set("lastlocation.yaw", rot[0]);
                userConf.set("lastlocation.pitch", rot[1]);
                userConf.save(userFile);
            }
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void exportWarps() {
        debug("Exporting warps...");
        Plugin essentials = Bukkit.getPluginManager().getPlugin("Essentials");
        File warpsFolder = new File(essentials.getDataFolder(), "warps");
        if (!warpsFolder.exists()) {
            warpsFolder.mkdirs();
        }
        try {
            PreparedStatement statement = SQLManager.getConnection().prepareStatement("SELECT * FROM ?");
            statement.setString(1, SQLManager.getTablePrefix() + "_warps");
            ResultSet set = statement.executeQuery();

            while (set.next()) {
                UUID uuid = UUID.fromString(set.getString("uuid_creator"));
                String name = set.getString("warp");
                double[] pos = new double[]{set.getDouble("x"), set.getDouble("y"), set.getDouble("z")};
                float[] rot = new float[]{set.getFloat("yaw"), set.getFloat("pitch")};
                String world = set.getString("world");

                File warpsFile = new File(warpsFolder, name + ".yml");
                if (!warpsFile.exists()) {
                    try {
                        warpsFile.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                        continue;
                    }
                }
                YamlConfiguration warpConf = YamlConfiguration.loadConfiguration(warpsFile);

                warpConf.set("x", pos[0]);
                warpConf.set("y", pos[1]);
                warpConf.set("z", pos[2]);
                warpConf.set("yaw", rot[0]);
                warpConf.set("pitch", rot[1]);
                warpConf.set("world", world);
                warpConf.set("name", name);
                warpConf.set("lastowner", uuid);

                warpConf.save(warpsFile);
            }

        } catch (SQLException | IOException exception) {
            exception.printStackTrace();
        }
        debug("Finished exporting warps!");
    }

    @Override
    public void exportSpawn() {
        debug("Exporting spawnpoints...");
        debug("WARNING - any changes made to the spawnpoints may be dodgy due to the differences between Essentials and AT's spawn systems.");
        debug("If you notice any problems, please fix them yourself.");
        Plugin essentials = Bukkit.getPluginManager().getPlugin("Essentials");
        File spawnFile = new File(essentials.getDataFolder(), "spawn");
        if (!spawnFile.exists()) {
            try {
                spawnFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        YamlConfiguration spawn = YamlConfiguration.loadConfiguration(spawnFile);
        String mainSpawn = Spawn.get().getMainSpawn();
        if (mainSpawn != null) {
            Location mainSpawnLoc = Spawn.get().getSpawn(mainSpawn);
            spawn.set("spawns.default.world", mainSpawnLoc.getWorld().getName());
            spawn.set("spawns.default.x", mainSpawnLoc.getX());
            spawn.set("spawns.default.y", mainSpawnLoc.getY());
            spawn.set("spawns.default.z", mainSpawnLoc.getZ());
            spawn.set("spawns.default.yaw", mainSpawnLoc.getYaw());
            spawn.set("spawns.default.pitch", mainSpawnLoc.getPitch());
        }
        for (String atSpawn : Spawn.get().getSpawns()) {
            for (String group : CoreClass.getPerms().getGroups()) {
                if (CoreClass.getPerms().groupHas((World) null, group, atSpawn)) {
                    Location spawnLoc = Spawn.get().getSpawn(atSpawn);
                    spawn.set("spawns." + group + ".world", spawnLoc.getWorld().getName());
                    spawn.set("spawns." + group + ".x", spawnLoc.getX());
                    spawn.set("spawns." + group + ".y", spawnLoc.getY());
                    spawn.set("spawns." + group + ".z", spawnLoc.getZ());
                    spawn.set("spawns." + group + ".yaw", spawnLoc.getYaw());
                    spawn.set("spawns." + group + ".pitch", spawnLoc.getPitch());
                }
            }
        }
        try {
            spawn.save(spawnFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        debug("Finished exporting spawns!");
    }

    @Override
    public void exportPlayerInformation() {
        debug("Exporting player information...");
        Plugin essentials = Bukkit.getPluginManager().getPlugin("Essentials");
        File userFolder = new File(essentials.getDataFolder(), "userdata");
        if (!userFolder.exists()) {
            userFolder.mkdirs();
        }

        try {
            PreparedStatement statement = SQLManager.getConnection().prepareStatement("SELECT uuid, teleportation_on FROM ?");
            statement.setString(1, SQLManager.getTablePrefix() + "_players");
            ResultSet set = statement.executeQuery();

            while (set.next()) {

            }

        } catch (SQLException ex) {
            ex.printStackTrace();
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

    private static ConfigurationSection getSectionFromLocation(Location location, ConfigurationSection parent, String name) {
        ConfigurationSection section = parent.createSection(name);
        section.set("world", location.getWorld().getName());
        section.set("x", location.getX());
        section.set("y", location.getY());
        section.set("z", location.getZ());
        section.set("yaw", location.getYaw());
        section.set("pitch", location.getPitch());
        return section;
    }
}
