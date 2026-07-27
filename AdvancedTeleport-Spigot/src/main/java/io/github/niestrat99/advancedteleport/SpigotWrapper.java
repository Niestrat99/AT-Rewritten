package io.github.niestrat99.advancedteleport;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.github.niestrat99.advancedteleport.adventure.BungeeComponentSerializer;
import io.github.niestrat99.advancedteleport.commands.DummyTpHereOffline;
import io.github.niestrat99.advancedteleport.listeners.SpigotSignChangeListener;
import io.github.niestrat99.advancedteleport.listeners.SpigotSignOpenListener;
import io.github.niestrat99.advancedteleport.managers.CommandManager;
import io.github.niestrat99.advancedteleport.update.UpdateChecker;
import io.github.slimjar.app.builder.ApplicationBuilder;
import io.github.slimjar.logging.ProcessLogger;
import net.kyori.adventure.key.KeyPattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import net.md_5.bungee.api.ChatMessageType;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public class SpigotWrapper extends CoreAdvancedTeleport {

    private static final Pattern HEX_PATTERN = Pattern.compile("^[0-9a-fA-F]+$");
    private final JavaPlugin plugin;
    private final UpdateChecker updateChecker;

    public SpigotWrapper(JavaPlugin plugin) {
        this.plugin = plugin;
        this.updateChecker = new io.github.niestrat99.advancedteleport.update.SpigetUpdateChecker();
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

        // TODO forces this to disable, but would like a nicer solution permanently
        CommandManager.register("tpofflinehere", new DummyTpHereOffline());
        CommandManager.syncCommands();
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
        if (rawSound.matches(KeyPattern.NAMESPACE_PATTERN) || rawSound.matches(KeyPattern.VALUE_PATTERN)) {
            NamespacedKey soundKey = NamespacedKey.fromString(rawSound);
            if (soundKey == null) {
                getPlugin().getLogger().warning("Sound " + rawSound + " is not a valid sound key.");
                return;
            }
            Sound sound = Registry.SOUNDS.get(soundKey);
            if (sound == null) {
                getPlugin().getLogger().warning("Sound " + rawSound + " does not exist and cannot be played to the player.");
                return;
            }
            player.playSound(player, sound, volume, pitch);
        } else {
            try {
                CoreAdvancedTeleport.getInstance().getPlugin().getLogger().warning("Screaming snake case sounds (" + rawSound + ") are " +
                        "deprecated in Spigot - please switch to use the namespaced key format (e.g. minecraft:entity.player.levelup)");
                player.playSound(player, org.bukkit.Sound.valueOf(rawSound), volume, pitch);
            } catch (IllegalArgumentException ex) {
                CoreAdvancedTeleport.getInstance().getPlugin()
                        .getLogger()
                        .warning(
                                "Sound for " + rawSound + " could not be played: sound does not exist.");
            }
        }
    }

    @Override
    public void setTexture(@NotNull ItemStack item, String texture) throws MalformedURLException {
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        PlayerProfile profile = Bukkit.createPlayerProfile(UUID.nameUUIDFromBytes(texture.getBytes()), "AdvTPHead");
        PlayerTextures textures = profile.getTextures();

        if (HEX_PATTERN.matcher(texture).matches()) {
            textures.setSkin(URL.of(URI.create("https://textures.minecraft.net/texture/" + texture), null));
        } else if (texture.startsWith("http")) {
            textures.setSkin(URL.of(URI.create(texture), null));
        } else {
            byte[] rawTexture = Base64.getDecoder().decode(texture.getBytes(StandardCharsets.UTF_8));
            JsonElement result = JsonParser.parseString(new String(rawTexture).toLowerCase());
            String textureUrl = result.getAsJsonObject().getAsJsonObject("textures")
                    .getAsJsonObject("skin")
                    .getAsJsonObject("url")
                    .getAsString();
            textures.setSkin(URL.of(URI.create(textureUrl), null));
        }

        profile.setTextures(textures);
        meta.setOwnerProfile(profile);  // ?? why might it be null
        item.setItemMeta(meta);
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

    @Override
    public Component getTranslatableItemComponent(ItemStack item) {
        return Component.translatable(item.getTranslationKey());
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

    @Override
    public UpdateChecker getUpdateChecker() {
        return this.updateChecker;
    }
}
