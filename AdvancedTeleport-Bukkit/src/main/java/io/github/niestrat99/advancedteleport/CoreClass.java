package io.github.niestrat99.advancedteleport;

import io.github.niestrat99.advancedteleport.config.*;
import io.github.niestrat99.advancedteleport.listeners.MapEventListeners;
import io.github.niestrat99.advancedteleport.listeners.SignInteractListener;
import io.github.niestrat99.advancedteleport.listeners.PlayerListeners;
import io.github.niestrat99.advancedteleport.listeners.WorldLoadListener;
import io.github.niestrat99.advancedteleport.managers.*;
import io.github.niestrat99.advancedteleport.sql.*;
import io.github.niestrat99.advancedteleport.utilities.RandomTPAlgorithms;
import io.github.slimjar.app.builder.InjectingApplicationBuilder;
import io.github.slimjar.logging.ProcessLogger;
import io.papermc.lib.PaperLib;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import net.milkbowl.vault.permission.Permission;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

public final class CoreClass extends JavaPlugin {

    public static String pltitle(String title) {
        title = "&3[&bAdvancedTeleport&3] " + title;
        return ChatColor.translateAlternateColorCodes('&', title);
    }

    private static CoreClass Instance;
    private static Permission perms = null;
    private int version;
    private Object[] updateInfo = null;

