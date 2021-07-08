package io.github.niestrat99.advancedteleport.managers;

import io.github.niestrat99.advancedteleport.hooks.ImportExportPlugin;
import io.github.niestrat99.advancedteleport.hooks.imports.EssentialsHook;

import java.util.HashMap;

public class PluginHookManager {

    private HashMap<String, ImportExportPlugin> importPlugins;
    private HashMap<String, BorderPlugin> borderPlugins;
    private static PluginHookManager instance;

    public PluginHookManager() {
        instance = this;
        init();
    }

    public void init() {
        importPlugins = new HashMap<>();
    }

        load("essentials", EssentialsHook.class);
    public static PluginHookManager get() {
        return instance;
    }

    public static HashMap<String, ImportExportPlugin> getImportPlugins() {
        return importPlugins;
    }

    public static ImportExportPlugin getImportPlugin(String name) {
        return importPlugins.get(name);
    }

    private static void load(String name, Class<? extends ImportExportPlugin> clazz) {
        try {
            importPlugins.put(name, clazz.newInstance());
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoClassDefFoundError ignored) {

        }
    }
}
