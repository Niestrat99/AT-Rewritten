package io.github.niestrat99.advancedteleport.utilities;

import io.github.niestrat99.advancedteleport.folia.RunnableManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_20_R1.CraftOfflinePlayer;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NMSUtilities {

    // Temporary hotfix for world mismatching on Folia
    public static @Nullable Location getRespawnLocation(@NotNull OfflinePlayer player) {
        if (!RunnableManager.isFolia() || !(player instanceof Player onlinePlayer)) {
            return player.getBedSpawnLocation();
        }

        ServerPlayer serverPlayer = ((CraftPlayer) onlinePlayer).getHandle();

        ServerLevel world = serverPlayer.server.getLevel(serverPlayer.getRespawnDimension());
        BlockPos pos = serverPlayer.getRespawnPosition();
        float yaw = serverPlayer.getRespawnAngle();

        // If world is null, stop there
        if (world == null || pos == null) return null;

        return new Location(world.getWorld(), pos.getX(), pos.getY(), pos.getZ(), yaw, 0);
    }
}
