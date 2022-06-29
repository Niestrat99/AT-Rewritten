package io.github.niestrat99.advancedteleport.managers;

import io.github.niestrat99.advancedteleport.CoreClass;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;

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
            images.put(fileName.replace("-", "_"), stream);
        }
    }

    public Collection<String> getImageNames() {
        return images.keySet();
    }
}
