package com.destroystokyo.paper.loottable;

import net.minecraft.world.level.Level;
import org.bukkit.entity.Entity;

public interface PaperLootableEntityInventory extends LootableEntityInventory, PaperLootableInventory {

    net.minecraft.world.entity.Entity getHandle();

    @Override
    default LootableInventory getAPILootableInventory() {
        return this;
    }

    default Entity getEntity() {
        return getHandle().getBukkitEntity();
    }

    @Override
    default Level getNMSWorld() {
        return getHandle().getCommandSenderWorld();
    }

    @Override
    default PaperLootableInventoryData getLootableData() {
        return getHandle().lootableData;
    }
}
