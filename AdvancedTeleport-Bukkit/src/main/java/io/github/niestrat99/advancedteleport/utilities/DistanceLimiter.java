package io.github.niestrat99.advancedteleport.utilities;

import io.github.niestrat99.advancedteleport.config.NewConfig;
import org.bukkit.Location;

public class DistanceLimiter {

    public static boolean canTeleport(Location loc1, Location loc2, String command) {
        if (command == null && !NewConfig.get().MONITOR_ALL_TELEPORTS.get()) return true;
        int allowedDistance;
        if (command == null) {
            allowedDistance = NewConfig.get().MAXIMUM_TELEPORT_DISTANCE.get();
        } else {
            allowedDistance = NewConfig.get().DISTANCE_LIMITS.valueOf(command).get();
        }
        if (NewConfig.get().ENABLE_DISTANCE_LIMITATIONS.get() && allowedDistance > 0) {
            if (loc1.getWorld() != loc2.getWorld()) return true;
            return loc1.distanceSquared(loc2) < allowedDistance * allowedDistance;
        }
        return true;
    }
}
