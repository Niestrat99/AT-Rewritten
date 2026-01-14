package io.github.niestrat99.advancedteleport;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.logging.Logger;

public interface IAdvancedTeleport {

    void onLoad();

    void onEnable();

    void onDisable();

    void registerEvents();

    void registerCommands();

    CompletableFuture<Boolean> teleportWithOptions(
            @NotNull Player player,
            @NotNull Location location,
            @NotNull PlayerTeleportEvent.TeleportCause cause);

    void playSound(@NotNull Player player, @NotNull String rawSound, float volume, float pitch);

    void setTexture(@NotNull ItemStack item, String texture) throws MalformedURLException;

    void sendMessage(CommandSender sender, Component component);

    void sendTitle(Player player, Title title);

    void sendActionBar(Player player, Component component);

    JavaPlugin getPlugin();
}
