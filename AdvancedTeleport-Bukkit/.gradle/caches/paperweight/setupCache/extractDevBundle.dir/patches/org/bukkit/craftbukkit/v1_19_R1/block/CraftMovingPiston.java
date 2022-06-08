package org.bukkit.craftbukkit.v1_19_R1.block;

import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import org.bukkit.World;

public class CraftMovingPiston extends CraftBlockEntityState<PistonMovingBlockEntity> implements io.papermc.paper.block.MovingPiston { // Paper - Add Moving Piston API

    public CraftMovingPiston(World world, PistonMovingBlockEntity tileEntity) {
        super(world, tileEntity);
    }
    // Paper start - Add Moving Piston API
    @Override
    public org.bukkit.block.data.BlockData getMovingBlock() {
        return org.bukkit.craftbukkit.v1_19_R1.block.data.CraftBlockData.fromData(this.getTileEntity().getMovedState());
    }

    @Override
    public org.bukkit.block.BlockFace getDirection() {
        return org.bukkit.craftbukkit.v1_19_R1.block.CraftBlock.notchToBlockFace(this.getTileEntity().getDirection());
    }

    @Override
    public boolean isExtending() {
        return this.getTileEntity().isExtending();
    }

    @Override
    public boolean isPistonHead() {
        return this.getTileEntity().isSourcePiston();
    }
    // Paper end - Add Moving Piston API
}
