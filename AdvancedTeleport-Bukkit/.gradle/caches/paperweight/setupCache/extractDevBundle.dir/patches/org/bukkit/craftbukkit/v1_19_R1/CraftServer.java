package org.bukkit.craftbukkit.v1_19_R1;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.serialization.Lifecycle;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import net.minecraft.advancements.Advancement;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ConsoleInput;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.bossevents.CustomBossEvent;
import net.minecraft.server.commands.ReloadCommand;
import net.minecraft.server.dedicated.DedicatedPlayerList;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.server.dedicated.DedicatedServerSettings;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.players.IpBanListEntry;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.players.ServerOpListEntry;
import net.minecraft.server.players.UserBanListEntry;
import net.minecraft.server.players.UserWhiteListEntry;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.village.VillageSiege;
import net.minecraft.world.entity.npc.CatSpawner;
import net.minecraft.world.entity.npc.WanderingTraderSpawner;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.RepairItemRecipe;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.PatrolSpawner;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang.Validate;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Keyed;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.Registry;
import org.bukkit.Server;
import org.bukkit.StructureType;
import org.bukkit.UnsafeValues;
import org.bukkit.Warning.WarningState;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldBorder;
import org.bukkit.WorldCreator;
import org.bukkit.block.data.BlockData;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.KeyedBossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.conversations.Conversable;
import org.bukkit.craftbukkit.v1_19_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_19_R1.boss.CraftBossBar;
import org.bukkit.craftbukkit.v1_19_R1.boss.CraftKeyedBossbar;
import org.bukkit.craftbukkit.v1_19_R1.command.BukkitCommandWrapper;
import org.bukkit.craftbukkit.v1_19_R1.command.CraftCommandMap;
import org.bukkit.craftbukkit.v1_19_R1.command.VanillaCommandWrapper;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_19_R1.generator.CraftWorldInfo;
import org.bukkit.craftbukkit.v1_19_R1.generator.OldCraftChunkData;
import org.bukkit.craftbukkit.v1_19_R1.help.SimpleHelpMap;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftBlastingRecipe;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftCampfireRecipe;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftFurnaceRecipe;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemFactory;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftMerchantCustom;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftRecipe;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftShapedRecipe;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftShapelessRecipe;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftSmithingRecipe;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftSmokingRecipe;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftStonecuttingRecipe;
import org.bukkit.craftbukkit.v1_19_R1.inventory.RecipeIterator;
import org.bukkit.craftbukkit.v1_19_R1.inventory.util.CraftInventoryCreator;
import org.bukkit.craftbukkit.v1_19_R1.map.CraftMapColorCache;
import org.bukkit.craftbukkit.v1_19_R1.map.CraftMapView;
import org.bukkit.craftbukkit.v1_19_R1.metadata.EntityMetadataStore;
import org.bukkit.craftbukkit.v1_19_R1.metadata.PlayerMetadataStore;
import org.bukkit.craftbukkit.v1_19_R1.metadata.WorldMetadataStore;
import org.bukkit.craftbukkit.v1_19_R1.potion.CraftPotionBrewer;
import org.bukkit.craftbukkit.v1_19_R1.profile.CraftPlayerProfile;
import org.bukkit.craftbukkit.v1_19_R1.scheduler.CraftScheduler;
import org.bukkit.craftbukkit.v1_19_R1.scoreboard.CraftCriteria;
import org.bukkit.craftbukkit.v1_19_R1.scoreboard.CraftScoreboardManager;
import org.bukkit.craftbukkit.v1_19_R1.structure.CraftStructureManager;
import org.bukkit.craftbukkit.v1_19_R1.tag.CraftBlockTag;
import org.bukkit.craftbukkit.v1_19_R1.tag.CraftEntityTag;
import org.bukkit.craftbukkit.v1_19_R1.tag.CraftFluidTag;
import org.bukkit.craftbukkit.v1_19_R1.tag.CraftItemTag;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftChatMessage;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftIconCache;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftNamespacedKey;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftSpawnCategory;
import org.bukkit.craftbukkit.v1_19_R1.util.DatFileFilter;
import org.bukkit.craftbukkit.v1_19_R1.util.Versioning;
import org.bukkit.craftbukkit.v1_19_R1.util.permissions.CraftDefaultPermissions;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.SpawnCategory;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;
import org.bukkit.event.server.BroadcastMessageEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.bukkit.help.HelpMap;
import org.bukkit.inventory.BlastingRecipe;
import org.bukkit.inventory.CampfireRecipe;
import org.bukkit.inventory.ComplexRecipe;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.SmithingRecipe;
import org.bukkit.inventory.SmokingRecipe;
import org.bukkit.inventory.StonecuttingRecipe;
import org.bukkit.loot.LootTable;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapView;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLoadOrder;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.SimpleServicesManager;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.StandardMessenger;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.scheduler.BukkitWorker;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.structure.StructureManager;
import org.bukkit.util.StringUtil;
import org.bukkit.util.permissions.DefaultPermissions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.MarkedYAMLException;

import net.md_5.bungee.api.chat.BaseComponent; // Spigot

import javax.annotation.Nullable; // Paper
import javax.annotation.Nonnull; // Paper

public final class CraftServer implements Server {
    private final String serverName = "Paper"; // Paper
    private final String serverVersion;
    private final String bukkitVersion = Versioning.getBukkitVersion();
    private final Logger logger = Logger.getLogger("Minecraft");
    private final ServicesManager servicesManager = new SimpleServicesManager();
    private final CraftScheduler scheduler = new CraftScheduler();
    private final CraftCommandMap commandMap = new CraftCommandMap(this);
    private final SimpleHelpMap helpMap = new SimpleHelpMap(this);
    private final StandardMessenger messenger = new StandardMessenger();
    private final SimplePluginManager pluginManager = new SimplePluginManager(this, this.commandMap);
    private final StructureManager structureManager;
    protected final DedicatedServer console;
    protected final DedicatedPlayerList playerList;
    private final Map<String, World> worlds = new LinkedHashMap<String, World>();
    private final Map<Class<?>, Registry<?>> registries = new HashMap<>();
    private YamlConfiguration configuration;
    private YamlConfiguration commandsConfiguration;
    private final Yaml yaml = new Yaml(new SafeConstructor());
    private final Map<UUID, OfflinePlayer> offlinePlayers = new MapMaker().weakValues().makeMap();
    private final EntityMetadataStore entityMetadata = new EntityMetadataStore();
    private final PlayerMetadataStore playerMetadata = new PlayerMetadataStore();
    private final WorldMetadataStore worldMetadata = new WorldMetadataStore();
    private final Object2IntOpenHashMap<SpawnCategory> spawnCategoryLimit = new Object2IntOpenHashMap<>();
    private File container;
    private WarningState warningState = WarningState.DEFAULT;
    public String minimumAPI;
    public CraftScoreboardManager scoreboardManager;
    public boolean playerCommandState;
    private boolean printSaveWarning;
    private CraftIconCache icon;
    private boolean overrideAllCommandBlockCommands = false;
    public boolean ignoreVanillaPermissions = false;
    private final List<CraftPlayer> playerView;
    public int reloadCount;
    private final io.papermc.paper.datapack.PaperDatapackManager datapackManager; // Paper
    public static Exception excessiveVelEx; // Paper - Velocity warnings
    private final io.papermc.paper.logging.SysoutCatcher sysoutCatcher = new io.papermc.paper.logging.SysoutCatcher(); // Paper
    private final CraftPotionBrewer potionBrewer = new CraftPotionBrewer(); // Paper

    static {
        ConfigurationSerialization.registerClass(CraftOfflinePlayer.class);
        ConfigurationSerialization.registerClass(CraftPlayerProfile.class);
        ConfigurationSerialization.registerClass(com.destroystokyo.paper.profile.CraftPlayerProfile.class); // Paper
        CraftItemFactory.instance();
    }

