package io.github.niestrat99.advancedteleport.hooks.maps;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.Home;
import io.github.niestrat99.advancedteleport.api.Warp;
import io.github.niestrat99.advancedteleport.config.Spawn;
import io.github.niestrat99.advancedteleport.hooks.MapPlugin;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;

import java.io.InputStream;
import java.util.HashMap;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

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
            .ifPresent(api -> {
                markerAPI = api.getMarkerAPI();
                warpsMarker = getSet("advancedteleport_warps", "Warps");
                homesMarker = getSet("advancedteleport_homes", "Homes");
                spawnsMarker = getSet("advancedteleport_spawns", "Spawns");
            });
    }

    @Override
    public void addWarp(@NotNull final Warp warp) {
        addMarker("advancedteleport_warp_" + warp.getName(), warp.getName(), warpsMarker, warp.getLocation());
    }

    @Override
    public void addHome(@NotNull final Home home) {
        addMarker("advancedteleport_home_" + home.getOwner() + "_" + home.getName(), "Home: " + home.getName(), homesMarker, home.getLocation());
    }

    @Override
    public void addSpawn(
        @NotNull final String name,
        @NotNull final Location location
    ) {
        addMarker("advancedteleport_spawn_" + name, "Spawn", spawnsMarker, location);
    }

    @Override
    public void removeWarp(@NotNull final Warp warp) {
        removeMarker("advancedteleport_warp_" + warp.getName(), warpsMarker);
    }

    @Override
    public void removeHome(@NotNull final Home home) {
        removeMarker("advancedteleport_home_" + home.getOwner() + "_" + home.getName(), homesMarker);
    }

    @Override
    public void removeSpawn(@NotNull final String name) {
        removeMarker("advancedteleport_spawn_" + name, spawnsMarker);
    }

    @Override
    public void moveWarp(@NotNull final Warp warp) {
        moveMarker("advancedteleport_warp_" + warp.getName(), warp.getName(), warpsMarker, warp.getLocation());
    }

    @Override
    public void moveHome(@NotNull final Home home) {
        moveMarker("advancedteleport_home_" + home.getOwner() + "_" + home.getName(), "Home: " + home.getName(), homesMarker, home.getLocation());
    }

    @Override
    public void moveSpawn(
        @NotNull final String name,
        @NotNull final Location location
    ) {
        moveMarker("advancedteleport_spawn_" + name, "Spawn", spawnsMarker, Spawn.get().getSpawn(name));
    }

    @Override
    public void registerImage(
        @NotNull final String name,
        @NotNull final InputStream stream
    ) {
        MarkerIcon icon = markerAPI.createMarkerIcon(name, name, stream);
        icons.put(name, icon);
    }

    private void addMarker(
        @NotNull final String name,
        @NotNull final String label,
        @NotNull final MarkerSet set,
        @NotNull final Location location
    ) {
        set.createMarker(name, label, location.getWorld().getName(), location.getX(), location.getY(), location.getZ(), icons.get(name), false);
    }

    private void removeMarker(
        @NotNull final String name,
        @NotNull final MarkerSet set
    ) {
        for (Marker marker : set.getMarkers()) {
            if (marker.getMarkerID().equals(name)) {
                marker.deleteMarker();
            }
        }
    }

    private void moveMarker(
        @NotNull final String name,
        @NotNull final String label,
        @NotNull final MarkerSet set,
        @NotNull final Location location
    ) {
        removeMarker(name, set);
        addMarker(name, label, set, location);
    }

    private @NotNull MarkerSet getSet(
        @NotNull final String id,
        @NotNull final String label
    ) {
        MarkerSet set = markerAPI.getMarkerSet(id);
        if (set == null) set = markerAPI.createMarkerSet(id, label, null, false);
        return set;
    }
}
