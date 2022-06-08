package com.destroystokyo.paper.loottable;

import io.papermc.paper.util.MCUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftNamespacedKey;

public class PaperTileEntityLootableInventory implements PaperLootableBlockInventory {
    private RandomizableContainerBlockEntity tileEntityLootable;

    public PaperTileEntityLootableInventory(RandomizableContainerBlockEntity tileEntityLootable) {
        this.tileEntityLootable = tileEntityLootable;
    }

    @Override
    public org.bukkit.loot.LootTable getLootTable() {
        return tileEntityLootable.lootTable != null && !tileEntityLootable.lootTable.getPath().isEmpty() ? Bukkit.getLootTable(CraftNamespacedKey.fromMinecraft(tileEntityLootable.lootTable)) : null;
    }

    @Override
    public void setLootTable(org.bukkit.loot.LootTable table, long seed) {
        setLootTable(table);
        setSeed(seed);
    }

    @Override
    public void setLootTable(org.bukkit.loot.LootTable table) {
        tileEntityLootable.lootTable = (table == null) ? null : CraftNamespacedKey.toMinecraft(table.getKey());
    }

    @Override
    public void setSeed(long seed) {
        tileEntityLootable.lootTableSeed = seed;
    }

    @Override
    public long getSeed() {
        return tileEntityLootable.lootTableSeed;
    }

    @Override
    public PaperLootableInventoryData getLootableData() {
        return tileEntityLootable.lootableData;
    }

    @Override
    public RandomizableContainerBlockEntity getTileEntity() {
        return tileEntityLootable;
    }

    @Override
    public LootableInventory getAPILootableInventory() {
        Level world = tileEntityLootable.getLevel();
        if (world == null) {
            return null;
        }
        return (LootableInventory) getBukkitWorld().getBlockAt(MCUtil.toLocation(world, tileEntityLootable.getBlockPos())).getState();
    }

    @Override
    public Level getNMSWorld() {
        return tileEntityLootable.getLevel();
    }
}
