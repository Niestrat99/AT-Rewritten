package org.bukkit.craftbukkit.v1_19_R1.entity;

import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.craftbukkit.v1_19_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.EntityType;
import org.bukkit.material.MaterialData;

public class CraftEnderman extends CraftMonster implements Enderman {
    public CraftEnderman(CraftServer server, EnderMan entity) {
        super(server, entity);
    }

    @Override public boolean teleportRandomly() { return getHandle().teleport(); } // Paper
    @Override
    public MaterialData getCarriedMaterial() {
        BlockState blockData = this.getHandle().getCarriedBlock();
        return (blockData == null) ? Material.AIR.getNewData((byte) 0) : CraftMagicNumbers.getMaterial(blockData);
    }

    @Override
    public BlockData getCarriedBlock() {
        BlockState blockData = this.getHandle().getCarriedBlock();
        return (blockData == null) ? null : CraftBlockData.fromData(blockData);
    }

    @Override
    public void setCarriedMaterial(MaterialData data) {
        this.getHandle().setCarriedBlock(CraftMagicNumbers.getBlock(data));
    }

    @Override
    public void setCarriedBlock(BlockData blockData) {
        this.getHandle().setCarriedBlock(blockData == null ? null : ((CraftBlockData) blockData).getState());
    }

    // Paper start
    @Override
    public boolean isScreaming() {
        return this.getHandle().isCreepy();
    }

    @Override
    public void setScreaming(boolean screaming) {
        this.getHandle().setCreepy(screaming);
    }

    @Override
    public boolean hasBeenStaredAt() {
        return this.getHandle().hasBeenStaredAt();
    }

    @Override
    public void setHasBeenStaredAt(boolean hasBeenStaredAt) {
        this.getHandle().setHasBeenStaredAt(hasBeenStaredAt);
    }
    // Paper end

    @Override
    public EnderMan getHandle() {
        return (EnderMan) entity;
    }

    @Override
    public String toString() {
        return "CraftEnderman";
    }

    @Override
    public EntityType getType() {
        return EntityType.ENDERMAN;
    }
}
