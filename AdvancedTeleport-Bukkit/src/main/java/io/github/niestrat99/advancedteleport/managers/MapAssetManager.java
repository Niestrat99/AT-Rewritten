package io.github.niestrat99.advancedteleport.managers;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.config.MainConfig;
import io.github.niestrat99.advancedteleport.hooks.MapPlugin;
import io.github.niestrat99.advancedteleport.sql.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;
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
        if (!mapAssetsFolder.exists() && !mapAssetsFolder.mkdirs()) {
            CoreClass.getInstance().getLogger().warning("Failed to create the map-assets folder.");
            return;
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
        PluginHookManager.get().getPluginHooks(MapPlugin.class, true)
            .forEach(mapPlugin -> mapPlugin.registerImage(name, stream));
        CoreClass.getInstance().getLogger().info("Registered the image " + name + "!");
    }

    public static CompletableFuture<@Nullable IconInfo> getIcon(
            @NotNull String name,
            @NotNull IconType type,
            @Nullable UUID owner
    ) {
        CompletableFuture<Integer> idFetcher = switch (type) {
            case WARP -> WarpSQLManager.get().getWarpId(name);
            case HOME -> HomeSQLManager.get().getHomeId(name, owner);
            case SPAWN -> SpawnSQLManager.get().getSpawnId(name);
        };

        return idFetcher.thenApplyAsync(id -> IconInfo.fromSQL(id, type), CoreClass.async);
    }

    public record IconInfo(String imageKey, int size, boolean shown, String clickTooltip, String hoverTooltip) {

        public static @Nullable IconInfo fromSQL(int id, IconType type) {

            //
            try (Connection connection = MetadataSQLManager.get().implementConnection()) {

                // Create an interface for managing SQL connections.
                SQLInterface sql = new SQLInterface(connection, String.valueOf(id), type);

                String imageKey = sql.get("map_icon", type.section.getDefaultIcon().replace('-', '_'));
                String size = sql.get("map_icon_size", String.valueOf(type.section.getIconSize()));
                String shown = sql.get("map_visibility", String.valueOf(type.section.isEnabled()));
                String clickTooltip = sql.get("map_click_tooltip", type.section.getClickTooltip());
                String hoverTooltip = sql.get("map_hover_tooltip", type.section.getHoverTooltip());

                return new IconInfo(imageKey, Integer.parseInt(size), Boolean.parseBoolean(shown), clickTooltip, hoverTooltip);

            } catch (SQLException exception) {
                exception.printStackTrace();
            }

            return null;
        }
    }

    private record SQLInterface(Connection connection, String id, IconType type) {

        private @NotNull String get(String key, String defaultOpt) throws SQLException {
            return Objects.requireNonNullElse(MetadataSQLManager.get().getValue(connection, id, type.name(), key), defaultOpt);
        }
    }

    public enum IconType {
        WARP(MainConfig.get().MAP_WARPS),
        HOME(MainConfig.get().MAP_HOMES),
        SPAWN(MainConfig.get().MAP_SPAWNS);

        private final MainConfig.MapOptions section;

        IconType(MainConfig.MapOptions section) {
            this.section = section;
        }
    }
}
