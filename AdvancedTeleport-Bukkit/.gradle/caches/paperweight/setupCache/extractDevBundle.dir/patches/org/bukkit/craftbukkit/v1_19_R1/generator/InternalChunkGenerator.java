package org.bukkit.craftbukkit.v1_19_R1.generator;

import java.util.Optional;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.levelgen.structure.StructureSet;

// Do not implement functions to this class, add to NormalChunkGenerator
public abstract class InternalChunkGenerator extends net.minecraft.world.level.chunk.ChunkGenerator {

    public InternalChunkGenerator(Registry<StructureSet> structureSetRegistry, Optional<HolderSet<StructureSet>> structureOverrides, BiomeSource biomeSource) {
        super(structureSetRegistry, structureOverrides, biomeSource);
    }
}
