package io.papermc.paper.configuration;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.mojang.logging.LogUtils;
import io.papermc.paper.configuration.constraint.Constraint;
import io.papermc.paper.configuration.constraint.Constraints;
import io.papermc.paper.configuration.legacy.MaxEntityCollisionsInitializer;
import io.papermc.paper.configuration.legacy.RequiresSpigotInitialization;
import io.papermc.paper.configuration.legacy.SpawnLoadedRangeInitializer;
import io.papermc.paper.configuration.transformation.world.FeatureSeedsGeneration;
import io.papermc.paper.configuration.type.BooleanOrDefault;
import io.papermc.paper.configuration.type.DoubleOrDefault;
import io.papermc.paper.configuration.type.Duration;
import io.papermc.paper.configuration.type.EngineMode;
import io.papermc.paper.configuration.type.IntOr;
import io.papermc.paper.configuration.type.fallback.ArrowDespawnRate;
import io.papermc.paper.configuration.type.fallback.AutosavePeriod;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2LongMap;
import it.unimi.dsi.fastutil.objects.Reference2LongOpenHashMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.slf4j.Logger;
import org.spigotmc.SpigotWorldConfig;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Required;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal", "NotNullFieldNotInitialized", "InnerClassMayBeStatic"})
public class WorldConfiguration extends ConfigurationPart {
    private static final Logger LOGGER = LogUtils.getLogger();
    static final int CURRENT_VERSION = 29; // (when you change the version, change the comment, so it conflicts on rebases): zero height fixes

    private transient final SpigotWorldConfig spigotConfig;
    private transient final ResourceLocation worldKey;
    WorldConfiguration(SpigotWorldConfig spigotConfig, ResourceLocation worldKey) {
        this.spigotConfig = spigotConfig;
        this.worldKey = worldKey;
    }

    public boolean isDefault() {
        return this.worldKey.equals(PaperConfigurations.WORLD_DEFAULTS_KEY);
    }

    @Setting(Configuration.VERSION_FIELD)
    public int version = CURRENT_VERSION;

    public Anticheat anticheat;

    public class Anticheat extends ConfigurationPart {

        public Obfuscation obfuscation;

        public class Obfuscation extends ConfigurationPart {
            public Items items = new Items();
            public class Items extends ConfigurationPart {
                public boolean hideItemmeta = false;
                public boolean hideDurability = false;
            }
        }

        public AntiXray antiXray;

        public class AntiXray extends ConfigurationPart {
            public boolean enabled = false;
            public EngineMode engineMode = EngineMode.HIDE;
            public int maxBlockHeight = 64;
            public int updateRadius = 2;
            public boolean lavaObscures = false;
            public boolean usePermission = false;
            public List<String> hiddenBlocks = List.of("copper_ore", "deepslate_copper_ore", "gold_ore", "deepslate_gold_ore", "iron_ore", "deepslate_iron_ore",
                "coal_ore", "deepslate_coal_ore", "lapis_ore", "deepslate_lapis_ore", "mossy_cobblestone", "obsidian", "chest", "diamond_ore", "deepslate_diamond_ore",
                "redstone_ore", "deepslate_redstone_ore", "clay", "emerald_ore", "deepslate_emerald_ore", "ender_chest"); // TODO update type to List<Block>
            public List<String> replacementBlocks = List.of("stone", "oak_planks", "deepslate"); // TODO update type to List<Block>
        }
    }

    public Entities entities;

    public class Entities extends ConfigurationPart {
        public boolean entitiesTargetWithFollowRange = false;
        public MobEffects mobEffects;

        public class MobEffects extends ConfigurationPart {
            public boolean undeadImmuneToCertainEffects = true;
            public boolean spidersImmuneToPoisonEffect = true;
            public ImmuneToWitherEffect immuneToWitherEffect;

            public class ImmuneToWitherEffect extends ConfigurationPart {
                public boolean wither = true;
                public boolean witherSkeleton = true;
            }
        }

        public ArmorStands armorStands;

        public class ArmorStands extends ConfigurationPart {
            public boolean doCollisionEntityLookups = true;
            public boolean tick = true;
        }

        public Spawning spawning;

