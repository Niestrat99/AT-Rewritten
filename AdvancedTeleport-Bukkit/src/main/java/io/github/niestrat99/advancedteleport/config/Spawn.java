package io.github.niestrat99.advancedteleport.config;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.thatsmusic99.configurationmaster.CMFile;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

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
        addLenientSection("spawns");
    }

    @Override
    public void moveToNew() {
        String defaultName = getString("spawnpoint.world");
        if (defaultName == null || defaultName.isEmpty()) return;
        moveTo("spawnpoint.x", "spawns." + defaultName + ".x");
        moveTo("spawnpoint.y", "spawns." + defaultName + ".y");
        moveTo("spawnpoint.z", "spawns." + defaultName + ".z");
        moveTo("spawnpoint.yaw", "spawns." + defaultName + ".yaw");
        moveTo("spawnpoint.pitch", "spawns." + defaultName + ".pitch");
        set("spawnpoint.world", null);
    }

    @Override
    public void postSave() {
        ConfigurationSection spawns = getConfig().getConfigurationSection("spawns");
        if (spawns == null) return;
        if (spawns.getKeys(false).isEmpty()) return;
        String key = spawns.getKeys(false).iterator().next();
        mainSpawn = new Location(Bukkit.getWorld(key),
                getDouble("spawns." + key + ".x"),
                getDouble("spawns." + key + ".y"),
                getDouble("spawns." + key + ".z"),
                getFloat("spawns." + key + ".yaw"),
                getFloat("spawns." + key + ".pitch"));
    }

    public void setSpawn(Location location) {
        String worldName = location.getWorld().getName();
        set("spawns." + worldName + ".x", location.getX());
        set("spawns." + worldName + ".y", location.getY());
        set("spawns." + worldName + ".z", location.getZ());
        set("spawns." + worldName + ".yaw", location.getYaw());
        set("spawns." + worldName + ".pitch", location.getPitch());
        save(true);
        if (mainSpawn == null) {
            mainSpawn = location;
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
                        set("spawns." + from + ".mirror", mirror);
                        save(true);
                        return "Success";
                    } else {
                        return "NoSuchSpawn";
                    }
                } else {
                    return "NoSuchSpawn";
                }
            }

        } else {
            return "NoSuchSpawn";
        }
    }

    public Location getSpawn(Player player) {
        String worldName = player.getWorld().getName();
        // Would do less looping
        for (World world : Bukkit.getWorlds()) {
            if (player.hasPermission("at.member.spawn." + world.getName())
                    && player.isPermissionSet("at.member.spawn." + world.getName())) {
                worldName = world.getName();
            }
        }
        ConfigurationSection spawns = getConfig().getConfigurationSection("spawns");
        if (spawns != null && spawns.contains(worldName)) {
            ConfigurationSection toSection = spawns.getConfigurationSection(worldName);
            while (true) {
                if (toSection != null) {
                    if (toSection.getString("mirror") != null && !toSection.getString("mirror").isEmpty()) {
                        worldName = toSection.getString("mirror");
                        toSection = spawns.getConfigurationSection(worldName);
                    } else if (toSection.contains("x")
                            && toSection.contains("y")
                            && toSection.contains("z")
                            && toSection.contains("yaw")
                            && toSection.contains("pitch")) {
                        return new Location(Bukkit.getWorld(worldName),
                                getDouble("spawns." + worldName + ".x"),
                                getDouble("spawns." + worldName + ".y"),
                                getDouble("spawns." + worldName + ".z"),
                                getFloat("spawns." + worldName + ".yaw"),
                                getFloat("spawns." + worldName + ".pitch"));
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            }
        }
        return mainSpawn;
    }

    public static Spawn get() {
        return instance;
    }
}
