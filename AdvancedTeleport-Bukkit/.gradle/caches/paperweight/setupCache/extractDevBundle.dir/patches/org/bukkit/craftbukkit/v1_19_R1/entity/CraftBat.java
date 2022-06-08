package org.bukkit.craftbukkit.v1_19_R1.entity;

import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.entity.Bat;
import org.bukkit.entity.EntityType;

public class CraftBat extends CraftAmbient implements Bat {
    public CraftBat(CraftServer server, net.minecraft.world.entity.ambient.Bat entity) {
        super(server, entity);
    }

    @Override
    public net.minecraft.world.entity.ambient.Bat getHandle() {
        return (net.minecraft.world.entity.ambient.Bat) entity;
    }

    @Override
    public String toString() {
        return "CraftBat";
    }

    @Override
    public EntityType getType() {
        return EntityType.BAT;
    }

    @Override
    public boolean isAwake() {
        return !this.getHandle().isResting();
    }

    @Override
    public void setAwake(boolean state) {
        this.getHandle().setResting(!state);
    }
    // Paper start
    @Override
    public org.bukkit.Location getTargetLocation() {
        net.minecraft.core.BlockPos pos = this.getHandle().targetPosition;
        if (pos == null) {
            return null;
        }

        return io.papermc.paper.util.MCUtil.toLocation(this.getHandle().getLevel(), pos);
    }

    @Override
    public void setTargetLocation(org.bukkit.Location location) {
        net.minecraft.core.BlockPos pos = null;
        if (location != null) {
            pos = io.papermc.paper.util.MCUtil.toBlockPosition(location);
        }

        this.getHandle().targetPosition = pos;
    }
    // Paper end
}
