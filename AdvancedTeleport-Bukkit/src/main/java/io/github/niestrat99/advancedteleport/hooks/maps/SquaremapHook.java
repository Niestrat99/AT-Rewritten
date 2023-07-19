package io.github.niestrat99.advancedteleport.hooks.maps;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.api.AdvancedTeleportAPI;
import io.github.niestrat99.advancedteleport.api.Home;
import io.github.niestrat99.advancedteleport.api.Warp;
import io.github.niestrat99.advancedteleport.api.Spawn;
import io.github.niestrat99.advancedteleport.config.MainConfig;
import io.github.niestrat99.advancedteleport.hooks.MapPlugin;
import io.github.niestrat99.advancedteleport.managers.MapAssetManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.jpenilla.squaremap.api.BukkitAdapter;
import xyz.jpenilla.squaremap.api.Key;
import xyz.jpenilla.squaremap.api.LayerProvider;
import xyz.jpenilla.squaremap.api.Point;
import xyz.jpenilla.squaremap.api.SimpleLayerProvider;
import xyz.jpenilla.squaremap.api.Squaremap;
import xyz.jpenilla.squaremap.api.marker.Icon;
import xyz.jpenilla.squaremap.api.marker.Marker;
import xyz.jpenilla.squaremap.api.marker.MarkerOptions;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;

public final class SquaremapHook extends MapPlugin<Plugin, Squaremap> {

    private Squaremap provider;

    public SquaremapHook() {
        super("squaremap", Squaremap.class);
    }

    @Override
    @Contract(pure = true)
    public void enable() {
        CoreClass.getInstance().getLogger().info("Found squaremap, hooking...");

        // Get the API provider
        this.provider().ifPresent(squaremap -> {
            this.provider = squaremap;
            for (final var world : Bukkit.getWorlds()) {
                provider.getWorldIfEnabled(BukkitAdapter.worldIdentifier(world)).ifPresent(mapWorld -> {
                    final var key = Key.of("advancedteleport_warps");
                    mapWorld.layerRegistry().register(key, createLayerProvider(MainConfig.get().MAP_WARPS));
                    CoreClass.getInstance().getLogger().info("Added the warp layer for " + world.getName() + ".");

                    final var homesKey = Key.of("advancedteleport_homes");
                    mapWorld.layerRegistry().register(homesKey, createLayerProvider(MainConfig.get().MAP_HOMES));
                    CoreClass.getInstance().getLogger().info("Added the homes layer for " + world.getName() + ".");

                    final var spawnsKey = Key.of("advancedteleport_spawns");
                    mapWorld.layerRegistry().register(spawnsKey, createLayerProvider(MainConfig.get().MAP_SPAWNS));
                    CoreClass.getInstance().getLogger().info("Added the spawns layer for " + world.getName() + ".");
                });
            }
        });
    }

    @Override
    @Contract(pure = true)
    public void registerImage(
        @NotNull final String key,
        @NotNull final InputStream stream
    ) {
        try {
            final var image = ImageIO.read(stream);
            provider.iconRegistry().register(Key.of("advancedteleport_" + key), image);
        } catch (final IllegalArgumentException ex) {
            CoreClass.getInstance().getLogger().warning(ex.getMessage());
        } catch (final IOException e) {
            CoreClass.getInstance().getLogger().warning("Failed to read image for key " + key + "!");
        }
    }

    @Override
    public void addWarp(@NotNull final Warp warp) {
        addMarker(warp.getName(), MapAssetManager.IconType.WARP, warp.getLocation(), null);
    }

    @Override
    public void addHome(@NotNull final Home home) {
        addMarker(home.getName() + home.getOwner(), MapAssetManager.IconType.HOME, home.getLocation(), home.getOwner());
    }

    @Override
    public void addSpawn(@NotNull final Spawn spawn) {
        addMarker(spawn.getName(), MapAssetManager.IconType.SPAWN, spawn.getLocation(), null);
    }

    @Override
    public void removeWarp(@NotNull final Warp warp) {
        removeMarker(warp.getName(), MapAssetManager.IconType.WARP, warp.getLocation().getWorld());
    }

    @Override
    public void removeHome(@NotNull final Home home) {
        removeMarker(home.getName() + home.getOwner(), MapAssetManager.IconType.HOME, home.getLocation().getWorld());
    }

    @Override
    public void removeSpawn(@NotNull final Spawn spawn) {
        removeMarker(spawn.getName(), MapAssetManager.IconType.SPAWN, spawn.getLocation().getWorld());
    }

    @Override
    public void moveWarp(@NotNull final Warp warp) {
        moveMarker(warp.getName(), MapAssetManager.IconType.WARP, warp.getLocation(), null);
    }

    @Override
    public void moveHome(@NotNull final Home home) {
        moveMarker(home.getName(), MapAssetManager.IconType.HOME, home.getLocation(), home.getOwner());
    }

    @Override
    public void moveSpawn(@NotNull final Spawn spawn) {
        moveMarker(spawn.getName(), MapAssetManager.IconType.SPAWN, spawn.getLocation(), null);
    }