    public CraftServer(DedicatedServer console, PlayerList playerList) {
        this.console = console;
        this.playerList = (DedicatedPlayerList) playerList;
        this.playerView = Collections.unmodifiableList(Lists.transform(playerList.players, new Function<ServerPlayer, CraftPlayer>() {
            @Override
            public CraftPlayer apply(ServerPlayer player) {
                return player.getBukkitEntity();
            }
        }));
        this.serverVersion = CraftServer.class.getPackage().getImplementationVersion();
        this.structureManager = new CraftStructureManager(console.getStructureManager());

        Bukkit.setServer(this);

        // Register all the Enchantments and PotionTypes now so we can stop new registration immediately after
        Enchantments.SHARPNESS.getClass();
        org.bukkit.enchantments.Enchantment.stopAcceptingRegistrations();

        Potion.setPotionBrewer(potionBrewer); // Paper
        MobEffects.BLINDNESS.getClass();
        PotionEffectType.stopAcceptingRegistrations();
        // Ugly hack :(

        if (!Main.useConsole) {
            this.getLogger().info("Console input is disabled due to --noconsole command argument");
        }

        this.configuration = YamlConfiguration.loadConfiguration(this.getConfigFile());
        this.configuration.options().copyDefaults(true);
        this.configuration.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("configurations/bukkit.yml"), Charsets.UTF_8)));
        ConfigurationSection legacyAlias = null;
        if (!this.configuration.isString("aliases")) {
            legacyAlias = this.configuration.getConfigurationSection("aliases");
            this.configuration.set("aliases", "now-in-commands.yml");
        }
        this.saveConfig();
        if (this.getCommandsConfigFile().isFile()) {
            legacyAlias = null;
        }
        this.commandsConfiguration = YamlConfiguration.loadConfiguration(this.getCommandsConfigFile());
        this.commandsConfiguration.options().copyDefaults(true);
        this.commandsConfiguration.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("configurations/commands.yml"), Charsets.UTF_8)));
        this.saveCommandsConfig();

        // Migrate aliases from old file and add previously implicit $1- to pass all arguments
        if (legacyAlias != null) {
            ConfigurationSection aliases = this.commandsConfiguration.createSection("aliases");
            for (String key : legacyAlias.getKeys(false)) {
                ArrayList<String> commands = new ArrayList<String>();

                if (legacyAlias.isList(key)) {
                    for (String command : legacyAlias.getStringList(key)) {
                        commands.add(command + " $1-");
                    }
                } else {
                    commands.add(legacyAlias.getString(key) + " $1-");
                }

                aliases.set(key, commands);
            }
        }

        this.saveCommandsConfig();
        this.overrideAllCommandBlockCommands = this.commandsConfiguration.getStringList("command-block-overrides").contains("*");
        this.ignoreVanillaPermissions = this.commandsConfiguration.getBoolean("ignore-vanilla-permissions");
        this.pluginManager.useTimings(this.configuration.getBoolean("settings.plugin-profiling"));
        this.overrideSpawnLimits();
        console.autosavePeriod = this.configuration.getInt("ticks-per.autosave");
        this.warningState = WarningState.value(this.configuration.getString("settings.deprecated-verbose"));
        TicketType.PLUGIN.timeout = Math.min(20, this.configuration.getInt("chunk-gc.period-in-ticks")); // Paper - cap plugin loads to 1 second
        this.minimumAPI = this.configuration.getString("settings.minimum-api");
        this.loadIcon();

        // Set map color cache
        if (this.configuration.getBoolean("settings.use-map-color-cache")) {
            MapPalette.setMapColorCache(new CraftMapColorCache(this.logger));
        }
        datapackManager = new io.papermc.paper.datapack.PaperDatapackManager(console.getPackRepository()); // Paper
    }

    public boolean getCommandBlockOverride(String command) {
        return this.overrideAllCommandBlockCommands || this.commandsConfiguration.getStringList("command-block-overrides").contains(command);
    }

    private File getConfigFile() {
        return (File) console.options.valueOf("bukkit-settings");
    }

    private File getCommandsConfigFile() {
        return (File) console.options.valueOf("commands-settings");
    }

    private void overrideSpawnLimits() {
        for (SpawnCategory spawnCategory : SpawnCategory.values()) {
            if (CraftSpawnCategory.isValidForLimits(spawnCategory)) {
                this.spawnCategoryLimit.put(spawnCategory, this.configuration.getInt(CraftSpawnCategory.getConfigNameSpawnLimit(spawnCategory)));
            }
        }
    }

    private void saveConfig() {
        try {
            this.configuration.save(this.getConfigFile());
        } catch (IOException ex) {
            Logger.getLogger(CraftServer.class.getName()).log(Level.SEVERE, "Could not save " + this.getConfigFile(), ex);
        }
    }

    private void saveCommandsConfig() {
        try {
            this.commandsConfiguration.save(this.getCommandsConfigFile());
        } catch (IOException ex) {
            Logger.getLogger(CraftServer.class.getName()).log(Level.SEVERE, "Could not save " + this.getCommandsConfigFile(), ex);
        }
    }

    public void loadPlugins() {
        this.pluginManager.registerInterface(JavaPluginLoader.class);

        File pluginFolder = this.getPluginsFolder(); // Paper

        // Paper start
        if (true || pluginFolder.exists()) {
            if (!pluginFolder.exists()) {
                pluginFolder.mkdirs();
            }
            Plugin[] plugins = this.pluginManager.loadPlugins(pluginFolder, this.extraPluginJars());
            // Paper end
            for (Plugin plugin : plugins) {
                try {
                    String message = String.format("Loading %s", plugin.getDescription().getFullName());
                    plugin.getLogger().info(message);
                    plugin.onLoad();
                } catch (Throwable ex) {
                    Logger.getLogger(CraftServer.class.getName()).log(Level.SEVERE, ex.getMessage() + " initializing " + plugin.getDescription().getFullName() + " (Is it up to date?)", ex);
                }
            }
        } else {
            pluginFolder.mkdir();
        }
    }

    // Paper start
    @Override
    public File getPluginsFolder() {
        return (File) this.console.options.valueOf("plugins");
    }

    private List<File> extraPluginJars() {
        @SuppressWarnings("unchecked")
        final List<File> jars = (List<File>) this.console.options.valuesOf("add-plugin");
        final List<File> list = new ArrayList<>();
        for (final File file : jars) {
            if (!file.exists()) {
                net.minecraft.server.MinecraftServer.LOGGER.warn("File '{}' specified through 'add-plugin' argument does not exist, cannot load a plugin from it!", file.getAbsolutePath());
                continue;
            }
            if (!file.isFile()) {
                net.minecraft.server.MinecraftServer.LOGGER.warn("File '{}' specified through 'add-plugin' argument is not a file, cannot load a plugin from it!", file.getAbsolutePath());
                continue;
            }
            if (!file.getName().endsWith(".jar")) {
                net.minecraft.server.MinecraftServer.LOGGER.warn("File '{}' specified through 'add-plugin' argument is not a jar file, cannot load a plugin from it!", file.getAbsolutePath());
                continue;
            }
            list.add(file);
        }
        return list;
    }
    // Paper end

    public void enablePlugins(PluginLoadOrder type) {
        if (type == PluginLoadOrder.STARTUP) {
            this.helpMap.clear();
            this.helpMap.initializeGeneralTopics();
            if (io.papermc.paper.configuration.GlobalConfiguration.get().misc.loadPermissionsYmlBeforePlugins) loadCustomPermissions(); // Paper
        }

        Plugin[] plugins = this.pluginManager.getPlugins();

        for (Plugin plugin : plugins) {
            if ((!plugin.isEnabled()) && (plugin.getDescription().getLoad() == type)) {
                this.enablePlugin(plugin);
            }
        }

        if (type == PluginLoadOrder.POSTWORLD) {
            // Spigot start - Allow vanilla commands to be forced to be the main command
            this.setVanillaCommands(true);
            this.commandMap.setFallbackCommands();
            this.setVanillaCommands(false);
            // Spigot end
            this.commandMap.registerServerAliases();
            DefaultPermissions.registerCorePermissions();
            CraftDefaultPermissions.registerCorePermissions();
            if (!io.papermc.paper.configuration.GlobalConfiguration.get().misc.loadPermissionsYmlBeforePlugins) this.loadCustomPermissions(); // Paper
            this.helpMap.initializeCommands();
            this.syncCommands();
        }
    }

    public void disablePlugins() {
        this.pluginManager.disablePlugins();
    }

    private void setVanillaCommands(boolean first) { // Spigot
        Commands dispatcher = console.vanillaCommandDispatcher;

        // Build a list of all Vanilla commands and create wrappers
        for (CommandNode<CommandSourceStack> cmd : dispatcher.getDispatcher().getRoot().getChildren()) {
            // Spigot start
            VanillaCommandWrapper wrapper = new VanillaCommandWrapper(dispatcher, cmd);
            if (org.spigotmc.SpigotConfig.replaceCommands.contains( wrapper.getName() ) ) {
                if (first) {
                    this.commandMap.register("minecraft", wrapper);
                }
            } else if (!first) {
                this.commandMap.register("minecraft", wrapper);
            }
            // Spigot end
        }
    }

    public void syncCommands() {
        // Clear existing commands
        Commands dispatcher = console.resources.managers().commands = new Commands();

        // Register all commands, vanilla ones will be using the old dispatcher references
        for (Map.Entry<String, Command> entry : this.commandMap.getKnownCommands().entrySet()) {
            String label = entry.getKey();
            Command command = entry.getValue();

            if (command instanceof VanillaCommandWrapper) {
                LiteralCommandNode<CommandSourceStack> node = (LiteralCommandNode<CommandSourceStack>) ((VanillaCommandWrapper) command).vanillaCommand;
                if (!node.getLiteral().equals(label)) {
                    LiteralCommandNode<CommandSourceStack> clone = new LiteralCommandNode(label, node.getCommand(), node.getRequirement(), node.getRedirect(), node.getRedirectModifier(), node.isFork());

                    for (CommandNode<CommandSourceStack> child : node.getChildren()) {
                        clone.addChild(child);
                    }
                    node = clone;
                }

                dispatcher.getDispatcher().getRoot().addChild(node);
            } else {
                new BukkitCommandWrapper(this, entry.getValue()).register(dispatcher.getDispatcher(), label);
            }
        }

        // Refresh commands
        for (ServerPlayer player : this.getHandle().players) {
            dispatcher.sendCommands(player);
        }
    }

    private void enablePlugin(Plugin plugin) {
        try {
            List<Permission> perms = plugin.getDescription().getPermissions();

            for (Permission perm : perms) {
                try {
                    this.pluginManager.addPermission(perm, false);
                } catch (IllegalArgumentException ex) {
                    this.getLogger().log(Level.WARNING, "Plugin " + plugin.getDescription().getFullName() + " tried to register permission '" + perm.getName() + "' but it's already registered", ex);
                }
            }
            this.pluginManager.dirtyPermissibles();

            this.pluginManager.enablePlugin(plugin);
        } catch (Throwable ex) {
            Logger.getLogger(CraftServer.class.getName()).log(Level.SEVERE, ex.getMessage() + " loading " + plugin.getDescription().getFullName() + " (Is it up to date?)", ex);
        }
    }

    @Override
    public String getName() {
        return this.serverName;
    }

    @Override
    public String getVersion() {
        return this.serverVersion + " (MC: " + this.console.getServerVersion() + ")";
    }

    @Override
    public String getBukkitVersion() {
        return this.bukkitVersion;
    }

    // Paper start - expose game version
    @Override
    public String getMinecraftVersion() {
        return console.getServerVersion();
    }
    // Paper end

    @Override
    public List<CraftPlayer> getOnlinePlayers() {
        return this.playerView;
    }

    @Override
    @Deprecated
    public Player getPlayer(final String name) {
        Validate.notNull(name, "Name cannot be null");

        Player found = this.getPlayerExact(name);
        // Try for an exact match first.
        if (found != null) {
            return found;
        }

        String lowerName = name.toLowerCase(java.util.Locale.ENGLISH);
        int delta = Integer.MAX_VALUE;
        for (Player player : this.getOnlinePlayers()) {
            if (player.getName().toLowerCase(java.util.Locale.ENGLISH).startsWith(lowerName)) {
                int curDelta = Math.abs(player.getName().length() - lowerName.length());
                if (curDelta < delta) {
                    found = player;
                    delta = curDelta;
                }
                if (curDelta == 0) break;
            }
        }
        return found;
    }

    @Override
    @Deprecated
    public Player getPlayerExact(String name) {
        Validate.notNull(name, "Name cannot be null");

        ServerPlayer player = this.playerList.getPlayerByName(name);
        return (player != null) ? player.getBukkitEntity() : null;
    }

    @Override
    public Player getPlayer(UUID id) {
        ServerPlayer player = this.playerList.getPlayer(id);

        if (player != null) {
            return player.getBukkitEntity();
        }

        return null;
    }

    @Override
    @Deprecated // Paper start
    public int broadcastMessage(String message) {
        return this.broadcast(message, BROADCAST_CHANNEL_USERS);
        // Paper end
    }

    @Override
    @Deprecated
    public List<Player> matchPlayer(String partialName) {
        Validate.notNull(partialName, "PartialName cannot be null");

        List<Player> matchedPlayers = new ArrayList<Player>();

        for (Player iterPlayer : this.getOnlinePlayers()) {
            String iterPlayerName = iterPlayer.getName();

            if (partialName.equalsIgnoreCase(iterPlayerName)) {
                // Exact match
                matchedPlayers.clear();
                matchedPlayers.add(iterPlayer);
                break;
            }
            if (iterPlayerName.toLowerCase(java.util.Locale.ENGLISH).contains(partialName.toLowerCase(java.util.Locale.ENGLISH))) {
                // Partial match
                matchedPlayers.add(iterPlayer);
            }
        }

        return matchedPlayers;
    }

    @Override
    public int getMaxPlayers() {
        return this.playerList.getMaxPlayers();
    }

    // Paper start
    @Override
    public void setMaxPlayers(int maxPlayers) {
        this.playerList.setMaxPlayers(maxPlayers);
    }
    // Paper end

    // NOTE: These are dependent on the corresponding call in MinecraftServer
    // so if that changes this will need to as well
    @Override
    public int getPort() {
        return this.getServer().getPort();
    }

    @Override
    public int getViewDistance() {
        return this.getProperties().viewDistance;
    }

    @Override
    public int getSimulationDistance() {
        return this.getProperties().simulationDistance;
    }

    @Override
    public String getIp() {
        return this.getServer().getLocalIp();
    }

    @Override
    public String getWorldType() {
        return this.getProperties().properties.getProperty("level-type");
    }

    @Override
    public boolean getGenerateStructures() {
        return this.getProperties().getWorldGenSettings(this.getServer().registryAccess()).generateStructures();
    }

    @Override
    public int getMaxWorldSize() {
        return this.getProperties().maxWorldSize;
    }

    @Override
    public boolean getAllowEnd() {
        return this.configuration.getBoolean("settings.allow-end");
    }

    @Override
    public boolean getAllowNether() {
        return this.getServer().isNetherEnabled();
    }

    public boolean getWarnOnOverload() {
        return this.configuration.getBoolean("settings.warn-on-overload");
    }

    public boolean getQueryPlugins() {
        return this.configuration.getBoolean("settings.query-plugins");
    }

    @Override
    public String getResourcePack() {
        return this.getServer().getServerResourcePack().map(MinecraftServer.ServerResourcePackInfo::url).orElse("");
    }

    @Override
    public String getResourcePackHash() {
        return this.getServer().getServerResourcePack().map(MinecraftServer.ServerResourcePackInfo::hash).orElse("").toUpperCase(Locale.ROOT);
    }

    @Override
    public String getResourcePackPrompt() {
        return this.getServer().getServerResourcePack().map(MinecraftServer.ServerResourcePackInfo::prompt).map(CraftChatMessage::fromComponent).orElse("");
    }

    @Override
    public boolean isResourcePackRequired() {
        return this.getServer().isResourcePackRequired();
    }

    @Override
    public boolean hasWhitelist() {
        return this.getProperties().whiteList.get();
    }

    // NOTE: Temporary calls through to server.properies until its replaced
    private DedicatedServerProperties getProperties() {
        return this.console.getProperties();
    }
    // End Temporary calls

    @Override
    public String getUpdateFolder() {
        return this.configuration.getString("settings.update-folder", "update");
    }

    @Override
    public File getUpdateFolderFile() {
        return new File((File) console.options.valueOf("plugins"), this.configuration.getString("settings.update-folder", "update"));
    }

    @Override
    public long getConnectionThrottle() {
        // Spigot Start - Automatically set connection throttle for bungee configurations
        if (org.spigotmc.SpigotConfig.bungee || io.papermc.paper.configuration.GlobalConfiguration.get().proxies.velocity.enabled) { // Paper - Velocity support
            return -1;
        } else {
            return this.configuration.getInt("settings.connection-throttle");
        }
        // Spigot End
    }

    @Override
    @Deprecated
    public int getTicksPerAnimalSpawns() {
        return this.getTicksPerSpawns(SpawnCategory.ANIMAL);
    }

    @Override
    @Deprecated
    public int getTicksPerMonsterSpawns() {
        return this.getTicksPerSpawns(SpawnCategory.MONSTER);
    }

    @Override
    @Deprecated
    public int getTicksPerWaterSpawns() {
        return this.getTicksPerSpawns(SpawnCategory.WATER_ANIMAL);
    }

    @Override
    @Deprecated
    public int getTicksPerWaterAmbientSpawns() {
        return this.getTicksPerSpawns(SpawnCategory.WATER_AMBIENT);
    }

    @Override
    @Deprecated
    public int getTicksPerWaterUndergroundCreatureSpawns() {
        return this.getTicksPerSpawns(SpawnCategory.WATER_UNDERGROUND_CREATURE);
    }

    @Override
    @Deprecated
    public int getTicksPerAmbientSpawns() {
        return this.getTicksPerSpawns(SpawnCategory.AMBIENT);
    }

    @Override
    public int getTicksPerSpawns(SpawnCategory spawnCategory) {
        Validate.notNull(spawnCategory, "SpawnCategory cannot be null");
        Validate.isTrue(CraftSpawnCategory.isValidForLimits(spawnCategory), "SpawnCategory." + spawnCategory + " are not supported.");
        return this.configuration.getInt(CraftSpawnCategory.getConfigNameTicksPerSpawn(spawnCategory));
    }

    @Override
    public PluginManager getPluginManager() {
        return this.pluginManager;
    }

    @Override
    public CraftScheduler getScheduler() {
        return this.scheduler;
    }

    @Override
    public ServicesManager getServicesManager() {
        return this.servicesManager;
    }

    @Override
    public List<World> getWorlds() {
        return new ArrayList<World>(this.worlds.values());
    }

    @Override
    public boolean isTickingWorlds() {
        return console.isIteratingOverLevels;
    }

    public DedicatedPlayerList getHandle() {
        return this.playerList;
    }

    // NOTE: Should only be called from DedicatedServer.ah()
    public boolean dispatchServerCommand(CommandSender sender, ConsoleInput serverCommand) {
        if (sender instanceof Conversable) {
            Conversable conversable = (Conversable) sender;

            if (conversable.isConversing()) {
                conversable.acceptConversationInput(serverCommand.msg);
                return true;
            }
        }
        try {
            this.playerCommandState = true;
            return this.dispatchCommand(sender, serverCommand.msg);
        } catch (Exception ex) {
            this.getLogger().log(Level.WARNING, "Unexpected exception while parsing console command \"" + serverCommand.msg + '"', ex);
            return false;
        } finally {
            this.playerCommandState = false;
        }
    }

    @Override
    public boolean dispatchCommand(CommandSender sender, String commandLine) {
        Validate.notNull(sender, "Sender cannot be null");
        Validate.notNull(commandLine, "CommandLine cannot be null");
        org.spigotmc.AsyncCatcher.catchOp("command dispatch"); // Spigot

        // Paper Start
        if (!org.spigotmc.AsyncCatcher.shuttingDown && !Bukkit.isPrimaryThread()) {
            final CommandSender fSender = sender;
            final String fCommandLine = commandLine;
            Bukkit.getLogger().log(Level.SEVERE, "Command Dispatched Async: " + commandLine);
            Bukkit.getLogger().log(Level.SEVERE, "Please notify author of plugin causing this execution to fix this bug! see: http://bit.ly/1oSiM6C", new Throwable());
            org.bukkit.craftbukkit.v1_19_R1.util.Waitable<Boolean> wait = new org.bukkit.craftbukkit.v1_19_R1.util.Waitable<Boolean>() {
                @Override
                protected Boolean evaluate() {
                    return dispatchCommand(fSender, fCommandLine);
                }
            };
            net.minecraft.server.MinecraftServer.getServer().processQueue.add(wait);
            try {
                return wait.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // This is proper habit for java. If we aren't handling it, pass it on!
            } catch (Exception e) {
                throw new RuntimeException("Exception processing dispatch command", e.getCause());
            }
        }
        // Paper End
        if (this.commandMap.dispatch(sender, commandLine)) {
            return true;
        }

        // Spigot start
        if (!org.spigotmc.SpigotConfig.unknownCommandMessage.isEmpty()) {
            // Paper start
            org.bukkit.event.command.UnknownCommandEvent event = new org.bukkit.event.command.UnknownCommandEvent(sender, commandLine, org.spigotmc.SpigotConfig.unknownCommandMessage);
            Bukkit.getServer().getPluginManager().callEvent(event);
            if (event.message() != null) {
                sender.sendMessage(event.message());
            }
            // Paper end
        }
        // Spigot end

        return false;
    }

    @Override
    public void reload() {
        org.spigotmc.WatchdogThread.hasStarted = false; // Paper - Disable watchdog early timeout on reload
        this.reloadCount++;
        this.configuration = YamlConfiguration.loadConfiguration(this.getConfigFile());
        this.commandsConfiguration = YamlConfiguration.loadConfiguration(this.getCommandsConfigFile());

        console.settings = new DedicatedServerSettings(console.options);
        DedicatedServerProperties config = console.settings.getProperties();

        this.console.setPvpAllowed(config.pvp);
        this.console.setFlightAllowed(config.allowFlight);
        this.console.setMotd(config.motd);
        this.overrideSpawnLimits();
        this.warningState = WarningState.value(this.configuration.getString("settings.deprecated-verbose"));
        TicketType.PLUGIN.timeout = Math.min(20, configuration.getInt("chunk-gc.period-in-ticks")); // Paper - cap plugin loads to 1 second
        this.minimumAPI = this.configuration.getString("settings.minimum-api");
        this.printSaveWarning = false;
        console.autosavePeriod = this.configuration.getInt("ticks-per.autosave");
        this.loadIcon();

        try {
            this.playerList.getIpBans().load();
        } catch (IOException ex) {
            this.logger.log(Level.WARNING, "Failed to load banned-ips.json, " + ex.getMessage());
        }
        try {
            this.playerList.getBans().load();
        } catch (IOException ex) {
            this.logger.log(Level.WARNING, "Failed to load banned-players.json, " + ex.getMessage());
        }

        org.spigotmc.SpigotConfig.init((File) console.options.valueOf("spigot-settings")); // Spigot
        this.console.paperConfigurations.reloadConfigs(this.console);
        for (ServerLevel world : this.console.getAllLevels()) {
            // world.serverLevelData.setDifficulty(config.difficulty); // Paper - per level difficulty
            world.setSpawnSettings(world.serverLevelData.getDifficulty() != Difficulty.PEACEFUL && config.spawnMonsters, config.spawnAnimals); // Paper - per level difficulty (from MinecraftServer#setDifficulty(ServerLevel, Difficulty, boolean))

            for (SpawnCategory spawnCategory : SpawnCategory.values()) {
                if (CraftSpawnCategory.isValidForLimits(spawnCategory)) {
                    long ticksPerCategorySpawn = this.getTicksPerSpawns(spawnCategory);
                    if (ticksPerCategorySpawn < 0) {
                        world.ticksPerSpawnCategory.put(spawnCategory, CraftSpawnCategory.getDefaultTicksPerSpawn(spawnCategory));
                    } else {
                        world.ticksPerSpawnCategory.put(spawnCategory, ticksPerCategorySpawn);
                    }
                }
            }
            world.spigotConfig.init(); // Spigot
        }

        Plugin[] pluginClone = pluginManager.getPlugins().clone(); // Paper
        this.pluginManager.clearPlugins();
        this.commandMap.clearCommands();
        // Paper start
        for (Plugin plugin : pluginClone) {
            entityMetadata.removeAll(plugin);
            worldMetadata.removeAll(plugin);
            playerMetadata.removeAll(plugin);
        }
        // Paper end
        this.reloadData();
        org.spigotmc.SpigotConfig.registerCommands(); // Spigot
        io.papermc.paper.command.PaperCommands.registerCommands(this.console); // Paper
        this.overrideAllCommandBlockCommands = this.commandsConfiguration.getStringList("command-block-overrides").contains("*");
        this.ignoreVanillaPermissions = this.commandsConfiguration.getBoolean("ignore-vanilla-permissions");

        int pollCount = 0;

        // Wait for at most 2.5 seconds for plugins to close their threads
        while (pollCount < 50 && this.getScheduler().getActiveWorkers().size() > 0) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {}
            pollCount++;
        }

        List<BukkitWorker> overdueWorkers = this.getScheduler().getActiveWorkers();
        for (BukkitWorker worker : overdueWorkers) {
            Plugin plugin = worker.getOwner();
            this.getLogger().log(Level.SEVERE, String.format(
                "Nag author(s): '%s' of '%s' about the following: %s",
                plugin.getDescription().getAuthors(),
                plugin.getDescription().getFullName(),
                "This plugin is not properly shutting down its async tasks when it is being reloaded.  This may cause conflicts with the newly loaded version of the plugin"
            ));
            if (console.isDebugging()) io.papermc.paper.util.TraceUtil.dumpTraceForThread(worker.getThread(), "still running"); // Paper
        }
        this.loadPlugins();
        this.enablePlugins(PluginLoadOrder.STARTUP);
        this.enablePlugins(PluginLoadOrder.POSTWORLD);
        this.getPluginManager().callEvent(new ServerLoadEvent(ServerLoadEvent.LoadType.RELOAD));
        org.spigotmc.WatchdogThread.hasStarted = true; // Paper - Disable watchdog early timeout on reload
    }

    // Paper start
    public void waitForAsyncTasksShutdown() {
        int pollCount = 0;

        // Wait for at most 5 seconds for plugins to close their threads
        while (pollCount < 10*5 && getScheduler().getActiveWorkers().size() > 0) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {}
            pollCount++;
        }

        List<BukkitWorker> overdueWorkers = getScheduler().getActiveWorkers();
        for (BukkitWorker worker : overdueWorkers) {
            Plugin plugin = worker.getOwner();
            String author = "<NoAuthorGiven>";
            if (plugin.getDescription().getAuthors().size() > 0) {
                author = plugin.getDescription().getAuthors().get(0);
            }
            getLogger().log(Level.SEVERE, String.format(
                "Nag author: '%s' of '%s' about the following: %s",
                author,
                plugin.getDescription().getName(),
                "This plugin is not properly shutting down its async tasks when it is being shut down. This task may throw errors during the final shutdown logs and might not complete before process dies."
            ));
        }
    }
    // Paper end

    @Override
    public void reloadData() {
        ReloadCommand.reload(console);
    }

    private void loadIcon() {
        this.icon = new CraftIconCache(null);
        try {
            final File file = new File(new File("."), "server-icon.png");
            if (file.isFile()) {
                this.icon = CraftServer.loadServerIcon0(file);
            }
        } catch (Exception ex) {
            this.getLogger().log(Level.WARNING, "Couldn't load server icon", ex);
        }
    }

    @SuppressWarnings({ "unchecked", "finally" })
    private void loadCustomPermissions() {
        File file = new File(this.configuration.getString("settings.permissions-file"));
        FileInputStream stream;

        try {
            stream = new FileInputStream(file);
        } catch (FileNotFoundException ex) {
            try {
                file.createNewFile();
            } finally {
                return;
            }
        }

        Map<String, Map<String, Object>> perms;

        try {
            perms = (Map<String, Map<String, Object>>) this.yaml.load(stream);
        } catch (MarkedYAMLException ex) {
            this.getLogger().log(Level.WARNING, "Server permissions file " + file + " is not valid YAML: " + ex.toString());
            return;
        } catch (Throwable ex) {
            this.getLogger().log(Level.WARNING, "Server permissions file " + file + " is not valid YAML.", ex);
            return;
        } finally {
            try {
                stream.close();
            } catch (IOException ex) {}
        }

        if (perms == null) {
            this.getLogger().log(Level.INFO, "Server permissions file " + file + " is empty, ignoring it");
            return;
        }

        List<Permission> permsList = Permission.loadPermissions(perms, "Permission node '%s' in " + file + " is invalid", Permission.DEFAULT_PERMISSION);

        for (Permission perm : permsList) {
            try {
                this.pluginManager.addPermission(perm);
            } catch (IllegalArgumentException ex) {
                this.getLogger().log(Level.SEVERE, "Permission in " + file + " was already defined", ex);
            }
        }
    }

    @Override
    public String toString() {
        return "CraftServer{" + "serverName=" + this.serverName + ",serverVersion=" + this.serverVersion + ",minecraftVersion=" + this.console.getServerVersion() + '}';
    }

    public World createWorld(String name, World.Environment environment) {
        return WorldCreator.name(name).environment(environment).createWorld();
    }

    public World createWorld(String name, World.Environment environment, long seed) {
        return WorldCreator.name(name).environment(environment).seed(seed).createWorld();
    }

    public World createWorld(String name, Environment environment, ChunkGenerator generator) {
        return WorldCreator.name(name).environment(environment).generator(generator).createWorld();
    }

    public World createWorld(String name, Environment environment, long seed, ChunkGenerator generator) {
        return WorldCreator.name(name).environment(environment).seed(seed).generator(generator).createWorld();
    }

    @Override
    public World createWorld(WorldCreator creator) {
        Preconditions.checkState(this.console.getAllLevels().iterator().hasNext(), "Cannot create additional worlds on STARTUP");
        //Preconditions.checkState(!this.console.isIteratingOverLevels, "Cannot create a world while worlds are being ticked"); // Paper - Cat - Temp disable. We'll see how this goes.
        Validate.notNull(creator, "Creator may not be null");

        String name = creator.name();
        ChunkGenerator generator = creator.generator();
        BiomeProvider biomeProvider = creator.biomeProvider();
        File folder = new File(this.getWorldContainer(), name);
        World world = this.getWorld(name);

        // Paper start
        World worldByKey = this.getWorld(creator.key());
        if (world != null || worldByKey != null) {
            if (world == worldByKey) {
                return world;
            }
            throw new IllegalArgumentException("Cannot create a world with key " + creator.key() + " and name " + name + " one (or both) already match a world that exists");
        }
        // Paper end

        if ((folder.exists()) && (!folder.isDirectory())) {
            throw new IllegalArgumentException("File exists with the name '" + name + "' and isn't a folder");
        }

        if (generator == null) {
            generator = this.getGenerator(name);
        }

        if (biomeProvider == null) {
            biomeProvider = this.getBiomeProvider(name);
        }

        ResourceKey<LevelStem> actualDimension;
        switch (creator.environment()) {
            case NORMAL:
                actualDimension = LevelStem.OVERWORLD;
                break;
            case NETHER:
                actualDimension = LevelStem.NETHER;
                break;
            case THE_END:
                actualDimension = LevelStem.END;
                break;
            default:
                throw new IllegalArgumentException("Illegal dimension");
        }

        LevelStorageSource.LevelStorageAccess worldSession;
        try {
            worldSession = LevelStorageSource.createDefault(this.getWorldContainer().toPath()).createAccess(name, actualDimension);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        boolean hardcore = creator.hardcore();

        PrimaryLevelData worlddata = (PrimaryLevelData) worldSession.getDataTag(console.registryreadops, console.datapackconfiguration, console.registryHolder.allElementsLifecycle());

        LevelSettings worldSettings;
        // See MinecraftServer.a(String, String, long, WorldType, JsonElement)
        if (worlddata == null) {
            DedicatedServerProperties.WorldGenProperties properties = new DedicatedServerProperties.WorldGenProperties(Objects.toString(creator.seed()), GsonHelper.parse((creator.generatorSettings().isEmpty()) ? "{}" : creator.generatorSettings()), creator.generateStructures(), creator.type().name().toLowerCase(Locale.ROOT));

            WorldGenSettings generatorsettings = properties.create(this.console.registryAccess());
            worldSettings = new LevelSettings(name, GameType.byId(this.getDefaultGameMode().getValue()), hardcore, Difficulty.EASY, false, new GameRules(), console.datapackconfiguration);
            worlddata = new PrimaryLevelData(worldSettings, generatorsettings, Lifecycle.stable());
        }
        worlddata.checkName(name);
        worlddata.setModdedInfo(this.console.getServerModName(), this.console.getModdedStatus().shouldReportAsModified());
        // Paper - move down

        long j = BiomeManager.obfuscateSeed(creator.seed());
        List<CustomSpawner> list = ImmutableList.of(new PhantomSpawner(), new PatrolSpawner(), new CatSpawner(), new VillageSiege(), new WanderingTraderSpawner(worlddata));
        net.minecraft.core.Registry<LevelStem> iregistry = worlddata.worldGenSettings().dimensions();
        LevelStem worlddimension = (LevelStem) iregistry.get(actualDimension);

        WorldInfo worldInfo = new CraftWorldInfo(worlddata, worldSession, creator.environment(), worlddimension.typeHolder().value(), worlddimension.generator(), this.getHandle().getServer().registryAccess()); // Paper
        if (biomeProvider == null && generator != null) {
            biomeProvider = generator.getDefaultBiomeProvider(worldInfo);
        }

        // Paper start - fix and optimise world upgrading
        if (console.options.has("forceUpgrade")) {
            net.minecraft.server.Main.convertWorldButItWorks(
                actualDimension,  worldSession, DataFixers.getDataFixer(), worlddimension.generator().getTypeNameForDataFixer(), console.options.has("eraseCache")
            );
        }
        // Paper end - fix and optimise world upgrading
        ResourceKey<net.minecraft.world.level.Level> worldKey;
        String levelName = this.getServer().getProperties().levelName;
        if (name.equals(levelName + "_nether")) {
            worldKey = net.minecraft.world.level.Level.NETHER;
        } else if (name.equals(levelName + "_the_end")) {
            worldKey = net.minecraft.world.level.Level.END;
        } else {
            worldKey = ResourceKey.create(net.minecraft.core.Registry.DIMENSION_REGISTRY, new net.minecraft.resources.ResourceLocation(creator.key().getNamespace().toLowerCase(java.util.Locale.ENGLISH), creator.key().getKey().toLowerCase(java.util.Locale.ENGLISH))); // Paper
        }

        ServerLevel internal = (ServerLevel) new ServerLevel(this.console, console.executor, worldSession, worlddata, worldKey, worlddimension, this.getServer().progressListenerFactory.create(11),
                worlddata.worldGenSettings().isDebug(), j, creator.environment() == Environment.NORMAL ? list : ImmutableList.of(), true, creator.environment(), generator, biomeProvider);

        if (!(this.worlds.containsKey(name.toLowerCase(java.util.Locale.ENGLISH)))) {
            return null;
        }

        this.console.addLevel(internal); // Paper - move up
        this.console.initWorld(internal, worlddata, worlddata, worlddata.worldGenSettings());

        internal.setSpawnSettings(true, true);
        // Paper - move up

        internal.keepSpawnInMemory = creator.keepSpawnLoaded().toBooleanOrElse(internal.getWorld().getKeepSpawnInMemory()); // Paper
        this.getServer().prepareLevels(internal.getChunkSource().chunkMap.progressListener, internal);
        //internal.entityManager.tick(); // SPIGOT-6526: Load pending entities so they are available to the API // Paper - rewrite chunk system

        this.pluginManager.callEvent(new WorldLoadEvent(internal.getWorld()));
        return internal.getWorld();
    }

    @Override
    public boolean unloadWorld(String name, boolean save) {
        return this.unloadWorld(this.getWorld(name), save);
    }

    @Override
    public boolean unloadWorld(World world, boolean save) {
        //Preconditions.checkState(!this.console.isIteratingOverLevels, "Cannot unload a world while worlds are being ticked"); // Paper - Cat - Temp disable. We'll see how this goes.
        if (world == null) {
            return false;
        }

        ServerLevel handle = ((CraftWorld) world).getHandle();

        if (this.console.getLevel(handle.dimension()) == null) {
            return false;
        }

        if (handle.dimension() == net.minecraft.world.level.Level.OVERWORLD) {
            return false;
        }

        if (handle.players().size() > 0 || handle.pendingLogin.size() > 0) { // Paper
            return false;
        }

        WorldUnloadEvent e = new WorldUnloadEvent(handle.getWorld());
        this.pluginManager.callEvent(e);

        if (e.isCancelled()) {
            return false;
        }

        try {
            if (save) {
                handle.save(null, true, false); // Paper - don't disable saving
            }

            handle.getChunkSource().close(save);
            // handle.entityManager.close(save); // SPIGOT-6722: close entityManager // Paper - rewrite chunk system
            handle.convertable.close();
        } catch (Exception ex) {
            this.getLogger().log(Level.SEVERE, null, ex);
        }

        this.worlds.remove(world.getName().toLowerCase(java.util.Locale.ENGLISH));
        this.console.removeLevel(handle);
        return true;
    }

    public DedicatedServer getServer() {
        return this.console;
    }

    @Override
    public World getWorld(String name) {
        Validate.notNull(name, "Name cannot be null");

        return this.worlds.get(name.toLowerCase(java.util.Locale.ENGLISH));
    }

    @Override
    public World getWorld(UUID uid) {
        for (World world : this.worlds.values()) {
            if (world.getUID().equals(uid)) {
                return world;
            }
        }
        return null;
    }

    // Paper start
    @Override
    public World getWorld(NamespacedKey worldKey) {
        ServerLevel worldServer = console.getLevel(ResourceKey.create(net.minecraft.core.Registry.DIMENSION_REGISTRY, CraftNamespacedKey.toMinecraft(worldKey)));
        if (worldServer == null) return null;
        return worldServer.getWorld();
    }
    // Paper end

    public void addWorld(World world) {
        // Check if a World already exists with the UID.
        if (this.getWorld(world.getUID()) != null) {
            System.out.println("World " + world.getName() + " is a duplicate of another world and has been prevented from loading. Please delete the uid.dat file from " + world.getName() + "'s world directory if you want to be able to load the duplicate world.");
            return;
        }
        this.worlds.put(world.getName().toLowerCase(java.util.Locale.ENGLISH), world);
    }

    @Override
    public WorldBorder createWorldBorder() {
        return new CraftWorldBorder(new net.minecraft.world.level.border.WorldBorder());
    }

    @Override
    public Logger getLogger() {
        return this.logger;
    }

    // Paper start - JLine update
    /*
    public ConsoleReader getReader() {
        return console.reader;
    }
    */
    // Paper end

    @Override
    public PluginCommand getPluginCommand(String name) {
        Command command = this.commandMap.getCommand(name);

        if (command instanceof PluginCommand) {
            return (PluginCommand) command;
        } else {
            return null;
        }
    }

    @Override
    public void savePlayers() {
        this.checkSaveState();
        this.playerList.saveAll();
    }

    @Override
    public boolean addRecipe(Recipe recipe) {
        CraftRecipe toAdd;
        if (recipe instanceof CraftRecipe) {
            toAdd = (CraftRecipe) recipe;
        } else {
            if (recipe instanceof ShapedRecipe) {
                toAdd = CraftShapedRecipe.fromBukkitRecipe((ShapedRecipe) recipe);
            } else if (recipe instanceof ShapelessRecipe) {
                toAdd = CraftShapelessRecipe.fromBukkitRecipe((ShapelessRecipe) recipe);
            } else if (recipe instanceof FurnaceRecipe) {
                toAdd = CraftFurnaceRecipe.fromBukkitRecipe((FurnaceRecipe) recipe);
            } else if (recipe instanceof BlastingRecipe) {
                toAdd = CraftBlastingRecipe.fromBukkitRecipe((BlastingRecipe) recipe);
            } else if (recipe instanceof CampfireRecipe) {
                toAdd = CraftCampfireRecipe.fromBukkitRecipe((CampfireRecipe) recipe);
            } else if (recipe instanceof SmokingRecipe) {
                toAdd = CraftSmokingRecipe.fromBukkitRecipe((SmokingRecipe) recipe);
            } else if (recipe instanceof StonecuttingRecipe) {
                toAdd = CraftStonecuttingRecipe.fromBukkitRecipe((StonecuttingRecipe) recipe);
            } else if (recipe instanceof SmithingRecipe) {
                toAdd = CraftSmithingRecipe.fromBukkitRecipe((SmithingRecipe) recipe);
            } else if (recipe instanceof ComplexRecipe) {
                throw new UnsupportedOperationException("Cannot add custom complex recipe");
            } else {
                return false;
            }
        }
        toAdd.addToCraftingManager();
        return true;
    }

    @Override
    public List<Recipe> getRecipesFor(ItemStack result) {
        Validate.notNull(result, "Result cannot be null");

        List<Recipe> results = new ArrayList<Recipe>();
        Iterator<Recipe> iter = this.recipeIterator();
        while (iter.hasNext()) {
            Recipe recipe = iter.next();
            ItemStack stack = recipe.getResult();
            if (stack.getType() != result.getType()) {
                continue;
            }
            if (result.getDurability() == -1 || result.getDurability() == stack.getDurability()) {
                results.add(recipe);
            }
        }
        return results;
    }

    @Override
    public Recipe getRecipe(NamespacedKey recipeKey) {
        Preconditions.checkArgument(recipeKey != null, "recipeKey == null");

        return this.getServer().getRecipeManager().byKey(CraftNamespacedKey.toMinecraft(recipeKey)).map(net.minecraft.world.item.crafting.Recipe::toBukkitRecipe).orElse(null);
    }

    @Override
    public Recipe getCraftingRecipe(ItemStack[] craftingMatrix, World world) {
        // Create a players Crafting Inventory
        AbstractContainerMenu container = new AbstractContainerMenu(null, -1) {
            @Override
            public InventoryView getBukkitView() {
                return null;
            }

            @Override
            public boolean stillValid(net.minecraft.world.entity.player.Player player) {
                return false;
            }

            @Override
            public net.minecraft.world.item.ItemStack quickMoveStack(net.minecraft.world.entity.player.Player player, int index) {
                return net.minecraft.world.item.ItemStack.EMPTY;
            }
        };
        CraftingContainer inventoryCrafting = new CraftingContainer(container, 3, 3);

        return this.getNMSRecipe(craftingMatrix, inventoryCrafting, (CraftWorld) world).map(net.minecraft.world.item.crafting.Recipe::toBukkitRecipe).orElse(null);
    }

    @Override
    public ItemStack craftItem(ItemStack[] craftingMatrix, World world, Player player) {
        Preconditions.checkArgument(world != null, "world must not be null");
        Preconditions.checkArgument(player != null, "player must not be null");

        CraftWorld craftWorld = (CraftWorld) world;
        CraftPlayer craftPlayer = (CraftPlayer) player;

        // Create a players Crafting Inventory and get the recipe
        CraftingMenu container = new CraftingMenu(-1, craftPlayer.getHandle().getInventory());
        CraftingContainer inventoryCrafting = container.craftSlots;
        ResultContainer craftResult = container.resultSlots;

        Optional<CraftingRecipe> recipe = this.getNMSRecipe(craftingMatrix, inventoryCrafting, craftWorld);

        // Generate the resulting ItemStack from the Crafting Matrix
        net.minecraft.world.item.ItemStack itemstack = net.minecraft.world.item.ItemStack.EMPTY;

        if (recipe.isPresent()) {
            CraftingRecipe recipeCrafting = recipe.get();
            if (craftResult.setRecipeUsed(craftWorld.getHandle(), craftPlayer.getHandle(), recipeCrafting)) {
                itemstack = recipeCrafting.assemble(inventoryCrafting);
            }
        }

        // Call Bukkit event to check for matrix/result changes.
        net.minecraft.world.item.ItemStack result = CraftEventFactory.callPreCraftEvent(inventoryCrafting, craftResult, itemstack, container.getBukkitView(), recipe.orElse(null) instanceof RepairItemRecipe);

        // Set the resulting matrix items
        for (int i = 0; i < craftingMatrix.length; i++) {
            Item remaining = inventoryCrafting.getContents().get(i).getItem().getCraftingRemainingItem();
            craftingMatrix[i] = (remaining != null) ? CraftItemStack.asBukkitCopy(remaining.getDefaultInstance()) : null;
        }

        return CraftItemStack.asBukkitCopy(result);
    }

    private Optional<CraftingRecipe> getNMSRecipe(ItemStack[] craftingMatrix, CraftingContainer inventoryCrafting, CraftWorld world) {
        Preconditions.checkArgument(craftingMatrix != null, "craftingMatrix must not be null");
        Preconditions.checkArgument(craftingMatrix.length == 9, "craftingMatrix must be an array of length 9");
        Preconditions.checkArgument(world != null, "world must not be null");

        for (int i = 0; i < craftingMatrix.length; i++) {
            inventoryCrafting.setItem(i, CraftItemStack.asNMSCopy(craftingMatrix[i]));
        }

        return this.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, inventoryCrafting, world.getHandle());
    }

    @Override
    public Iterator<Recipe> recipeIterator() {
        return new RecipeIterator();
    }

    @Override
    public void clearRecipes() {
        this.console.getRecipeManager().clearRecipes();
    }

    @Override
    public void resetRecipes() {
        this.reloadData(); // Not ideal but hard to reload a subset of a resource pack
    }

    @Override
    public boolean removeRecipe(NamespacedKey recipeKey) {
        Preconditions.checkArgument(recipeKey != null, "recipeKey == null");

        ResourceLocation mcKey = CraftNamespacedKey.toMinecraft(recipeKey);
        return this.getServer().getRecipeManager().removeRecipe(mcKey);
    }

    @Override
    public Map<String, String[]> getCommandAliases() {
        ConfigurationSection section = this.commandsConfiguration.getConfigurationSection("aliases");
        Map<String, String[]> result = new LinkedHashMap<String, String[]>();

        if (section != null) {
            for (String key : section.getKeys(false)) {
                List<String> commands;

                if (section.isList(key)) {
                    commands = section.getStringList(key);
                } else {
                    commands = ImmutableList.of(section.getString(key));
                }

                result.put(key, commands.toArray(new String[commands.size()]));
            }
        }

        return result;
    }

    public void removeBukkitSpawnRadius() {
        this.configuration.set("settings.spawn-radius", null);
        this.saveConfig();
    }

    public int getBukkitSpawnRadius() {
        return this.configuration.getInt("settings.spawn-radius", -1);
    }

    // Paper start
    @Override
    public net.kyori.adventure.text.Component shutdownMessage() {
        String msg = getShutdownMessage();
        return msg != null ? net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize(msg) : null;
    }
    // Paper end
    @Override
    @Deprecated // Paper
    public String getShutdownMessage() {
        return this.configuration.getString("settings.shutdown-message");
    }

    @Override
    public int getSpawnRadius() {
        return this.getServer().getSpawnProtectionRadius();
    }

    @Override
    public void setSpawnRadius(int value) {
        this.configuration.set("settings.spawn-radius", value);
        this.saveConfig();
    }

    @Override
    public boolean shouldSendChatPreviews() {
        return this.getServer().previewsChat();
    }

    @Override
    public boolean isEnforcingSecureProfiles() {
        return this.getServer().enforceSecureProfile();
    }

    @Override
    public boolean getHideOnlinePlayers() {
        return this.console.hidesOnlinePlayers();
    }

    @Override
    public boolean getOnlineMode() {
        return this.console.usesAuthentication();
    }

    @Override
    public boolean getAllowFlight() {
        return this.console.isFlightAllowed();
    }

    @Override
    public boolean isHardcore() {
        return this.console.isHardcore();
    }

    public ChunkGenerator getGenerator(String world) {
        ConfigurationSection section = this.configuration.getConfigurationSection("worlds");
        ChunkGenerator result = null;

        if (section != null) {
            section = section.getConfigurationSection(world);

            if (section != null) {
                String name = section.getString("generator");

                if ((name != null) && (!name.equals(""))) {
                    String[] split = name.split(":", 2);
                    String id = (split.length > 1) ? split[1] : null;
                    Plugin plugin = this.pluginManager.getPlugin(split[0]);

                    if (plugin == null) {
                        this.getLogger().severe("Could not set generator for default world '" + world + "': Plugin '" + split[0] + "' does not exist");
                    } else if (!plugin.isEnabled()) {
                        this.getLogger().severe("Could not set generator for default world '" + world + "': Plugin '" + plugin.getDescription().getFullName() + "' is not enabled yet (is it load:STARTUP?)");
                    } else {
                        try {
                            result = plugin.getDefaultWorldGenerator(world, id);
                            if (result == null) {
                                this.getLogger().severe("Could not set generator for default world '" + world + "': Plugin '" + plugin.getDescription().getFullName() + "' lacks a default world generator");
                            }
                        } catch (Throwable t) {
                            plugin.getLogger().log(Level.SEVERE, "Could not set generator for default world '" + world + "': Plugin '" + plugin.getDescription().getFullName(), t);
                        }
                    }
                }
            }
        }

        return result;
    }

    public BiomeProvider getBiomeProvider(String world) {
        ConfigurationSection section = this.configuration.getConfigurationSection("worlds");
        BiomeProvider result = null;

        if (section != null) {
            section = section.getConfigurationSection(world);

            if (section != null) {
                String name = section.getString("biome-provider");

                if ((name != null) && (!name.equals(""))) {
                    String[] split = name.split(":", 2);
                    String id = (split.length > 1) ? split[1] : null;
                    Plugin plugin = this.pluginManager.getPlugin(split[0]);

                    if (plugin == null) {
                        this.getLogger().severe("Could not set biome provider for default world '" + world + "': Plugin '" + split[0] + "' does not exist");
                    } else if (!plugin.isEnabled()) {
                        this.getLogger().severe("Could not set biome provider for default world '" + world + "': Plugin '" + plugin.getDescription().getFullName() + "' is not enabled yet (is it load:STARTUP?)");
                    } else {
                        try {
                            result = plugin.getDefaultBiomeProvider(world, id);
                            if (result == null) {
                                this.getLogger().severe("Could not set biome provider for default world '" + world + "': Plugin '" + plugin.getDescription().getFullName() + "' lacks a default world biome provider");
                            }
                        } catch (Throwable t) {
                            plugin.getLogger().log(Level.SEVERE, "Could not set biome provider for default world '" + world + "': Plugin '" + plugin.getDescription().getFullName(), t);
                        }
                    }
                }
            }
        }

        return result;
    }

    @Override
    @Deprecated
    public CraftMapView getMap(int id) {
        MapItemSavedData worldmap = this.console.getLevel(net.minecraft.world.level.Level.OVERWORLD).getMapData("map_" + id);
        if (worldmap == null) {
            return null;
        }
        return worldmap.mapView;
    }

    @Override
    public CraftMapView createMap(World world) {
        Validate.notNull(world, "World cannot be null");

        net.minecraft.world.level.Level minecraftWorld = ((CraftWorld) world).getHandle();
        // creates a new map at world spawn with the scale of 3, with out tracking position and unlimited tracking
        int newId = MapItem.createNewSavedData(minecraftWorld, minecraftWorld.getLevelData().getXSpawn(), minecraftWorld.getLevelData().getZSpawn(), 3, false, false, minecraftWorld.dimension());
        return minecraftWorld.getMapData(MapItem.makeKey(newId)).mapView;
    }

    @Override
    public ItemStack createExplorerMap(World world, Location location, StructureType structureType) {
        return this.createExplorerMap(world, location, structureType, 100, true);
    }

    @Override
    public ItemStack createExplorerMap(World world, Location location, StructureType structureType, int radius, boolean findUnexplored) {
        Validate.notNull(world, "World cannot be null");
        Validate.notNull(structureType, "StructureType cannot be null");
        Validate.notNull(structureType.getMapIcon(), "Cannot create explorer maps for StructureType " + structureType.getName());

        ServerLevel worldServer = ((CraftWorld) world).getHandle();
        Location structureLocation = world.locateNearestStructure(location, structureType, radius, findUnexplored);
        BlockPos structurePosition = new BlockPos(structureLocation.getBlockX(), structureLocation.getBlockY(), structureLocation.getBlockZ());

        // Create map with trackPlayer = true, unlimitedTracking = true
        net.minecraft.world.item.ItemStack stack = MapItem.create(worldServer, structurePosition.getX(), structurePosition.getZ(), MapView.Scale.NORMAL.getValue(), true, true);
        MapItem.renderBiomePreviewMap(worldServer, stack);
        // "+" map ID taken from EntityVillager
        MapItem.getSavedData(stack, worldServer).addTargetDecoration(stack, structurePosition, "+", MapDecoration.Type.byIcon(structureType.getMapIcon().getValue()));

        return CraftItemStack.asBukkitCopy(stack);
    }

    @Override
    public void shutdown() {
        this.console.halt(false);
    }

    @Override
    @Deprecated // Paper
    public int broadcast(String message, String permission) {
        // Paper start - Adventure
        return this.broadcast(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize(message), permission);
    }

    @Override
    public int broadcast(net.kyori.adventure.text.Component message) {
        return this.broadcast(message, BROADCAST_CHANNEL_USERS);
    }

    @Override
    public int broadcast(net.kyori.adventure.text.Component message, String permission) {
        // Paper end
        Set<CommandSender> recipients = new HashSet<>();
        for (Permissible permissible : this.getPluginManager().getPermissionSubscriptions(permission)) {
            if (permissible instanceof CommandSender && !(permissible instanceof org.bukkit.command.BlockCommandSender) && permissible.hasPermission(permission)) { // Paper - don't broadcast to BlockCommandSender (specifically Command Blocks)
                recipients.add((CommandSender) permissible);
            }
        }

        BroadcastMessageEvent broadcastMessageEvent = new BroadcastMessageEvent(!Bukkit.isPrimaryThread(), message, recipients); // Paper - Adventure
        this.getPluginManager().callEvent(broadcastMessageEvent);

        if (broadcastMessageEvent.isCancelled()) {
            return 0;
        }

        message = broadcastMessageEvent.message(); // Paper - Adventure

        for (CommandSender recipient : recipients) {
            recipient.sendMessage(message);
        }

        return recipients.size();
    }

    // Paper start
    @Nullable
    public UUID getPlayerUniqueId(String name) {
        Player player = Bukkit.getPlayerExact(name);
        if (player != null) {
            return player.getUniqueId();
        }
        GameProfile profile;
        // Only fetch an online UUID in online mode
        if (io.papermc.paper.configuration.GlobalConfiguration.get().proxies.isProxyOnlineMode()) {
            profile = console.getProfileCache().get(name).orElse(null);
        } else {
            // Make an OfflinePlayer using an offline mode UUID since the name has no profile
            profile = new GameProfile(UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(Charsets.UTF_8)), name);
        }
        return profile != null ? profile.getId() : null;
    }
    // Paper end

    @Override
    @Deprecated
    public OfflinePlayer getOfflinePlayer(String name) {
        Validate.notNull(name, "Name cannot be null");
        Validate.notEmpty(name, "Name cannot be empty");

        OfflinePlayer result = this.getPlayerExact(name);
        if (result == null) {
            // Spigot Start
            GameProfile profile = null;
            // Only fetch an online UUID in online mode
            if ( this.getOnlineMode() || io.papermc.paper.configuration.GlobalConfiguration.get().proxies.isProxyOnlineMode() ) // Paper - Handle via setting
            {
                profile = this.console.getProfileCache().get(name).orElse(null);
            }
            // Spigot end
            if (profile == null) {
                // Make an OfflinePlayer using an offline mode UUID since the name has no profile
                result = this.getOfflinePlayer(new GameProfile(UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(Charsets.UTF_8)), name));
            } else {
                // Use the GameProfile even when we get a UUID so we ensure we still have a name
                result = this.getOfflinePlayer(profile);
            }
        } else {
            this.offlinePlayers.remove(result.getUniqueId());
        }

        return result;
    }

    // Paper start
    @Override
    @Nullable
    public OfflinePlayer getOfflinePlayerIfCached(String name) {
        Validate.notNull(name, "Name cannot be null");
        Validate.notEmpty(name, "Name cannot be empty");

        OfflinePlayer result = getPlayerExact(name);
        if (result == null) {
            GameProfile profile = console.getProfileCache().getProfileIfCached(name);

            if (profile != null) {
                result = getOfflinePlayer(profile);
            }
        } else {
            offlinePlayers.remove(result.getUniqueId());
        }

        return result;
    }
    // Paper end

    @Override
    public OfflinePlayer getOfflinePlayer(UUID id) {
        Validate.notNull(id, "UUID cannot be null");

        OfflinePlayer result = this.getPlayer(id);
        if (result == null) {
            result = this.offlinePlayers.get(id);
            if (result == null) {
                result = new CraftOfflinePlayer(this, new GameProfile(id, null));
                this.offlinePlayers.put(id, result);
            }
        } else {
            this.offlinePlayers.remove(id);
        }

        return result;
    }

    @Override
    public PlayerProfile createPlayerProfile(UUID uniqueId, String name) {
        return new CraftPlayerProfile(uniqueId, name);
    }

    @Override
    public PlayerProfile createPlayerProfile(UUID uniqueId) {
        return new CraftPlayerProfile(uniqueId, null);
    }

    @Override
    public PlayerProfile createPlayerProfile(String name) {
        return new CraftPlayerProfile(null, name);
    }

    public OfflinePlayer getOfflinePlayer(GameProfile profile) {
        OfflinePlayer player = new CraftOfflinePlayer(this, profile);
        this.offlinePlayers.put(profile.getId(), player);
        return player;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<String> getIPBans() {
        return this.playerList.getIpBans().getEntries().stream().map(IpBanListEntry::getUser).collect(Collectors.toSet());
    }

    @Override
    public void banIP(String address) {
        Validate.notNull(address, "Address cannot be null.");

        this.getBanList(org.bukkit.BanList.Type.IP).addBan(address, null, null, null);
    }

    @Override
    public void unbanIP(String address) {
        Validate.notNull(address, "Address cannot be null.");

        this.getBanList(org.bukkit.BanList.Type.IP).pardon(address);
    }

    @Override
    public Set<OfflinePlayer> getBannedPlayers() {
        Set<OfflinePlayer> result = new HashSet<OfflinePlayer>();

        for (UserBanListEntry entry : this.playerList.getBans().getValues()) {
            result.add(this.getOfflinePlayer(entry.getUser()));
        }

        return result;
    }

    @Override
    public BanList getBanList(BanList.Type type) {
        Validate.notNull(type, "Type cannot be null");

        switch (type) {
        case IP:
            return new CraftIpBanList(this.playerList.getIpBans());
        case NAME:
        default:
            return new CraftProfileBanList(this.playerList.getBans());
        }
    }

    @Override
    public void setWhitelist(boolean value) {
        this.playerList.setUsingWhiteList(value);
        this.console.storeUsingWhiteList(value);
    }

    @Override
    public boolean isWhitelistEnforced() {
        return this.console.isEnforceWhitelist();
    }

    @Override
    public void setWhitelistEnforced(boolean value) {
        this.console.setEnforceWhitelist(value);
    }

    @Override
    public Set<OfflinePlayer> getWhitelistedPlayers() {
        Set<OfflinePlayer> result = new LinkedHashSet<OfflinePlayer>();

        for (UserWhiteListEntry entry : this.playerList.getWhiteList().getValues()) {
            result.add(this.getOfflinePlayer(entry.getUser()));
        }

        return result;
    }

    @Override
    public Set<OfflinePlayer> getOperators() {
        Set<OfflinePlayer> result = new HashSet<OfflinePlayer>();

        for (ServerOpListEntry entry : this.playerList.getOps().getValues()) {
            result.add(this.getOfflinePlayer(entry.getUser()));
        }

        return result;
    }

    @Override
    public void reloadWhitelist() {
        this.playerList.reloadWhiteList();
    }

    @Override
    public GameMode getDefaultGameMode() {
        return GameMode.getByValue(this.console.getLevel(net.minecraft.world.level.Level.OVERWORLD).serverLevelData.getGameType().getId());
    }

    @Override
    public void setDefaultGameMode(GameMode mode) {
        Validate.notNull(mode, "Mode cannot be null");

        for (World world : this.getWorlds()) {
            ((CraftWorld) world).getHandle().serverLevelData.setGameType(GameType.byId(mode.getValue()));
        }
    }

    @Override
    public ConsoleCommandSender getConsoleSender() {
        return console.console;
    }

    // Paper start
    @Override
    public CommandSender createCommandSender(final java.util.function.Consumer<? super net.kyori.adventure.text.Component> feedback) {
        return new io.papermc.paper.commands.FeedbackForwardingSender(feedback, this);
    }
    // Paper end

    public EntityMetadataStore getEntityMetadata() {
        return this.entityMetadata;
    }

    public PlayerMetadataStore getPlayerMetadata() {
        return this.playerMetadata;
    }

    public WorldMetadataStore getWorldMetadata() {
        return this.worldMetadata;
    }

    @Override
    public File getWorldContainer() {
        return this.getServer().storageSource.getDimensionPath(net.minecraft.world.level.Level.OVERWORLD).getParent().toFile();
    }

    @Override
    public OfflinePlayer[] getOfflinePlayers() {
        PlayerDataStorage storage = console.playerDataStorage;
        String[] files = storage.getPlayerDir().list(new DatFileFilter());
        Set<OfflinePlayer> players = new HashSet<OfflinePlayer>();

        for (String file : files) {
            try {
                players.add(this.getOfflinePlayer(UUID.fromString(file.substring(0, file.length() - 4))));
            } catch (IllegalArgumentException ex) {
                // Who knows what is in this directory, just ignore invalid files
            }
        }

        players.addAll(this.getOnlinePlayers());

        return players.toArray(new OfflinePlayer[players.size()]);
    }

    @Override
    public Messenger getMessenger() {
        return this.messenger;
    }

    @Override
    public void sendPluginMessage(Plugin source, String channel, byte[] message) {
        StandardMessenger.validatePluginMessage(this.getMessenger(), source, channel, message);

        for (Player player : this.getOnlinePlayers()) {
            player.sendPluginMessage(source, channel, message);
        }
    }

    @Override
    public Set<String> getListeningPluginChannels() {
        Set<String> result = new HashSet<String>();

        for (Player player : this.getOnlinePlayers()) {
            result.addAll(player.getListeningPluginChannels());
        }

        return result;
    }

    @Override
    public Inventory createInventory(InventoryHolder owner, InventoryType type) {
        Validate.isTrue(type.isCreatable(), "Cannot open an inventory of type ", type);
        return CraftInventoryCreator.INSTANCE.createInventory(owner, type);
    }

    // Paper start
    @Override
    public Inventory createInventory(InventoryHolder owner, InventoryType type, net.kyori.adventure.text.Component title) {
        Validate.isTrue(type.isCreatable(), "Cannot open an inventory of type ", type);
        return CraftInventoryCreator.INSTANCE.createInventory(owner, type, title);
    }
    // Paper end

    @Override
    public Inventory createInventory(InventoryHolder owner, InventoryType type, String title) {
        Validate.isTrue(type.isCreatable(), "Cannot open an inventory of type ", type);
        return CraftInventoryCreator.INSTANCE.createInventory(owner, type, title);
    }

    @Override
    public Inventory createInventory(InventoryHolder owner, int size) throws IllegalArgumentException {
        Validate.isTrue(9 <= size && size <= 54 && size % 9 == 0, "Size for custom inventory must be a multiple of 9 between 9 and 54 slots (got " + size + ")");
        return CraftInventoryCreator.INSTANCE.createInventory(owner, size);
    }

    // Paper start
    @Override
    public Inventory createInventory(InventoryHolder owner, int size, net.kyori.adventure.text.Component title) throws IllegalArgumentException {
        Validate.isTrue(9 <= size && size <= 54 && size % 9 == 0, "Size for custom inventory must be a multiple of 9 between 9 and 54 slots (got " + size + ")");
        return CraftInventoryCreator.INSTANCE.createInventory(owner, size, title);
    }
    // Paper end

    @Override
    public Inventory createInventory(InventoryHolder owner, int size, String title) throws IllegalArgumentException {
        Validate.isTrue(9 <= size && size <= 54 && size % 9 == 0, "Size for custom inventory must be a multiple of 9 between 9 and 54 slots (got " + size + ")");
        return CraftInventoryCreator.INSTANCE.createInventory(owner, size, title);
    }

    // Paper start
    @Override
    public Merchant createMerchant(net.kyori.adventure.text.Component title) {
        return new org.bukkit.craftbukkit.v1_19_R1.inventory.CraftMerchantCustom(title == null ? InventoryType.MERCHANT.defaultTitle() : title);
    }
    // Paper end
    @Override
    @Deprecated // Paper
    public Merchant createMerchant(String title) {
        return new CraftMerchantCustom(title == null ? InventoryType.MERCHANT.getDefaultTitle() : title);
    }

    @Override
    public int getMaxChainedNeighborUpdates() {
        return this.getServer().getMaxChainedNeighborUpdates();
    }

    @Override
    public HelpMap getHelpMap() {
        return this.helpMap;
    }

    @Override // Paper - add override
    public SimpleCommandMap getCommandMap() {
        return this.commandMap;
    }

    @Override
    @Deprecated
    public int getMonsterSpawnLimit() {
        return this.getSpawnLimit(SpawnCategory.MONSTER);
    }

    @Override
    @Deprecated
    public int getAnimalSpawnLimit() {
        return this.getSpawnLimit(SpawnCategory.ANIMAL);
    }

    @Override
    @Deprecated
    public int getWaterAnimalSpawnLimit() {
        return this.getSpawnLimit(SpawnCategory.WATER_ANIMAL);
    }

    @Override
    @Deprecated
    public int getWaterAmbientSpawnLimit() {
        return this.getSpawnLimit(SpawnCategory.WATER_AMBIENT);
    }

    @Override
    @Deprecated
    public int getWaterUndergroundCreatureSpawnLimit() {
        return this.getSpawnLimit(SpawnCategory.WATER_UNDERGROUND_CREATURE);
    }

    @Override
    @Deprecated
    public int getAmbientSpawnLimit() {
        return this.getSpawnLimit(SpawnCategory.AMBIENT);
    }

    @Override
    public int getSpawnLimit(SpawnCategory spawnCategory) {
        // Paper start
        Validate.notNull(spawnCategory, "SpawnCategory cannot be null");
        Validate.isTrue(CraftSpawnCategory.isValidForLimits(spawnCategory), "SpawnCategory." + spawnCategory + " does not have a spawn limit.");
        return this.getSpawnLimitUnsafe(spawnCategory);
    }
    public int getSpawnLimitUnsafe(final SpawnCategory spawnCategory) {
        // Paper end
        return this.spawnCategoryLimit.getOrDefault(spawnCategory, -1);
    }

    @Override
    public boolean isPrimaryThread() {
        return io.papermc.paper.util.TickThread.isTickThread(); // Paper - rewrite chunk system
    }

    // Paper start
    @Override
    public net.kyori.adventure.text.Component motd() {
        return console.getComponentMotd();
    }
    // Paper end
    @Override
    public String getMotd() {
        return this.console.getMotd();
    }

    @Override
    public WarningState getWarningState() {
        return this.warningState;
    }

    public List<String> tabComplete(CommandSender sender, String message, ServerLevel world, Vec3 pos, boolean forceCommand) {
        if (!(sender instanceof Player)) {
            return ImmutableList.of();
        }

        List<String> offers;
        Player player = (Player) sender;
        if (message.startsWith("/") || forceCommand) {
            offers = this.tabCompleteCommand(player, message, world, pos);
        } else {
            offers = this.tabCompleteChat(player, message);
        }

        TabCompleteEvent tabEvent = new TabCompleteEvent(player, message, offers, message.startsWith("/") || forceCommand, pos != null ? io.papermc.paper.util.MCUtil.toLocation(((CraftWorld) player.getWorld()).getHandle(), new BlockPos(pos)) : null); // Paper
        this.getPluginManager().callEvent(tabEvent);

        return tabEvent.isCancelled() ? Collections.EMPTY_LIST : tabEvent.getCompletions();
    }

    public List<String> tabCompleteCommand(Player player, String message, ServerLevel world, Vec3 pos) {
        // Spigot Start
        if ( (org.spigotmc.SpigotConfig.tabComplete < 0 || message.length() <= org.spigotmc.SpigotConfig.tabComplete) && !message.contains( " " ) )
        {
            return ImmutableList.of();
        }
        // Spigot End

        List<String> completions = null;
        try {
            if (message.startsWith("/")) {
                // Trim leading '/' if present (won't always be present in command blocks)
                message = message.substring(1);
            }
            if (pos == null) {
                completions = this.getCommandMap().tabComplete(player, message);
            } else {
                completions = this.getCommandMap().tabComplete(player, message, new Location(world.getWorld(), pos.x, pos.y, pos.z));
            }
        } catch (CommandException ex) {
            player.sendMessage(ChatColor.RED + "An internal error occurred while attempting to tab-complete this command");
            this.getLogger().log(Level.SEVERE, "Exception when " + player.getName() + " attempted to tab complete " + message, ex);
        }

        return completions == null ? ImmutableList.<String>of() : completions;
    }

    public List<String> tabCompleteChat(Player player, String message) {
        List<String> completions = new ArrayList<String>();
        PlayerChatTabCompleteEvent event = new PlayerChatTabCompleteEvent(player, message, completions);
        String token = event.getLastToken();
        for (Player p : this.getOnlinePlayers()) {
            if (player.canSee(p) && StringUtil.startsWithIgnoreCase(p.getName(), token)) {
                completions.add(p.getName());
            }
        }
        this.pluginManager.callEvent(event);

        Iterator<?> it = completions.iterator();
        while (it.hasNext()) {
            Object current = it.next();
            if (!(current instanceof String)) {
                // Sanity
                it.remove();
            }
        }
        Collections.sort(completions, String.CASE_INSENSITIVE_ORDER);
        return completions;
    }

    @Override
    public CraftItemFactory getItemFactory() {
        return CraftItemFactory.instance();
    }

    @Override
    public CraftScoreboardManager getScoreboardManager() {
        return this.scoreboardManager;
    }

    @Override
    public Criteria getScoreboardCriteria(String name) {
        return CraftCriteria.getFromBukkit(name);
    }

    public void checkSaveState() {
        if (this.playerCommandState || this.printSaveWarning || this.console.autosavePeriod <= 0) {
            return;
        }
        this.printSaveWarning = true;
        this.getLogger().log(Level.WARNING, "A manual (plugin-induced) save has been detected while server is configured to auto-save. This may affect performance.", this.warningState == WarningState.ON ? new Throwable() : null);
    }

    @Override
    public CraftIconCache getServerIcon() {
        return this.icon;
    }

    @Override
    public CraftIconCache loadServerIcon(File file) throws Exception {
        Validate.notNull(file, "File cannot be null");
        if (!file.isFile()) {
            throw new IllegalArgumentException(file + " is not a file");
        }
        return CraftServer.loadServerIcon0(file);
    }

    static CraftIconCache loadServerIcon0(File file) throws Exception {
        return CraftServer.loadServerIcon0(ImageIO.read(file));
    }

    @Override
    public CraftIconCache loadServerIcon(BufferedImage image) throws Exception {
        Validate.notNull(image, "Image cannot be null");
        return CraftServer.loadServerIcon0(image);
    }

    static CraftIconCache loadServerIcon0(BufferedImage image) throws Exception {
        ByteBuf bytebuf = Unpooled.buffer();

        Validate.isTrue(image.getWidth() == 64, "Must be 64 pixels wide");
        Validate.isTrue(image.getHeight() == 64, "Must be 64 pixels high");
        ImageIO.write(image, "PNG", new ByteBufOutputStream(bytebuf));
        ByteBuffer bytebuffer = Base64.getEncoder().encode(bytebuf.nioBuffer());

        return new CraftIconCache("data:image/png;base64," + StandardCharsets.UTF_8.decode(bytebuffer));
    }

    @Override
    public void setIdleTimeout(int threshold) {
        this.console.setPlayerIdleTimeout(threshold);
    }

    @Override
    public int getIdleTimeout() {
        return this.console.getPlayerIdleTimeout();
    }

    @Override
    public ChunkGenerator.ChunkData createChunkData(World world) {
        Validate.notNull(world, "World cannot be null");
        ServerLevel handle = ((CraftWorld) world).getHandle();
        return new OldCraftChunkData(world.getMinHeight(), world.getMaxHeight(), handle.registryAccess().registryOrThrow(net.minecraft.core.Registry.BIOME_REGISTRY), world); // Paper - Anti-Xray - Add parameters
    }

    // Paper start
    private static final List<net.minecraft.world.level.chunk.ChunkStatus> VANILLA_GEN_STATUSES = List.of(
        net.minecraft.world.level.chunk.ChunkStatus.EMPTY,
        net.minecraft.world.level.chunk.ChunkStatus.STRUCTURE_STARTS,
        net.minecraft.world.level.chunk.ChunkStatus.STRUCTURE_REFERENCES,
        net.minecraft.world.level.chunk.ChunkStatus.BIOMES,
        net.minecraft.world.level.chunk.ChunkStatus.NOISE,
        net.minecraft.world.level.chunk.ChunkStatus.SURFACE,
        net.minecraft.world.level.chunk.ChunkStatus.CARVERS,
        net.minecraft.world.level.chunk.ChunkStatus.LIQUID_CARVERS,
        net.minecraft.world.level.chunk.ChunkStatus.FEATURES,
        net.minecraft.world.level.chunk.ChunkStatus.LIGHT
    );

    @Override
    @Deprecated(forRemoval = true)
    public ChunkGenerator.ChunkData createVanillaChunkData(World world, int x, int z) {
        // do bunch of vanilla shit
        final net.minecraft.server.level.ServerLevel serverLevel = ((CraftWorld) world).getHandle();
        final net.minecraft.core.Registry<net.minecraft.world.level.biome.Biome> biomeRegistry = serverLevel.getServer().registryAccess().registryOrThrow(net.minecraft.core.Registry.BIOME_REGISTRY);
        final net.minecraft.world.level.chunk.ProtoChunk protoChunk = new net.minecraft.world.level.chunk.ProtoChunk(
            new net.minecraft.world.level.ChunkPos(x, z),
            net.minecraft.world.level.chunk.UpgradeData.EMPTY,
            serverLevel,
            biomeRegistry,
            null
        );

        final net.minecraft.world.level.chunk.ChunkGenerator chunkGenerator;
        if (serverLevel.chunkSource.getGenerator() instanceof org.bukkit.craftbukkit.v1_19_R1.generator.CustomChunkGenerator bukkit) {
            chunkGenerator = bukkit.delegate;
        } else {
            chunkGenerator = serverLevel.chunkSource.getGenerator();
        }

        final net.minecraft.world.level.ChunkPos chunkPos = new net.minecraft.world.level.ChunkPos(x, z);
        final net.minecraft.util.thread.ProcessorMailbox<Runnable> mailbox = net.minecraft.util.thread.ProcessorMailbox.create(
            net.minecraft.Util.backgroundExecutor(),
            "CraftServer#createVanillaChunkData(worldName='" + world.getName() + "', x='" + x + "', z='" + z + "')"
        );
        for (final net.minecraft.world.level.chunk.ChunkStatus chunkStatus : VANILLA_GEN_STATUSES) {
            final List<net.minecraft.world.level.chunk.ChunkAccess> chunks = Lists.newArrayList();
            final int statusRange = Math.max(1, chunkStatus.getRange());

            for (int zz = chunkPos.z - statusRange; zz <= chunkPos.z + statusRange; ++zz) {
                for (int xx = chunkPos.x - statusRange; xx <= chunkPos.x + statusRange; ++xx) {
                    if (xx == chunkPos.x && zz == chunkPos.z) {
                        chunks.add(protoChunk);
                    } else {
                        final net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome> biomeHolder = serverLevel.registryAccess().registryOrThrow(net.minecraft.core.Registry.BIOME_REGISTRY).getHolderOrThrow(net.minecraft.world.level.biome.Biomes.PLAINS);
                        final net.minecraft.world.level.chunk.ChunkAccess chunk = new net.minecraft.world.level.chunk.EmptyLevelChunk(serverLevel, new net.minecraft.world.level.ChunkPos(xx, zz), biomeHolder);
                        chunks.add(chunk);
                    }
                }
            }

            chunkStatus.generate(
                mailbox::tell,
                serverLevel,
                chunkGenerator,
                serverLevel.getStructureManager(),
                serverLevel.chunkSource.getLightEngine(),
                chunk -> {
                    throw new UnsupportedOperationException("Not creating full chunks here");
                },
                chunks,
                true
            ).thenAccept(either -> {
                if (chunkStatus == net.minecraft.world.level.chunk.ChunkStatus.NOISE) {
                    either.left().ifPresent(chunk -> net.minecraft.world.level.levelgen.Heightmap.primeHeightmaps(chunk, net.minecraft.world.level.chunk.ChunkStatus.POST_FEATURES));
                }
            }).join();
        }

        // get empty object
        OldCraftChunkData data = (OldCraftChunkData) this.createChunkData(world);
        // copy over generated sections
        data.getLights().addAll(protoChunk.getLights().toList());
        data.setRawChunkData(protoChunk.getSections());
        // hooray!
        return data;
    }
    // Paper end

    @Override
    public BossBar createBossBar(String title, BarColor color, BarStyle style, BarFlag... flags) {
        return new CraftBossBar(title, color, style, flags);
    }

    @Override
    public KeyedBossBar createBossBar(NamespacedKey key, String title, BarColor barColor, BarStyle barStyle, BarFlag... barFlags) {
        Preconditions.checkArgument(key != null, "key");

        CustomBossEvent bossBattleCustom = this.getServer().getCustomBossEvents().create(CraftNamespacedKey.toMinecraft(key), CraftChatMessage.fromString(title, true)[0]);
        CraftKeyedBossbar craftKeyedBossbar = new CraftKeyedBossbar(bossBattleCustom);
        craftKeyedBossbar.setColor(barColor);
        craftKeyedBossbar.setStyle(barStyle);
        for (BarFlag flag : barFlags) {
            craftKeyedBossbar.addFlag(flag);
        }

        return craftKeyedBossbar;
    }

    @Override
    public Iterator<KeyedBossBar> getBossBars() {
        return Iterators.unmodifiableIterator(Iterators.transform(this.getServer().getCustomBossEvents().getEvents().iterator(), new Function<CustomBossEvent, org.bukkit.boss.KeyedBossBar>() {
            @Override
            public org.bukkit.boss.KeyedBossBar apply(CustomBossEvent bossBattleCustom) {
                return bossBattleCustom.getBukkitEntity();
            }
        }));
    }

    @Override
    public KeyedBossBar getBossBar(NamespacedKey key) {
        Preconditions.checkArgument(key != null, "key");
        net.minecraft.server.bossevents.CustomBossEvent bossBattleCustom = this.getServer().getCustomBossEvents().get(CraftNamespacedKey.toMinecraft(key));

        return (bossBattleCustom == null) ? null : bossBattleCustom.getBukkitEntity();
    }

    @Override
    public boolean removeBossBar(NamespacedKey key) {
        Preconditions.checkArgument(key != null, "key");
        net.minecraft.server.bossevents.CustomBossEvents bossBattleCustomData = this.getServer().getCustomBossEvents();
        net.minecraft.server.bossevents.CustomBossEvent bossBattleCustom = bossBattleCustomData.get(CraftNamespacedKey.toMinecraft(key));

        if (bossBattleCustom != null) {
            bossBattleCustomData.remove(bossBattleCustom);
            return true;
        }

        return false;
    }

    @Override
    public Entity getEntity(UUID uuid) {
        Validate.notNull(uuid, "UUID cannot be null");

        for (ServerLevel world : this.getServer().getAllLevels()) {
            net.minecraft.world.entity.Entity entity = world.getEntity(uuid);
            if (entity != null) {
                return entity.getBukkitEntity();
            }
        }

        return null;
    }

    @Override
    public org.bukkit.advancement.Advancement getAdvancement(NamespacedKey key) {
        Preconditions.checkArgument(key != null, "key");

        Advancement advancement = this.console.getAdvancements().getAdvancement(CraftNamespacedKey.toMinecraft(key));
        return (advancement == null) ? null : advancement.bukkit;
    }

    @Override
    public Iterator<org.bukkit.advancement.Advancement> advancementIterator() {
        return Iterators.unmodifiableIterator(Iterators.transform(this.console.getAdvancements().getAllAdvancements().iterator(), new Function<Advancement, org.bukkit.advancement.Advancement>() {
            @Override
            public org.bukkit.advancement.Advancement apply(Advancement advancement) {
                return advancement.bukkit;
            }
        }));
    }

    @Override
    public BlockData createBlockData(org.bukkit.Material material) {
        Validate.isTrue(material != null, "Must provide material");

        return this.createBlockData(material, (String) null);
    }

    @Override
    public BlockData createBlockData(org.bukkit.Material material, Consumer<BlockData> consumer) {
        BlockData data = this.createBlockData(material);

        if (consumer != null) {
            consumer.accept(data);
        }

        return data;
    }

    @Override
    public BlockData createBlockData(String data) throws IllegalArgumentException {
        Validate.isTrue(data != null, "Must provide data");

        return this.createBlockData(null, data);
    }

    @Override
    public BlockData createBlockData(org.bukkit.Material material, String data) {
        Validate.isTrue(material != null || data != null, "Must provide one of material or data");

        return CraftBlockData.newData(material, data);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Keyed> org.bukkit.Tag<T> getTag(String registry, NamespacedKey tag, Class<T> clazz) {
        Validate.notNull(registry, "registry cannot be null");
        Validate.notNull(tag, "NamespacedKey cannot be null");
        Validate.notNull(clazz, "Class cannot be null");
        ResourceLocation key = CraftNamespacedKey.toMinecraft(tag);

        switch (registry) {
            case org.bukkit.Tag.REGISTRY_BLOCKS -> {
                Preconditions.checkArgument(clazz == org.bukkit.Material.class, "Block namespace must have material type");
                TagKey<Block> blockTagKey = TagKey.create(net.minecraft.core.Registry.BLOCK_REGISTRY, key);
                if (net.minecraft.core.Registry.BLOCK.isKnownTagName(blockTagKey)) {
                    return (org.bukkit.Tag<T>) new CraftBlockTag(net.minecraft.core.Registry.BLOCK, blockTagKey);
                }
            }
            case org.bukkit.Tag.REGISTRY_ITEMS -> {
                Preconditions.checkArgument(clazz == org.bukkit.Material.class, "Item namespace must have material type");
                TagKey<Item> itemTagKey = TagKey.create(net.minecraft.core.Registry.ITEM_REGISTRY, key);
                if (net.minecraft.core.Registry.ITEM.isKnownTagName(itemTagKey)) {
                    return (org.bukkit.Tag<T>) new CraftItemTag(net.minecraft.core.Registry.ITEM, itemTagKey);
                }
            }
            case org.bukkit.Tag.REGISTRY_FLUIDS -> {
                Preconditions.checkArgument(clazz == org.bukkit.Fluid.class, "Fluid namespace must have fluid type");
                TagKey<Fluid> fluidTagKey = TagKey.create(net.minecraft.core.Registry.FLUID_REGISTRY, key);
                if (net.minecraft.core.Registry.FLUID.isKnownTagName(fluidTagKey)) {
                    return (org.bukkit.Tag<T>) new CraftFluidTag(net.minecraft.core.Registry.FLUID, fluidTagKey);
                }
            }
            case org.bukkit.Tag.REGISTRY_ENTITY_TYPES -> {
                Preconditions.checkArgument(clazz == org.bukkit.entity.EntityType.class, "Entity type namespace must have entity type");
                TagKey<EntityType<?>> entityTagKey = TagKey.create(net.minecraft.core.Registry.ENTITY_TYPE_REGISTRY, key);
                if (net.minecraft.core.Registry.ENTITY_TYPE.isKnownTagName(entityTagKey)) {
                    return (org.bukkit.Tag<T>) new CraftEntityTag(net.minecraft.core.Registry.ENTITY_TYPE, entityTagKey);
                }
            }
            // Paper start
            case org.bukkit.Tag.REGISTRY_GAME_EVENTS -> {
                Preconditions.checkArgument(clazz == org.bukkit.GameEvent.class, "Game Event namespace must have GameEvent type");
                TagKey<net.minecraft.world.level.gameevent.GameEvent> gameEventTagKey = TagKey.create(net.minecraft.core.Registry.GAME_EVENT_REGISTRY, key);
                if (net.minecraft.core.Registry.GAME_EVENT.isKnownTagName(gameEventTagKey)) {
                    return (org.bukkit.Tag<T>) new io.papermc.paper.CraftGameEventTag(net.minecraft.core.Registry.GAME_EVENT, gameEventTagKey);
                }
            }
            // Paper end
            default -> throw new IllegalArgumentException();
        }

        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Keyed> Iterable<org.bukkit.Tag<T>> getTags(String registry, Class<T> clazz) {
        Validate.notNull(registry, "registry cannot be null");
        Validate.notNull(clazz, "Class cannot be null");
        switch (registry) {
            case org.bukkit.Tag.REGISTRY_BLOCKS -> {
                Preconditions.checkArgument(clazz == org.bukkit.Material.class, "Block namespace must have material type");
                net.minecraft.core.Registry<Block> blockTags = net.minecraft.core.Registry.BLOCK;
                return blockTags.getTags().map(pair -> (org.bukkit.Tag<T>) new CraftBlockTag(blockTags, pair.getFirst())).collect(ImmutableList.toImmutableList());
            }
            case org.bukkit.Tag.REGISTRY_ITEMS -> {
                Preconditions.checkArgument(clazz == org.bukkit.Material.class, "Item namespace must have material type");
                net.minecraft.core.Registry<Item> itemTags = net.minecraft.core.Registry.ITEM;
                return itemTags.getTags().map(pair -> (org.bukkit.Tag<T>) new CraftItemTag(itemTags, pair.getFirst())).collect(ImmutableList.toImmutableList());
            }
            case org.bukkit.Tag.REGISTRY_FLUIDS -> {
                Preconditions.checkArgument(clazz == org.bukkit.Material.class, "Fluid namespace must have fluid type");
                net.minecraft.core.Registry<Fluid> fluidTags = net.minecraft.core.Registry.FLUID;
                return fluidTags.getTags().map(pair -> (org.bukkit.Tag<T>) new CraftFluidTag(fluidTags, pair.getFirst())).collect(ImmutableList.toImmutableList());
            }
            case org.bukkit.Tag.REGISTRY_ENTITY_TYPES -> {
                Preconditions.checkArgument(clazz == org.bukkit.entity.EntityType.class, "Entity type namespace must have entity type");
                net.minecraft.core.Registry<EntityType<?>> entityTags = net.minecraft.core.Registry.ENTITY_TYPE;
                return entityTags.getTags().map(pair -> (org.bukkit.Tag<T>) new CraftEntityTag(entityTags, pair.getFirst())).collect(ImmutableList.toImmutableList());
            }
            // Paper start
            case org.bukkit.Tag.REGISTRY_GAME_EVENTS -> {
                Preconditions.checkArgument(clazz == org.bukkit.GameEvent.class);
                net.minecraft.core.Registry<net.minecraft.world.level.gameevent.GameEvent> gameEvents = net.minecraft.core.Registry.GAME_EVENT;
                return gameEvents.getTags().map(pair -> (org.bukkit.Tag<T>) new io.papermc.paper.CraftGameEventTag(gameEvents, pair.getFirst())).collect(ImmutableList.toImmutableList());
            // Paper end
            }
            default -> throw new IllegalArgumentException();
        }
    }

    @Override
    public LootTable getLootTable(NamespacedKey key) {
        Validate.notNull(key, "NamespacedKey cannot be null");

        LootTables registry = this.getServer().getLootTables();
        // Paper start - honor method contract
        final ResourceLocation lootTableKey = CraftNamespacedKey.toMinecraft(key);
        if (!registry.getIds().contains(lootTableKey)) {
            return null;
        }
        return new CraftLootTable(key, registry.get(lootTableKey));
        // Paper end
    }

    @Override
    public List<Entity> selectEntities(CommandSender sender, String selector) {
        Preconditions.checkArgument(selector != null, "Selector cannot be null");
        Preconditions.checkArgument(sender != null, "Sender cannot be null");

        EntityArgument arg = EntityArgument.entities();
        List<? extends net.minecraft.world.entity.Entity> nms;

        try {
            StringReader reader = new StringReader(selector);
            nms = arg.parse(reader, true).findEntities(VanillaCommandWrapper.getListener(sender));
            Preconditions.checkArgument(!reader.canRead(), "Spurious trailing data in selector: " + selector);
        } catch (CommandSyntaxException ex) {
            throw new IllegalArgumentException("Could not parse selector: " + selector, ex);
        }

        return new ArrayList<>(Lists.transform(nms, (entity) -> entity.getBukkitEntity()));
    }

    @Override
    public StructureManager getStructureManager() {
        return this.structureManager;
    }

    @Override
    public <T extends Keyed> Registry<T> getRegistry(Class<T> aClass) {
        return (Registry<T>) this.registries.computeIfAbsent(aClass, key -> CraftRegistry.createRegistry(aClass, console.registryHolder));
    }

    @Deprecated
    @Override
    public UnsafeValues getUnsafe() {
        return CraftMagicNumbers.INSTANCE;
    }

    // Paper - Add getTPS API - Further improve tick loop
    @Override
    public double[] getTPS() {
        return new double[] {
                net.minecraft.server.MinecraftServer.getServer().tps1.getAverage(),
                net.minecraft.server.MinecraftServer.getServer().tps5.getAverage(),
                net.minecraft.server.MinecraftServer.getServer().tps15.getAverage()
        };
    }

    @Override
    public long[] getTickTimes() {
        return getServer().tickTimes5s.getTimes();
    }

    @Override
    public double getAverageTickTime() {
        return getServer().tickTimes5s.getAverage();
    }
    // Paper end

    // Spigot start
    private final org.bukkit.Server.Spigot spigot = new org.bukkit.Server.Spigot()
    {

        @Deprecated
        @Override
        public YamlConfiguration getConfig()
        {
            return org.spigotmc.SpigotConfig.config;
        }

        @Override
        public YamlConfiguration getBukkitConfig()
        {
            return configuration;
        }

        @Override
        public YamlConfiguration getSpigotConfig()
        {
            return org.spigotmc.SpigotConfig.config;
        }

        @Override
        public YamlConfiguration getPaperConfig()
        {
            return CraftServer.this.console.paperConfigurations.createLegacyObject(CraftServer.this.console);
        }

        @Override
        public void restart() {
            org.spigotmc.RestartCommand.restart();
        }

        @Override
        public void broadcast(BaseComponent component) {
            for (Player player : CraftServer.this.getOnlinePlayers()) {
                player.spigot().sendMessage(component);
            }
        }

        @Override
        public void broadcast(BaseComponent... components) {
            for (Player player : CraftServer.this.getOnlinePlayers()) {
                player.spigot().sendMessage(components);
            }
        }
    };

    public org.bukkit.Server.Spigot spigot()
    {
        return this.spigot;
    }
    // Spigot end

    // Paper start
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static java.nio.file.Path dumpHeap(java.nio.file.Path dir, String name) {
        try {
            java.nio.file.Files.createDirectories(dir);

            javax.management.MBeanServer server = java.lang.management.ManagementFactory.getPlatformMBeanServer();
            java.nio.file.Path file;

            try {
                Class clazz = Class.forName("openj9.lang.management.OpenJ9DiagnosticsMXBean");
                Object openj9Mbean = java.lang.management.ManagementFactory.newPlatformMXBeanProxy(server, "openj9.lang.management:type=OpenJ9Diagnostics", clazz);
                java.lang.reflect.Method m = clazz.getMethod("triggerDumpToFile", String.class, String.class);
                file = dir.resolve(name + ".phd");
                m.invoke(openj9Mbean, "heap", file.toString());
            } catch (ClassNotFoundException e) {
                Class clazz = Class.forName("com.sun.management.HotSpotDiagnosticMXBean");
                Object hotspotMBean = java.lang.management.ManagementFactory.newPlatformMXBeanProxy(server, "com.sun.management:type=HotSpotDiagnostic", clazz);
                java.lang.reflect.Method m = clazz.getMethod("dumpHeap", String.class, boolean.class);
                file = dir.resolve(name + ".hprof");
                m.invoke(hotspotMBean, file.toString(), true);
            }

            return file;
        } catch (Throwable t) {
            Bukkit.getLogger().log(Level.SEVERE, "Could not write heap", t);
            return null;
        }
    }
    private Iterable<? extends net.kyori.adventure.audience.Audience> adventure$audiences;
    @Override
    public Iterable<? extends net.kyori.adventure.audience.Audience> audiences() {
        if (this.adventure$audiences == null) {
            this.adventure$audiences = com.google.common.collect.Iterables.concat(java.util.Collections.singleton(this.getConsoleSender()), this.getOnlinePlayers());
        }
        return this.adventure$audiences;
    }

    @Override
    public void reloadPermissions() {
        pluginManager.clearPermissions();
        if (io.papermc.paper.configuration.GlobalConfiguration.get().misc.loadPermissionsYmlBeforePlugins) loadCustomPermissions();
        for (Plugin plugin : pluginManager.getPlugins()) {
            for (Permission perm : plugin.getDescription().getPermissions()) {
                try {
                    pluginManager.addPermission(perm);
                } catch (IllegalArgumentException ex) {
                    getLogger().log(Level.WARNING, "Plugin " + plugin.getDescription().getFullName() + " tried to register permission '" + perm.getName() + "' but it's already registered", ex);
                }
            }
        }
        if (!io.papermc.paper.configuration.GlobalConfiguration.get().misc.loadPermissionsYmlBeforePlugins) loadCustomPermissions();
        DefaultPermissions.registerCorePermissions();
        CraftDefaultPermissions.registerCorePermissions();
    }

    @Override
    public boolean reloadCommandAliases() {
        Set<String> removals = getCommandAliases().keySet().stream()
                .map(key -> key.toLowerCase(java.util.Locale.ENGLISH))
                .collect(java.util.stream.Collectors.toSet());
        getCommandMap().getKnownCommands().keySet().removeIf(removals::contains);
        File file = getCommandsConfigFile();
        try {
            commandsConfiguration.load(file);
        } catch (FileNotFoundException ex) {
            return false;
        } catch (IOException | org.bukkit.configuration.InvalidConfigurationException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Cannot load " + file, ex);
            return false;
        }
        commandMap.registerServerAliases();
        return true;
    }

    @Override
    public boolean suggestPlayerNamesWhenNullTabCompletions() {
        return io.papermc.paper.configuration.GlobalConfiguration.get().commands.suggestPlayerNamesWhenNullTabCompletions;
    }

    @Override
    public String getPermissionMessage() {
        return net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand().serialize(io.papermc.paper.configuration.GlobalConfiguration.get().messages.noPermission);
    }

    @Override
    public net.kyori.adventure.text.Component permissionMessage() {
        return io.papermc.paper.configuration.GlobalConfiguration.get().messages.noPermission;
    }

    @Override
    public com.destroystokyo.paper.profile.PlayerProfile createProfile(@Nonnull UUID uuid) {
        return createProfile(uuid, null);
    }

    @Override
    public com.destroystokyo.paper.profile.PlayerProfile createProfile(@Nonnull String name) {
        return createProfile(null, name);
    }

    @Override
    public com.destroystokyo.paper.profile.PlayerProfile createProfile(@Nullable UUID uuid, @Nullable String name) {
        Player player = uuid != null ? Bukkit.getPlayer(uuid) : (name != null ? Bukkit.getPlayerExact(name) : null);
        if (player != null) return new com.destroystokyo.paper.profile.CraftPlayerProfile((CraftPlayer) player);

        return new com.destroystokyo.paper.profile.CraftPlayerProfile(uuid, name);
    }

    @Override
    public com.destroystokyo.paper.profile.PlayerProfile createProfileExact(@Nullable UUID uuid, @Nullable String name) {
        Player player = uuid != null ? Bukkit.getPlayer(uuid) : (name != null ? Bukkit.getPlayerExact(name) : null);
        if (player == null) return new com.destroystokyo.paper.profile.CraftPlayerProfile(uuid, name);

        if (Objects.equals(uuid, player.getUniqueId()) && Objects.equals(name, player.getName())) {
            return new com.destroystokyo.paper.profile.CraftPlayerProfile((CraftPlayer) player);
        }

        final com.mojang.authlib.GameProfile profile = new com.mojang.authlib.GameProfile(uuid, name);
        profile.getProperties().putAll(((CraftPlayer)player).getHandle().getGameProfile().getProperties());
        return new com.destroystokyo.paper.profile.CraftPlayerProfile(profile);
    }

    @Override
    public int getCurrentTick() {
        return net.minecraft.server.MinecraftServer.currentTick;
    }

    @Override
    public boolean isStopping() {
        return net.minecraft.server.MinecraftServer.getServer().hasStopped();
    }

    private com.destroystokyo.paper.entity.ai.MobGoals mobGoals = new com.destroystokyo.paper.entity.ai.PaperMobGoals();
    @Override
    public com.destroystokyo.paper.entity.ai.MobGoals getMobGoals() {
        return mobGoals;
    }

    @Override
    public io.papermc.paper.datapack.PaperDatapackManager getDatapackManager() {
        return datapackManager;
    }

    @Override
    public CraftPotionBrewer getPotionBrewer() {
        return this.potionBrewer;
    }

    // Paper end
}
