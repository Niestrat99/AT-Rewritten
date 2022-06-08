package org.bukkit.craftbukkit.v1_19_R1.entity;

import com.google.common.base.Preconditions;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.projectiles.ProjectileSource;

public class CraftShulkerBullet extends AbstractProjectile implements ShulkerBullet {

    public CraftShulkerBullet(CraftServer server, net.minecraft.world.entity.projectile.ShulkerBullet entity) {
        super(server, entity);
    }

    @Override
    public ProjectileSource getShooter() {
        return this.getHandle().projectileSource;
    }

    @Override
    public void setShooter(ProjectileSource shooter) {
        if (shooter instanceof Entity) {
            this.getHandle().setOwner(((CraftEntity) shooter).getHandle());
        } else {
            this.getHandle().setOwner(null);
        }
        this.getHandle().projectileSource = shooter;
    }

    @Override
    public org.bukkit.entity.Entity getTarget() {
        return this.getHandle().getTarget() != null ? this.getHandle().getTarget().getBukkitEntity() : null;
    }

    @Override
    public void setTarget(org.bukkit.entity.Entity target) {
        Preconditions.checkState(!this.getHandle().generation, "Cannot set target during world generation");

        this.getHandle().setTarget(target == null ? null : ((CraftEntity) target).getHandle());
    }

    @Override
    public org.bukkit.util.Vector getTargetDelta() {
        net.minecraft.world.entity.projectile.ShulkerBullet bullet = this.getHandle();
        return new org.bukkit.util.Vector(bullet.targetDeltaX, bullet.targetDeltaY, bullet.targetDeltaZ);
    }

    @Override
    public void setTargetDelta(org.bukkit.util.Vector vector) {
        net.minecraft.world.entity.projectile.ShulkerBullet bullet = this.getHandle();
        bullet.targetDeltaX = vector.getX();
        bullet.targetDeltaY = vector.getY();
        bullet.targetDeltaZ = vector.getZ();
    }

    @Override
    public org.bukkit.block.BlockFace getCurrentMovementDirection() {
        return org.bukkit.craftbukkit.v1_19_R1.block.CraftBlock.notchToBlockFace(this.getHandle().currentMoveDirection);
    }

    @Override
    public void setCurrentMovementDirection(org.bukkit.block.BlockFace movementDirection) {
        this.getHandle().currentMoveDirection = org.bukkit.craftbukkit.v1_19_R1.block.CraftBlock.blockFaceToNotch(movementDirection);
    }

    @Override
    public int getFlightSteps() {
        return this.getHandle().flightSteps;
    }

    @Override
    public void setFlightSteps(int steps) {
        this.getHandle().flightSteps = steps;
    }

    @Override
    public String toString() {
        return "CraftShulkerBullet";
    }

    @Override
    public EntityType getType() {
        return EntityType.SHULKER_BULLET;
    }

    @Override
    public net.minecraft.world.entity.projectile.ShulkerBullet getHandle() {
        return (net.minecraft.world.entity.projectile.ShulkerBullet) entity;
    }
}
