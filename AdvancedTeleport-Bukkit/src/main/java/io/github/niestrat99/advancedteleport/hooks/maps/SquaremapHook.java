package io.github.niestrat99.advancedteleport.hooks.maps;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.hooks.MapPlugin;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import xyz.jpenilla.squaremap.api.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class SquaremapHook extends MapPlugin {

    private Squaremap provider;
    private LayerProvider WARP_LAYER;
    private LayerProvider HOME_LAYER;
    private LayerProvider SPAWN_LAYER;

    @Override
    public boolean canEnable() {
        Plugin plex = Bukkit.getPluginManager().getPlugin("Pl3xMap");
        return plex != null && plex.isEnabled();
    }

    @Override
    public void enable() {
        // Get the API provider
        provider = SquaremapProvider.get();

        registerImage("warp_default", CoreClass.getInstance().getResource("warp-default.png"));
        registerImage("home_default", CoreClass.getInstance().getResource("home-default.png"));
        registerImage("spawn_default", CoreClass.getInstance().getResource("spawn-default.png"));

        SimpleLayerProvider warpProvider = SimpleLayerProvider.builder("Warps")
                .showControls(true)
                .defaultHidden(false)
                .build();

        for (World world : Bukkit.getWorlds()) {

        }
    }

    private void checkIcons(String subfolder) {

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


}
