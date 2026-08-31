package io.github.niestrat99.advancedteleport.utilities;

import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.config.MainConfig;

import io.github.thatsmusic99.configurationmaster.api.ConfigSection;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

public class DistanceLimiter {

    public static boolean canTeleport(
            Location loc1, Location loc2, String command, ATPlayer player) {
        return canTeleport(loc1, loc2, command, player, null);
    }

    public static boolean canTeleport(
            Location loc1, Location loc2, String command, ATPlayer player, @Nullable String locName) {
        if (command == null && !MainConfig.get().MONITOR_ALL_TELEPORTS.get()) return true;

        // Check for per-warp distance overrides when the command is "warp"
        if ("warp".equalsIgnoreCase(command) && locName != null && !locName.isEmpty()) {
            ConfigSection overrides = MainConfig.get().WARP_DISTANCE_OVERRIDES.get();
            if (overrides != null) {
                Object overrideValue = overrides.get(locName);
                if (overrideValue != null) {
                    int overrideDistance = overrides.getInteger(locName);
                    if (overrideDistance == -1) return true;
                    if (overrideDistance > 0) {
                        if (!MainConfig.get().ENABLE_DISTANCE_LIMITATIONS.get()) return true;
                        if (loc1.getWorld() != loc2.getWorld()) return true;
                        return loc1.distanceSquared(loc2) < (long) overrideDistance * overrideDistance;
                    }
                }
            }
        }

        int allowedDistance = player.getDistanceLimitation(command, loc2.getWorld());
        if (MainConfig.get().ENABLE_DISTANCE_LIMITATIONS.get() && allowedDistance > 0) {
            if (loc1.getWorld() != loc2.getWorld()) return true;
            return loc1.distanceSquared(loc2) < allowedDistance * allowedDistance;
        }
        return true;
    }
}
