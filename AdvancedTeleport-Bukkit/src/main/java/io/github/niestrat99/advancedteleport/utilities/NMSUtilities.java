package io.github.niestrat99.advancedteleport.utilities;

import io.github.niestrat99.advancedteleport.folia.RunnableManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class NMSUtilities {

    // Temporary hotfix for world mismatching on Folia
    public static @Nullable Location getRespawnLocation(@NotNull OfflinePlayer player) {
        if (!RunnableManager.isFolia() || !(player instanceof Player onlinePlayer)) {
            return player.getBedSpawnLocation();
        }

        ServerPlayer serverPlayer;
        try {
            Method method = onlinePlayer.getClass().getMethod("getHandleRaw");
            serverPlayer = (ServerPlayer) method.invoke(onlinePlayer);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }

        ServerLevel world = serverPlayer.server.getLevel(serverPlayer.getRespawnDimension());
        BlockPos pos = serverPlayer.getRespawnPosition();
        float yaw = serverPlayer.getRespawnAngle();

        // If world is null, stop there
        if (world == null || pos == null) return null;

        return new Location(world.getWorld(), pos.getX(), pos.getY(), pos.getZ(), yaw, 0);
    }
}
