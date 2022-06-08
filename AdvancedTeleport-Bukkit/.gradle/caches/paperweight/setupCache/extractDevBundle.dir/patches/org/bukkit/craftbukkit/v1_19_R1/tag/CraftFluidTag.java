package org.bukkit.craftbukkit.v1_19_R1.tag;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import org.bukkit.Fluid;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftMagicNumbers;

public class CraftFluidTag extends CraftTag<net.minecraft.world.level.material.Fluid, Fluid> {

    public CraftFluidTag(Registry<net.minecraft.world.level.material.Fluid> registry, TagKey<net.minecraft.world.level.material.Fluid> tag) {
        super(registry, tag);
    }

    private static final java.util.Map<Fluid, net.minecraft.resources.ResourceKey<net.minecraft.world.level.material.Fluid>> KEY_CACHE = Collections.synchronizedMap(new java.util.EnumMap<>(Fluid.class)); // Paper
    @Override
    public boolean isTagged(Fluid fluid) {
        return registry.getHolderOrThrow(KEY_CACHE.computeIfAbsent(fluid, f -> net.minecraft.resources.ResourceKey.create(net.minecraft.core.Registry.FLUID_REGISTRY, org.bukkit.craftbukkit.v1_19_R1.util.CraftNamespacedKey.toMinecraft(f.getKey())))).is(tag); // Paper - cache key
    }

    @Override
    public Set<Fluid> getValues() {
        return getHandle().stream().map((fluid) -> CraftMagicNumbers.getFluid(fluid.value())).collect(Collectors.toUnmodifiableSet());
    }
}
