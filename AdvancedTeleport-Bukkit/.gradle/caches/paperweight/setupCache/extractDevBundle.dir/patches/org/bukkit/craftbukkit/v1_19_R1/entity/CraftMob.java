package org.bukkit.craftbukkit.v1_19_R1.entity;

import com.google.common.base.Preconditions;
import net.minecraft.sounds.SoundEvent;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.craftbukkit.v1_19_R1.CraftSound;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftNamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.loot.LootTable;

public abstract class CraftMob extends CraftLivingEntity implements Mob {
    public CraftMob(CraftServer server, net.minecraft.world.entity.Mob entity) {
        super(server, entity);
         paperPathfinder = new com.destroystokyo.paper.entity.PaperPathfinder(entity); // Paper
    }

    private final com.destroystokyo.paper.entity.PaperPathfinder paperPathfinder; // Paper
    @Override public com.destroystokyo.paper.entity.Pathfinder getPathfinder() { return paperPathfinder; } // Paper
    @Override
    public void setTarget(LivingEntity target) {
        Preconditions.checkState(!this.getHandle().generation, "Cannot set target during world generation");

        net.minecraft.world.entity.Mob entity = this.getHandle();
        if (target == null) {
            entity.setTarget(null, null, false);
        } else if (target instanceof CraftLivingEntity) {
            entity.setTarget(((CraftLivingEntity) target).getHandle(), null, false);
        }
    }

    @Override
    public CraftLivingEntity getTarget() {
        if (this.getHandle().getTarget() == null) return null;

        return (CraftLivingEntity) this.getHandle().getTarget().getBukkitEntity();
    }

    @Override
    public void setAware(boolean aware) {
        this.getHandle().aware = aware;
    }

    @Override
    public boolean isAware() {
        return this.getHandle().aware;
    }

    @Override
    public Sound getAmbientSound() {
        SoundEvent sound = this.getHandle().getAmbientSound0();
        return (sound != null) ? CraftSound.getBukkit(sound) : null;
    }

    @Override
    public net.minecraft.world.entity.Mob getHandle() {
        return (net.minecraft.world.entity.Mob) entity;
    }

    // Paper start
    @Override
    public void setHandle(net.minecraft.world.entity.Entity entity) {
        super.setHandle(entity);
        paperPathfinder.setHandle(getHandle());
    }
    // Paper end

    @Override
    public String toString() {
        return "CraftMob";
    }

    @Override
    public void setLootTable(LootTable table) {
        this.getHandle().lootTable = (table == null) ? null : CraftNamespacedKey.toMinecraft(table.getKey());
    }

    @Override
    public LootTable getLootTable() {
        NamespacedKey key = CraftNamespacedKey.fromMinecraft(this.getHandle().getLootTable());
        return Bukkit.getLootTable(key);
    }

    @Override
    public void setSeed(long seed) {
        this.getHandle().lootTableSeed = seed;
    }

    @Override
    public long getSeed() {
        return this.getHandle().lootTableSeed;
    }

    // Paper start
    @Override
    public boolean isInDaylight() {
        return getHandle().isSunBurnTick();
    }

    @Override
    public void lookAt(@org.jetbrains.annotations.NotNull org.bukkit.Location location) {
        com.google.common.base.Preconditions.checkNotNull(location, "location cannot be null");
        com.google.common.base.Preconditions.checkArgument(location.getWorld().equals(getWorld()), "location in a different world");
        getHandle().getLookControl().setLookAt(location.getX(), location.getY(), location.getZ());
    }

    @Override
    public void lookAt(@org.jetbrains.annotations.NotNull org.bukkit.Location location, float headRotationSpeed, float maxHeadPitch) {
        com.google.common.base.Preconditions.checkNotNull(location, "location cannot be null");
        com.google.common.base.Preconditions.checkArgument(location.getWorld().equals(getWorld()), "location in a different world");
        getHandle().getLookControl().setLookAt(location.getX(), location.getY(), location.getZ(), headRotationSpeed, maxHeadPitch);
    }

    @Override
    public void lookAt(@org.jetbrains.annotations.NotNull org.bukkit.entity.Entity entity) {
        com.google.common.base.Preconditions.checkNotNull(entity, "entity cannot be null");
        com.google.common.base.Preconditions.checkArgument(entity.getWorld().equals(getWorld()), "entity in a different world");
        getHandle().getLookControl().setLookAt(((CraftEntity) entity).getHandle());
    }

    @Override
    public void lookAt(@org.jetbrains.annotations.NotNull org.bukkit.entity.Entity entity, float headRotationSpeed, float maxHeadPitch) {
        com.google.common.base.Preconditions.checkNotNull(entity, "entity cannot be null");
        com.google.common.base.Preconditions.checkArgument(entity.getWorld().equals(getWorld()), "entity in a different world");
        getHandle().getLookControl().setLookAt(((CraftEntity) entity).getHandle(), headRotationSpeed, maxHeadPitch);
    }

    @Override
    public void lookAt(double x, double y, double z) {
        getHandle().getLookControl().setLookAt(x, y, z);
    }

    @Override
    public void lookAt(double x, double y, double z, float headRotationSpeed, float maxHeadPitch) {
        getHandle().getLookControl().setLookAt(x, y, z, headRotationSpeed, maxHeadPitch);
    }

    @Override
    public int getHeadRotationSpeed() {
        return getHandle().getHeadRotSpeed();
    }

    @Override
    public int getMaxHeadPitch() {
        return getHandle().getMaxHeadXRot();
    }

    @Override
    public boolean isLeftHanded() {
        return getHandle().isLeftHanded();
    }

    @Override
    public void setLeftHanded(boolean leftHanded) {
        getHandle().setLeftHanded(leftHanded);
    }
    // Paper end
}
