package io.github.niestrat99.advancedteleport.config;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.thatsmusic99.configurationmaster.api.ConfigFile;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public abstract class ATConfig extends ConfigFile {

    protected ATConfig(@NotNull final String name) throws Exception {
        super(getOrCreateFile(name));
        load();
    }

    protected static File getOrCreateFile(@NotNull final String name) throws IllegalStateException {
        final var dataFolder = CoreClass.getInstance().getDataFolder();
        if (!dataFolder.exists() && dataFolder.mkdirs())
            throw new IllegalStateException("Could not create data folder");
        final var file = new File(dataFolder, name);

        try {
            if (!file.exists()) file.createNewFile();
            return file;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }
}
