package io.github.niestrat99.advancedteleport.hooks.maps;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.AdvancedTeleportAPI;
import io.github.niestrat99.advancedteleport.api.Home;
import io.github.niestrat99.advancedteleport.api.Warp;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.config.Spawn;
import io.github.niestrat99.advancedteleport.hooks.MapPlugin;
import io.github.niestrat99.advancedteleport.managers.MapAssetManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import xyz.jpenilla.squaremap.api.*;
import xyz.jpenilla.squaremap.api.marker.Icon;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;

public class SquaremapHook extends MapPlugin {

    private Squaremap provider;

    @Override
    public boolean canEnable() {
        Plugin plex = Bukkit.getPluginManager().getPlugin("squaremap");
        return plex != null && plex.isEnabled();
    }

    @Override
    public void enable() {
        CoreClass.getInstance().getLogger().info("Found squaremap, hooking...");
        // Get the API provider
        provider = SquaremapProvider.get();

        for (World world : Bukkit.getWorlds()) {
            provider.getWorldIfEnabled(BukkitAdapter.worldIdentifier(world)).ifPresent(mapWorld -> {
                // Warps
                Key key = Key.of("advancedteleport_warps");
                mapWorld.layerRegistry().register(key, createLayerProvider(NewConfig.get().MAP_WARPS));
                CoreClass.getInstance().getLogger().info("Added the warp layer for " + world.getName() + ".");
                // Homes
                Key homesKey = Key.of("advancedteleport_homes");
                mapWorld.layerRegistry().register(homesKey, createLayerProvider(NewConfig.get().MAP_HOMES));
                CoreClass.getInstance().getLogger().info("Added the homes layer for " + world.getName() + ".");
                // Spawns
                Key spawnsKey = Key.of("advancedteleport_spawns");
                mapWorld.layerRegistry().register(spawnsKey, createLayerProvider(NewConfig.get().MAP_SPAWNS));
                CoreClass.getInstance().getLogger().info("Added the spawns layer for " + world.getName() + ".");
            });
        }
    }

    @Override
    public void registerImage(String key, InputStream stream) {
        try {
            if (stream == null) throw new IllegalArgumentException("Image for key " + key + " was not found!");
            BufferedImage image = ImageIO.read(stream);
            provider.iconRegistry().register(Key.of("advancedteleport_" + key), image);
        } catch (IllegalArgumentException ex) {
            CoreClass.getInstance().getLogger().warning(ex.getMessage());
        } catch (IOException e) {
            CoreClass.getInstance().getLogger().warning("Failed to read image for key " + key + "!");
        }
    }

    @Override
    public void addWarp(Warp warp) {
        addMarker(warp.getName(), "warp", warp.getLocation(), null);
    }

    @Override
    public void addHome(Home home) {
        addMarker(home.getName() + home.getOwner(), "home", home.getLocation(), home.getOwner());
    }

    @Override
    public void addSpawn(String name, Location location) {
        addMarker(name, "spawn", location, null);
    }

    @Override
    public void removeWarp(Warp warp) {
        removeMarker(warp.getName(), "warp", warp.getLocation().getWorld());
    }

    @Override
    public void removeHome(Home home) {
        removeMarker(home.getName() + home.getOwner(), "home", home.getLocation().getWorld());
    }

    @Override
    public void removeSpawn(String name) {
        Location spawn = Spawn.get().getSpawn(name);
        removeMarker(name, "spawn", spawn.getWorld());
    }

    @Override
    public void moveWarp(Warp warp) {
        moveMarker(warp.getName(), "warp", warp.getLocation(), null);
    }

    @Override
    public void moveHome(Home home) {
        moveMarker(home.getName(), "home", home.getLocation(), home.getOwner());
    }

    @Override
    public void moveSpawn(String name, Location location) {
        moveMarker(name, "spawn", location, null);
    }

    private void addMarker(String name, String type, Location location, UUID owner) {
        World world = location.getWorld();
        Objects.requireNonNull(world, "The world for " + name + " is not loaded.");
        provider.getWorldIfEnabled(BukkitAdapter.worldIdentifier(world)).ifPresent(mapWorld -> {
            // Get the key
            Key layerKey = Key.of("advancedteleport_" + type + "s");
            // Get the layer provider associated
            SimpleLayerProvider layer = (SimpleLayerProvider) mapWorld.layerRegistry().get(layerKey);
            // Create the icon key
            Key key = Key.of("advancedteleport_" + type + "_" + name);
            // Get the point
            Point point = Point.of(location.getX(), location.getZ());
            // Get the image associated with the icon
            MapAssetManager.getImageKey(name, type, owner).thenAcceptAsync(result -> {
                result = "advancedteleport_" + result;
                if (!provider.iconRegistry().hasEntry(Key.of(result))) {
                    CoreClass.getInstance().getLogger().severe("Key " + result + " is not registered.");
                }
                Icon icon = Icon.icon(point, Key.of(result), 40);
                layer.addMarker(key, icon);
                CoreClass.getInstance().getLogger().info("Added the " + location + " for " + name + ".");
            }, task -> Bukkit.getScheduler().runTask(CoreClass.getInstance(), task));
        });
    }

    private void removeMarker(String name, String type, World world) {
        Objects.requireNonNull(world, "The world for " + name + " is not loaded.");
        provider.getWorldIfEnabled(BukkitAdapter.worldIdentifier(world)).ifPresent(mapWorld -> {
            // Get the key
            Key layerKey = Key.of("advancedteleport_" + type + "s");
            // Get the layer provider associated
            SimpleLayerProvider layer = (SimpleLayerProvider) mapWorld.layerRegistry().get(layerKey);
            // Get the icon key
            Key key = Key.of("advancedteleport_" + type + "_" + name);
            // Remove the icon
            layer.removeMarker(key);
        });
    }

    private void moveMarker(String name, String type, Location location, UUID owner) {
        removeMarker(name, type, location.getWorld());
        addMarker(name, type, location, owner);
    }

    private LayerProvider createLayerProvider(NewConfig.MapOptions options) {
        return SimpleLayerProvider.builder(options.getLayerName())
                .showControls(true)
                .defaultHidden(!options.isShownByDefault())
                .build();
    }
}
