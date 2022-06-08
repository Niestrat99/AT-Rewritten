package org.bukkit.craftbukkit.v1_19_R1.entity;

import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.entity.Dolphin;
import org.bukkit.entity.EntityType;

public class CraftDolphin extends CraftWaterMob implements Dolphin {

    public CraftDolphin(CraftServer server, net.minecraft.world.entity.animal.Dolphin entity) {
        super(server, entity);
    }

    @Override
    public net.minecraft.world.entity.animal.Dolphin getHandle() {
        return (net.minecraft.world.entity.animal.Dolphin) super.getHandle();
    }

    @Override
    public String toString() {
        return "CraftDolphin";
    }

    @Override
    public EntityType getType() {
        return EntityType.DOLPHIN;
    }

    @Override
    public int getMoistness() {
        return this.getHandle().getMoistnessLevel();
    }

    @Override
    public void setMoistness(int moistness) {
        this.getHandle().setMoisntessLevel(moistness);
    }

    @Override
    public void setHasFish(boolean hasFish) {
        this.getHandle().setGotFish(hasFish);
    }

    @Override
    public boolean hasFish() {
        return this.getHandle().gotFish();
    }

    @Override
    public org.bukkit.Location getTreasureLocation() {
        return io.papermc.paper.util.MCUtil.toLocation(this.getHandle().level, this.getHandle().getTreasurePos());
    }

    @Override
    public void setTreasureLocation(org.bukkit.Location location) {
        this.getHandle().setTreasurePos(io.papermc.paper.util.MCUtil.toBlockPosition(location));
    }
}
