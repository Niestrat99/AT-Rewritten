package io.github.niestrat99.advancedteleport.utilities;

import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.config.MainConfig;

import org.bukkit.Location;

public class DistanceLimiter {

    public static boolean canTeleport(
            Location loc1, Location loc2, String command, ATPlayer player) {
        if (command == null && !MainConfig.get().MONITOR_ALL_TELEPORTS.get()) return true;
        int allowedDistance = player.getDistanceLimitation(command, loc2.getWorld());
        if (MainConfig.get().ENABLE_DISTANCE_LIMITATIONS.get() && allowedDistance > 0) {
            if (loc1.getWorld() != loc2.getWorld()) return true;
            return loc1.distanceSquared(loc2) < allowedDistance * allowedDistance;
        }
        return true;
    }
}
