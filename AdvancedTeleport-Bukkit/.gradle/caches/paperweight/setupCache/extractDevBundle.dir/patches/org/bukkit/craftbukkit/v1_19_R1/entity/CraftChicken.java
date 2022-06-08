package org.bukkit.craftbukkit.v1_19_R1.entity;

import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.EntityType;

public class CraftChicken extends CraftAnimals implements Chicken {

    public CraftChicken(CraftServer server, net.minecraft.world.entity.animal.Chicken entity) {
        super(server, entity);
    }

    @Override
    public net.minecraft.world.entity.animal.Chicken getHandle() {
        return (net.minecraft.world.entity.animal.Chicken) entity;
    }

    @Override
    public String toString() {
        return "CraftChicken";
    }

    @Override
    public EntityType getType() {
        return EntityType.CHICKEN;
    }
    // Paper start
    @Override
    public boolean isChickenJockey() {
        return this.getHandle().isChickenJockey();
    }

    @Override
    public void setIsChickenJockey(boolean isChickenJockey) {
        this.getHandle().setChickenJockey(isChickenJockey);
    }

    @Override
    public int getEggLayTime() {
        return this.getHandle().eggTime;
    }

    @Override
    public void setEggLayTime(int eggLayTime) {
        this.getHandle().eggTime = eggLayTime;
    }
    // Paper end
}
