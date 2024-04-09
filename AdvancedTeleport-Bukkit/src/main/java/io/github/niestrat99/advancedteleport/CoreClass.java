package io.github.niestrat99.advancedteleport;

import io.github.niestrat99.advancedteleport.config.ATConfig;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.GUIConfig;
import io.github.niestrat99.advancedteleport.config.MainConfig;
import io.github.niestrat99.advancedteleport.listeners.MapEventListeners;
import io.github.niestrat99.advancedteleport.listeners.PlayerListeners;
import io.github.niestrat99.advancedteleport.listeners.SignInteractListener;
import io.github.niestrat99.advancedteleport.listeners.WorldLoadListener;
import io.github.niestrat99.advancedteleport.managers.*;
import io.github.niestrat99.advancedteleport.sql.*;
import io.github.niestrat99.advancedteleport.utilities.RandomTPAlgorithms;
import io.github.slimjar.app.builder.InjectingApplicationBuilder;
import io.github.slimjar.logging.ProcessLogger;
import io.papermc.lib.PaperLib;

import net.milkbowl.vault.permission.Permission;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

public final class CoreClass extends JavaPlugin {

    private static CoreClass instance;
    public static final Executor async =
            task -> Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), task);
    public static final Executor sync =
            task -> Bukkit.getScheduler().runTask(CoreClass.getInstance(), task);
    private static Permission perms;
    private Object[] updateInfo;
    private int version;

    public static CoreClass getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        try {
            loadLibraries();
        } catch (final Exception err) {
            getLogger().severe("Failed to load libraries!");
            err.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        DataFailManager.get().onDisable();

        try {
            hackTheMainFrame();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            getLogger().warning("Failed to shut down async tasks.");
            e.printStackTrace();
        }

        try {
            RTPManager.saveLocations();
        } catch (IOException e) {
            getLogger().warning("Failed to save RTP locations: " + e.getMessage());
        }
    }

    @Override
    public void onEnable() {
        instance = this;

        // Perform version checks.
        checkVersion();
        getLogger().info("Advanced Teleport is now enabling...");

        // Set up Vault.
        setupPermissions();

        // Set up main configuration files.
        for (Class<? extends ATConfig> config :
                Arrays.asList(MainConfig.class, CustomMessages.class, GUIConfig.class)) {
            try {
                debug("Loading " + config.getSimpleName() + ".");
                config.getDeclaredConstructor().newInstance();
                debug("Finished loading " + config.getSimpleName() + ".");
            } catch (NoSuchMethodException ex) {
                getLogger()
                        .severe(
                                config.getSimpleName()
                                        + " is not properly formed, it shouldn't take any constructor arguments. Please inform the developer.");
            } catch (InvocationTargetException | InstantiationException e) {
                getLogger()
                        .severe(
                                "Failed to load "
                                        + config.getSimpleName()
                                        + ": "
                                        + e.getCause().getMessage());
            } catch (IllegalAccessException e) {
                getLogger()
                        .severe(
                                "Failed to load "
                                        + config.getSimpleName()
                                        + ", why is the constructor not accessible? Please inform the developer.");
            }
        }

        // Initiate the named locations manager early
        new NamedLocationManager();

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
        CommandManager.registerCommands();
        registerEvents();
        CooldownManager.init();
        RandomTPAlgorithms.init();

        new Metrics(this, 5146);
        Bukkit.getScheduler()
                .runTaskAsynchronously(
                        this,
                        () -> {
                            RTPManager.init();
                            if (MainConfig.get().CHECK_FOR_UPDATES.get()) {
                                updateInfo = UpdateChecker.getUpdate();
                                if (updateInfo != null) {
                                    getLogger().info("A new version is available!");
                                    getLogger().info("Current version you're using: " + getDescription().getVersion());
                                    getLogger().info("Latest version available: " + updateInfo[0]);
                                    getLogger().info("Download link: https://www.spigotmc.org/resources/advancedteleport.64139/");
                                } else {
                                    getLogger().info("Plugin is up to date!");
                                }
                            }
                        });
    }

    private void checkVersion() {

        // Get the version in question.
        debug("Performing server version check.");
        int number = Integer.parseInt(Bukkit.getBukkitVersion().split("\\.")[1]);
        if (number < 17) {
            getLogger().severe("!!! YOU ARE USING ADVANCEDTELEPORT ON AN UNSUPPORTED VERSION. !!!");
            getLogger().severe("The plugin only receives mainstream support for 1.17.1 to 1.19.x");
            getLogger()
                    .severe(
                            "If you experience an issue with the plugin, please confirm whether it occurs on newer versions as well.");
            getLogger()
                    .severe(
                            "If you experience issues that only occur on your version, then we are not responsible for addressing it.");
            getLogger().severe("You have been warned.");
        }
        debug("Detected major version: " + number);
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new SignInteractListener(), this);
        getServer().getPluginManager().registerEvents(new TeleportTrackingManager(), this);
        getServer().getPluginManager().registerEvents(new MovementManager(), this);
        getServer().getPluginManager().registerEvents(new PlayerListeners(), this);
        getServer().getPluginManager().registerEvents(new WorldLoadListener(), this);
        getServer().getPluginManager().registerEvents(new MapEventListeners(), this);
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
        if (!PaperLib.isPaper()) return;

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
            CoreClass.getInstance().getLogger().warning(sound + " is an invalid sound name");
        }
    }

    public static Permission getPerms() {
        return perms;
    }

    private void setupVersion() {

        // Parse the major version of the server (e.g. 1.19)
        debug("Performing version checks.");
        String bukkitVersion =
                Bukkit.getServer()
                        .getClass()
                        .getPackage()
                        .getName()
                        .replace(".", ",")
                        .split(",")[3]
                        .split("_")[1];
        this.version = Integer.parseInt(bukkitVersion);
        debug("Parsed major version: " + this.version);
    }

    public int getVersion() {
        return version;
    }

    public static void debug(String message) {
        if (MainConfig.get() == null || MainConfig.get().DEBUG.get()) {
            CoreClass.getInstance().getLogger().info(message);
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

    private void loadLibraries()
            throws ReflectiveOperationException,
                    IOException,
                    URISyntaxException,
                    NoSuchAlgorithmException,
                    InterruptedException {
        InjectingApplicationBuilder.createAppending("AT", getClassLoader())
                .downloadDirectoryPath(getDataFolder().toPath().resolve(".libs"))
                .logger(
                        new ProcessLogger() {
                            @Override
                            public void log(String s, Object... objects) {
                                getLogger().info(String.format(s, objects));
                            }

                            @Override
                            public void debug(String message, Object... args) {}
                        })
                .build();
    }

    public Object[] getUpdateInfo() {
        return updateInfo;
    }
}
