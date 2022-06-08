package org.bukkit.craftbukkit.v1_19_R1.entity;

import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.entity.SkeletonHorse;

public class CraftSkeletonHorse extends CraftAbstractHorse implements SkeletonHorse {

    public CraftSkeletonHorse(CraftServer server, net.minecraft.world.entity.animal.horse.SkeletonHorse entity) {
        super(server, entity);
    }

    @Override
    public String toString() {
        return "CraftSkeletonHorse";
    }

    @Override
    public EntityType getType() {
        return EntityType.SKELETON_HORSE;
    }

    @Override
    public Variant getVariant() {
        return Variant.SKELETON_HORSE;
    }

    @Override
    public net.minecraft.world.entity.animal.horse.SkeletonHorse getHandle() {
        return (net.minecraft.world.entity.animal.horse.SkeletonHorse) entity;
    }

    @Override
    public boolean isTrapped() {
        return this.getHandle().isTrap();
    }

    @Override
    public void setTrapped(boolean trapped) {
        this.getHandle().setTrap(trapped);
    }

    @Override
    public int getTrapTime() {
        return this.getHandle().trapTime;
    }

    @Override
    public void setTrapTime(int trapTime) {
        this.getHandle().trapTime = trapTime;
    }

    // Paper start - replaced by above methods
    @Override
    public boolean isTrap() {
        return getHandle().isTrap();
    }

    @Override
    public void setTrap(boolean trap) {
        getHandle().setTrap(trap);
    }
    // Paper end
}
