package io.github.niestrat99.advancedteleport.managers;

import io.github.niestrat99.advancedteleport.hooks.BorderPlugin;
import io.github.niestrat99.advancedteleport.hooks.ImportExportPlugin;
import io.github.niestrat99.advancedteleport.hooks.MapPlugin;
import io.github.niestrat99.advancedteleport.hooks.borders.ChunkyBorderHook;
import io.github.niestrat99.advancedteleport.hooks.borders.VanillaBorderHook;
import io.github.niestrat99.advancedteleport.hooks.borders.WorldBorderHook;
import io.github.niestrat99.advancedteleport.hooks.imports.EssentialsHook;
import io.github.niestrat99.advancedteleport.hooks.maps.SquaremapHook;
import io.github.niestrat99.advancedteleport.utilities.RandomCoords;
import org.bukkit.World;

import java.util.HashMap;

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

        loadImportPlugin("essentials", EssentialsHook.class);

        loadBorderPlugin("worldborder", WorldBorderHook.class);
        loadBorderPlugin("chunkyborder", ChunkyBorderHook.class);
        loadBorderPlugin("vanilla", VanillaBorderHook.class);

        loadMapPlugin("squaremap", SquaremapHook.class);

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

    private void loadImportPlugin(String name, Class<? extends ImportExportPlugin> clazz) {
        try {
            importPlugins.put(name, clazz.newInstance());
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
            return new double[]{
                    RandomCoords.getRandomCoords(plugin.getMinX(world), plugin.getMaxX(world)),
                    RandomCoords.getRandomCoords(plugin.getMinZ(world), plugin.getMaxZ(world))
            };
        }
        return null;
    }
}
