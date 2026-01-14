package io.github.niestrat99.advancedteleport.api;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WorldlessLocation extends Location {

    private @NotNull String worldName;

    public WorldlessLocation(@NotNull Location location, @NotNull String worldName) {
        super(location.getWorld(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        this.worldName = worldName;
    }

    public WorldlessLocation(@NotNull World world, double x, double y, double z, float yaw, float pitch) {
        super(world, x, y, z, yaw, pitch);
        this.worldName = world.getName();
    }

    public WorldlessLocation(@NotNull String worldName, double x, double y, double z, float yaw, float pitch) {
        super(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
        this.worldName = worldName;
    }

    @Override
    public World getWorld() {
        if (super.getWorld() != null) return super.getWorld();
        return Bukkit.getWorld(worldName);
    }

    @Override
    public void setWorld(@Nullable World world) {
        if (world == null) return;
        this.worldName = world.getName();
    }
}
