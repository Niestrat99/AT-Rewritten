package io.github.niestrat99.advancedteleport;

import io.github.niestrat99.advancedteleport.config.ATConfig;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.GUIConfig;
import io.github.niestrat99.advancedteleport.config.MainConfig;
import io.github.niestrat99.advancedteleport.listeners.*;
import io.github.niestrat99.advancedteleport.managers.*;
import io.github.niestrat99.advancedteleport.sql.*;
import io.github.niestrat99.advancedteleport.update.AvailableUpdate;
import io.github.niestrat99.advancedteleport.utilities.RandomTPAlgorithms;

import net.milkbowl.vault.permission.Permission;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.Executor;

public abstract class CoreAdvancedTeleport implements IAdvancedTeleport {

    protected static IAdvancedTeleport instance;
    public static final Executor async =
            task -> Bukkit.getScheduler().runTaskAsynchronously(CoreAdvancedTeleport.getInstance().getPlugin(), task);
    public static final Executor sync =
            task -> Bukkit.getScheduler().runTask(CoreAdvancedTeleport.getInstance().getPlugin(), task);
    private static Permission perms;
    private @Nullable AvailableUpdate availableUpdate;
    private static @Nullable ZonedDateTime buildTimestamp;

    public static IAdvancedTeleport getInstance() {
        return instance;
    }

    @Override
    public void onDisable() {
        DataFailManager.get().onDisable();

        try {
            RTPManager.saveLocations();
        } catch (IOException e) {
            this.getPlugin().getLogger().warning("Failed to save RTP locations: " + e.getMessage());
        }
    }

    @Override
    public void onEnable() {
        instance = this;
        try {
            this.buildTimestamp = getInternalTimestamp();

        } catch (IOException | ParseException ex) {

        }
        this.getPlugin().getLogger().info("Advanced Teleport is now enabling...");
        setupPermissions();
        for (Class<? extends ATConfig> config :
                Arrays.asList(MainConfig.class, CustomMessages.class, GUIConfig.class)) {
            try {
                debug("Loading " + config.getSimpleName() + ".");
                config.getDeclaredConstructor().newInstance();
                debug("Finished loading " + config.getSimpleName() + ".");
            } catch (NoSuchMethodException ex) {
                this.getPlugin().getLogger()
                        .severe(
                                config.getSimpleName()
                                        + " is not properly formed, it shouldn't take any constructor arguments. Please inform the developer.");
            } catch (InvocationTargetException | InstantiationException e) {
                this.getPlugin().getLogger()
                        .severe(
                                "Failed to load "
                                        + config.getSimpleName()
                                        + ": "
                                        + e.getCause().getMessage());
            } catch (IllegalAccessException e) {
                this.getPlugin().getLogger()
                        .severe(
                                "Failed to load "
                                        + config.getSimpleName()
                                        + ", why is the constructor not accessible? Please inform the developer.");
            }
        }

        // Initiate the named locations manager early
        new NamedLocationManager();
        new SignManager();

        {
            new BlocklistManager();
            new HomeSQLManager();
            new PlayerSQLManager();
            new WarpSQLManager();
            new DataFailManager();
            new MetadataSQLManager();
            new SpawnSQLManager();
        }
        new PluginHookManager();
        MapAssetManager.init();
        registerCommands();
        registerEvents();
        CooldownManager.init();
        RandomTPAlgorithms.init();

        new Metrics(this.getPlugin(), 5146);
        Bukkit.getScheduler()
                .runTaskAsynchronously(
                        this.getPlugin(),
                        () -> {
                            RTPManager.init();
                            if (MainConfig.get().CHECK_FOR_UPDATES.get()) {
                                this.checkForUpdate();
                            }
                        });
    }

    @Override
    public void registerCommands() {
        CommandManager.registerCommands();
    }

