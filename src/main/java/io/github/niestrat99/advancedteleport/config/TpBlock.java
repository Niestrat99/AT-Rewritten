package io.github.niestrat99.advancedteleport.config;

import io.github.niestrat99.advancedteleport.CoreClass;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;


public class TpBlock {
    public static File configFile = new File(CoreClass.getInstance().getDataFolder(),"blocklist.yml");

    public static FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

    public static void save() throws IOException {
        config.save(configFile);
    }
    public static void addBlockedPlayer(Player bpl, UUID target) throws IOException {
        if (getBlockedPlayers(bpl).size()>0){
            List<String> players = config.getStringList("players." + bpl.getUniqueId().toString());
            players.add(target.toString());
            config.set("players." + bpl.getUniqueId().toString(), players);
        } else {
            config.set("players." + bpl.getUniqueId().toString(), new ArrayList<>(Collections.singleton(target.toString())));
        }
        save();
    }
    public static List<UUID> getBlockedPlayers(Player target){
        List<UUID> players = new ArrayList<>();
        for (String uniqueID: config.getStringList("players." + target.getUniqueId().toString())){
            players.add(UUID.fromString(uniqueID));
        }
        return players;
    }
    public static void remBlockedPlayer(Player rpl, UUID target) throws IOException {
        List<String> players = config.getStringList("players." + rpl.getUniqueId().toString());
        players.remove((target.toString()));
        config.set("players." + rpl.getUniqueId().toString(), players);
        save();
    }

    public static void reloadBlocks() throws IOException {
        if (configFile == null) {
            configFile = new File(CoreClass.getInstance().getDataFolder(), "blocklist.yml");
        }
        config = YamlConfiguration.loadConfiguration(configFile);
        save();
    }

}
