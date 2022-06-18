package io.github.niestrat99.advancedteleport.hooks.maps;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.AdvancedTeleportAPI;
import io.github.niestrat99.advancedteleport.api.Warp;
import io.github.niestrat99.advancedteleport.hooks.MapPlugin;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import xyz.jpenilla.squaremap.api.*;
import xyz.jpenilla.squaremap.api.marker.Icon;
import xyz.jpenilla.squaremap.api.marker.MarkerOptions;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class SquaremapHook extends MapPlugin {

    private Squaremap provider;
    private LayerProvider WARP_LAYER;
    private LayerProvider HOME_LAYER;
    private LayerProvider SPAWN_LAYER;

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

        registerImage("warp_default", CoreClass.getInstance().getResource("warp-default.png"));
        registerImage("home_default", CoreClass.getInstance().getResource("home-default.png"));
        registerImage("spawn_default", CoreClass.getInstance().getResource("spawn-default.png"));

        for (World world : Bukkit.getWorlds()) {
            provider.getWorldIfEnabled(BukkitAdapter.worldIdentifier(world)).ifPresent(mapWorld -> {
                Key key = Key.of("advancedteleport_warps");
                SimpleLayerProvider warpProvider = SimpleLayerProvider.builder("Warps")
                        .showControls(true)
                        .defaultHidden(false)
                        .build();
                mapWorld.layerRegistry().register(key, warpProvider);
                CoreClass.getInstance().getLogger().info("Added the warp layer for " + world.getName() + ".");
            });
        }

        Bukkit.getScheduler().runTaskLater(CoreClass.getInstance(), () -> {
            for (Warp warp : AdvancedTeleportAPI.getWarps().values()) {
                addWarp(warp);
            }
        }, 20);

    }

    private void registerImage(String key, InputStream stream) {
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

    public void addWarp(Warp warp) {
        World world = warp.getLocation().getWorld();
        Objects.requireNonNull(world, "The world for " + warp.getName() + " is not loaded.");
        provider.getWorldIfEnabled(BukkitAdapter.worldIdentifier(world)).ifPresent(mapWorld -> {
            // Get the warp registry
            Key warpKey = Key.of("advancedteleport_warps");
            // Get the layer provider associated
            SimpleLayerProvider layer = (SimpleLayerProvider) mapWorld.layerRegistry().get(warpKey);
            // Create the warp ID
            Key key = Key.of("advancedteleport_warp_" + warp.getName());
            // Get the point
            Point point = Point.of(warp.getLocation().getX(), warp.getLocation().getZ());
            // Get the image associated with the warp
            Icon icon = Icon.icon(point, Key.of("advancedteleport_warp_default"), 40);
            icon.markerOptions(MarkerOptions.builder().hoverTooltip("Warp: " + warp.getName()).build());
            layer.addMarker(key, icon);
            CoreClass.getInstance().getLogger().info("Added the warp for " + warp.getName() + ".");
        });
    }
}
