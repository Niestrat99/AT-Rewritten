package io.github.niestrat99.advancedteleport.hooks;

import io.github.niestrat99.advancedteleport.CoreClass;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public abstract class PluginHook<P extends Plugin, R> {
    @NotNull private final Optional<String> pluginName;
    @Nullable private final Class<R> providerClazz;

    @Contract(pure = true)
    protected PluginHook(@Nullable final String pluginName, @Nullable final Class<R> provider) {
        this.pluginName = Optional.ofNullable(pluginName);
        this.providerClazz = provider;
    }

    @Contract(pure = true)
    protected PluginHook(@Nullable final String pluginName) {
        this(pluginName, null);
    }

    @Contract(pure = true)
    public @NotNull String pluginName() {
        return pluginName.orElse("Unknown");
    }

    @Contract(pure = true)
    public boolean pluginUsable() {
        return plugin().map(Plugin::isEnabled).orElse(false);
    }

    @Contract(pure = true)
    protected @NotNull Optional<P> plugin() {
        return pluginName
                .filter(name -> !name.equals("Unknown"))
                .map(name -> Bukkit.getServer().getPluginManager().getPlugin(name))
                .map(plugin -> (P) plugin); // Is there a way to do a safe or catching cast in java?
    }

    @Contract(pure = true)
    protected @NotNull Optional<R> provider() {
        if (providerClazz == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(
                        Bukkit.getServer().getServicesManager().getRegistration(providerClazz))
                .map(RegisteredServiceProvider::getProvider);
    }
}
