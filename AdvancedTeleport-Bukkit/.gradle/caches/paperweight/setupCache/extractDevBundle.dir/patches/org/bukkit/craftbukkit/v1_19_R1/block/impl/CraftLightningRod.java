/**
 * Automatically generated file, changes will be lost.
 */
package org.bukkit.craftbukkit.v1_19_R1.block.impl;

public final class CraftLightningRod extends org.bukkit.craftbukkit.v1_19_R1.block.data.CraftBlockData implements org.bukkit.block.data.type.LightningRod, org.bukkit.block.data.Directional, org.bukkit.block.data.Powerable, org.bukkit.block.data.Waterlogged {

    public CraftLightningRod() {
        super();
    }

    public CraftLightningRod(net.minecraft.world.level.block.state.BlockState state) {
        super(state);
    }

    // org.bukkit.craftbukkit.v1_19_R1.block.data.CraftDirectional

    private static final net.minecraft.world.level.block.state.properties.EnumProperty<?> FACING = getEnum(net.minecraft.world.level.block.LightningRodBlock.class, "facing");

    @Override
    public org.bukkit.block.BlockFace getFacing() {
        return get(CraftLightningRod.FACING, org.bukkit.block.BlockFace.class);
    }

    @Override
    public void setFacing(org.bukkit.block.BlockFace facing) {
        set(CraftLightningRod.FACING, facing);
    }

    @Override
    public java.util.Set<org.bukkit.block.BlockFace> getFaces() {
        return getValues(CraftLightningRod.FACING, org.bukkit.block.BlockFace.class);
    }

    // org.bukkit.craftbukkit.v1_19_R1.block.data.CraftPowerable

    private static final net.minecraft.world.level.block.state.properties.BooleanProperty POWERED = getBoolean(net.minecraft.world.level.block.LightningRodBlock.class, "powered");

    @Override
    public boolean isPowered() {
        return get(CraftLightningRod.POWERED);
    }

    @Override
    public void setPowered(boolean powered) {
        set(CraftLightningRod.POWERED, powered);
    }

    // org.bukkit.craftbukkit.v1_19_R1.block.data.CraftWaterlogged

    private static final net.minecraft.world.level.block.state.properties.BooleanProperty WATERLOGGED = getBoolean(net.minecraft.world.level.block.LightningRodBlock.class, "waterlogged");

    @Override
    public boolean isWaterlogged() {
        return get(CraftLightningRod.WATERLOGGED);
    }

    @Override
    public void setWaterlogged(boolean waterlogged) {
        set(CraftLightningRod.WATERLOGGED, waterlogged);
    }
}
