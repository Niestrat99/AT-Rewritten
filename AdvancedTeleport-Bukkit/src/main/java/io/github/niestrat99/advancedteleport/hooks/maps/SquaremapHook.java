package io.github.niestrat99.advancedteleport.hooks.maps;

import io.github.niestrat99.advancedteleport.CoreClass;
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
                    mapWorld.layerRegistry().register(key, createLayerProvider(NewConfig.get().MAP_WARPS));
                    CoreClass.getInstance().getLogger().info("Added the warp layer for " + world.getName() + ".");

                    final var homesKey = Key.of("advancedteleport_homes");
                    mapWorld.layerRegistry().register(homesKey, createLayerProvider(NewConfig.get().MAP_HOMES));
                    CoreClass.getInstance().getLogger().info("Added the homes layer for " + world.getName() + ".");

                    final var spawnsKey = Key.of("advancedteleport_spawns");
                    mapWorld.layerRegistry().register(spawnsKey, createLayerProvider(NewConfig.get().MAP_SPAWNS));
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
        addMarker(warp.getName(), "warp", warp.getLocation(), null);
    }

    @Override
    public void addHome(@NotNull final Home home) {
        addMarker(home.getName() + home.getOwner(), "home", home.getLocation(), home.getOwner());
    }

    @Override
    public void addSpawn(
        @NotNull final String name,
        @NotNull final Location location
    ) {
        addMarker(name, "spawn", location, null);
    }

    @Override
    public void removeWarp(@NotNull final Warp warp) {
        removeMarker(warp.getName(), "warp", warp.getLocation().getWorld());
    }

    @Override
    public void removeHome(@NotNull final Home home) {
        removeMarker(home.getName() + home.getOwner(), "home", home.getLocation().getWorld());
    }

    @Override
    public void removeSpawn(@NotNull final String name) {
        Location spawn = Spawn.get().getSpawn(name);
        removeMarker(name, "spawn", spawn.getWorld());
    }

    @Override
    public void moveWarp(@NotNull final Warp warp) {
        moveMarker(warp.getName(), "warp", warp.getLocation(), null);
    }

    @Override
    public void moveHome(@NotNull final Home home) {
        moveMarker(home.getName(), "home", home.getLocation(), home.getOwner());
    }

    @Override
    public void moveSpawn(
        @NotNull final String name,
        @NotNull final Location location
    ) {
        moveMarker(name, "spawn", location, null);
    }

    private void addMarker(
        @NotNull final String name,
        @NotNull final String type,
        @NotNull final Location location,
        @Nullable final UUID owner
    ) {
        World world = location.getWorld();
        Objects.requireNonNull(world, "The world for " + name + " is not loaded.");
        provider.getWorldIfEnabled(BukkitAdapter.worldIdentifier(world)).ifPresent(mapWorld -> {
            // Get the key
            final var layerKey = Key.of("advancedteleport_" + type + "s");
            // Get the layer provider associated
            final var layer = (SimpleLayerProvider) mapWorld.layerRegistry().get(layerKey);
            // Create the icon key
            final var key = Key.of("advancedteleport_" + type + "_" + name);
            // Get the point
            final var point = Point.of(location.getX(), location.getZ());
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

    private void removeMarker(
        @NotNull final String name,
        @NotNull final String type,
        @NotNull final World world
    ) {
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

    private void moveMarker(
        @NotNull final String name,
        @NotNull final String type,
        @NotNull final Location location,
        @Nullable final UUID owner
    ) {
        removeMarker(name, type, location.getWorld());
        addMarker(name, type, location, owner);
    }

    private @NotNull LayerProvider createLayerProvider(@NotNull final NewConfig.MapOptions options) {
        return SimpleLayerProvider.builder(options.getLayerName())
                .showControls(true)
                .defaultHidden(!options.isShownByDefault())
                .build();
    }
}
