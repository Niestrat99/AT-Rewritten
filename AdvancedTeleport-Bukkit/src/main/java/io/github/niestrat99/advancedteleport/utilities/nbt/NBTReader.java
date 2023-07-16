package io.github.niestrat99.advancedteleport.utilities.nbt;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.config.CustomMessages;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.DoubleBinaryTag;
import net.kyori.adventure.nbt.FloatBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class NBTReader {

    public static void getLocation(String name, NBTCallback<Location> callback) {
        Bukkit.getScheduler()
                .runTaskAsynchronously(
                        CoreClass.getInstance(),
                        () -> {
                            OfflinePlayer player = Bukkit.getOfflinePlayer(name);
                            try {
                                Location location = getLocation(player);
                                if (location == null) {
                                    callback.onFail(
                                            CustomMessages.getComponent(
                                                    "Error.noOfflineLocation",
                                                    Placeholder.unparsed("player", name)));
                                    return;
                                }
                                callback.onSuccess(location);
                            } catch (IOException e) {
                                callback.onFail(
                                        CustomMessages.getComponent(
                                                "Error.failedOfflineTeleport",
                                                Placeholder.unparsed("player", name)));
                                e.printStackTrace();
                            }
                        });
    }

    private static Location getLocation(OfflinePlayer player) throws IOException {
        UUID uuid = player.getUniqueId();
        File dataFile = getPlayerFile(uuid);

        if (dataFile == null) return null;
        CompoundBinaryTag tag =
                BinaryTagIO.unlimitedReader().read(dataFile.toPath(), BinaryTagIO.Compression.GZIP);
        ListBinaryTag posTag = tag.getList("Pos");
        ListBinaryTag rotTag = tag.getList("Rotation");
        long worldUUIDMost = tag.getLong("WorldUUIDMost");
        long worldUUIDLeast = tag.getLong("WorldUUIDLeast");

        World world = Bukkit.getWorld(new UUID(worldUUIDMost, worldUUIDLeast));

        return new Location(
                world,
                posTag.getDouble(0),
                posTag.getDouble(1),
                posTag.getDouble(2),
                rotTag.getFloat(0),
                rotTag.getFloat(1));
    }

    private static File getPlayerFile(UUID uuid) {
        for (World world : Bukkit.getWorlds()) {
            File worldFolder = world.getWorldFolder();
            if (!worldFolder.isDirectory()) continue;
            File[] children = worldFolder.listFiles();
            if (children == null) continue;
            for (File file : children) {
                if (!file.isDirectory() || !file.getName().equals("playerdata")) continue;
                return getPlayerFile(file, uuid);
            }
        }
        return null;
    }

    private static File getPlayerFile(File playerDataFolder, UUID uuid) {
        File[] files = playerDataFolder.listFiles();
        if (files == null) return null;
        for (File file : files) {
            if (file.getName().equals(uuid.toString() + ".dat")) return file;
        }
        return null;
    }

    public static void setLocation(String name, Location newLoc, NBTCallback<Boolean> callback) {
        Bukkit.getScheduler()
                .runTaskAsynchronously(
                        CoreClass.getInstance(),
                        () -> {
                            OfflinePlayer player = Bukkit.getOfflinePlayer(name);
                            try {
                                setLocation(player, newLoc);
                                callback.onSuccess(true);
                            } catch (IOException e) {
                                e.printStackTrace();
                                callback.onFail(
                                        CustomMessages.getComponent(
                                                "Error.failedOfflineTeleportHere",
                                                Placeholder.unparsed("player", name)));
                            }
                        });
    }

    private static void setLocation(OfflinePlayer player, Location location) throws IOException {
        UUID uuid = player.getUniqueId();
        File dataFile = getPlayerFile(uuid);

        if (dataFile == null) return;
        CompoundBinaryTag rawTag =
                BinaryTagIO.unlimitedReader().read(dataFile.toPath(), BinaryTagIO.Compression.GZIP);
        CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder().put(rawTag);

        ListBinaryTag.Builder<BinaryTag> posTag = ListBinaryTag.builder();
        posTag.add(DoubleBinaryTag.of(location.getX()));
        posTag.add(DoubleBinaryTag.of(location.getY()));
        posTag.add(DoubleBinaryTag.of(location.getZ()));

        ListBinaryTag.Builder<BinaryTag> rotTag = ListBinaryTag.builder();
        rotTag.add(FloatBinaryTag.of(location.getYaw()));
        rotTag.add(FloatBinaryTag.of(location.getPitch()));

        builder.put("Pos", posTag.build());
        builder.put("Rotation", rotTag.build());

        BinaryTagIO.writer()
                .write(builder.build(), dataFile.toPath(), BinaryTagIO.Compression.GZIP);
    }

    public interface NBTCallback<D> {

        void onSuccess(D data);

        default void onFail(@NotNull final Component message) {}
    }
}
