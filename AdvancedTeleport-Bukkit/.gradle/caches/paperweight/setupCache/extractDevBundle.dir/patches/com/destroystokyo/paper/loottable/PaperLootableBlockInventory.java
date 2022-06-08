package com.destroystokyo.paper.loottable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import org.bukkit.Chunk;
import org.bukkit.block.Block;

public interface PaperLootableBlockInventory extends LootableBlockInventory, PaperLootableInventory {

    RandomizableContainerBlockEntity getTileEntity();

    @Override
    default LootableInventory getAPILootableInventory() {
        return this;
    }

    @Override
    default Level getNMSWorld() {
        return getTileEntity().getLevel();
    }

    default Block getBlock() {
        final BlockPos position = getTileEntity().getBlockPos();
        final Chunk bukkitChunk = getTileEntity().getLevel().getChunkAt(position).bukkitChunk;
        return bukkitChunk.getBlock(position.getX(), position.getY(), position.getZ());
    }

    @Override
    default PaperLootableInventoryData getLootableData() {
        return getTileEntity().lootableData;
    }
}
