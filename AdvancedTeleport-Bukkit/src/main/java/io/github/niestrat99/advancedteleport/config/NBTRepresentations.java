package io.github.niestrat99.advancedteleport.config;

import io.github.thatsmusic99.configurationmaster.CMFile;
import org.bukkit.plugin.Plugin;

public class NBTRepresentations extends CMFile {

    public NBTRepresentations(Plugin plugin) {
        super(plugin, "item-nbt-data-types");
    }

    @Override
    public void loadDefaults() {
        addComment("This configuration is used to decide what data types are used for each specified NBT tag.\n" +
                "The Bukkit API forces you to use a specific data type when looking for a piece of data.\n" +
                "If you have a special tag, you can specify it in here, otherwise the plugin attempts to brute-force it.\n" +
                "If you have no idea what this config does, don't worry, you probably won't need it. :)\n" +
                "Reference: https://minecraft.gamepedia.com/Player.dat_format#Item_structure");

        addExample("Count", "BYTE");
        addExample("Slot", "BYTE");
        addExample("id", "STRING");
        addExample("tag.Damage", "INTEGER");
        addExample("tag.Unbreakable", "BYTE");
        addExample("tag.CanDestroy", "TAG_CONTAINER_ARRAY:STRING");
        addExample("tag.CustomModelData", "INTEGER");
        addExample("tag.CanPlaceOn", "TAG_CONTAINER_ARRAY:STRING");

    }

    @Override
    public void loadTitle() {

    }
}
