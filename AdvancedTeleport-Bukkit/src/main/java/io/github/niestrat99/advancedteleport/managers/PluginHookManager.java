package io.github.niestrat99.advancedteleport.managers;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.config.Spawn;
import io.github.niestrat99.advancedteleport.hooks.BorderPlugin;
import io.github.niestrat99.advancedteleport.hooks.ImportExportPlugin;
import io.github.niestrat99.advancedteleport.hooks.MapPlugin;
import io.github.niestrat99.advancedteleport.hooks.borders.ChunkyBorderHook;
import io.github.niestrat99.advancedteleport.hooks.borders.VanillaBorderHook;
import io.github.niestrat99.advancedteleport.hooks.borders.WorldBorderHook;
import io.github.niestrat99.advancedteleport.hooks.imports.EssentialsHook;
import io.github.niestrat99.advancedteleport.hooks.maps.DynmapHook;
import io.github.niestrat99.advancedteleport.sql.HomeSQLManager;
import io.github.niestrat99.advancedteleport.sql.WarpSQLManager;
import io.github.niestrat99.advancedteleport.utilities.RandomCoords;
import org.bukkit.Location;
import io.github.niestrat99.advancedteleport.hooks.maps.SquaremapHook;
import io.github.niestrat99.advancedteleport.utilities.RandomCoords;
import org.bukkit.World;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class PluginHookManager {

    private HashMap<String, ImportExportPlugin> importPlugins;
    private HashMap<String, BorderPlugin> borderPlugins;
    private HashMap<String, MapPlugin> mapPlugins;
    private static PluginHookManager instance;

    public PluginHookManager() {
        instance = this;
        init();
    }

    public void init() {
        importPlugins = new HashMap<>();
        borderPlugins = new HashMap<>();
        mapPlugins = new HashMap<>();

        loadPlugin(importPlugins, "essentials", EssentialsHook.class);

        // World border Plugins
        loadBorderPlugin("worldborder", WorldBorderHook.class);
        loadBorderPlugin("chunkyborder", ChunkyBorderHook.class);
        loadBorderPlugin("vanilla", VanillaBorderHook.class);

        // Claim Plugins
        loadPlugin(claimPlugins, "worldguard", WorldGuardClaimHook.class);
        loadPlugin(claimPlugins, "lands", LandsClaimHook.class);
        loadPlugin(claimPlugins, "griefprevention", GriefPreventionClaimHook.class);

        loadPlugin(mapPlugins, "squaremap", SquaremapHook.class);
        loadPlugin(mapPlugins, "dynmap", DynmapHook.class);

        for (MapPlugin plugin : mapPlugins.values()) {
            if (plugin.canEnable()) {
                plugin.enable();
                addIcons(NewConfig.get().MAP_WARPS.isEnabled(), WarpSQLManager.get().getWarpsBulk(), plugin::addWarp);
                addIcons(NewConfig.get().MAP_HOMES.isEnabled(), HomeSQLManager.get().getHomesBulk(), plugin::addHome);
                addIcons(NewConfig.get().MAP_SPAWNS.isEnabled(), CompletableFuture.completedFuture(Spawn.get().getSpawns()), (spawn) -> plugin.addSpawn(spawn, Spawn.get().getSpawn(spawn)));
            }
        }
    }

    public static PluginHookManager get() {
        return instance;
    }

    public HashMap<String, ImportExportPlugin> getImportPlugins() {
        return importPlugins;
    }

    public ImportExportPlugin getImportPlugin(String name) {
        return importPlugins.get(name);
    }

    public HashMap<String, MapPlugin> getMapPlugins() {
        return mapPlugins;
    }

    private <T> void loadPlugin(HashMap<String, T> map, String name, Class<? extends T> clazz) {
        try {
            map.put(name, clazz.newInstance());
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoClassDefFoundError ignored) {

        }
    }

    private void loadBorderPlugin(String name, Class<? extends BorderPlugin> clazz) {
        try {
            borderPlugins.put(name, clazz.newInstance());
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoClassDefFoundError ignored) {

        }
    }

    public double[] getRandomCoords(World world) {
        for (BorderPlugin plugin : borderPlugins.values()) {
            if (!plugin.canUse(world)) continue;
            return new double[]{
                    RandomCoords.getRandomCoords(plugin.getMinX(world), plugin.getMaxX(world)),
                    RandomCoords.getRandomCoords(plugin.getMinZ(world), plugin.getMaxZ(world))
            };
        }
        return null;
    }

    public boolean isClaimed(Location location) {
        for (ClaimPlugin plugin : claimPlugins.values()) {
            if (!plugin.canUse(location.getWorld())) continue;
            return plugin.isClaimed(location);
        }
        return false;
    }

    private <T> void addIcons(boolean requirement, CompletableFuture<List<T>> pois, Consumer<T> handler) {
        if (!requirement) return;
        pois.thenAcceptAsync(result -> {
            for (T poi : result) {
                handler.accept(poi);
            }
        }, CoreClass.sync);
    }
}
