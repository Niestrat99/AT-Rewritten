package io.github.niestrat99.advancedteleport.managers;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.config.MainConfig;
import io.github.niestrat99.advancedteleport.extensions.ExCast;
import io.github.niestrat99.advancedteleport.hooks.BorderPlugin;
import io.github.niestrat99.advancedteleport.hooks.ClaimPlugin;
import io.github.niestrat99.advancedteleport.hooks.MapPlugin;
import io.github.niestrat99.advancedteleport.hooks.PluginHook;
import io.github.niestrat99.advancedteleport.hooks.borders.ChunkyBorderHook;
import io.github.niestrat99.advancedteleport.hooks.borders.VanillaBorderHook;
import io.github.niestrat99.advancedteleport.hooks.borders.WorldBorderHook;
import io.github.niestrat99.advancedteleport.hooks.claims.GriefPreventionClaimHook;
import io.github.niestrat99.advancedteleport.hooks.claims.LandsClaimHook;
import io.github.niestrat99.advancedteleport.hooks.claims.WorldGuardClaimHook;
import io.github.niestrat99.advancedteleport.hooks.imports.EssentialsHook;
import io.github.niestrat99.advancedteleport.hooks.maps.DynmapHook;
import io.github.niestrat99.advancedteleport.sql.HomeSQLManager;
import io.github.niestrat99.advancedteleport.sql.SpawnSQLManager;
import io.github.niestrat99.advancedteleport.sql.WarpSQLManager;
import java.util.stream.Stream;
import org.bukkit.Location;
import io.github.niestrat99.advancedteleport.hooks.maps.SquaremapHook;
import io.github.niestrat99.advancedteleport.hooks.particles.PlayerParticlesHook;
import org.bukkit.World;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("rawtypes")
public final class PluginHookManager {
    private HashMap<String, ? extends PluginHook> activePluginHooks;
    private static PluginHookManager instance;

    public PluginHookManager() {
        instance = this;
        init();
    }

    public void init() {
        activePluginHooks = new HashMap<>();

        // Import plugins
        loadPlugin("essentials", EssentialsHook.class);

        // World border Plugins
        loadPlugin("worldborder", WorldBorderHook.class);
        loadPlugin("chunkyborder", ChunkyBorderHook.class);
        loadPlugin("vanilla", VanillaBorderHook.class);

        // Particle plugins
        loadPlugin("playerparticles", PlayerParticlesHook.class);

        // Claim Plugins
        loadPlugin("worldguard", WorldGuardClaimHook.class);
        loadPlugin("lands", LandsClaimHook.class);
        loadPlugin("griefprevention", GriefPreventionClaimHook.class);

        loadPlugin("squaremap", SquaremapHook.class);
        loadPlugin("dynmap", DynmapHook.class);
        
        getPluginHooks(MapPlugin.class, true).forEach(mapPlugin -> {
            mapPlugin.enable();
            addIcons(MainConfig.get().MAP_WARPS.isEnabled(), WarpSQLManager.get().getWarpsBulk(), mapPlugin::addWarp);
            addIcons(MainConfig.get().MAP_HOMES.isEnabled(), HomeSQLManager.get().getHomesBulk(), mapPlugin::addHome);
            addIcons(MainConfig.get().MAP_SPAWNS.isEnabled(), SpawnSQLManager.get().getSpawns(), mapPlugin::addSpawn);
        });
    }

    @Contract(pure = true)
    public static PluginHookManager get() {
        return instance;
    }

    @Contract(pure = true)
    public <H extends PluginHook> @NotNull Stream<H> getPluginHooks(
        @NotNull final Class<H> clazz,
        final boolean filterUsable
    ) {
        return activePluginHooks.values().stream()
            .filter(clazz::isInstance)
            .map(clazz::cast)
            .filter(hook -> !filterUsable || hook.pluginUsable());
    }

    @Contract(pure = true)
    public <H extends PluginHook> @NotNull Stream<H> getPluginHooks(@NotNull final Class<H> clazz) {
        return getPluginHooks(clazz, false);
    }

    @Contract(pure = true)
    public <H extends PluginHook> @Nullable H getPluginHook(
        @NotNull final String name,
        @NotNull final Class<H> clazz
    ) {
        final var plugin = activePluginHooks.get(name);
        if (plugin == null) return null;
        if (!clazz.isInstance(plugin)) return null;
        return clazz.cast(plugin);
    }

    private <T extends PluginHook> void loadPlugin(
        @NotNull final String name,
        @NotNull final Class<? extends T> clazz
    ) {
        try {
            activePluginHooks.put(name, ExCast.cast(clazz.getConstructor().newInstance())); // Honestly couldn't be arsed to fight java on this one.
        } catch (NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException | InstantiationException | NoClassDefFoundError ignored) { // Why are you like this essentials?
        }
    }

    @Contract(pure = true)
    public double[] getRandomCoords(@NotNull final World world) {
        return getPluginHooks(BorderPlugin.class, true)
            .filter(plugin -> plugin.canUse(world))
            .findFirst()
            .map(hook -> new double[]{
                hook.getMinX(world),
                hook.getMaxX(world),
                hook.getMinZ(world),
                hook.getMaxZ(world)
            }).orElse(null);
    }

    @Contract(pure = true)
    public boolean isClaimed(@NotNull final Location location) {
        return getPluginHooks(ClaimPlugin.class, true)
            .filter(plugin -> plugin.canUse(location.getWorld()))
            .findFirst()
            .map(hook -> hook.isClaimed(location))
            .orElse(false);
    }

    @Contract(pure = true)
    public boolean floodgateEnabled() {
        try {
            Class.forName("org.geysermc.floodgate.api.FloodgateApi");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private <T> void addIcons(
        final boolean requirement,
        @NotNull final CompletableFuture<List<T>> pois,
        @NotNull final Consumer<T> handler
    ) {
        if (!requirement) return;
        pois.thenAcceptAsync(result -> result.forEach(handler), CoreClass.sync);
    }
}
