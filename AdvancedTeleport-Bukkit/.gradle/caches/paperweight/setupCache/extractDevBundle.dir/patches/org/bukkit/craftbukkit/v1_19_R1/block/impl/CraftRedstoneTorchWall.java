/**
 * Automatically generated file, changes will be lost.
 */
package org.bukkit.craftbukkit.v1_19_R1.block.impl;

public final class CraftRedstoneTorchWall extends org.bukkit.craftbukkit.v1_19_R1.block.data.CraftBlockData implements org.bukkit.block.data.type.RedstoneWallTorch, org.bukkit.block.data.Directional, org.bukkit.block.data.Lightable {

    public CraftRedstoneTorchWall() {
        super();
    }

    public CraftRedstoneTorchWall(net.minecraft.world.level.block.state.BlockState state) {
        super(state);
    }

    // org.bukkit.craftbukkit.v1_19_R1.block.data.CraftDirectional

    private static final net.minecraft.world.level.block.state.properties.EnumProperty<?> FACING = getEnum(net.minecraft.world.level.block.RedstoneWallTorchBlock.class, "facing");

    @Override
    public org.bukkit.block.BlockFace getFacing() {
        return get(CraftRedstoneTorchWall.FACING, org.bukkit.block.BlockFace.class);
    }

    @Override
    public void setFacing(org.bukkit.block.BlockFace facing) {
        set(CraftRedstoneTorchWall.FACING, facing);
    }

    @Override
    public java.util.Set<org.bukkit.block.BlockFace> getFaces() {
        return getValues(CraftRedstoneTorchWall.FACING, org.bukkit.block.BlockFace.class);
    }

    // org.bukkit.craftbukkit.v1_19_R1.block.data.CraftLightable

    private static final net.minecraft.world.level.block.state.properties.BooleanProperty LIT = getBoolean(net.minecraft.world.level.block.RedstoneWallTorchBlock.class, "lit");

    @Override
    public boolean isLit() {
        return get(CraftRedstoneTorchWall.LIT);
    }

    @Override
    public void setLit(boolean lit) {
        set(CraftRedstoneTorchWall.LIT, lit);
    }
}
