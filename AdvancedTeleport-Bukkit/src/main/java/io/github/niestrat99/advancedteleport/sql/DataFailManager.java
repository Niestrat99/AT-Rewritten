package io.github.niestrat99.advancedteleport.sql;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.AdvancedTeleportAPI;
import io.github.niestrat99.advancedteleport.api.Warp;
import io.github.niestrat99.advancedteleport.api.WorldlessLocation;
import io.github.niestrat99.advancedteleport.folia.RunnableManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class DataFailManager {

    private static DataFailManager instance;
    private final HashMap<Fail, Integer> pendingFails;
    private final File failCsv;

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
                    addFailure(operation, newData.toArray(new String[] {}));
                }
                failCsv.delete();
            } catch (IOException e) {
                CoreClass.getInstance()
                        .getLogger()
                        .severe("Failed to read the failure CSV file: " + e.getMessage());
                e.printStackTrace();
            }
        }

        RunnableManager.setupRunnerPeriodAsync(() -> {
            for (Fail fail : pendingFails.keySet()) {
                CoreClass.getInstance().getLogger().warning("Handling failure " + fail.operation.name() + ".");
                handleFailure(fail);
            }
        }, 1200, 1200);
    }

    public void addFailure(Operation operation, String... data) {
        Fail fail = new Fail(operation, data);
        if (!pendingFails.containsKey(fail)) {
            CoreClass.getInstance()
                    .getLogger()
                    .warning("SQL failure added for operation " + operation.name() + ".");
            pendingFails.put(fail, 1);
        }
    }

    public void handleFailure(Fail fail) {

        Runnable run = null;

        switch (fail.operation) {
            case ADD_HOME -> run =
                    () ->
                            HomeSQLManager.get()
                                    .addHome(
                                            locFromStrings(fail.data),
                                            UUID.fromString(fail.data[7]),
                                            fail.data[6]);
            case DELETE_HOME -> run =
                    () ->
                            HomeSQLManager.get()
                                    .removeHome(UUID.fromString(fail.data[0]), fail.data[1]);
            case MOVE_HOME -> run =
                    () ->
                            HomeSQLManager.get()
                                    .moveHome(
                                            locFromStrings(fail.data),
                                            UUID.fromString(fail.data[7]),
                                            fail.data[6]);
            case ADD_PLAYER -> {
                // TODO - apply this where necessary for other checks that require a UUID check.
                OfflinePlayer offlinePlayer =
                        Bukkit.getOfflinePlayer(UUID.fromString(fail.data[0]));
                if (offlinePlayer.getName() == null) {
                    CoreClass.getInstance()
                            .getLogger()
                            .warning(
                                    "Null name for " + fail.data[0] + ". Won't proceed with this.");
                    pendingFails.remove(fail);
                    return;
                }
                run = () -> PlayerSQLManager.get().addPlayer(offlinePlayer);
            }
            case UPDATE_PLAYER -> run =
                    () ->
                            PlayerSQLManager.get()
                                    .updatePlayerInformation(
                                            Bukkit.getOfflinePlayer(UUID.fromString(fail.data[0])));
            case CHANGE_TELEPORTATION -> run =
                    () ->
                            PlayerSQLManager.get()
                                    .setTeleportationOn(
                                            UUID.fromString(fail.data[0]),
                                            Boolean.parseBoolean(fail.data[1]));
            case SET_MAIN_HOME -> run =
                    () ->
                            PlayerSQLManager.get()
                                    .setMainHome(UUID.fromString(fail.data[1]), fail.data[0]);
            case ADD_BLOCK -> run =
                    () ->
                            BlocklistManager.get()
                                    .blockUser(fail.data[0], fail.data[1], fail.data[2]);
            case UNBLOCK -> run =
                    () -> BlocklistManager.get().unblockUser(fail.data[0], fail.data[1]);
            case ADD_WARP -> {
                Warp warp;
                if (AdvancedTeleportAPI.getWarps().get(fail.data[6]) != null) {
                    warp = AdvancedTeleportAPI.getWarps().get(fail.data[6]);
                } else {
                    warp =
                            new Warp(
                                    UUID.fromString(fail.data[7]),
                                    fail.data[6],
                                    locFromStrings(fail.data),
                                    Long.parseLong(fail.data[8]),
                                    Long.parseLong(fail.data[9]));
                }
                if (warp == null) return;
                run = () -> WarpSQLManager.get().addWarp(warp);
            }
            case MOVE_WARP -> run =
                    () -> WarpSQLManager.get().moveWarp(locFromStrings(fail.data), fail.data[6]);
            case DELETE_WARP -> run = () -> WarpSQLManager.get().removeWarp(fail.data[0]);
            case UPDATE_LOCATION -> run =
                    () ->
                            PlayerSQLManager.get()
                                    .setPreviousLocation(fail.data[6], locFromStrings(fail.data));
        }

        CompletableFuture.runAsync(run, CoreClass.async)
                .whenComplete(
                        (v, err) -> {
                            if (err != null) {
                                pendingFails.put(fail, pendingFails.get(fail) + 1);
                            } else {
                                pendingFails.remove(fail);
                            }
                        });
    }

    private @NotNull WorldlessLocation locFromStrings(String... data) {
        if (data.length < 6) {
            throw new IllegalArgumentException(
                    "Not enough arguments to get a location! " + Arrays.toString(data));
        }
        String worldStr = data[0];
        double x = Double.parseDouble(data[1]);
        double y = Double.parseDouble(data[2]);
        double z = Double.parseDouble(data[3]);
        float yaw = Float.parseFloat(data[4]);
        float pitch = Float.parseFloat(data[5]);
        return new WorldlessLocation(worldStr, x, y, z, yaw, pitch);
    }

    public static DataFailManager get() {
        return instance;
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
                        builder.append(",").append(data.replaceAll(",", "\\\\,"));
                    }
                    writer.write(builder.toString());
                    writer.write("\n");
                }
                writer.close();
            } else {
                failCsv.delete();
            }
        } catch (IOException e) {
            CoreClass.getInstance()
                    .getLogger()
                    .severe("Failed to write to the failure CSV file: " + e.getMessage());
            e.printStackTrace();
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

    public static class Fail {

        private final String[] data;
        private final Operation operation;

        public Fail(Operation operation, String... data) {
            this.data = data;
            this.operation = operation;
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(operation);
            result = 31 * result + Arrays.hashCode(data);
            return result;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Fail fail = (Fail) o;
            return Arrays.equals(data, fail.data) && operation == fail.operation;
        }
    }
}
