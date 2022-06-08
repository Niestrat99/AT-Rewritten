package org.bukkit.craftbukkit.v1_19_R1.entity;

import net.minecraft.world.entity.vehicle.MinecartTNT;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.minecart.ExplosiveMinecart;

public final class CraftMinecartTNT extends CraftMinecart implements ExplosiveMinecart { // Paper - getHandle -> make public
    CraftMinecartTNT(CraftServer server, MinecartTNT entity) {
        super(server, entity);
    }

    @Override
    public String toString() {
        return "CraftMinecartTNT";
    }

    @Override
    public EntityType getType() {
        return EntityType.MINECART_TNT;
    }
    // Paper start
    @Override
    public net.minecraft.world.entity.vehicle.MinecartTNT getHandle() {
        return (net.minecraft.world.entity.vehicle.MinecartTNT) entity;
    }

    @Override
    public void setFuseTicks(int fuseTicks) {
        this.getHandle().fuse = fuseTicks;
    }

    @Override
    public int getFuseTicks() {
        return this.getHandle().getFuse();
    }
    // Paper end
}
