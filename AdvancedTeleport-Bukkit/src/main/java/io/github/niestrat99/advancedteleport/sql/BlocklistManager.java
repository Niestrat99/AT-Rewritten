package io.github.niestrat99.advancedteleport.sql;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.BlockInfo;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.sql.Connection;
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

    public static BlocklistManager get() {
        return instance;
    }

    @Override
    public void createTable() {
        Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), () -> {

            CoreClass.debug("Creating table data for the block list manager if it is not already set up.");

            // Attempt to create the table.
            try (Connection connection = implementConnection()) {
                PreparedStatement createTable = prepareStatement(
                    connection,
                    "CREATE TABLE IF NOT EXISTS " + tablePrefix + "_blocklist " +
                        "(id INTEGER PRIMARY KEY " + getStupidAutoIncrementThing() + ", " +
                        "uuid_receiver VARCHAR(256) NOT NULL, " +
                        "uuid_blocked VARCHAR(256) NOT NULL," +
                        "timestamp BIGINT NOT NULL," +
                        "reason TEXT)"
                );
                executeUpdate(createTable);
            } catch (SQLException exception) {
                CoreClass.getInstance().getLogger().severe("Failed to create the blocklist table.");
                exception.printStackTrace();
            }

            // Transfer old data.
            transferOldData();
        });

    }

    @Override
    public void transferOldData() {

        CoreClass.debug("Transferring old blocklist data...");

        // Get the legacy blocklist file - if it doesn't exist, stop there.
        File blocklistFile = new File(CoreClass.getInstance().getDataFolder(), "blocklist.yml");
        if (!blocklistFile.exists()) {
            CoreClass.debug("No blocklist data to import.");
            return;
        }

        // Load the config file.
        YamlConfiguration blocklist = YamlConfiguration.loadConfiguration(blocklistFile);

        // Get the player section that stores all blocklist data.
        ConfigurationSection playersSection = blocklist.getConfigurationSection("players");
        if (playersSection != null) {
            // For each player found...
            for (String player : playersSection.getKeys(false)) {
                // Get the list for the player blocked.
                List<String> blockedPlayers = blocklist.getStringList("players." + player);
                // For each blocked player...
                for (String blockedPlayer : blockedPlayers) {
                    // Reasons didn't exist pre-5.4, so the reason is null.
                    blockUser(player, blockedPlayer, null);
                }
            }
        }

        // See if renaming was successful.
        boolean renameResult = blocklistFile.renameTo(new File(CoreClass.getInstance().getDataFolder(), "blocklist-backup.yml"));
        CoreClass.debug(renameResult ? "Successfully renamed the blocklist file." : "Failed to rename the blocklist file.");
    }

    public void blockUser(
            @NotNull String receiverUUID,
            @NotNull String blockedUUID,
            @Nullable String reason
    ) {
        try (Connection connection = implementConnection()) {
            PreparedStatement statement;
            if (reason != null) {
                statement = prepareStatement(
                    connection,
                    "INSERT INTO " + tablePrefix + "_blocklist (uuid_receiver, uuid_blocked, timestamp, reason) " +
                        "VALUES (?, ?, ?, ?)"
                );
                statement.setString(4, reason);
            } else {
                statement = prepareStatement(
                    connection,
                    "INSERT INTO " + tablePrefix + "_blocklist (uuid_receiver, uuid_blocked, timestamp) VALUES " +
                        "(?, ?, ?)"
                );
            }
            statement.setString(1, receiverUUID);
            statement.setString(2, blockedUUID);
            statement.setLong(3, System.currentTimeMillis());
            executeUpdate(statement);
        } catch (SQLException exception) {
            DataFailManager.get().addFailure(DataFailManager.Operation.ADD_BLOCK, receiverUUID, blockedUUID, reason);
            throw new RuntimeException(exception);
        }
    }

    public void unblockUser(
        String receiverUUID,
        String blockedUUID
    ) {
        try (Connection connection = implementConnection()) {
            PreparedStatement statement = prepareStatement(
                connection,
                "DELETE FROM " + tablePrefix + "_blocklist WHERE uuid_receiver = ? AND uuid_blocked = ?"
            );
            statement.setString(1, receiverUUID);
            statement.setString(2, blockedUUID);
            executeUpdate(statement);
        } catch (SQLException exception) {
            DataFailManager.get().addFailure(DataFailManager.Operation.UNBLOCK, receiverUUID, blockedUUID);
            throw new RuntimeException(exception);
        }
    }

    public HashMap<UUID, BlockInfo> getBlockedPlayers(String receiverUUID) {
        try (Connection connection = implementConnection()) {
            PreparedStatement statement = prepareStatement(
                connection,
                "SELECT * FROM " + tablePrefix + "_blocklist WHERE uuid_receiver = ?"
            );
            statement.setString(1, receiverUUID);
            ResultSet results = executeQuery(statement);
            // Create a list for all blocked players.
            HashMap<UUID, BlockInfo> blockedPlayers = new HashMap<>();
            // For each blocked player...
            while (results.next()) {
                // Create the BI object.
                BlockInfo blockInfo = new BlockInfo(
                    UUID.fromString(receiverUUID),
                    UUID.fromString(results.getString("uuid_blocked")),
                    results.getString("reason"),
                    results.getLong("timestamp")
                );
                // Add it to the list.
                blockedPlayers.put(blockInfo.getBlockedUUID(), blockInfo);
            }
            // Go back to the main thread and return the list.
            return blockedPlayers;
        } catch (SQLException exception) {
            throw new RuntimeException(exception);
        }
    }
}
