/**
 * Automatically generated file, changes will be lost.
 */
package org.bukkit.craftbukkit.v1_19_R1.block.impl;

public final class CraftRedstoneComparator extends org.bukkit.craftbukkit.v1_19_R1.block.data.CraftBlockData implements org.bukkit.block.data.type.Comparator, org.bukkit.block.data.Directional, org.bukkit.block.data.Powerable {

    public CraftRedstoneComparator() {
        super();
    }

    public CraftRedstoneComparator(net.minecraft.world.level.block.state.BlockState state) {
        super(state);
    }

    // org.bukkit.craftbukkit.v1_19_R1.block.data.type.CraftComparator

    private static final net.minecraft.world.level.block.state.properties.EnumProperty<?> MODE = getEnum(net.minecraft.world.level.block.ComparatorBlock.class, "mode");

    @Override
    public org.bukkit.block.data.type.Comparator.Mode getMode() {
        return get(CraftRedstoneComparator.MODE, org.bukkit.block.data.type.Comparator.Mode.class);
    }

    @Override
    public void setMode(org.bukkit.block.data.type.Comparator.Mode mode) {
        set(CraftRedstoneComparator.MODE, mode);
    }

    // org.bukkit.craftbukkit.v1_19_R1.block.data.CraftDirectional

    private static final net.minecraft.world.level.block.state.properties.EnumProperty<?> FACING = getEnum(net.minecraft.world.level.block.ComparatorBlock.class, "facing");

    @Override
    public org.bukkit.block.BlockFace getFacing() {
        return get(CraftRedstoneComparator.FACING, org.bukkit.block.BlockFace.class);
    }

    @Override
    public void setFacing(org.bukkit.block.BlockFace facing) {
        set(CraftRedstoneComparator.FACING, facing);
    }

    @Override
    public java.util.Set<org.bukkit.block.BlockFace> getFaces() {
        return getValues(CraftRedstoneComparator.FACING, org.bukkit.block.BlockFace.class);
    }

    // org.bukkit.craftbukkit.v1_19_R1.block.data.CraftPowerable

    private static final net.minecraft.world.level.block.state.properties.BooleanProperty POWERED = getBoolean(net.minecraft.world.level.block.ComparatorBlock.class, "powered");

    @Override
    public boolean isPowered() {
        return get(CraftRedstoneComparator.POWERED);
    }

    @Override
    public void setPowered(boolean powered) {
        set(CraftRedstoneComparator.POWERED, powered);
    }
}
