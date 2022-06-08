package org.bukkit.craftbukkit.v1_19_R1.entity;

import com.google.common.base.Preconditions;
import net.minecraft.core.BlockPos;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.craftbukkit.v1_19_R1.CraftSound;
import org.bukkit.craftbukkit.v1_19_R1.block.CraftBlock;
import org.bukkit.entity.Raider;

public abstract class CraftRaider extends CraftMonster implements Raider {

    public CraftRaider(CraftServer server, net.minecraft.world.entity.raid.Raider entity) {
        super(server, entity);
    }

    @Override
    public net.minecraft.world.entity.raid.Raider getHandle() {
        return (net.minecraft.world.entity.raid.Raider) super.getHandle();
    }

    @Override
    public String toString() {
        return "CraftRaider";
    }

    @Override
    public Block getPatrolTarget() {
        return this.getHandle().getPatrolTarget() == null ? null : CraftBlock.at(this.getHandle().level, this.getHandle().getPatrolTarget());
    }

    @Override
    public void setPatrolTarget(Block block) {
        if (block == null) {
            this.getHandle().setPatrolTarget((BlockPos) null);
        } else {
            Preconditions.checkArgument(block.getWorld().equals(this.getWorld()), "Block must be in same world");

            this.getHandle().setPatrolTarget(new BlockPos(block.getX(), block.getY(), block.getZ()));
        }
    }

    @Override
    public boolean isPatrolLeader() {
        return this.getHandle().isPatrolLeader();
    }

    @Override
    public void setPatrolLeader(boolean leader) {
        this.getHandle().setPatrolLeader(leader);
    }

    @Override
    public boolean isCanJoinRaid() {
        return this.getHandle().canJoinRaid();
    }

    @Override
    public void setCanJoinRaid(boolean join) {
        this.getHandle().setCanJoinRaid(join);
    }

    @Override
    public Sound getCelebrationSound() {
        return CraftSound.getBukkit(this.getHandle().getCelebrateSound());
    }

    // Paper start
    @Override
    public boolean isCelebrating() {
        return this.getHandle().isCelebrating();
    }

    @Override
    public void setCelebrating(boolean celebrating) {
        this.getHandle().setCelebrating(celebrating);
    }
    // Paper end
}
