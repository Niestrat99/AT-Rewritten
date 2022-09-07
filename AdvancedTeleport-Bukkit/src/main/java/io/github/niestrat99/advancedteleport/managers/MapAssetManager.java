package io.github.niestrat99.advancedteleport.managers;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.config.NewConfig;
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
            plugin.registerImage(name, stream);
        }
        CoreClass.getInstance().getLogger().info("Registered the image " + name + "!");
    }

    public static CompletableFuture<IconInfo> getIconInfo(String name, String type, UUID owner) {
        CompletableFuture<HashMap<String, String>> completableFuture;
        NewConfig.MapOptions options;
        String[] keys = new String[]{"map_icon", "map_size", "map_hidden", "map_label", "map_click_tooltip", "map_hover_tooltip"};
        switch (type) {
            case "warp":
                completableFuture = MetadataSQLManager.get().getWarpMetadataBulk(name, keys);
                options = NewConfig.get().MAP_WARPS;
                break;
            case "home":
                completableFuture = MetadataSQLManager.get().getHomeMetadataBulk(name, owner, keys);
                options = NewConfig.get().MAP_HOMES;
                break;
            case "spawn":
                completableFuture = MetadataSQLManager.get().getSpawnMetadataBulk(name, keys);
                options = NewConfig.get().MAP_SPAWNS;
                break;
            default:
                return null;
        }
        return completableFuture.thenApplyAsync(result -> {
            String imageKey = getIconInfo(result.get("map_icon"), type, name, options.getDefaultIcon());
            int size = getImageSize(result.get("map_size"), options.getIconSize());
            boolean hidden = result.containsKey("map_hidden") ? Boolean.getBoolean(result.get("map_hidden")) : options.isShownByDefault();
            String label = result.getOrDefault("map_label", name).replaceAll("\\{name}", name);
            String clickTooltip = result.getOrDefault("map_click_tooltip", name).replaceAll("\\{name}", name);
            String hoverTooltip = result.getOrDefault("map_hover_tooltip", name).replaceAll("\\{name}", name);
            return new IconInfo(imageKey, size, hidden, label, clickTooltip, hoverTooltip);
        });
    }

    private static String getIconInfo(String result, String type, String name, String defaultOption) {
        if (result != null && images.containsKey(result)) return result;
        result = type + "_" + name;
        if (images.containsKey(result)) return result;
        return defaultOption;
    }

    private static int getImageSize(String result, int defaultOption) {
        if (result == null) return defaultOption;
        if (result.matches("^[0-9]+$")) return Integer.parseInt(result);
        return defaultOption;
    }

    public static class IconInfo {
        private final String imageKey;
        private final int size;
        private final boolean hidden;
        private final String label;
        private final String clickTooltip;
        private final String hoverTooltip;
        
        public IconInfo(String imageKey, int size, boolean hidden, String label, String clickTooltip, String hoverTooltip) {
            this.imageKey = imageKey;
            this.size = size;
            this.hidden = hidden;
            this.label = label;
            this.clickTooltip = clickTooltip;
            this.hoverTooltip = hoverTooltip;
        }

        public String getImageKey() {
            return imageKey;
        }

        public int getSize() {
            return size;
        }

        public boolean isHidden() {
            return hidden;
        }

        public String getLabel() {
            return label;
        }

        public String getClickTooltip() {
            return clickTooltip;
        }

        public String getHoverTooltip() {
            return hoverTooltip;
        }
    }
}