        public class Spawning extends ConfigurationPart {
            public ArrowDespawnRate nonPlayerArrowDespawnRate = ArrowDespawnRate.def(WorldConfiguration.this.spigotConfig);
            public ArrowDespawnRate creativeArrowDespawnRate = ArrowDespawnRate.def(WorldConfiguration.this.spigotConfig);
            public boolean filterNbtDataFromSpawnEggsAndRelated = true;
            public boolean disableMobSpawnerSpawnEggTransformation = false;
            public boolean perPlayerMobSpawns = true;
            public boolean scanForLegacyEnderDragon = true;
            @MergeMap
            public Reference2IntMap<MobCategory> spawnLimits = Util.make(new Reference2IntOpenHashMap<>(NaturalSpawner.SPAWNING_CATEGORIES.length), map -> Arrays.stream(NaturalSpawner.SPAWNING_CATEGORIES).forEach(mobCategory -> map.put(mobCategory, -1)));
            @MergeMap
            public Map<MobCategory, DespawnRange> despawnRanges = Arrays.stream(MobCategory.values()).collect(Collectors.toMap(Function.identity(), category -> new DespawnRange(category.getNoDespawnDistance(), category.getDespawnDistance())));

            @ConfigSerializable
            public record DespawnRange(@Required int soft, @Required int hard) {
            }

            public WaterAnimalSpawnHeight wateranimalSpawnHeight;

            public class WaterAnimalSpawnHeight extends ConfigurationPart {
                public IntOr.Default maximum = IntOr.Default.USE_DEFAULT;
                public IntOr.Default minimum = IntOr.Default.USE_DEFAULT;
            }

            public SlimeSpawnHeight slimeSpawnHeight;

            public class SlimeSpawnHeight extends ConfigurationPart {

                public SurfaceSpawnableSlimeBiome surfaceBiome;

                public class SurfaceSpawnableSlimeBiome extends ConfigurationPart {
                    public double maximum = 70;
                    public double minimum = 50;
                }

                public SlimeChunk slimeChunk;

                public class SlimeChunk extends ConfigurationPart {
                    public double maximum = 40;
                }
            }

            public WanderingTrader wanderingTrader;

            public class WanderingTrader extends ConfigurationPart {
                public int spawnMinuteLength = 1200;
                public int spawnDayLength = 24000;
                public int spawnChanceFailureIncrement = 25;
                public int spawnChanceMin = 25;
                public int spawnChanceMax = 75;
            }

            public boolean allChunksAreSlimeChunks = false;
            @Constraint(Constraints.BelowZeroDoubleToDefault.class)
            public DoubleOrDefault skeletonHorseThunderSpawnChance = DoubleOrDefault.USE_DEFAULT;
            public boolean ironGolemsCanSpawnInAir = false;
            public boolean countAllMobsForSpawning = false;
            public int monsterSpawnMaxLightLevel = -1;
            public DuplicateUUID duplicateUuid;

            public class DuplicateUUID extends ConfigurationPart {
                public DuplicateUUIDMode mode = DuplicateUUIDMode.SAFE_REGEN;
                public int safeRegenDeleteRange = 32;

                public enum DuplicateUUIDMode {
                    SAFE_REGEN, DELETE, NOTHING, WARN;
                }
            }
            public AltItemDespawnRate altItemDespawnRate;

            public class AltItemDespawnRate extends ConfigurationPart {
                public boolean enabled = false;
                public Reference2IntMap<Item> items = new Reference2IntOpenHashMap<>(Map.of(Items.COBBLESTONE, 300));
            }
        }

        public Behavior behavior;

        public class Behavior extends ConfigurationPart {
            public boolean disableChestCatDetection = false;
            public boolean spawnerNerfedMobsShouldJump = false;
            public int experienceMergeMaxValue = -1;
            public boolean shouldRemoveDragon = false;
            public boolean zombiesTargetTurtleEggs = true;
            public boolean piglinsGuardChests = true;
            public double babyZombieMovementModifier = 0.5;
            public DoorBreakingDifficulty doorBreakingDifficulty;

            public class DoorBreakingDifficulty extends ConfigurationPart { // TODO convert to map at some point
                public List<Difficulty> zombie = Arrays.stream(Difficulty.values()).filter(Zombie.DOOR_BREAKING_PREDICATE).toList();
                public List<Difficulty> husk = Arrays.stream(Difficulty.values()).filter(Zombie.DOOR_BREAKING_PREDICATE).toList();
                @Setting("zombie_villager")
                public List<Difficulty> zombieVillager = Arrays.stream(Difficulty.values()).filter(Zombie.DOOR_BREAKING_PREDICATE).toList();
                @Setting("zombified_piglin")
                public List<Difficulty> zombified_piglin = Arrays.stream(Difficulty.values()).filter(Zombie.DOOR_BREAKING_PREDICATE).toList();
                public List<Difficulty> vindicator = Arrays.stream(Difficulty.values()).filter(Vindicator.DOOR_BREAKING_PREDICATE).toList();

                // TODO remove when this becomes a proper map
                public List<Difficulty> get(EntityType<?> type) {
                    return this.getOrDefault(type, null);
                }

