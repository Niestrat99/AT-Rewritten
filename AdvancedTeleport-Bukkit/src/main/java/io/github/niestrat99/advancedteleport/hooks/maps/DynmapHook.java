package io.github.niestrat99.advancedteleport.hooks.maps;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.Home;
import io.github.niestrat99.advancedteleport.api.Warp;
import io.github.niestrat99.advancedteleport.config.Spawn;
import io.github.niestrat99.advancedteleport.hooks.MapPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;

import java.io.InputStream;
import java.util.HashMap;

public class DynmapHook extends MapPlugin {

    private DynmapAPI api;
    private MarkerAPI markerAPI;

    private MarkerSet WARPS;
    private MarkerSet HOMES;
    private MarkerSet SPAWNS;

    private HashMap<String, MarkerIcon> icons;

    @Override
    public boolean canEnable() {
        Plugin dynmap = Bukkit.getPluginManager().getPlugin("dynmap");
        return dynmap != null && dynmap.isEnabled();
    }

    @Override
    public void enable() {
        CoreClass.getInstance().getLogger().info("Found Dynmap, hooking...");
        icons = new HashMap<>();
        api = (DynmapAPI) Bukkit.getPluginManager().getPlugin("dynmap");
        if (api == null) throw new NoClassDefFoundError("You fool.");
        markerAPI = api.getMarkerAPI();
        // Create the warps
        WARPS = markerAPI.createMarkerSet("advancedteleport_warps", "Warps", null, true);
        // Create the homes
        HOMES = markerAPI.createMarkerSet("advancedteleport_homes", "Homes", null, true);
        // Create the spawns
        SPAWNS = markerAPI.createMarkerSet("advancedteleport_spawns", "Spawns", null, true);
    }

    @Override
    public void addWarp(Warp warp) {
        addMarker("advancedteleport_warp_" + warp.getName(), warp.getName(), WARPS, warp.getLocation());
    }

    @Override
    public void addHome(Home home) {
        addMarker("advancedteleport_home_" + home.getOwner() + "_" + home.getName(), "Home: " + home.getName(), HOMES, home.getLocation());
    }

    @Override
    public void addSpawn(String name, Location location) {
        addMarker("advancedteleport_spawn_" + name, "Spawn", SPAWNS, location);
    }

    @Override
    public void removeWarp(Warp warp) {
        removeMarker("advancedteleport_warp_" + warp.getName(), WARPS);
    }

    @Override
    public void removeHome(Home home) {
        removeMarker("advancedteleport_home_" + home.getOwner() + "_" + home.getName(), HOMES);
    }

    @Override
    public void removeSpawn(String name) {
        removeMarker("advancedteleport_spawn_" + name, SPAWNS);
    }

    @Override
    public void moveWarp(Warp warp) {
        moveMarker("advancedteleport_warp_" + warp.getName(), warp.getName(), WARPS, warp.getLocation());
    }

    @Override
    public void moveHome(Home home) {
        moveMarker("advancedteleport_home_" + home.getOwner() + "_" + home.getName(), "Home: " + home.getName(), HOMES, home.getLocation());
    }

    @Override
    public void moveSpawn(String name, Location location) {
        moveMarker("advancedteleport_spawn_" + name, "Spawn", SPAWNS, Spawn.get().getSpawn(name));
    }

    @Override
    public void registerImage(String name, InputStream stream) {
        MarkerIcon icon = markerAPI.createMarkerIcon(name, name, stream);
        icons.put(name, icon);
    }

    private void addMarker(String name, String label, MarkerSet set, Location location) {
        set.createMarker(name, label, location.getWorld().getName(), location.getX(), location.getY(), location.getZ(), icons.get(name), false);
    }

    private void removeMarker(String name, MarkerSet set) {
        for (Marker marker : set.getMarkers()) {
            if (marker.getMarkerID().equals(name)) {
                marker.deleteMarker();
            }
        }
    }

    private void moveMarker(String name, String label, MarkerSet set, Location location) {
        removeMarker(name, set);
        addMarker(name, label, set, location);
    }
}
