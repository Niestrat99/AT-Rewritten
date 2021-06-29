package io.github.niestrat99.advancedteleport.config;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.thatsmusic99.configurationmaster.CMFile;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Spawn extends CMFile {

    private static Spawn instance;
    private Location mainSpawn;

    public Spawn() {
        super(CoreClass.getInstance(), "spawn");
        instance = this;
        load();
    }

    @Override
    public void loadTitle() {

    }

    @Override
    public void loadDefaults() {
        addDefault("main-spawn", "");
        addLenientSection("spawns");
    }

    @Override
    public void moveToNew() {
        String defaultName = getString("spawnpoint.world");
        if (defaultName == null || defaultName.isEmpty()) return;
        moveTo("spawnpoint.x", "spawns." + defaultName + ".x");
        moveTo("spawnpoint.y", "spawns." + defaultName + ".y");
        moveTo("spawnpoint.z", "spawns." + defaultName + ".z");
        moveTo("spawnpoint.world", "spawns." + defaultName + ".world");
        moveTo("spawnpoint.yaw", "spawns." + defaultName + ".yaw");
        moveTo("spawnpoint.pitch", "spawns." + defaultName + ".pitch");
        set("main-spawn", defaultName);
    }

    @Override
    public void postSave() {
        String mainSpawn = getString("main-spawn", "");
        ConfigurationSection spawns = getConfig().getConfigurationSection("spawns");
        if (spawns == null) return;
        if (!spawns.contains(mainSpawn) || mainSpawn.isEmpty()) return;
        this.mainSpawn = new Location(Bukkit.getWorld(getString("spawns." + mainSpawn + ".world")),
                getDouble("spawns." + mainSpawn + ".x"),
                getDouble("spawns." + mainSpawn + ".y"),
                getDouble("spawns." + mainSpawn + ".z"),
                getFloat("spawns." + mainSpawn + ".yaw"),
                getFloat("spawns." + mainSpawn + ".pitch"));
    }

    public void setSpawn(Location location, String name) {
        set("spawns." + name + ".x", location.getX());
        set("spawns." + name + ".y", location.getY());
        set("spawns." + name + ".z", location.getZ());
        set("spawns." + name + ".world", location.getWorld().getName());
        set("spawns." + name + ".yaw", location.getYaw());
        set("spawns." + name + ".pitch", location.getPitch());
        set("spawns." + name + ".mirror", null);
        save(true);
        if (mainSpawn == null) {
            setMainSpawn(name, location);
        }
    }

    public String mirrorSpawn(String from, String to) {
        ConfigurationSection section = getConfig().getConfigurationSection("spawns");
        String mirror = to;
        if (section != null && section.contains(to)) {
            ConfigurationSection toSection = section.getConfigurationSection(to);
            while (true) {
                if (toSection != null) {
                    if (toSection.getString("mirror") != null && !toSection.getString("mirror").isEmpty()) {
                        // honest to god intellij shut up
                        mirror = toSection.getString("mirror");
                        toSection = section.getConfigurationSection(mirror);
                    } else if (toSection.contains("x")
                            && toSection.contains("y")
                            && toSection.contains("z")
                            && toSection.contains("yaw")
                            && toSection.contains("pitch")) {
                        set("spawns." + from, null);
                        set("spawns." + from + ".mirror", mirror);
                        save(true);
                        return "Info.mirroredSpawn";
                    } else {
                        return "Error.noSuchSpawn";
                    }
                } else {
                    return "Error.noSuchSpawn";
                }
            }

        } else {
            return "Error.noSuchSpawn";
        }
    }

    public Location getSpawn(Player player) {
        String worldName = player.getWorld().getName();
        // Would do less looping
        for (String spawn : getSpawns()) {
            // Weird annoying bug >:(
            if (player.hasPermission("at.member.spawn." + spawn)
                    && player.isPermissionSet("at.member.spawn." + spawn)) {
                worldName = spawn;
            }
        }
        return getSpawn(worldName);
    }

    public Location getSpawn(String name) {
        if (getConfig().get("spawns." + name) == null) return getProperMainSpawn();
        ConfigurationSection spawns = getConfig().getConfigurationSection("spawns");
        ConfigurationSection toSection = spawns.getConfigurationSection(name);
        while (true) {
            if (toSection != null) {
                if (toSection.getString("mirror") != null && !toSection.getString("mirror").isEmpty()) {
                    name = toSection.getString("mirror");
                    toSection = spawns.getConfigurationSection(name);
                } else if (toSection.contains("x")
                        && toSection.contains("y")
                        && toSection.contains("z")
                        && toSection.contains("yaw")
                        && toSection.contains("pitch")
                        && toSection.contains("world")) {
                    return new Location(Bukkit.getWorld(toSection.getString("world")),
                            toSection.getDouble("x"),
                            toSection.getDouble("y"),
                            toSection.getDouble("z"),
                            (float) toSection.getDouble( "yaw"),
                            (float) toSection.getDouble("pitch"));
                } else {
                    break;
                }
            } else {
                break;
            }
        }
        return mainSpawn;
    }

    public String setMainSpawn(String id, Location location) {
        mainSpawn = location;
        set("main-spawn", id);
        save(true);
        return "Info.setMainSpawn";
    }

    public String getMainSpawn() {
        return getString("main-spawn");
    }

    public String removeSpawn(String id) {
        set("spawns." + id, null);
        save(true);
        return "Info.removedSpawn";
    }

    public Location getProperMainSpawn() {
        if (mainSpawn == null || mainSpawn.getWorld() == null) {
            mainSpawn = new Location(Bukkit.getWorld(getString("spawns." + getMainSpawn() + ".world")),
                    getDouble("spawns." + getMainSpawn() + ".x"),
                    getDouble("spawns." + getMainSpawn() + ".y"),
                    getDouble("spawns." + getMainSpawn() + ".z"),
                    getFloat("spawns." + getMainSpawn() + ".yaw"),
                    getFloat("spawns." + getMainSpawn() + ".pitch"));
        }
        return mainSpawn;
    }

    public boolean doesSpawnExist(String id) {
        return get("spawns." + id) != null;
    }

    public List<String> getSpawns() {
        ConfigurationSection section = getConfig().getConfigurationSection("spawns");
        if (section == null) return new ArrayList<>();
        return new ArrayList<>(section.getKeys(false));
    }

    public static Spawn get() {
        return instance;
    }
}
