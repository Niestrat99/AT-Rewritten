package io.papermc.paper.registry;

import io.papermc.paper.world.structure.ConfiguredStructure;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.bukkit.Keyed;

public record RegistryKey<API extends Keyed, MINECRAFT>(Class<API> apiClass, ResourceKey<? extends Registry<MINECRAFT>> resourceKey) {

    public static final RegistryKey<ConfiguredStructure, Structure> CONFIGURED_STRUCTURE_REGISTRY = new RegistryKey<>(ConfiguredStructure.class, Registry.STRUCTURE_REGISTRY);

}
