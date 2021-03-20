package io.github.niestrat99.advancedteleport.hooks;

import io.github.niestrat99.advancedteleport.CoreClass;
import java.util.logging.Level;

public abstract class ImportExportPlugin {

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

    public void debug(String message) {
        CoreClass.getInstance().getLogger().log(Level.INFO, message);
    }
}
