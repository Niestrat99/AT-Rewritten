package io.github.niestrat99.advancedteleport.managers;

import io.github.niestrat99.advancedteleport.hooks.ImportExportPlugin;
import io.github.niestrat99.advancedteleport.hooks.imports.EssentialsHook;

import java.util.HashMap;

public class PluginHookManager {

    private static HashMap<String, ImportExportPlugin> importPlugins;

    public static void init() {
        importPlugins = new HashMap<>();

        importPlugins.put("essentials", new EssentialsHook());
    }

    public static HashMap<String, ImportExportPlugin> getImportPlugins() {
        return importPlugins;
    }

    public static ImportExportPlugin getImportPlugin(String name) {
        return importPlugins.get(name);
    }
}
