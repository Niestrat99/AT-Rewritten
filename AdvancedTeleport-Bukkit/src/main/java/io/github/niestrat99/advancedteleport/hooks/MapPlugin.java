package io.github.niestrat99.advancedteleport.hooks;

import io.github.niestrat99.advancedteleport.api.Home;
import io.github.niestrat99.advancedteleport.api.Warp;
import io.github.niestrat99.advancedteleport.api.spawn.Spawn;

import java.io.InputStream;
import java.util.UUID;

import io.github.niestrat99.advancedteleport.managers.MapAssetManager;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class MapPlugin<P extends Plugin, R> extends PluginHook<P, R> {

    @Contract(pure = true)
    protected MapPlugin(
        @Nullable final String pluginName,
        @Nullable final Class<R> providerClazz
    ) {
        super(pluginName, providerClazz);
    }

    @Contract(pure = true)
    protected MapPlugin(@Nullable final String pluginName) {
        super(pluginName, null);
    }

    public abstract void enable();

    public abstract void addWarp(Warp warp);

    public abstract void addHome(Home home);

    public abstract void addSpawn(Spawn spawn);

    public abstract void removeWarp(Warp warp);

    public abstract void removeHome(Home home);

    public abstract void removeSpawn(Spawn spawn);

    public abstract void moveWarp(Warp warp);

    public abstract void moveHome(Home home);

    public abstract void moveSpawn(Spawn spawn);

    public abstract void registerImage(
        String name,
        InputStream stream
    );

    public abstract void updateIcon(
            @NotNull String id,
            @NotNull MapAssetManager.IconType type,
            @Nullable UUID owner
    );

    public enum TeleportPoint {
        WARP,
        HOME,
        SPAWN
    }
}
