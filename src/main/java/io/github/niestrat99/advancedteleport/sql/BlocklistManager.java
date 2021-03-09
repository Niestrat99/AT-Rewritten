package io.github.niestrat99.advancedteleport.sql;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.BlockInfo;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class BlocklistManager extends SQLManager {

    private static BlocklistManager instance;

    public BlocklistManager() {
        super();
        instance = this;
    }

    @Override
    public void createTable() {
        Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), () -> {
            try {
                PreparedStatement createTable = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + tablePrefix + "_blocklist " +
                        "(id INTEGER PRIMARY KEY " + getStupidAutoIncrementThing() + ", " +
                        "uuid_receiver VARCHAR(256) NOT NULL, " +
                        "uuid_blocked VARCHAR(256) NOT NULL," +
                        "timestamp BIGINT NOT NULL," +
                        "reason TEXT)");
                createTable.executeUpdate();
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
            transferOldData();
        });

    }

    @Override
    public void transferOldData() {
        //
        File blocklistFile = new File(CoreClass.getInstance().getDataFolder(), "blocklist.yml");
        if (!blocklistFile.exists()) return;
        // Load the config file.
        YamlConfiguration blocklist = YamlConfiguration.loadConfiguration(blocklistFile);

        ConfigurationSection playersSection = blocklist.getConfigurationSection("players");
        if (playersSection != null) {
            // For each player found...
            for (String player : playersSection.getKeys(false)) {
                // Get the list for the player blocked.
                List<String> blockedPlayers = blocklist.getStringList("players." + player);
                // For each blocked player...
                for (String blockedPlayer : blockedPlayers) {
                    // Reasons didn't exist pre-5.4, so the reason is null.
                    blockUser(player, blockedPlayer, null, null);
                }
            }
        }

        blocklistFile.renameTo(new File(CoreClass.getInstance().getDataFolder(), "blocklist-backup.yml"));
    }

    public void blockUser(String receiverUUID, String blockedUUID, String reason, SQLCallback<Boolean> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), () -> {
            try {
                PreparedStatement statement;
                if (reason != null) {
                    statement = connection.prepareStatement(
                            "INSERT INTO " + tablePrefix + "_blocklist (uuid_receiver, uuid_blocked, timestamp, reason) VALUES (?, ?, ?, ?)");
                    statement.setString(4, reason);
                } else {
                    statement = connection.prepareStatement(
                            "INSERT INTO " + tablePrefix + "_blocklist (uuid_receiver, uuid_blocked, timestamp) VALUES (?, ?, ?)");
                }
                statement.setString(1, receiverUUID);
                statement.setString(2, blockedUUID);
                statement.setLong(3, System.currentTimeMillis());
                statement.executeUpdate();
                if (callback != null) {
                    callback.onSuccess(true);
                }
            } catch (SQLException exception) {
                DataFailManager.get().addFailure(DataFailManager.Operation.ADD_BLOCK, receiverUUID, blockedUUID, reason);
                callback.onFail();
                exception.printStackTrace();
            }
        });

    }

    public void unblockUser(String receiverUUID, String blockedUUID, SQLCallback<Boolean> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), () -> {
            try {
                PreparedStatement statement = connection.prepareStatement("DELETE FROM " + tablePrefix + "_blocklist WHERE uuid_receiver = ? AND uuid_blocked = ?");
                statement.setString(1, receiverUUID);
                statement.setString(2, blockedUUID);
                statement.executeUpdate();
                if (callback != null) {
                    callback.onSuccess(true);
                }
            } catch (SQLException exception) {
                DataFailManager.get().addFailure(DataFailManager.Operation.UNBLOCK, receiverUUID, blockedUUID);
                callback.onFail();
                exception.printStackTrace();
            }
        });
    }

    public void getBlockedPlayers(String receiverUUID, SQLCallback<HashMap<UUID, BlockInfo>> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), () -> {
            try {
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + tablePrefix + "_blocklist WHERE uuid_receiver = ?");
                statement.setString(1, receiverUUID);
                ResultSet results = statement.executeQuery();
                // Create a list for all blocked players.
                HashMap<UUID, BlockInfo> blockedPlayers = new HashMap<>();
                // For each blocked player...
                while (results.next()) {
                    // Create the BI object.
                    BlockInfo blockInfo = new BlockInfo(UUID.fromString(receiverUUID),
                            UUID.fromString(results.getString("uuid_blocked")),
                            results.getString("reason"),
                            results.getLong("timestamp"));
                    // Add it to the list.
                    blockedPlayers.put(blockInfo.getBlockedUUID(), blockInfo);
                }
                // Go back to the main thread and return the list.
                Bukkit.getScheduler().runTask(CoreClass.getInstance(), () -> callback.onSuccess(blockedPlayers));
            } catch (SQLException exception) {
                exception.printStackTrace();
                callback.onFail();
            }
        });
    }

    public static BlocklistManager get() {
        return instance;
    }
}
