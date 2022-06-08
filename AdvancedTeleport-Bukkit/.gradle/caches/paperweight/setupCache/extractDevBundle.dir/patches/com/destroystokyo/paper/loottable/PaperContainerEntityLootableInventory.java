package com.destroystokyo.paper.loottable;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.AbstractMinecartContainer;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.level.Level;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftNamespacedKey;

public class PaperContainerEntityLootableInventory implements PaperLootableEntityInventory {

    private final ContainerEntity entity;

    public PaperContainerEntityLootableInventory(ContainerEntity entity) {
        this.entity = entity;
    }

    @Override
    public org.bukkit.loot.LootTable getLootTable() {
        return entity.getLootTable() != null && !entity.getLootTable().getPath().isEmpty() ? Bukkit.getLootTable(CraftNamespacedKey.fromMinecraft(entity.getLootTable())) : null;
    }

    @Override
    public void setLootTable(org.bukkit.loot.LootTable table, long seed) {
        setLootTable(table);
        setSeed(seed);
    }

    @Override
    public void setSeed(long seed) {
        entity.setLootTableSeed(seed);
    }

    @Override
    public long getSeed() {
        return entity.getLootTableSeed();
    }

    @Override
    public void setLootTable(org.bukkit.loot.LootTable table) {
        entity.setLootTable((table == null) ? null : CraftNamespacedKey.toMinecraft(table.getKey()));
    }

    @Override
    public PaperLootableInventoryData getLootableData() {
        return entity.getLootableData();
    }

    @Override
    public Entity getHandle() {
        return entity.getEntity();
    }

    @Override
    public LootableInventory getAPILootableInventory() {
        return (LootableInventory) entity.getEntity().getBukkitEntity();
    }

    @Override
    public Level getNMSWorld() {
        return entity.getLevel();
    }
}
