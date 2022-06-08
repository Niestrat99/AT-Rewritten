/**
 * Automatically generated file, changes will be lost.
 */
package org.bukkit.craftbukkit.v1_19_R1.block.impl;

public final class CraftEnderChest extends org.bukkit.craftbukkit.v1_19_R1.block.data.CraftBlockData implements org.bukkit.block.data.type.EnderChest, org.bukkit.block.data.Directional, org.bukkit.block.data.Waterlogged {

    public CraftEnderChest() {
        super();
    }

    public CraftEnderChest(net.minecraft.world.level.block.state.BlockState state) {
        super(state);
    }

    // org.bukkit.craftbukkit.v1_19_R1.block.data.CraftDirectional

    private static final net.minecraft.world.level.block.state.properties.EnumProperty<?> FACING = getEnum(net.minecraft.world.level.block.EnderChestBlock.class, "facing");

    @Override
    public org.bukkit.block.BlockFace getFacing() {
        return get(CraftEnderChest.FACING, org.bukkit.block.BlockFace.class);
    }

    @Override
    public void setFacing(org.bukkit.block.BlockFace facing) {
        set(CraftEnderChest.FACING, facing);
    }

    @Override
    public java.util.Set<org.bukkit.block.BlockFace> getFaces() {
        return getValues(CraftEnderChest.FACING, org.bukkit.block.BlockFace.class);
    }

    // org.bukkit.craftbukkit.v1_19_R1.block.data.CraftWaterlogged

    private static final net.minecraft.world.level.block.state.properties.BooleanProperty WATERLOGGED = getBoolean(net.minecraft.world.level.block.EnderChestBlock.class, "waterlogged");

    @Override
    public boolean isWaterlogged() {
        return get(CraftEnderChest.WATERLOGGED);
    }

    @Override
    public void setWaterlogged(boolean waterlogged) {
        set(CraftEnderChest.WATERLOGGED, waterlogged);
    }
}
