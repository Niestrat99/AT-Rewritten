package io.github.niestrat99.advancedteleport.tpoffline;

import io.github.niestrat99.advancedteleport.CoreAdvancedTeleport;
import io.github.niestrat99.advancedteleport.config.CustomMessages;

import net.kyori.adventure.nbt.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class PlayerDataReader {

    private static Path getPlayerFile(UUID uuid) {
        Path levelsDir = Bukkit.getServer().getLevelDirectory();
        Path playersFolder = levelsDir.resolve("players");
        Path dataFolder = playersFolder.resolve("data");
        return dataFolder.resolve(uuid.toString() + ".dat");
    }

    public static void setLocation(String name, Location newLoc, NBTCallback<Boolean> callback) {
        Bukkit.getScheduler()
                .runTaskAsynchronously(
                        CoreAdvancedTeleport.getInstance().getPlugin(),
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
        Path dataFile = getPlayerFile(uuid);

        if (!Files.exists(dataFile)) return;
        CompoundBinaryTag rawTag =
                BinaryTagIO.unlimitedReader().read(dataFile, BinaryTagIO.Compression.GZIP);
        CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder().put(rawTag);

        ListBinaryTag.Builder<BinaryTag> posTag = ListBinaryTag.builder();
        posTag.add(DoubleBinaryTag.doubleBinaryTag(location.getX()));
        posTag.add(DoubleBinaryTag.doubleBinaryTag(location.getY()));
        posTag.add(DoubleBinaryTag.doubleBinaryTag(location.getZ()));

        ListBinaryTag.Builder<BinaryTag> rotTag = ListBinaryTag.builder();
        rotTag.add(FloatBinaryTag.floatBinaryTag(location.getYaw()));
        rotTag.add(FloatBinaryTag.floatBinaryTag(location.getPitch()));

        builder.put("Pos", posTag.build());
        builder.put("Rotation", rotTag.build());

        builder.put("WorldUUIDMost", LongBinaryTag.longBinaryTag(location.getWorld().getUID().getMostSignificantBits()));
        builder.put("WorldUUIDLeast", LongBinaryTag.longBinaryTag(location.getWorld().getUID().getLeastSignificantBits()));

        BinaryTagIO.writer()
                .write(builder.build(), dataFile, BinaryTagIO.Compression.GZIP);
    }

    public interface NBTCallback<D> {

        void onSuccess(D data);

        default void onFail(@NotNull final Component message) {}
    }
}
