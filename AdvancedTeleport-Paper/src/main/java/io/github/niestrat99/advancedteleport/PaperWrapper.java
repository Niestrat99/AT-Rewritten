package io.github.niestrat99.advancedteleport;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.github.niestrat99.advancedteleport.commands.TpHereOffline;
import io.github.niestrat99.advancedteleport.config.MainConfig;
import io.github.niestrat99.advancedteleport.listeners.PaperSignChangeListener;
import io.github.niestrat99.advancedteleport.listeners.PaperSignOpenListener;
import io.github.niestrat99.advancedteleport.managers.CommandManager;
import io.github.niestrat99.advancedteleport.update.SpigetUpdateChecker;
import io.github.niestrat99.advancedteleport.update.UpdateChecker;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.profile.PlayerTextures;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class PaperWrapper extends CoreAdvancedTeleport {

    private static final Pattern HEX_PATTERN = Pattern.compile("^[0-9a-fA-F]+$");
    private final JavaPlugin plugin;
    private final UpdateChecker updateChecker;

    public PaperWrapper(JavaPlugin plugin) {
        this.plugin = plugin;
        this.updateChecker = new SpigetUpdateChecker();
    }

    @Override
    public void onLoad() {
    }

    @Override
    public void onEnable() {
        instance = this;
        super.onEnable();

        CommandManager.register("tpofflinehere", new TpHereOffline());
        CommandManager.syncCommands();
    }

    @Override
    public void onDisable() {
        super.onDisable();

        try {
            hackTheMainFrame();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            this.plugin.getLogger().warning("Failed to shut down async tasks.");
            this.plugin.getLogger().throwing(CoreAdvancedTeleport.class.getName(), "onDisable", e);
        }
    }

    @Override
    public void registerEvents() {
        super.registerEvents();

        this.plugin.getServer().getPluginManager().registerEvents(new PaperSignOpenListener(), this.plugin);
        this.plugin.getServer().getPluginManager().registerEvents(new PaperSignChangeListener(), this.plugin);
    }

    @Override
    public CompletableFuture<Boolean> teleportWithOptions(@NotNull Player player, @NotNull Location location, PlayerTeleportEvent.@NotNull TeleportCause cause) {

        // If we should retain passengers and are able to do so
        // https://jd.papermc.io/paper/1.21.11/io/papermc/paper/entity/TeleportFlag.EntityState.html#RETAIN_VEHICLE - slated for removal :(
        if (!MainConfig.get().RETAIN_PASSENGERS.get() && !player.getPassengers().isEmpty()) {
            for (Entity passenger : player.getPassengers()) {
                player.removePassenger(passenger);
            }
        }

        return player.teleportAsync(location, cause);

    }

    @Override
    public void playSound(@NotNull Player player, @NonNull String rawSound, float volume, float pitch) {
        if (rawSound.matches(KeyPattern.NAMESPACE_PATTERN) || rawSound.matches(KeyPattern.VALUE_PATTERN)) {
            try {

                player.playSound(
                        Sound.sound(Key.key(rawSound), Sound.Source.NEUTRAL, volume, pitch));
            } catch (NoSuchMethodError | NoClassDefFoundError ignored) {
                CoreAdvancedTeleport.getInstance().getPlugin()
                        .getLogger()
                        .warning(
                                "Sound " + rawSound + " could not be played: namespaces are not supported on your platform.");
            }
        } else {
            try {
                CoreAdvancedTeleport.getInstance().getPlugin().getLogger().warning("Screaming snake case sounds (" + rawSound + ") are " +
                        "deprecated in Paper - please switch to use the namespaced key format (e.g. minecraft:entity.player.levelup)");
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
        PlayerProfile profile = Bukkit.createProfile(UUID.nameUUIDFromBytes(texture.getBytes()), "AdvTPHead");
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
        meta.setPlayerProfile(profile);
    }

    @Override
    public void sendMessage(CommandSender sender, Component component) {
        sender.sendMessage(component);
    }

    @Override
    public void sendTitle(Player player, Title title) {
        player.showTitle(title);
    }

    @Override
    public void sendActionBar(Player player, Component component) {
        player.sendActionBar(component);
    }

    @Override
    public JavaPlugin getPlugin() {
        return this.plugin;
    }

    @Override
    public UpdateChecker getUpdateChecker() {
        return this.updateChecker;
    }

    @Override
    public Component getTranslatableItemComponent(ItemStack item) {
        return Component.translatable(item.translationKey());
    }

    /**
     * Nag author: 'Niestrat99' of 'AdvancedTeleport' about the following: This plugin is not
     * properly shutting down its async tasks when it is being shut down. This task may throw errors
     * during the final shutdown logs and might not complete before process dies.
     *
     * <p>Careful what you consider proper, Paper...
     *
     * <p>FYI - any Paper devs that see this, is there a better way to work around this? AT freezes
     * up due to the PaperLib#getChunkAtAsync method being held up, then floods the console, and
     * considering the userbase... If there's a better way of handling this please either open an
     * issue or DM @ Error#7365 because this method honestly sucks ass That or probably only make it
     * so that it warns once per plugin.
     */
    private void hackTheMainFrame() throws NoSuchFieldException, IllegalAccessException {
        final var scheduler = Bukkit.getScheduler();

        // Get the async scheduler
        final var asyncField = scheduler.getClass().getDeclaredField("asyncScheduler");
        asyncField.setAccessible(true);
        final var asyncScheduler = (BukkitScheduler) asyncField.get(scheduler);

        final var runnersField = scheduler.getClass().getDeclaredField("runners");
        runnersField.setAccessible(true);
        final var runners =
                (ConcurrentHashMap<Integer, ? extends BukkitTask>) runnersField.get(asyncScheduler);

        runners.keySet().stream()
                .map(runners::get)
                .filter(runner -> runner.getOwner() == this)
                .forEach(
                        runner -> {
                            runner.cancel();
                            runners.remove(runner.getTaskId());
                        });

        runnersField.set(scheduler, runners);
    }
}
