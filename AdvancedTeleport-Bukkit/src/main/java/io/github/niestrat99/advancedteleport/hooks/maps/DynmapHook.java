package io.github.niestrat99.advancedteleport.hooks.maps;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.Home;
import io.github.niestrat99.advancedteleport.api.Warp;
import io.github.niestrat99.advancedteleport.api.Spawn;
import io.github.niestrat99.advancedteleport.hooks.MapPlugin;
import io.github.niestrat99.advancedteleport.managers.MapAssetManager;

import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.HashMap;
import java.util.UUID;

public final class DynmapHook extends MapPlugin<Plugin, Void> {

    private MarkerAPI markerAPI;

    private MarkerSet warpsMarker;
    private MarkerSet homesMarker;
    private MarkerSet spawnsMarker;

    private HashMap<String, MarkerIcon> icons;

    public DynmapHook() {
        super("dynmap");
    }

    @Override
    @Contract(pure = true)
    public void enable() {
        CoreClass.getInstance().getLogger().info("Found Dynmap, hooking...");
        icons = new HashMap<>();
        this.plugin()
                .map(DynmapAPI.class::cast)
                .ifPresent(
                        api -> {
                            markerAPI = api.getMarkerAPI();
                            warpsMarker = getSet("advancedteleport_warps", "Warps");
                            homesMarker = getSet("advancedteleport_homes", "Homes");
                            spawnsMarker = getSet("advancedteleport_spawns", "Spawns");
                        });
    }

    @Override
    public void addWarp(@NotNull final Warp warp) {
        addMarker(
                "advancedteleport_warp_" + warp.getName(),
                MapAssetManager.IconType.WARP,
                null,
                warpsMarker,
                warp.getLocation());
    }

    @Override
    public void addHome(@NotNull final Home home) {
        addMarker(
                "advancedteleport_home_" + home.getOwner() + "_" + home.getName(),
                MapAssetManager.IconType.HOME,
                home.getOwner(),
                homesMarker,
                home.getLocation());
    }

    @Override
    public void addSpawn(@NotNull final Spawn spawn) {
        addMarker(
                "advancedteleport_spawn_" + spawn.getName(),
                MapAssetManager.IconType.SPAWN,
                null,
                spawnsMarker,
                spawn.getLocation());
    }

    @Override
    public void removeWarp(@NotNull final Warp warp) {
        removeMarker("advancedteleport_warp_" + warp.getName(), warpsMarker);
    }

    @Override
    public void removeHome(@NotNull final Home home) {
        removeMarker(
                "advancedteleport_home_" + home.getOwner() + "_" + home.getName(), homesMarker);
    }

    @Override
    public void removeSpawn(@NotNull final Spawn spawn) {
        removeMarker("advancedteleport_spawn_" + spawn.getName(), spawnsMarker);
    }

    @Override
    public void moveWarp(@NotNull final Warp warp) {
        moveMarker(
                "advancedteleport_warp_" + warp.getName(),
                warpsMarker,
                MapAssetManager.IconType.WARP,
                null,
                warp.getLocation());
    }

    @Override
    public void moveHome(@NotNull final Home home) {
        moveMarker(
                "advancedteleport_home_" + home.getOwner() + "_" + home.getName(),
                homesMarker,
                MapAssetManager.IconType.HOME,
                home.getOwner(),
                home.getLocation());
    }

    @Override
    public void moveSpawn(@NotNull final Spawn spawn) {
        moveMarker(
                "advancedteleport_spawn_" + spawn.getName(),
                spawnsMarker,
                MapAssetManager.IconType.SPAWN,
                null,
                spawn.getLocation());
    }

    @Override
    public void registerImage(@NotNull final String name, @NotNull final InputStream stream) {
        MarkerIcon icon = markerAPI.createMarkerIcon(name, name, stream);
        icons.put(name, icon);
    }

    private void addMarker(
            @NotNull final String name,
            @NotNull final MapAssetManager.IconType type,
            @Nullable final UUID owner,
            @NotNull final MarkerSet set,
            @NotNull final Location location) {

        MapAssetManager.getIcon(name, type, owner)
                .thenAcceptAsync(
                        iconData -> {

                            // If the icon is hidden or null, stop there
                            if (iconData == null) return;
                            if (!iconData.shown()) return;

                            // Create the marker
                            set.createMarker(
                                    name,
                                    iconData.hoverTooltip().replace("{name}", name),
                                    location.getWorld().getName(),
                                    location.getX(),
                                    location.getY(),
                                    location.getZ(),
                                    icons.get(iconData.imageKey()),
                                    false);
                        },
                        CoreClass.sync);
    }

    private void removeMarker(@NotNull final String name, @NotNull final MarkerSet set) {
        for (Marker marker : set.getMarkers()) {
            if (marker.getMarkerID().equals(name)) {
                marker.deleteMarker();
            }
        }
    }

    private void moveMarker(
            @NotNull final String name,
            @NotNull final MarkerSet set,
            @NotNull final MapAssetManager.IconType type,
            @Nullable final UUID owner,
            @NotNull final Location location) {
        removeMarker(name, set);
        addMarker(name, type, owner, set, location);
    }

    public void updateIcon(
            @NotNull String id, @NotNull MapAssetManager.IconType type, @Nullable UUID owner) {

        MarkerSet set =
                (type == MapAssetManager.IconType.SPAWN
                        ? spawnsMarker
                        : (type == MapAssetManager.IconType.HOME ? homesMarker : warpsMarker));
        String name =
                "advancedteleport_"
                        + type.name().toLowerCase()
                        + "_"
                        + (type == MapAssetManager.IconType.HOME ? owner + "_" + id : id);

        // Try to get the existing marker
        Marker marker = set.findMarker(name);
        if (marker == null) return;

        // Remove the current marker
        removeMarker(name, set);

        //
        MapAssetManager.getIcon(id, type, owner)
                .thenAcceptAsync(
                        iconData -> {

                            // If the icon is shown or null, stop there
                            if (iconData == null) return;
                            if (!iconData.shown()) return;

                            // Create the marker
                            set.createMarker(
                                    name,
                                    iconData.hoverTooltip().replace("{name}", id),
                                    marker.getWorld(),
                                    marker.getX(),
                                    marker.getY(),
                                    marker.getZ(),
                                    icons.get(iconData.imageKey()),
                                    false);
                        },
                        CoreClass.sync);
    }

    private @NotNull MarkerSet getSet(@NotNull final String id, @NotNull final String label) {
        MarkerSet set = markerAPI.getMarkerSet(id);
        if (set == null) set = markerAPI.createMarkerSet(id, label, null, false);
        return set;
    }
}
