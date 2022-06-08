package org.bukkit.craftbukkit.v1_19_R1.entity;

import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Ravager;

public class CraftRavager extends CraftRaider implements Ravager {

    public CraftRavager(CraftServer server, net.minecraft.world.entity.monster.Ravager entity) {
        super(server, entity);
    }

    @Override
    public net.minecraft.world.entity.monster.Ravager getHandle() {
        return (net.minecraft.world.entity.monster.Ravager) super.getHandle();
    }

    @Override
    public EntityType getType() {
        return EntityType.RAVAGER;
    }

    @Override
    public String toString() {
        return "CraftRavager";
    }
    // Paper start - Missing Entity Behavior
    @Override
    public int getAttackTicks() {
        return this.getHandle().getAttackTick();
    }

    @Override
    public void setAttackTicks(int ticks) {
        this.getHandle().attackTick = ticks;
    }

    @Override
    public int getStunnedTicks() {
        return this.getHandle().getStunnedTick();
    }

    @Override
    public void setStunnedTicks(int ticks) {
        this.getHandle().stunnedTick = ticks;
    }

    @Override
    public int getRoarTicks() {
        return this.getHandle().getRoarTick();
    }

    @Override
    public void setRoarTicks(int ticks) {
        this.getHandle().roarTick = ticks;
    }
    // Paper end
}