    private void addMarker(
        @NotNull final String name,
        @NotNull final MapAssetManager.IconType type,
        @NotNull final Location location,
        @Nullable final UUID owner
    ) {
        World world = location.getWorld();
        Objects.requireNonNull(world, "The world for " + name + " is not loaded.");
        provider.getWorldIfEnabled(BukkitAdapter.worldIdentifier(world)).ifPresent(mapWorld -> {

            // Get the key
            final var layerKey = Key.of("advancedteleport_" + type.name().toLowerCase() + "s");

            // Get the layer provider associated
            final var layer = (SimpleLayerProvider) mapWorld.layerRegistry().get(layerKey);

            // Create the icon key
            final var key = Key.of("advancedteleport_" + type.name().toLowerCase() + "_" + name);

            // Get the point
            final var point = Point.of(location.getX(), location.getZ());

            // Get the image associated with the icon
            MapAssetManager.getIcon(name, type, owner).thenAcceptAsync(iconData -> {

                if (iconData == null) return;
                if (!iconData.shown()) return;

                // Get the image key - if it isn't registered, send a warning
                String imageKey = "advancedteleport_" + iconData.imageKey();
                if (!provider.iconRegistry().hasEntry(Key.of(imageKey))) {
                    CoreClass.getInstance().getLogger().severe("Key " + imageKey + " is not registered.");
                }

                // Create the icon
                Icon icon = Icon.icon(point, Key.of(imageKey), iconData.size());

                // Set the tooltips
                MarkerOptions.Builder options = icon.markerOptions().asBuilder();
                options.clickTooltip(iconData.clickTooltip().replace("{name}", name));
                options.hoverTooltip(iconData.hoverTooltip().replace("{name}", name));
                icon.markerOptions(options);

                // Add the marker
                layer.addMarker(key, icon);
                CoreClass.getInstance().getLogger().info("Added the " + location + " for " + name + ".");

            }, CoreClass.sync);
        });
    }

    private void removeMarker(
        @NotNull final String name,
        @NotNull final MapAssetManager.IconType type,
        @NotNull final World world
    ) {
        Objects.requireNonNull(world, "The world for " + name + " is not loaded.");
        provider.getWorldIfEnabled(BukkitAdapter.worldIdentifier(world)).ifPresent(mapWorld -> {
            // Get the key
            Key layerKey = Key.of("advancedteleport_" + type.name().toLowerCase() + "s");
            // Get the layer provider associated
            SimpleLayerProvider layer = (SimpleLayerProvider) mapWorld.layerRegistry().get(layerKey);
            // Get the icon key
            Key key = Key.of("advancedteleport_" + type.name().toLowerCase() + "_" + name);
            // Remove the icon
            layer.removeMarker(key);
        });
    }

    private void moveMarker(
        @NotNull final String name,
        @NotNull final MapAssetManager.IconType type,
        @NotNull final Location location,
        @Nullable final UUID owner
    ) {
        removeMarker(name, type, location.getWorld());
        addMarker(name, type, location, owner);
    }

    public void updateIcon(
            @NotNull String id,
            @NotNull MapAssetManager.IconType type,
            @Nullable UUID owner
    ) {

        // Go through each world
        for (World world : Bukkit.getWorlds()) {
            provider.getWorldIfEnabled(BukkitAdapter.worldIdentifier(world)).ifPresent(mapWorld -> {

                // Get the layer needed
                Key layerKey = Key.of("advancedteleport_" + type.name().toLowerCase() + "s");
                SimpleLayerProvider layer = (SimpleLayerProvider) mapWorld.layerRegistry().get(layerKey);

                // Get the icon key
                Key key = Key.of("advancedteleport_" + type.name().toLowerCase() + "_" + id);

                // Get the marker
                Marker marker = layer.registeredMarkers().get(key);
                Point point = null;

                // If it doesn't exist, try getting the icon itself - we may be calling through /at map setvisible
                if (!(marker instanceof Icon icon)) {

                    switch (type) {
                        case WARP -> {
                            Warp warp = AdvancedTeleportAPI.getWarp(id);
                            if (warp == null) return;
                            point = Point.point(warp.getLocation().getX(), warp.getLocation().getZ());
                        }
                        case SPAWN -> {
                            Spawn spawn = AdvancedTeleportAPI.getSpawn(id);
                            if (spawn == null) return;
                            point = Point.point(spawn.getLocation().getX(), spawn.getLocation().getZ());
                        }
                        case HOME -> {
                            if (owner == null) return;
                            ATPlayer player = ATPlayer.getPlayer(Bukkit.getOfflinePlayer(owner));
                            Home home = player.getHome(id);
                            if (home == null) return;
                            point = Point.point(home.getLocation().getX(), home.getLocation().getZ());
                        }
                    }

                    if (point == null) return;
                } else {
                    point = icon.point();
                }

                Point finalPoint = point;

                // Get the image associated with the icon
                MapAssetManager.getIcon(id, type, owner).thenAcceptAsync(iconData -> {

                    if (iconData == null) return;
                    if (!iconData.shown()) {
                        layer.removeMarker(key);
                        return;
                    }

                    // Get the image key - if it isn't registered, send a warning
                    String imageKey = "advancedteleport_" + iconData.imageKey();
                    if (!provider.iconRegistry().hasEntry(Key.of(imageKey))) {
                        CoreClass.getInstance().getLogger().severe("Key " + imageKey + " is not registered.");
                    }

                    // Create the icon
                    Icon newIcon = Icon.icon(finalPoint, Key.of(imageKey), iconData.size());

                    // Set the tooltips
                    MarkerOptions.Builder options = newIcon.markerOptions().asBuilder();
                    options.clickTooltip(iconData.clickTooltip().replace("{name}", id));
                    options.hoverTooltip(iconData.hoverTooltip().replace("{name}", id));
                    newIcon.markerOptions(options);

                    // Add the marker
                    layer.removeMarker(key);
                    layer.addMarker(key, newIcon);

                }, CoreClass.sync);
            });
        }
    }

    private @NotNull LayerProvider createLayerProvider(@NotNull final MainConfig.MapOptions options) {
        return SimpleLayerProvider.builder(options.getLayerName())
                .showControls(true)
                .defaultHidden(!options.isShownByDefault())
                .build();
    }
}
