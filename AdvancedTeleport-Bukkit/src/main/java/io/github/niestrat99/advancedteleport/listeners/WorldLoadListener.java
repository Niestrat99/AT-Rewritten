package io.github.niestrat99.advancedteleport.listeners;

import io.github.niestrat99.advancedteleport.config.MainConfig;
import io.github.niestrat99.advancedteleport.rtp.RTPManager;

import io.papermc.lib.PaperLib;
import io.papermc.paper.event.world.border.WorldBorderBoundsChangeEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

public class WorldLoadListener implements Listener {

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        if (!MainConfig.get().RAPID_RESPONSE.get() || !PaperLib.isPaper()) return;
        RTPManager.loadWorldData(event.getWorld());
    }

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
        RTPManager.unloadWorldData(event.getWorld());
    }

    @EventHandler(ignoreCancelled = true)
    public void onWorldBorderChange(WorldBorderBoundsChangeEvent event) {
        RTPManager.checkLocationsInBorder();
    }
}