    public void registerEvents() {
        getPlugin().getServer().getPluginManager().registerEvents(new TeleportTrackingManager(), this.getPlugin());
        getPlugin().getServer().getPluginManager().registerEvents(new MovementManager(), this.getPlugin());
        getPlugin().getServer().getPluginManager().registerEvents(new PlayerListeners(), this.getPlugin());
        getPlugin().getServer().getPluginManager().registerEvents(new WorldLoadListener(), this.getPlugin());
        getPlugin().getServer().getPluginManager().registerEvents(new MapEventListeners(), this.getPlugin());
    }

    private static void setupPermissions() {

        debug("Setting up permissions integration with Vault.");

        // If Vault is not on the server, stop there.
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            debug("Vault is not on the server, skipping.");
            return;
        }

        // If Vault isn't even enabled, stop there.
        if (!Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            debug("Vault is not enabled, skipping.");
            return;
        }

        // Get the permission interface to use.
        Optional.ofNullable(Bukkit.getServicesManager().getRegistration(Permission.class))
                .map(RegisteredServiceProvider::getProvider)
                .ifPresent(permission -> perms = permission);

        debug(
                perms == null
                        ? "No permissions hook for Vault found."
                        : perms.getName() + " hooked into successfully.");
    }

    public static void playSound(String type, String subType, Player target) {
        String sound = null;
        switch (type) {
            case "tpa":
                switch (subType) {
                    case "sent" -> sound = MainConfig.get().TPA_REQUEST_SENT.get();
                    case "received" -> sound = MainConfig.get().TPA_REQUEST_RECEIVED.get();
                }
                break;
            case "tpahere":
                sound =
                        switch (subType) {
                            case "sent" -> MainConfig.get().TPAHERE_REQUEST_SENT.get();
                            case "received" -> MainConfig.get().TPAHERE_REQUEST_RECEIVED.get();
                            default -> null;
                        };
                break;
        }
        if (sound == null) return;
        if (sound.equalsIgnoreCase("none")) return;
        try {
            target.playSound(target.getLocation(), Sound.valueOf(sound), 10, 1);
        } catch (IllegalArgumentException e) {
            CoreAdvancedTeleport.getInstance().getPlugin().getLogger().warning(sound + " is an invalid sound name");
        }
    }

    public static Permission getPerms() {
        return perms;
    }

    public static void debug(String message) {
        if (MainConfig.get() == null || MainConfig.get().DEBUG.get()) {
            CoreAdvancedTeleport.getInstance().getPlugin().getLogger().info(message);
        }
    }

    public static String getShortLocation(Location location) {
        return location.getBlockX()
                + ", "
                + location.getBlockY()
                + ", "
                + location.getBlockZ()
                + ", "
                + location.getWorld();
    }

    public static @Nullable ZonedDateTime getBuildTimestamp() {
        return buildTimestamp;
    }

    @Override
    public @Nullable AvailableUpdate getAvailableUpdate() {
        return this.availableUpdate;
    }

    private @Nullable ZonedDateTime getInternalTimestamp() throws IOException, java.text.ParseException {
        InputStream updateStream = CoreAdvancedTeleport.class.getResourceAsStream("/update.properties");
        Properties updateProperties = new Properties();
        updateProperties.load(updateStream);
        String timestamp = (String) updateProperties.get("update-timestamp");
        try {
            return ZonedDateTime.parse(timestamp, DateTimeFormatter.ISO_DATE_TIME);
        } catch (DateTimeParseException ex) {
            getPlugin().getLogger().warning("Failed to parse internal build timestamp '" + timestamp + "' - update checking will not work.");
            return null;
        }
    }

    private void checkForUpdate() {
        this.availableUpdate = getUpdateChecker().getLatestVersion();
        if (this.availableUpdate != null && this.availableUpdate.isNewerThanCurrent()) {
            this.getPlugin().getLogger().info("A new version is available!");
            this.getPlugin().getLogger().info("Current version you're using: " + this.getPlugin().getDescription().getVersion());
            this.getPlugin().getLogger().info("Latest version available: " + this.availableUpdate.versionTag());
            this.getPlugin().getLogger().info("Download link: " + getUpdateChecker().getDownloadLink());
        } else {
            this.getPlugin().getLogger().info("Plugin is up to date!");
        }
    }
}
