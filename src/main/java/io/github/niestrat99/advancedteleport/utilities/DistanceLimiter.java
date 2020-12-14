package io.github.niestrat99.advancedteleport.utilities;

import io.github.niestrat99.advancedteleport.config.Config;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import org.bukkit.Location;

public class DistanceLimiter {

    public static boolean canTeleport(Location loc1, Location loc2, String command) {
        if (command == null && !NewConfig.getInstance().MONITOR_ALL_TELEPORTS.get()) return true;
        int allowedDistance;
        if (command == null) {
            allowedDistance = NewConfig.getInstance().MAXIMUM_TELEPORT_DISTANCE.get();
        } else {
            allowedDistance = NewConfig.getInstance().DISTANCE_LIMITS.valueOf(command).get();
        }
        if (NewConfig.getInstance().ENABLE_TELEPORT_LIMITATIONS.get() && allowedDistance > 0) {
            if (loc1.getWorld() != loc2.getWorld()) return true;
            // ((x2 - x1)^2 + (y2 - y1)^2 + (z2 - z1))^0.5
            double distance = Math.pow((Math.pow(loc2.getX() - loc1.getX(), 2)
                    + Math.pow(loc2.getY() - loc1.getY(), 2)
                    + Math.pow(loc2.getZ() - loc1.getZ(), 2)), 0.5);
            return distance < allowedDistance;
        }
        return true;
    }
}
