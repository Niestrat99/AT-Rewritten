package io.github.at.config;

import org.bukkit.Bukkit;
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
    public static File ConfigFile = new File("plugins/TBH_Teleport","Blocklist.yml");

    public static FileConfiguration Config = YamlConfiguration.loadConfiguration(ConfigFile);

    public static void save() throws IOException {
        Config.save(ConfigFile);
    }
    public static void addBlockedPlayer(Player bpl, Player target) throws IOException {
        if (getBlockedPlayers(bpl).size()>0){
            List<String> players = Config.getStringList("players." + bpl.getUniqueId().toString());
            players.add(target.getUniqueId().toString());
            Config.set("players." + bpl.getUniqueId().toString(), players);
        } else {
            Config.set("players." + bpl.getUniqueId().toString(), new ArrayList<>(Collections.singleton(target.getUniqueId().toString())));
        }save();
    }
    public static List<Player> getBlockedPlayers(Player target){
        List<Player> players = new ArrayList<>();
        for (String uniqueID:Config.getStringList("players." + target.getUniqueId().toString())){
            Player player = Bukkit.getPlayer(UUID.fromString(uniqueID));
            players.add(player);
        }
        return players;
    }
    public static void remBlockedPlayer(Player rpl, Player target) throws IOException {
        List<String> players = Config.getStringList("players." + rpl.getUniqueId().toString());
        players.remove((target.getUniqueId().toString()));
        Config.set("players." + rpl.getUniqueId().toString(), players);
        save();
    }

}
