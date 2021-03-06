package io.github.niestrat99.advancedteleport.utilities.nbt;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class NBTReader {

    /*
     * Welcome to absolute Reflection hell! Enjoy your stay.
     */

    private static File MAIN_FOLDER;
    private static Object DEDICATED_SERVER;
    private static Object WORLD_NBT_STORAGE;
    private static HashMap<String, Location> CACHE;
    private static long lastModified;
    private static String VERSION;

    public static void init() {
        DEDICATED_SERVER = getDedicatedServer();
        MAIN_FOLDER = getPlayerdataFolder();

        if (MAIN_FOLDER == null) {
            System.out.println("Main world folder was not found.");
        } else {
            lastModified = MAIN_FOLDER.lastModified();
        }

        WORLD_NBT_STORAGE = getWorldNBTStorage();
        CACHE = new HashMap<>();
        VERSION = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
    }

    public static void addLeaveToCache(Player player) {
        CACHE.put(player.getName().toLowerCase(), player.getLocation());
    }

    public static void getLocation(String name, NBTCallback<Location> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), () -> {
            String lowerName = name.toLowerCase();
            if (CACHE.containsKey(lowerName) && MAIN_FOLDER.lastModified() == lastModified) {
                callback.onSuccess(CACHE.get(lowerName));
            } else {
                OfflinePlayer player = Bukkit.getOfflinePlayer(name);
                if (DEDICATED_SERVER != null) {
                    if (WORLD_NBT_STORAGE != null) {
                        try {

                            Location location = getLocation(player);
                            if (location == null) {
                                callback.onFail(CustomMessages.getString("Error.noOfflineLocation", "{player}", name));
                                return;
                            }
                            CACHE.put(lowerName, location);
                            callback.onSuccess(location);
                        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | NoSuchFieldException e) {
                            callback.onFail(CustomMessages.getString("Error.failedOfflineTeleport", "{player}", name));
                            e.printStackTrace();
                        }

                    }

                } else {
                    callback.onFail("Dedicated server does not exist.");
                }
            }
        });
    }

    public static void setLocation(String name, Location newLoc, NBTCallback<Boolean> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), () -> {
            String lowerName = name.toLowerCase();
            OfflinePlayer player = Bukkit.getOfflinePlayer(name);
            if (DEDICATED_SERVER != null) {
                if (WORLD_NBT_STORAGE != null) {
                    try {
                        setLocation(player, newLoc);
                        CACHE.put(lowerName, newLoc);
                        callback.onSuccess(true);
                    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | ClassNotFoundException | NoSuchFieldException | InstantiationException | FileNotFoundException e) {
                        e.printStackTrace();
                        callback.onFail(CustomMessages.getString("Error.failedOfflineTeleportHere", "{player}", name));
                    }
                }
            }
        });
    }

    /**
     * @see org.bukkit.craftbukkit.v1_16_R2.CraftServer - server object
     * @see net.minecraft.server.v1_16_R2.DedicatedServer - console object
     * @see net.minecraft.server.v1_16_R2.MinecraftServer - subclass of console
     * @see net.minecraft.server.v1_16_R2.WorldNBTStorage - nbtStorage
     * @return
     */
    private static Object getWorldNBTStorage() {
        Object console = DEDICATED_SERVER;
        try {

            // Get the NBT storage
            Field nbtField = console.getClass().getSuperclass().getDeclaredField("worldNBTStorage");
            return nbtField.get(console);
        } catch (NoSuchFieldException e) {
           try {
               Field worlds = console.getClass().getSuperclass().getDeclaredField("worlds");
               List<Object> worldList = (List<Object>) worlds.get(console);
               Object world = worldList.get(0);

               Method dataManager = world.getClass().getSuperclass().getDeclaredMethod("getDataManager");
               return dataManager.invoke(world);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | NoSuchFieldException illegalAccessException) {
                illegalAccessException.printStackTrace();
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Object getDedicatedServer() {
        try {
            // First, get the server
            Server server = Bukkit.getServer();
            // Get the console
            Field consoleField = server.getClass().getDeclaredField("console");
            consoleField.setAccessible(true);
            return consoleField.get(server);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Location getLocation(OfflinePlayer player) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        Method getPlayerData = player.getClass().getDeclaredMethod("getData");
        getPlayerData.setAccessible(true);
        Object nbtCompound = getPlayerData.invoke(player);
        // Offline mode
        if (nbtCompound == null || !Bukkit.getOnlineMode()) {
            getPlayerData = WORLD_NBT_STORAGE.getClass().getDeclaredMethod("getPlayerData", String.class);
            nbtCompound = getPlayerData.invoke(WORLD_NBT_STORAGE, UUID.nameUUIDFromBytes(player.getName().getBytes()).toString());
        }
        if (nbtCompound == null) return null;
        // Double ID: 6
        // String ID: 5
        // Float ID:
        Method getList = nbtCompound.getClass().getDeclaredMethod("getList", String.class, int.class);

        Object pos = getList.invoke(nbtCompound, "Pos", 6);
        Object rotation = getList.invoke(nbtCompound, "Rotation", 5);

        Method getWorld = nbtCompound.getClass().getDeclaredMethod("getLong", String.class);

        long worldUUIDMost = (long) getWorld.invoke(nbtCompound, "WorldUUIDMost");
        long worldUUIDLeast = (long) getWorld.invoke(nbtCompound, "WorldUUIDLeast");

        World world = Bukkit.getWorld(new UUID(worldUUIDMost, worldUUIDLeast));

        return new Location(world,
                getPosition(pos)[0],
                getPosition(pos)[1],
                getPosition(pos)[2],
                getRotation(rotation)[0],
                getRotation(rotation)[1]);
    }

    private static void setLocation(OfflinePlayer player, Location location) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException, NoSuchFieldException, InstantiationException, FileNotFoundException {
        Method getPlayerData = player.getClass().getDeclaredMethod("getData");
        getPlayerData.setAccessible(true);
        Object nbtCompound = getPlayerData.invoke(player);
        // Offline mode
        if (nbtCompound == null || !Bukkit.getOnlineMode()) {
            getPlayerData = WORLD_NBT_STORAGE.getClass().getDeclaredMethod("getPlayerData", String.class);
            nbtCompound = getPlayerData.invoke(WORLD_NBT_STORAGE, UUID.nameUUIDFromBytes(player.getName().getBytes()).toString());
        }
        if (nbtCompound == null) return;

        Constructor<?> listConstructor = Class.forName("net.minecraft.server." + VERSION + ".NBTTagList").getDeclaredConstructor();
        Constructor<?> nbtDouble = Class.forName("net.minecraft.server." + VERSION + ".NBTTagDouble").getDeclaredConstructor(double.class);
        Constructor<?> nbtFloat = Class.forName("net.minecraft.server." + VERSION + ".NBTTagFloat").getDeclaredConstructor(float.class);
        Constructor<?> nbtLong = Class.forName("net.minecraft.server." + VERSION + ".NBTTagLong").getDeclaredConstructor(long.class);
        // You will NOT fool me! YOU HEAR ME??? YOU WILL NOT FOOL ME!!!!
        listConstructor.setAccessible(true);
        nbtDouble.setAccessible(true);
        nbtFloat.setAccessible(true);
        nbtLong.setAccessible(true);

        Object pos = listConstructor.newInstance();

        List<Object> posList = getListVariable(pos);

        // Set the position
        posList.add(nbtDouble.newInstance(location.getX()));
        posList.add(nbtDouble.newInstance(location.getY()));
        posList.add(nbtDouble.newInstance(location.getZ()));

        Object rot = listConstructor.newInstance();


        List<Object> rotList = getListVariable(rot);
        // Set the rotation
        rotList.add(nbtFloat.newInstance(location.getYaw()));
        rotList.add(nbtFloat.newInstance(location.getPitch()));

        UUID worldUUID = location.getWorld().getUID();

        Method set = nbtCompound.getClass().getDeclaredMethod("set", String.class, Class.forName("net.minecraft.server." + VERSION + ".NBTBase"));

        set.invoke(nbtCompound, "Pos", pos);
        set.invoke(nbtCompound, "Rotation", rot);
        set.invoke(nbtCompound, "WorldUUIDMost", nbtLong.newInstance(worldUUID.getMostSignificantBits()));
        set.invoke(nbtCompound, "WorldUUIDLeast", nbtLong.newInstance(worldUUID.getLeastSignificantBits()));

        Method getDataFile = player.getClass().getDeclaredMethod("getDataFile");
        getDataFile.setAccessible(true);
        File file = (File) getDataFile.invoke(player);
        FileOutputStream outputStream = new FileOutputStream(file);

        Class.forName("net.minecraft.server." + VERSION + ".NBTCompressedStreamTools")
                .getDeclaredMethod("a", Class.forName("net.minecraft.server." + VERSION + ".NBTTagCompound"), OutputStream.class)
                .invoke(null, nbtCompound, outputStream);
    }

    private static double[] getPosition(Object pos) throws IllegalAccessException, NoSuchFieldException {
        List<Object> list = getListVariable(pos);
        double[] posArray = new double[3];
        for (int i = 0; i < 3; i++) {
            Object nbtBase = list.get(i);
            Field data = nbtBase.getClass().getDeclaredField("data");
            data.setAccessible(true);
            posArray[i] = (double) data.get(nbtBase);
        }

        return posArray;
    }

    private static float[] getRotation(Object rot) throws IllegalAccessException, NoSuchFieldException {
        List<Object> list = getListVariable(rot);
        float[] rotArray = new float[2];
        for (int i = 0; i < 2; i++) {
            Object nbtBase = list.get(i);
            Field data = nbtBase.getClass().getDeclaredField("data");
            data.setAccessible(true);
            rotArray[i] = (float) data.get(nbtBase);
        }
        return rotArray;
    }

    private static List<Object> getListVariable(Object obj) throws IllegalAccessException, NoSuchFieldException {
        Field list = obj.getClass().getDeclaredField("list");
        list.setAccessible(true);
        return (List<Object>) list.get(obj);
    }

    private static File getPlayerdataFolder() {
        File root = new File(System.getProperty("user.dir"));
        if (!root.exists()) return null;
        if (!root.isDirectory()) return null;
        for (File file : root.listFiles()) {
            if (file.isDirectory()) {
                for (File subFile : file.listFiles()) {
                    if (!subFile.isDirectory()) continue;
                    if (subFile.getName().equals("playerdata")) return subFile;
                }
            }
        }
        return null;
    }

    public interface NBTCallback<D> {

        void onSuccess(D data);

        default void onFail(String message) {}
    }
}
