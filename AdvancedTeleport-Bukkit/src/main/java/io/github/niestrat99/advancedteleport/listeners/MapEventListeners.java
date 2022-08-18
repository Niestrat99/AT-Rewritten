package io.github.niestrat99.advancedteleport.listeners;

import io.github.niestrat99.advancedteleport.api.Warp;
import io.github.niestrat99.advancedteleport.api.events.warps.WarpDeleteEvent;
import io.github.niestrat99.advancedteleport.api.events.warps.WarpMoveEvent;
import io.github.niestrat99.advancedteleport.api.events.warps.WarpPostCreateEvent;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.managers.PluginHookManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MapEventListeners implements Listener {

    @EventHandler
    public void onWarpAdd(WarpPostCreateEvent event) {
        if (!NewConfig.get().MAP_WARPS.isEnabled()) return;
        Warp warp = event.getWarp();
        PluginHookManager.get().getMapPlugins().values().forEach(mapPlugin -> mapPlugin.addWarp(warp));
    }

    @EventHandler
    public void onWarpRemove(WarpDeleteEvent event) {
        PluginHookManager.get().getMapPlugins().values().forEach(mapPlugin -> mapPlugin.removeWarp(event.getWarp()));
    }

    @EventHandler
    public void onWarpMove(WarpMoveEvent event) {
        if (!NewConfig.get().MAP_WARPS.isEnabled()) return;
        PluginHookManager.get().getMapPlugins().values().forEach(mapPlugin -> mapPlugin.moveWarp(event.getWarp()));
    }
}
