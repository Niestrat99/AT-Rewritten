package org.bukkit.craftbukkit.v1_19_R1.entity;

import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.entity.Projectile;

public abstract class AbstractProjectile extends CraftEntity implements Projectile {

    private boolean doesBounce;

    public AbstractProjectile(CraftServer server, net.minecraft.world.entity.Entity entity) {
        super(server, entity);
        this.doesBounce = false;
    }

    @Override
    public boolean doesBounce() {
        return this.doesBounce;
    }

    @Override
    public void setBounce(boolean doesBounce) {
        this.doesBounce = doesBounce;
    }
    // Paper start
    @Override
    public boolean hasLeftShooter() {
        return this.getHandle().leftOwner;
    }

    @Override
    public void setHasLeftShooter(boolean leftShooter) {
        this.getHandle().leftOwner = leftShooter;
    }

    @Override
    public boolean hasBeenShot() {
        return this.getHandle().hasBeenShot;
    }

    @Override
    public void setHasBeenShot(boolean beenShot) {
        this.getHandle().hasBeenShot = beenShot;
    }

    @Override
    public net.minecraft.world.entity.projectile.Projectile getHandle() {
        return (net.minecraft.world.entity.projectile.Projectile) entity;
    }
    // Paper end

}
