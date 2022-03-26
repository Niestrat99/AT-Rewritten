package io.github.niestrat99.advancedteleport.config;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.thatsmusic99.configurationmaster.api.ConfigFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public abstract class ATConfig extends ConfigFile {

    public ATConfig(@NotNull String name) throws IOException {
        super(getOrCreateFile(name));
    }

    public void load() {
        loadDefaults();
        moveToNew();
        try {
            save();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        postSave();
    }

    public abstract void loadDefaults();

    public void moveToNew() {
    }

    public void postSave() {
    }

    @Override
    public void reload() throws IOException {
        super.reload();
        moveToNew();
        save();
        postSave();
    }

    protected static File getOrCreateFile(String name) {
        File file = new File(CoreClass.getInstance().getDataFolder(), name);
        try {
            if (!file.exists()) file.createNewFile();
            return file;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }
}
