package io.github.niestrat99.advancedteleport.listeners;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.config.Spawn;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.jetbrains.annotations.NotNull;

public class SpawnLoadListener implements Listener {

    private final @NotNull Location location;
    private final @NotNull String name;
    private final @NotNull String id;

    public SpawnLoadListener(@NotNull Location location, @NotNull String name, @NotNull String id) {
        this.location = location;
        this.name = name;
        this.id = id;
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        if (!event.getWorld().getName().equals(name)) return;
        this.location.setWorld(event.getWorld());
        Spawn.get().setMainSpawn(this.id, this.location);
        CoreClass.getInstance().getLogger().info("Main spawn " + this.id + " has now been registered with " + event.getWorld().getName() + " as its world.");
        HandlerList.unregisterAll(this);
    }
}
