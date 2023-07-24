package io.github.niestrat99.advancedteleport.hooks;

import io.github.niestrat99.advancedteleport.CoreClass;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;

public abstract class ImportExportPlugin<P extends Plugin, R> extends PluginHook<P, R> {

    @Contract(pure = true)
    protected ImportExportPlugin(
            @Nullable final String pluginName, @Nullable final Class<R> providerClazz) {
        super(pluginName, providerClazz);
    }

    @Contract(pure = true)
    protected ImportExportPlugin(@Nullable final String pluginName) {
        super(pluginName, null);
    }

    public abstract boolean canImport();

    public abstract void importHomes();

    public abstract void importLastLocations();

    public abstract void importWarps();

    public abstract void importSpawn();

    public abstract void importPlayerInformation();

    public void importAll() {
        importPlayerInformation();
        importHomes();
        importLastLocations();
        importWarps();
        importSpawn();
    }

    public abstract void exportHomes();

    public abstract void exportLastLocations();

    public abstract void exportWarps();

    public abstract void exportSpawn();

    public abstract void exportPlayerInformation();

    public void exportAll() {
        exportPlayerInformation();
        exportHomes();
        exportLastLocations();
        exportWarps();
        exportSpawn();
    }

    public void debug(String message) {
        CoreClass.getInstance().getLogger().log(Level.INFO, message);
    }
}
