package org.bukkit.craftbukkit.v1_19_R1.entity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.AbstractMinecartContainer;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftNamespacedKey;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.Lootable;

public abstract class CraftMinecartContainer extends CraftMinecart implements Lootable {

    public CraftMinecartContainer(CraftServer server, AbstractMinecart entity) {
        super(server, entity);
    }

    @Override
    public AbstractMinecartContainer getHandle() {
        return (AbstractMinecartContainer) entity;
    }

    @Override
    public void setLootTable(LootTable table) {
        this.setLootTable(table, this.getSeed());
    }

    @Override
    public LootTable getLootTable() {
        ResourceLocation nmsTable = this.getHandle().lootTable;
        if (nmsTable == null) {
            return null; // return empty loot table?
        }

        NamespacedKey key = CraftNamespacedKey.fromMinecraft(nmsTable);
        return Bukkit.getLootTable(key);
    }

    @Override
    public void setSeed(long seed) {
        this.setLootTable(this.getLootTable(), seed);
    }

    @Override
    public long getSeed() {
        return this.getHandle().lootTableSeed;
    }

    public void setLootTable(LootTable table, long seed) {
        ResourceLocation newKey = (table == null) ? null : CraftNamespacedKey.toMinecraft(table.getKey());
        this.getHandle().setLootTable(newKey, seed);
    }
}
