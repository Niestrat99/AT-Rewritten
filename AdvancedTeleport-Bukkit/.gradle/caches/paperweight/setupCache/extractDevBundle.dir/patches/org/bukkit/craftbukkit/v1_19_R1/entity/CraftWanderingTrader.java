package org.bukkit.craftbukkit.v1_19_R1.entity;

import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.WanderingTrader;

public class CraftWanderingTrader extends CraftAbstractVillager implements WanderingTrader {

    public CraftWanderingTrader(CraftServer server, net.minecraft.world.entity.npc.WanderingTrader entity) {
        super(server, entity);
    }

    @Override
    public net.minecraft.world.entity.npc.WanderingTrader getHandle() {
        return (net.minecraft.world.entity.npc.WanderingTrader) entity;
    }

    @Override
    public String toString() {
        return "CraftWanderingTrader";
    }

    @Override
    public EntityType getType() {
        return EntityType.WANDERING_TRADER;
    }

    @Override
    public int getDespawnDelay() {
        return this.getHandle().getDespawnDelay();
    }

    @Override
    public void setDespawnDelay(int despawnDelay) {
        this.getHandle().setDespawnDelay(despawnDelay);
    }

    // Paper start - Add more WanderingTrader API
    @Override
    public void setCanDrinkPotion(boolean bool) {
        getHandle().canDrinkPotion = bool;
    }

    @Override
    public boolean canDrinkPotion() {
        return getHandle().canDrinkPotion;
    }

    @Override
    public void setCanDrinkMilk(boolean bool) {
        getHandle().canDrinkMilk = bool;
    }

    @Override
    public boolean canDrinkMilk() {
        return getHandle().canDrinkMilk;
    }

    @Override
    public org.bukkit.Location getWanderingTowards() {
        net.minecraft.core.BlockPos pos = this.getHandle().wanderTarget;
        if (pos == null) {
            return null;
        }

        return io.papermc.paper.util.MCUtil.toLocation(this.getHandle().getLevel(), pos);
    }

    @Override
    public void setWanderingTowards(org.bukkit.Location location) {
        net.minecraft.core.BlockPos pos = null;
        if (location != null) {
            pos = io.papermc.paper.util.MCUtil.toBlockPosition(location);
        }

        this.getHandle().wanderTarget = pos;
    }
    // Paper end
}
