package io.papermc.paper.world.structure;

import io.papermc.paper.registry.PaperRegistry;
import io.papermc.paper.registry.RegistryKey;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.bukkit.NamespacedKey;
import org.bukkit.StructureType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.Objects;
import java.util.function.Supplier;

@DefaultQualifier(NonNull.class)
public final class PaperConfiguredStructure {

    private PaperConfiguredStructure() {
    }

    public static void init() {
        new ConfiguredStructureRegistry().register();
    }

    static final class ConfiguredStructureRegistry extends PaperRegistry<ConfiguredStructure, Structure> {

        private static final Supplier<Registry<Structure>> STRUCTURE_FEATURE_REGISTRY = registryFor(Registry.STRUCTURE_REGISTRY);

        public ConfiguredStructureRegistry() {
            super(RegistryKey.CONFIGURED_STRUCTURE_REGISTRY);
        }

        @Override
        public @Nullable ConfiguredStructure convertToApi(NamespacedKey key, Structure nms) {
            final ResourceLocation structureTypeLoc = Objects.requireNonNull(Registry.STRUCTURE_TYPES.getKey(nms.type()), "unexpected structure type " + nms.type());
            final @Nullable StructureType structureType = StructureType.getStructureTypes().get(structureTypeLoc.getPath());
            return structureType == null ? null : new ConfiguredStructure(key, structureType);
        }
    }
}
