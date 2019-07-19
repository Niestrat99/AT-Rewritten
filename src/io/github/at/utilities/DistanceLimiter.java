package io.github.at.utilities;

import io.github.at.config.Config;
import org.bukkit.Location;

public class DistanceLimiter {

    public static boolean canTeleport(Location loc1, Location loc2) {
        if (Config.isDistanceLimiterEnabled()) {
            // ((x2 - x1)^2 + (y2 - y1)^2 + (z2 - z1))^0.5
            double distance = Math.pow((Math.pow(loc2.getX() - loc1.getX(), 2)
                    + Math.pow(loc2.getY() - loc1.getY(), 2)
                    + Math.pow(loc2.getZ() - loc1.getZ(), 2)), 0.5);
            return distance < Config.getDistanceLimit();
        }
        return true;
    }
}