    public static final Executor async = task -> Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), task);
    public static final Executor sync = task -> Bukkit.getScheduler().runTask(CoreClass.getInstance(), task);

    public static CoreClass getInstance() {
        return Instance;
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
    public void onEnable() {
        Instance = this;
        checkVersion();
        getLogger().info("Advanced Teleport is now enabling...");
        setupPermissions();
        for (Class<? extends ATConfig> config : Arrays.asList(NewConfig.class, CustomMessages.class, Spawn.class, GUI.class)) {
            try {
                config.getDeclaredConstructor().newInstance();
            } catch (NoSuchMethodException ex) {
                getLogger().severe(config.getSimpleName() + " is not properly formed, it shouldn't take any constructor arguments. Please inform the developer.");
            } catch (InvocationTargetException | InstantiationException e) {
                getLogger().severe("Failed to load " + config.getSimpleName() + ": " + e.getCause().getMessage());
            } catch (IllegalAccessException e) {
                getLogger().severe("Failed to load " + config.getSimpleName() + ", why is the constructor not accessible? Please inform the developer.");
            }
        }

        {
            new BlocklistManager();
            new HomeSQLManager();
            new PlayerSQLManager();
            new WarpSQLManager();
            new DataFailManager();
            new MetadataSQLManager();
        }
        new PluginHookManager();
        MapAssetManager.init();
        CommandManager.registerCommands();
        registerEvents();
        CooldownManager.init();
        RandomTPAlgorithms.init();

        setupVersion();
        new Metrics(this, 5146);
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            RTPManager.init();
            if (NewConfig.get().CHECK_FOR_UPDATES.get()) {
                updateInfo = UpdateChecker.getUpdate();
                if (updateInfo != null) {
                    getLogger().info(ChatColor.AQUA + "" + ChatColor.BOLD + "A new version is available!");
                    getLogger().info(ChatColor.AQUA + "" + ChatColor.BOLD + "Current version you're using: " + ChatColor.WHITE + getDescription().getVersion());
                    getLogger().info(ChatColor.AQUA + "" + ChatColor.BOLD + "Latest version available: " + ChatColor.WHITE + updateInfo[0]);
                    getLogger().info(ChatColor.AQUA + "Download link: https://www.spigotmc.org/resources/advancedteleport.64139/");
                } else {
                    getLogger().info(ChatColor.AQUA + "Plugin is up to date!");
                }
            }
        });
    }

    private void checkVersion() {
        String bukkitVersion = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
        int number = Integer.parseInt(bukkitVersion.split("_")[1]);
        if (number < 17) {
            getLogger().severe("!!! YOU ARE USING ADVANCEDTELEPORT ON AN UNSUPPORTED VERSION. !!!");
            getLogger().severe("The plugin only receives mainstream support for 1.17.1 to 1.19.x");
            getLogger().severe("If you experience an issue with the plugin, please confirm whether it occurs on newer versions as well.");
            getLogger().severe("If you experience issues that only occur on your version, then we are not responsible for addressing it.");
            getLogger().severe("You have been warned.");
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

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new SignInteractListener(), this);
        getServer().getPluginManager().registerEvents(new TeleportTrackingManager(), this);
        getServer().getPluginManager().registerEvents(new MovementManager(), this);
        getServer().getPluginManager().registerEvents(new PlayerListeners(), this);
        getServer().getPluginManager().registerEvents(new WorldLoadListener(), this);
        getServer().getPluginManager().registerEvents(new MapEventListeners(), this);
    }

    /**
     * Nag author: 'Niestrat99' of 'AdvancedTeleport' about the following:
     * This plugin is not properly shutting down its async tasks when it is being shut down.
     * This task may throw errors during the final shutdown logs and might not complete before process dies.
     *
     * Careful what you consider proper, Paper...
     *
     * FYI - any Paper devs that see this, is there a better way to work around this?
     * AT freezes up due to the PaperLib#getChunkAtAsync method being held up, then floods the console, and considering the userbase...
     * If there's a better way of handling this please either open an issue or DM @ Error#7365 because this method honestly sucks ass
     * That or probably only make it so that it warns once per plugin.
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
        final var runners = (ConcurrentHashMap<Integer, ? extends BukkitTask>) runnersField.get(asyncScheduler);

        runners.keySet().stream()
            .map(runners::get)
            .filter(runner -> runner.getOwner() == this)
            .forEach(runner -> {
                runner.cancel();
                runners.remove(runner.getTaskId());
            });

        runnersField.set(scheduler, runners);
    }

    private static void setupPermissions() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) return;
        Optional.ofNullable(Bukkit.getServicesManager().getRegistration(Permission.class))
            .map(RegisteredServiceProvider::getProvider)
            .ifPresent(permission -> perms = permission);
    }

    public static void playSound(String type, String subType, Player target) {
        String sound = null;
        switch (type) {
            case "tpa":
                switch (subType) {
                    case "sent" -> sound = NewConfig.get().TPA_REQUEST_SENT.get();
                    case "received" -> sound = NewConfig.get().TPA_REQUEST_RECEIVED.get();
                }
                break;
            case "tpahere":
                sound = switch (subType) {
                    case "sent" -> NewConfig.get().TPAHERE_REQUEST_SENT.get();
                    case "received" -> NewConfig.get().TPAHERE_REQUEST_RECEIVED.get();
                    default -> sound;
                };
                break;
        }
        if (sound == null) return;
        if (sound.equalsIgnoreCase("none")) return;
        try {
            target.playSound(target.getLocation(), Sound.valueOf(sound), 10, 1);
        } catch(IllegalArgumentException e){
            CoreClass.getInstance().getLogger().warning(CoreClass.pltitle(sound + " is an invalid sound name"));
        }
    }

    public static Permission getPerms() {
        return perms;
    }

    private void setupVersion() {
        String bukkitVersion = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3].split("_")[1];
        this.version = Integer.parseInt(bukkitVersion);
    }

    public int getVersion() {
        return version;
    }

    public Object[] getUpdateInfo() {
        return updateInfo;
    }

    public static void debug(String message) {
        if (NewConfig.get().DEBUG.get()) {
            CoreClass.getInstance().getLogger().info(message);
        }
    }

    public static String getShortLocation(Location location) {
        return location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() + ", " + location.getWorld();
    }

    private void loadLibraries() throws ReflectiveOperationException, IOException, URISyntaxException, NoSuchAlgorithmException, InterruptedException {
        InjectingApplicationBuilder.createAppending("AT", getClassLoader())
            .downloadDirectoryPath(getDataFolder().toPath().resolve(".libs"))
            .logger(new ProcessLogger() {
                @Override
                public void log(String s, Object... objects) {
                    getLogger().info(String.format(s, objects));
                }

                @Override
                public void debug(String message, Object... args) {
                    getLogger().info(String.format(message, args));
                }
            }).build();
    }
}
