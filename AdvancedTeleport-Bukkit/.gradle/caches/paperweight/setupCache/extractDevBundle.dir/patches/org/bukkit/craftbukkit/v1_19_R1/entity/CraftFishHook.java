package org.bukkit.craftbukkit.v1_19_R1.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.FishingHook;
import org.apache.commons.lang.Validate;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.FishHook.HookState;

public class CraftFishHook extends CraftProjectile implements FishHook {
    private double biteChance = -1;

    public CraftFishHook(CraftServer server, FishingHook entity) {
        super(server, entity);
    }

    @Override
    public FishingHook getHandle() {
        return (FishingHook) entity;
    }

    @Override
    public String toString() {
        return "CraftFishingHook";
    }

    @Override
    public EntityType getType() {
        return EntityType.FISHING_HOOK;
    }

    @Override
    public int getMinWaitTime() {
        return this.getHandle().minWaitTime;
    }

    @Override
    public void setMinWaitTime(int minWaitTime) {
        FishingHook hook = this.getHandle();
        Validate.isTrue(minWaitTime >= 0 && minWaitTime <= this.getMaxWaitTime(), "The minimum wait time should be between 0 and the maximum wait time.");
        hook.minWaitTime = minWaitTime;
    }

    @Override
    public int getMaxWaitTime() {
        return this.getHandle().maxWaitTime;
    }

    @Override
    public void setMaxWaitTime(int maxWaitTime) {
        FishingHook hook = this.getHandle();
        Validate.isTrue(maxWaitTime >= 0 && maxWaitTime >= this.getMinWaitTime(), "The maximum wait time should be higher than or equal to 0 and the minimum wait time.");
        hook.maxWaitTime = maxWaitTime;
    }

    @Override
    public boolean getApplyLure() {
        return this.getHandle().applyLure;
    }

    @Override
    public void setApplyLure(boolean applyLure) {
        this.getHandle().applyLure = applyLure;
    }

    @Override
    public double getBiteChance() {
        FishingHook hook = this.getHandle();

        if (this.biteChance == -1) {
            if (hook.level.isRainingAt(new BlockPos(Mth.floor(hook.getX()), Mth.floor(hook.getY()) + 1, Mth.floor(hook.getZ())))) {
                return 1 / 300.0;
            }
            return 1 / 500.0;
        }
        return this.biteChance;
    }

    @Override
    public void setBiteChance(double chance) {
        Validate.isTrue(chance >= 0 && chance <= 1, "The bite chance must be between 0 and 1.");
        this.biteChance = chance;
    }

    @Override
    public boolean isInOpenWater() {
        return this.getHandle().isOpenWaterFishing();
    }

    @Override
    public Entity getHookedEntity() {
        net.minecraft.world.entity.Entity hooked = this.getHandle().hookedIn;
        return (hooked != null) ? hooked.getBukkitEntity() : null;
    }

    @Override
    public void setHookedEntity(Entity entity) {
        FishingHook hook = this.getHandle();

        hook.hookedIn = (entity != null) ? ((CraftEntity) entity).getHandle() : null;
        hook.getEntityData().set(FishingHook.DATA_HOOKED_ENTITY, hook.hookedIn != null ? hook.hookedIn.getId() + 1 : 0);
    }

    @Override
    public boolean pullHookedEntity() {
        FishingHook hook = this.getHandle();
        if (hook.hookedIn == null) {
            return false;
        }

        hook.pullEntity(hook.hookedIn);
        return true;
    }

    @Override
    public HookState getState() {
        return HookState.values()[this.getHandle().currentState.ordinal()];
    }
    // Paper start - More FishHook API
    @Override
    public int getWaitTime() {
        return this.getHandle().timeUntilLured;
    }

    @Override
    public void setWaitTime(int ticks) {
        this.getHandle().timeUntilLured = ticks;
    }
    // Paper end
}