                public List<Difficulty> getOrDefault(EntityType<?> type, List<Difficulty> fallback) {
                    if (type == EntityType.ZOMBIE) {
                        return this.zombie;
                    } else if (type == EntityType.HUSK) {
                        return this.husk;
                    } else if (type == EntityType.ZOMBIE_VILLAGER) {
                        return this.zombieVillager;
                    } else if (type == EntityType.ZOMBIFIED_PIGLIN) {
                        return this.zombified_piglin;
                    } else if (type == EntityType.VINDICATOR) {
                        return this.vindicator;
                    } else {
                        return fallback;
                    }
                }
            }

            public boolean disableCreeperLingeringEffect = false;
            public boolean enderDragonsDeathAlwaysPlacesDragonEgg = false;
            public boolean phantomsDoNotSpawnOnCreativePlayers = true;
            public boolean phantomsOnlyAttackInsomniacs = true;
            public boolean parrotsAreUnaffectedByPlayerMovement = false;
            public double zombieVillagerInfectionChance = -1.0;
            public MobsCanAlwaysPickUpLoot mobsCanAlwaysPickUpLoot;

            public class MobsCanAlwaysPickUpLoot extends ConfigurationPart {
                public boolean zombies = false;
                public boolean skeletons = false;
            }

            public boolean disablePlayerCrits = false;
            public boolean nerfPigmenFromNetherPortals = false;
            public PillagerPatrols pillagerPatrols;

            public class PillagerPatrols extends ConfigurationPart {
                public boolean disable = false;
                public double spawnChance = 0.2;
                public SpawnDelay spawnDelay;
                public Start start;

                public class SpawnDelay extends ConfigurationPart {
                    public boolean perPlayer = false;
                    public int ticks = 12000;
                }

                public class Start extends ConfigurationPart {
                    public boolean perPlayer = false;
                    public int day = 5;
                }
            }
        }
    }

    public Lootables lootables;

    public class Lootables extends ConfigurationPart {
        public boolean autoReplenish = false;
        public boolean restrictPlayerReloot = true;
        public boolean resetSeedOnFill = true;
        public int maxRefills = -1;
        public Duration refreshMin = Duration.of("12h");
        public Duration refreshMax = Duration.of("2d");
    }

    public MaxGrowthHeight maxGrowthHeight;

    public class MaxGrowthHeight extends ConfigurationPart {
        public int cactus = 3;
        public int reeds = 3;
        public Bamboo bamboo;

        public class Bamboo extends ConfigurationPart {
            public int max = 16;
            public int min = 11;
        }
    }

    public Scoreboards scoreboards;

    public class Scoreboards extends ConfigurationPart {
        public boolean allowNonPlayerEntitiesOnScoreboards = false;
        public boolean useVanillaWorldScoreboardNameColoring = false;
    }

    public Environment environment;

    public class Environment extends ConfigurationPart {
        public boolean disableThunder = false;
        public boolean disableIceAndSnow = false;
        public boolean optimizeExplosions = false;
        public boolean disableExplosionKnockback = false;
        public boolean generateFlatBedrock = false;
        public FrostedIce frostedIce;

        public class FrostedIce extends ConfigurationPart {
            public boolean enabled = true;
            public Delay delay;

            public class Delay extends ConfigurationPart {
                public int min = 20;
                public int max = 40;
            }
        }

        public TreasureMaps treasureMaps;
        public class TreasureMaps extends ConfigurationPart {
            public boolean enabled = true;
            @NestedSetting({"find-already-discovered", "villager-trade"})
            public boolean findAlreadyDiscoveredVillager = false;
            @NestedSetting({"find-already-discovered", "loot-tables"})
            public BooleanOrDefault findAlreadyDiscoveredLootTable = BooleanOrDefault.USE_DEFAULT;
        }

        public int fireTickDelay = 30;
        public int waterOverLavaFlowSpeed = 5;
        public int portalSearchRadius = 128;
        public int portalCreateRadius = 16;
        public boolean portalSearchVanillaDimensionScaling = true;
        public boolean disableTeleportationSuffocationCheck = false;
        public IntOr.Disabled netherCeilingVoidDamageHeight = IntOr.Disabled.DISABLED;
    }

    public Spawn spawn;

    public class Spawn extends ConfigurationPart {
        @RequiresSpigotInitialization(SpawnLoadedRangeInitializer.class)
        public short keepSpawnLoadedRange = 10;
        public boolean keepSpawnLoaded = true;
        public boolean allowUsingSignsInsideSpawnProtection = false;
    }

    public Maps maps;

