package io.github.niestrat99.advancedteleport.sql;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.Warp;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.*;
import java.util.*;

public class DataFailManager {

    private HashMap<Fail, Integer> pendingFails;
    private static DataFailManager instance;
    private File failCsv;

    public DataFailManager() {
        pendingFails = new HashMap<>();
        instance = this;
        failCsv = new File(CoreClass.getInstance().getDataFolder(), "fails.csv");

        if (failCsv.exists()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(failCsv));
                String currentLine;
                while ((currentLine = reader.readLine()) != null) {
                    String[] data = currentLine.split(",");
                    Operation operation = Operation.valueOf(data[0]);
                    List<String> newData = new ArrayList<>();
                    for (int i = 1; i < data.length; i++) {
                        StringBuilder wholeData = new StringBuilder();
                        wholeData.append(data[i]);
                        while (i + 1 < data.length && data[i].endsWith("\\")) {
                            wholeData.append(data[i + 1]);
                            i++;
                        }
                        newData.add(wholeData.toString());
                    }
                    addFailure(operation, newData.toArray(new String[]{}));
                }
                failCsv.delete();
            } catch (IOException e) {
                CoreClass.getInstance().getLogger().severe("Failed to read the failure CSV file: " + e.getMessage());
                e.printStackTrace();
            }

        }

        Bukkit.getScheduler().runTaskTimerAsynchronously(CoreClass.getInstance(), () -> {
            for (Fail fail : pendingFails.keySet()) {
                CoreClass.getInstance().getLogger().warning("Handling failure " + fail.operation.name() + ".");
                handleFailure(fail);
            }
        }, 1200, 1200);
    }

    public void addFailure(Operation operation, String... data) {
        Fail fail = new Fail(operation, data);
        if (!pendingFails.containsKey(fail)) {
            CoreClass.getInstance().getLogger().warning("SQL failure added for operation " + operation.name() + ".");
            pendingFails.put(fail, 1);
        }

    }

    public void handleFailure(Fail fail) {

        SQLManager.SQLCallback<Boolean> callback = new SQLManager.SQLCallback<Boolean>() {

            @Override
            public void onSuccess(Boolean data) {
                pendingFails.remove(fail);
            }

            @Override
            public void onFail() {
                pendingFails.put(fail, pendingFails.get(fail) + 1);
            }
        };

        switch (fail.operation) {
            case ADD_HOME:
                HomeSQLManager.get().addHome(locFromStrings(fail.data),
                        UUID.fromString(fail.data[7]),
                        fail.data[6], callback);
                break;

            case DELETE_HOME:
                HomeSQLManager.get().removeHome(UUID.fromString(fail.data[0]), fail.data[1], callback);
                break;
            case MOVE_HOME:
                HomeSQLManager.get().moveHome(locFromStrings(fail.data),
                        UUID.fromString(fail.data[7]),
                        fail.data[6], callback);
                break;
            case ADD_PLAYER:
                PlayerSQLManager.get().addPlayer(Bukkit.getOfflinePlayer(UUID.fromString(fail.data[0])), callback);
                break;
            case UPDATE_PLAYER:
                PlayerSQLManager.get().updatePlayerInformation(Bukkit.getOfflinePlayer(UUID.fromString(fail.data[0])), callback);
                break;
            case CHANGE_TELEPORTATION:
                PlayerSQLManager.get().setTeleportationOn(UUID.fromString(fail.data[0]), Boolean.parseBoolean(fail.data[1]), callback);
                break;
            case SET_MAIN_HOME:
                PlayerSQLManager.get().setMainHome(UUID.fromString(fail.data[1]), fail.data[0], callback);
                break;
            case ADD_BLOCK:
                BlocklistManager.get().blockUser(fail.data[0], fail.data[1], fail.data[2], callback);
                break;
            case UNBLOCK:
                BlocklistManager.get().unblockUser(fail.data[0], fail.data[1], callback);
                break;
            case ADD_WARP:
                Warp warp;
                if (Warp.getWarps().get(fail.data[6]) != null) {
                    warp = Warp.getWarps().get(fail.data[6]);
                } else {
                    warp = new Warp(UUID.fromString(fail.data[7]), fail.data[6], locFromStrings(fail.data), Long.parseLong(fail.data[8]), Long.parseLong(fail.data[9]));
                }
                WarpSQLManager.get().addWarp(warp, callback);
                break;
            case MOVE_WARP:
                WarpSQLManager.get().moveWarp(locFromStrings(fail.data), fail.data[6], callback);
                break;
            case DELETE_WARP:
                WarpSQLManager.get().removeWarp(fail.data[0], callback);
                break;
            case UPDATE_LOCATION:
                PlayerSQLManager.get().setPreviousLocation(fail.data[6], locFromStrings(fail.data), callback);

        }
    }

    public void onDisable() {
        try {
            if (!pendingFails.isEmpty()) {
                if (!failCsv.exists()) {
                    failCsv.createNewFile();
                }
                BufferedWriter writer = new BufferedWriter(new FileWriter(failCsv));
                for (Fail fail : pendingFails.keySet()) {
                    StringBuilder builder = new StringBuilder();
                    builder.append(fail.operation.name());
                    for (String data : fail.data) {
                        builder.append(",").append(data.replaceAll(",", "\\,"));
                    }
                    writer.write(builder.toString());
                    writer.write("\n");
                }
                writer.close();
            } else {
                failCsv.delete();
            }
        } catch (IOException e) {
            CoreClass.getInstance().getLogger().severe("Failed to write to the failure CSV file: " + e.getMessage());
            e.printStackTrace();
        }

    }

    private Location locFromStrings(String... data) {
        if (data.length < 6) {
            throw new IllegalArgumentException("Not enough arguments to get a location! " + Arrays.toString(data));
        }
        String worldStr = data[0];
        World world = Bukkit.getWorld(worldStr);
        if (world == null) return null;
        double x = Double.parseDouble(data[1]);
        double y = Double.parseDouble(data[2]);
        double z = Double.parseDouble(data[3]);
        float yaw = Float.parseFloat(data[4]);
        float pitch = Float.parseFloat(data[5]);
        return new Location(world, x, y, z, yaw, pitch);
    }

    public static DataFailManager get() {
        return instance;
    }

    public static class Fail {

        private String[] data;
        private Operation operation;

        public Fail(Operation operation, String... data) {
            this.data = data;
            this.operation = operation;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Fail fail = (Fail) o;
            return Arrays.equals(data, fail.data) &&
                    operation == fail.operation;
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(operation);
            result = 31 * result + Arrays.hashCode(data);
            return result;
        }
    }

    public enum Operation {
        ADD_BLOCK,
        UNBLOCK,
        ADD_HOME,
        MOVE_HOME,
        DELETE_HOME,
        CHANGE_TELEPORTATION,
        UPDATE_PLAYER,
        ADD_PLAYER,
        UPDATE_LOCATION,
        SET_MAIN_HOME,
        ADD_WARP,
        MOVE_WARP,
        DELETE_WARP
    }
}
