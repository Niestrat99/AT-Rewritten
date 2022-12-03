package io.github.niestrat99.advancedteleport.managers;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.hooks.MapPlugin;
import io.github.niestrat99.advancedteleport.sql.MetadataSQLManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MapAssetManager {

    private static HashMap<String, InputStream> images;

    public static void init() {
        images = new HashMap<>();
        // Get the AT folder
        File advTpFolder = CoreClass.getInstance().getDataFolder();
        // Get the map assets folder
        File mapAssetsFolder = new File(advTpFolder, "map-assets");
        // If it doesn't exist, try creating it
        if (!mapAssetsFolder.exists()) {
            if (!mapAssetsFolder.mkdirs()) {
                CoreClass.getInstance().getLogger().warning("Failed to create the map-assets folder.");
                return;
            }
        }

        // Register default images
        registerImage("warp_default", CoreClass.getInstance().getResource("warp-default.png"));
        registerImage("home_default", CoreClass.getInstance().getResource("home-default.png"));
        registerImage("spawn_default", CoreClass.getInstance().getResource("spawn-default.png"));

        // Register extra images
        String[] fileNames = mapAssetsFolder.list();
        if (fileNames == null) return;
        for (String fileName : fileNames) {
            File file = new File(mapAssetsFolder, fileName);
            // Create an InputStream
            InputStream stream;
            try {
                stream = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                CoreClass.getInstance().getLogger().warning("Failed to find the file " + fileName + "!");
                continue;
            }
            String id = fileName.replace("-", "_").substring(0, fileName.lastIndexOf('.'));
            registerImage(id, stream);

        }
    }

    public static Collection<String> getImageNames() {
        return images.keySet();
    }

    public static void registerImage(String name, InputStream stream) {
        images.put(name, stream);
        for (MapPlugin plugin : PluginHookManager.get().getMapPlugins().values()) {
            if (!plugin.canEnable()) continue;
            plugin.registerImage(name, stream);
        }
        CoreClass.getInstance().getLogger().info("Registered the image " + name + "!");
    }

    public static CompletableFuture<String> getImageKey(String name, String type, UUID owner) {
        CompletableFuture<String> completableFuture;
        switch (type) {
            case "warp":
                completableFuture = MetadataSQLManager.get().getWarpMetadata(name, "map_icon");
                break;
            case "home":
                completableFuture = MetadataSQLManager.get().getHomeMetadata(name, owner, "map_icon");
                break;
            case "spawn":
                completableFuture = MetadataSQLManager.get().getSpawnMetadata(name, "map_icon");
                break;
            default:
                return null;
        }
        return completableFuture.thenApplyAsync(result -> {
            if (result != null && images.containsKey(result)) return result;
            result = type + "_" + name;
            if (images.containsKey(result)) return result;
            return type + "_default";
        });
    }



    public static class IconInfo {
        private final String imageKey;
        private final int size;
        private final boolean hidden;
        private final String label;
        private final String clickTooltip;
        
        public IconInfo(String imageKey, int size, boolean hidden, String label, String clickTooltip) {
            this.imageKey = imageKey;
            this.size = size;
            this.hidden = hidden;
            this.label = label;
            this.clickTooltip = clickTooltip;
        }

        public String getImageKey() {
            return imageKey;
        }

        public int getSize() {
            return size;
        }
    }
}
