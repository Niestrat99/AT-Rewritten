/**
 * Automatically generated file, changes will be lost.
 */
package org.bukkit.craftbukkit.v1_19_R1.block.impl;

public final class CraftStonecutter extends org.bukkit.craftbukkit.v1_19_R1.block.data.CraftBlockData implements org.bukkit.block.data.Directional {

    public CraftStonecutter() {
        super();
    }

    public CraftStonecutter(net.minecraft.world.level.block.state.BlockState state) {
        super(state);
    }

    // org.bukkit.craftbukkit.v1_19_R1.block.data.CraftDirectional

    private static final net.minecraft.world.level.block.state.properties.EnumProperty<?> FACING = getEnum(net.minecraft.world.level.block.StonecutterBlock.class, "facing");

    @Override
    public org.bukkit.block.BlockFace getFacing() {
        return get(CraftStonecutter.FACING, org.bukkit.block.BlockFace.class);
    }

    @Override
    public void setFacing(org.bukkit.block.BlockFace facing) {
        set(CraftStonecutter.FACING, facing);
    }

    @Override
    public java.util.Set<org.bukkit.block.BlockFace> getFaces() {
        return getValues(CraftStonecutter.FACING, org.bukkit.block.BlockFace.class);
    }
}