    public class Maps extends ConfigurationPart {
        public int itemFrameCursorLimit = 128;
        public int itemFrameCursorUpdateInterval = 10;
    }

    public Fixes fixes;

    public class Fixes extends ConfigurationPart {
        public boolean fixItemsMergingThroughWalls = false;
        public boolean disableUnloadedChunkEnderpearlExploit = true;
        public boolean preventTntFromMovingInWater = false;
        public boolean splitOverstackedLoot = true;
        public boolean fixCuringZombieVillagerDiscountExploit = true;
        public IntOr.Disabled fallingBlockHeightNerf = IntOr.Disabled.DISABLED;
        public IntOr.Disabled tntEntityHeightNerf = IntOr.Disabled.DISABLED;
    }

    public UnsupportedSettings unsupportedSettings;

    public class UnsupportedSettings extends ConfigurationPart {
        public boolean fixInvulnerableEndCrystalExploit = true;
    }

    public Hopper hopper;

    public class Hopper extends ConfigurationPart {
        public boolean cooldownWhenFull = true;
        public boolean disableMoveEvent = false;
        public boolean ignoreOccludingBlocks = false;
    }

    public Collisions collisions;

    public class Collisions extends ConfigurationPart {
        public boolean onlyPlayersCollide = false;
        public boolean allowVehicleCollisions = true;
        public boolean fixClimbingBypassingCrammingRule = false;
        @RequiresSpigotInitialization(MaxEntityCollisionsInitializer.class)
        public int maxEntityCollisions = 8;
        public boolean allowPlayerCrammingDamage = false;
    }

    public Chunks chunks;

    public class Chunks extends ConfigurationPart {
        public AutosavePeriod autoSaveInterval = AutosavePeriod.def();
        public int maxAutoSaveChunksPerTick = 24;
        public int fixedChunkInhabitedTime = -1;
        public boolean preventMovingIntoUnloadedChunks = false;
        public Duration delayChunkUnloadsBy = Duration.of("10s");
        public Reference2IntMap<EntityType<?>> entityPerChunkSaveLimit = Util.make(new Reference2IntOpenHashMap<>(Registry.ENTITY_TYPE.size()), map -> {
            map.defaultReturnValue(-1);
            map.put(EntityType.EXPERIENCE_ORB, -1);
            map.put(EntityType.SNOWBALL, -1);
            map.put(EntityType.ENDER_PEARL, -1);
            map.put(EntityType.ARROW, -1);
            map.put(EntityType.FIREBALL, -1);
            map.put(EntityType.SMALL_FIREBALL, -1);
        });
    }

    public FishingTimeRange fishingTimeRange;

    public class FishingTimeRange extends ConfigurationPart {
        public int minimum = 100;
        public int maximum = 600;
    }

    public TickRates tickRates;

    public class TickRates extends ConfigurationPart {
        public int grassSpread = 1;
        public int containerUpdate = 1;
        public int mobSpawner = 1;
        public Table<EntityType<?>, String, Integer> sensor = Util.make(HashBasedTable.create(), table -> table.put(EntityType.VILLAGER, "secondarypoisensor", 40));
        public Table<EntityType<?>, String, Integer> behavior = Util.make(HashBasedTable.create(), table -> table.put(EntityType.VILLAGER, "validatenearbypoi", -1));
    }

    @Setting(FeatureSeedsGeneration.FEATURE_SEEDS_KEY)
    public FeatureSeeds featureSeeds;

    public class FeatureSeeds extends ConfigurationPart.Post {
        @Setting(FeatureSeedsGeneration.GENERATE_KEY)
        public boolean generateRandomSeedsForAll = false;
        @Setting(FeatureSeedsGeneration.FEATURES_KEY)
        public Reference2LongMap<Holder<ConfiguredFeature<?, ?>>> features = new Reference2LongOpenHashMap<>();

        @Override
        public void postProcess() {
            this.features.defaultReturnValue(-1);
        }
    }

    public Misc misc;

    public class Misc extends ConfigurationPart {
        public int lightQueueSize = 20;
        public boolean updatePathfindingOnBlockUpdate = true;
        public boolean showSignClickCommandFailureMsgsToPlayer = false;
        public RedstoneImplementation redstoneImplementation = RedstoneImplementation.VANILLA;
        public boolean disableEndCredits = false;
        public float maxLeashDistance = 10f;
        public boolean disableSprintInterruptionOnAttack = false;
        public int shieldBlockingDelay = 5;
        public boolean disableRelativeProjectileVelocity = false;

        public enum RedstoneImplementation {
            VANILLA, EIGENCRAFT, ALTERNATE_CURRENT
        }
    }

}
