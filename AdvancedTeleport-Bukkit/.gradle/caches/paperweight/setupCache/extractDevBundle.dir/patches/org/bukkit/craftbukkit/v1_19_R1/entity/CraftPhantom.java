package org.bukkit.craftbukkit.v1_19_R1.entity;

import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Phantom;

public class CraftPhantom extends CraftFlying implements Phantom {

    public CraftPhantom(CraftServer server, net.minecraft.world.entity.monster.Phantom entity) {
        super(server, entity);
    }

    @Override
    public net.minecraft.world.entity.monster.Phantom getHandle() {
        return (net.minecraft.world.entity.monster.Phantom) super.getHandle();
    }

    @Override
    public int getSize() {
        return this.getHandle().getPhantomSize();
    }

    @Override
    public void setSize(int sz) {
        this.getHandle().setPhantomSize(sz);
    }

    @Override
    public String toString() {
        return "CraftPhantom";
    }

    @Override
    public EntityType getType() {
        return EntityType.PHANTOM;
    }

    // Paper start
    @Override
    public java.util.UUID getSpawningEntity() {
        return getHandle().getSpawningEntity();
    }

    @Override
    public boolean shouldBurnInDay() {
        return getHandle().shouldBurnInDay();
    }

    @Override
    public void setShouldBurnInDay(boolean shouldBurnInDay) {
        getHandle().setShouldBurnInDay(shouldBurnInDay);
    }

    @Override
    public org.bukkit.Location getAnchorLocation() {
        net.minecraft.core.BlockPos pos = this.getHandle().anchorPoint;
        if (pos == null) {
            return null;
        }

        return io.papermc.paper.util.MCUtil.toLocation(this.getHandle().getLevel(), pos);
    }

    @Override
    public void setAnchorLocation(org.bukkit.Location location) {
        net.minecraft.core.BlockPos pos = null;
        if (location != null) {
            pos = io.papermc.paper.util.MCUtil.toBlockPosition(location);
        }

        this.getHandle().anchorPoint = pos;
    }
    // Paper end
}
