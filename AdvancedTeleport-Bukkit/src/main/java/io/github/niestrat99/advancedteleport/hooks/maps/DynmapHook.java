package io.github.niestrat99.advancedteleport.hooks.maps;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.Home;
import io.github.niestrat99.advancedteleport.api.Warp;
import io.github.niestrat99.advancedteleport.hooks.MapPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;

import java.io.InputStream;

public class DynmapHook extends MapPlugin {

    private DynmapAPI api;
    private MarkerAPI markerAPI;

    private MarkerSet WARPS;
    private MarkerSet HOMES;
    private MarkerSet SPAWNS;

    @Override
    public boolean canEnable() {
        Plugin dynmap = Bukkit.getPluginManager().getPlugin("dynmap");
        return dynmap != null && dynmap.isEnabled();
    }

    @Override
    public void enable() {
        CoreClass.getInstance().getLogger().info("Found Dynmap, hooking...");
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

    }

    @Override
    public void addHome(Home home) {

    }

    @Override
    public void addSpawn(String name, Location location) {

    }

    @Override
    public void removeWarp(Warp warp) {

    }

    @Override
    public void removeHome(Home home) {

    }

    @Override
    public void removeSpawn(String name) {

    }

    @Override
    public void moveWarp(Warp warp) {

    }

    @Override
    public void moveHome(Home home) {

    }

    @Override
    public void moveSpawn(String name, Location location) {

    }

    @Override
    public void registerImage(String name, InputStream stream) {

    }

    private void addMarker(String name, MarkerSet set, Location location) {

    }
}
