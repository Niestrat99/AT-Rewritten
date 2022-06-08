package io.github.niestrat99.advancedteleport.managers;

import io.github.niestrat99.advancedteleport.hooks.BorderPlugin;
import io.github.niestrat99.advancedteleport.hooks.ClaimPlugin;
import io.github.niestrat99.advancedteleport.hooks.ImportExportPlugin;
import io.github.niestrat99.advancedteleport.hooks.MapPlugin;
import io.github.niestrat99.advancedteleport.hooks.borders.ChunkyBorderHook;
import io.github.niestrat99.advancedteleport.hooks.borders.VanillaBorderHook;
import io.github.niestrat99.advancedteleport.hooks.borders.WorldBorderHook;
import io.github.niestrat99.advancedteleport.hooks.claims.GriefPreventionClaimHook;
import io.github.niestrat99.advancedteleport.hooks.claims.LandsClaimHook;
import io.github.niestrat99.advancedteleport.hooks.claims.WorldGuardClaimHook;
import io.github.niestrat99.advancedteleport.hooks.imports.EssentialsHook;
import org.bukkit.Location;
import io.github.niestrat99.advancedteleport.hooks.maps.SquaremapHook;
import io.github.niestrat99.advancedteleport.utilities.RandomCoords;
import org.bukkit.World;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class PluginHookManager {

    private HashMap<String, ImportExportPlugin> importPlugins;
    private HashMap<String, BorderPlugin> borderPlugins;
    private HashMap<String, ClaimPlugin> claimPlugins;
    private HashMap<String, MapPlugin> mapPlugins;
    private static PluginHookManager instance;

    public PluginHookManager() {
        instance = this;
        init();
    }

    public void init() {
        importPlugins = new HashMap<>();
        borderPlugins = new HashMap<>();
        claimPlugins = new HashMap<>();
        mapPlugins = new HashMap<>();

        // Import plugins
        loadPlugin(importPlugins, "essentials", EssentialsHook.class);

        // World border Plugins
        loadBorderPlugin("worldborder", WorldBorderHook.class);
        loadBorderPlugin("chunkyborder", ChunkyBorderHook.class);
        loadBorderPlugin("vanilla", VanillaBorderHook.class);

        // Claim Plugins
        loadPlugin(claimPlugins, "worldguard", WorldGuardClaimHook.class);
        loadPlugin(claimPlugins, "lands", LandsClaimHook.class);
        loadPlugin(claimPlugins, "griefprevention", GriefPreventionClaimHook.class);

        loadMapPlugin("pl3xmap", SquaremapHook.class);

        for (MapPlugin plugin : mapPlugins.values()) {
            if (plugin.canEnable()) plugin.enable();
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

    private <T> void loadPlugin(HashMap<String, T> map, String name, Class<? extends T> clazz) {
        try {
            map.put(name, clazz.getConstructor().newInstance());
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoClassDefFoundError ignored) { // Why are you like this essentials?
        }
    }

    private void loadMapPlugin(String name, Class<? extends MapPlugin> clazz) {
        try {
            mapPlugins.put(name, clazz.newInstance());
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoClassDefFoundError ignored) {

        }
    }

    public double[] getRandomCoords(World world) {
        for (BorderPlugin plugin : borderPlugins.values()) {
            if (!plugin.canUse(world)) continue;
            return new double[]{plugin.getMinX(world), plugin.getMaxX(world), plugin.getMinZ(world), plugin.getMaxZ(world)};
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
}
