package io.github.niestrat99.advancedteleport;

import io.github.niestrat99.advancedteleport.adventure.BungeeComponentSerializer;
import io.github.niestrat99.advancedteleport.listeners.SpigotSignChangeListener;
import io.github.niestrat99.advancedteleport.listeners.SpigotSignOpenListener;
import io.github.slimjar.app.builder.ApplicationBuilder;
import io.github.slimjar.logging.ProcessLogger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import net.md_5.bungee.api.ChatMessageType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class SpigotWrapper extends CoreAdvancedTeleport {

    private final JavaPlugin plugin;

    public SpigotWrapper(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onLoad() {
        try {
            loadLibraries();
        } catch (final Exception err) {
            this.plugin.getLogger().severe("Failed to load libraries!");
            this.plugin.getLogger().throwing(CoreAdvancedTeleport.class.getName(), "onLoad", err);
            Bukkit.getPluginManager().disablePlugin(this.plugin);
        }
    }

    @Override
    public void onEnable() {
        instance = this;
        super.onEnable();
    }

    @Override
    public void registerEvents() {
        super.registerEvents();

        this.plugin.getServer().getPluginManager().registerEvents(new SpigotSignOpenListener(), this.plugin);
        this.plugin.getServer().getPluginManager().registerEvents(new SpigotSignChangeListener(), this.plugin);
    }

    @Override
    public CompletableFuture<Boolean> teleportWithOptions(@NotNull Player player, @NotNull Location location, PlayerTeleportEvent.@NotNull TeleportCause cause) {
        return CompletableFuture.completedFuture(player.teleport(location, cause));
    }

    @Override
    public void playSound(@NotNull Player player, @NonNull String rawSound, float volume, float pitch) {

    }

    @Override
    public void setTexture(@NotNull ItemStack item, String texture) throws MalformedURLException {

    }

    @Override
    public void sendMessage(CommandSender sender, Component component) {
        BungeeComponentSerializer serializer = BungeeComponentSerializer.get();
        sender.spigot().sendMessage(serializer.serialize(component));
    }

    public void sendTitle(Player player, Title title) {
        LegacyComponentSerializer serializer = LegacyComponentSerializer.legacyAmpersand();
        player.sendTitle(serializer.serialize(title.title()),
                serializer.serialize(title.subtitle()),
                (int) (get(title.times()).fadeIn().toMillis() / 5),
                (int) (get(title.times()).stay().toMillis() / 5),
                (int) (get(title.times()).fadeOut().toMillis() / 5));
    }

    @Override
    public void sendActionBar(Player player, Component component) {
        BungeeComponentSerializer serializer = BungeeComponentSerializer.get();
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, serializer.serialize(component));
    }

    @Override
    public JavaPlugin getPlugin() {
        return this.plugin;
    }

    private static Title.Times get(@Nullable Title.Times actual) {
        return actual == null ? Title.Times.times(Duration.ofMillis(10 * 5), Duration.ofMillis(70 * 5), Duration.ofMillis(20 * 5)) : actual;
    }

    private void loadLibraries()
            throws ReflectiveOperationException,
            IOException,
            URISyntaxException,
            NoSuchAlgorithmException,
            InterruptedException {
        ApplicationBuilder.appending("AT")
                .downloadDirectoryPath(this.plugin.getDataFolder().toPath().resolve(".libs"))
                .logger(
                        new ProcessLogger() {
                            @Override
                            public void info(String s, Object... objects) {
                                SpigotWrapper.this.plugin.getLogger().info(String.format(s, objects));
                            }

                            @Override
                            public void debug(String message, Object... args) {}
                        })
                .build();
    }
}
